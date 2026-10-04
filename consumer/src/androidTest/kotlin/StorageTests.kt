package io.github.relaynet.roost.consumer

import android.test.InstrumentationTestCase
import io.github.relaynet.roost.*

class MemoryStorage : SecureStorage {
    private val values = mutableMapOf<String, ByteArray>()
    var failNextLoad = false
    var loseNextSaveReply = false

    @Synchronized override fun save(key: String, value: ByteArray) {
        values[key] = value.copyOf()
        if (loseNextSaveReply) {
            loseNextSaveReply = false
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

    @Synchronized override fun remove(key: String) { values.remove(key) }
}

class StorageTests : InstrumentationTestCase() {
    fun testUnexpectedCallbackErrorLeavesContextUsable() {
        val storage = MemoryStorage()
        val context = IdentityContext(storage)
        storage.failNextLoad = true
        try {
            context.identity()
            fail("expected storage error")
        } catch (_: RoostException.Storage) {}
        assertNull(context.identity())
    }

    fun testLostWriteReplyRecoversOriginalIdentityAcrossReopen() {
        val storage = MemoryStorage()
        val context = IdentityContext(storage)
        val prepared = context.prepareIdentityCreation()
        context.reserveOperation(prepared.operationId)
        storage.loseNextSaveReply = true
        try {
            context.finalizeOperation(prepared.operationId)
            fail("expected lost write reply")
        } catch (_: RoostException.Storage) {}
        val reopened = IdentityContext(storage)
        reopened.finalizeOperation(prepared.operationId)
        assertTrue(reopened.identity()!!.key.signingKey.contentEquals(
            prepared.identity.key.signingKey))
        assertTrue(reopened.unfinishedOperations().isEmpty())
    }
}
