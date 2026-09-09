package io.github.relaynet.roost.consumer

import android.test.InstrumentationTestCase
import io.github.relaynet.roost.*

private class MemoryStorage : SecureStorage {
    private val values = mutableMapOf<String, ByteArray>()
    var failNextLoad = false
    var loseNextStoreReply = false

    @Synchronized override fun store(key: String, value: ByteArray) {
        values[key] = value.copyOf()
        if (loseNextStoreReply) {
            loseNextStoreReply = false
            throw IllegalStateException("lost storage reply")
        }
    }

    @Synchronized override fun load(key: String): ByteArray? {
        if (failNextLoad) {
            failNextLoad = false
            throw IllegalStateException("foreign storage failure")
        }
        return values[key]?.copyOf()
    }

    @Synchronized override fun delete(key: String) { values.remove(key) }
}

class StorageTests : InstrumentationTestCase() {
    fun testUnexpectedCallbackErrorLeavesClientUsable() {
        val storage = MemoryStorage()
        val client = Client(storage)
        storage.failNextLoad = true
        try {
            client.identity().hasIdentity()
            fail("expected storage error")
        } catch (_: ClientException.Storage) {}
        assertFalse(client.identity().hasIdentity())
    }

    fun testLostWriteReplyRecoversOriginalIdentityAcrossReopen() {
        val storage = MemoryStorage()
        val client = Client(storage)
        val prepared = client.identity().prepareGenerateIdentity()
        client.commitOperation(prepared.operation.identifier)
        storage.loseNextStoreReply = true
        try {
            client.finalizeOperation(prepared.operation.identifier)
            fail("expected lost write reply")
        } catch (_: ClientException.Storage) {}
        val reopened = Client(storage)
        reopened.finalizeOperation(prepared.operation.identifier)
        assertTrue(reopened.identity().loadIdentity().ed25519VerifyingKey.contentEquals(
            prepared.identity.ed25519VerifyingKey))
        assertTrue(reopened.unfinishedOperations().isEmpty())
    }
}
