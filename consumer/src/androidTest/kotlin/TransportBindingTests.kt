package io.github.relaynet.roost.consumer

import android.test.InstrumentationTestCase
import io.github.relaynet.roost.*

class TransportBindingTests : InstrumentationTestCase() {
    fun testBinaryBindingAuthenticatesSlices() {
        val alice = Client(InMemorySecureStorage())
        val bob = Client(InMemorySecureStorage())
        val aliceIdentity = alice.identity().prepareGenerateIdentity()
        complete(alice, aliceIdentity.operation)
        val bobIdentity = bob.identity().prepareGenerateIdentity()
        complete(bob, bobIdentity.operation)
        val signedPrekey = bob.identity().prepareRotateSignedPrekeyNow()
        complete(bob, signedPrekey.operation)
        val publication = bob.identity().currentPublicationBundle()
        val selection = PreKeySelection(publication.signedPrekey.identifier, null)

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
        check(hint.ed25519VerifyingKey.contentEquals(aliceIdentity.identity.ed25519VerifyingKey))
        try {
            bob.messaging().initiatorIdentityHint(
                initiation.message,
                TransportBinding(byteArrayOf(0x00, 0x7f, 0x81.toByte(), 0xff.toByte())),
                selection,
            )
            error("changed binding byte authenticated")
        } catch (_: ClientException.AuthenticationFailed) { }
    }

    private fun complete(client: Client, operation: KeyStoreOperation) {
        client.commitOperation(operation.identifier)
        client.finalizeOperation(operation.identifier)
    }

}
