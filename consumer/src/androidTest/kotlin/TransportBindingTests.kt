package io.github.relaynet.roost.consumer

import android.test.InstrumentationTestCase
import io.github.relaynet.roost.*

class TransportBindingTests : InstrumentationTestCase() {
    fun testBinaryBindingAuthenticatesSlices() {
        val alice = IdentityContext(MemoryStorage())
        val bob = IdentityContext(MemoryStorage())
        val aliceIdentity = alice.prepareIdentityCreation()
        complete(alice, aliceIdentity.operationId)
        val bobIdentity = bob.prepareIdentityCreation()
        complete(bob, bobIdentity.operationId)
        val signedPrekey = bob.prepareSignedPrekeyRotation()
        complete(bob, signedPrekey.operationId)
        val publication = bob.currentPublication()
        val selection = PreKeySelection(publication.signedPrekey.id, null)

        // Kotlin bytes are signed. Slices must preserve NUL and high-bit bytes.
        val backing = byteArrayOf(0x55, 0x00, 0x7f, 0x80.toByte(), 0xff.toByte(), 0x55)
        val initiation = alice.messaging().initiateSession(
            ByteArray(16) { 0x58 },
            bobIdentity.identity,
            publication,
            null,
            byteArrayOf(),
            byteArrayOf(),
            "binding conversion".encodeToByteArray(),
            TransportBinding(backing.copyOfRange(1, 5)),
        )
        val hint = bob.messaging().initiatorIdentityHint(
            initiation.message,
            TransportBinding(byteArrayOf(0x00, 0x7f, 0x80.toByte(), 0xff.toByte())),
            selection,
        )
        check(hint.key.signingKey.contentEquals(aliceIdentity.identity.key.signingKey))
        try {
            bob.messaging().initiatorIdentityHint(
                initiation.message,
                TransportBinding(byteArrayOf(0x00, 0x7f, 0x81.toByte(), 0xff.toByte())),
                selection,
            )
            error("changed binding byte authenticated")
        } catch (_: RoostException.AuthenticationFailed) { }
    }

    private fun complete(context: IdentityContext, id: OperationId) {
        context.reserveOperation(id)
        context.finalizeOperation(id)
    }

}
