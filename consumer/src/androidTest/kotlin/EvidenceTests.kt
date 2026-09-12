package io.github.relaynet.roost.consumer

import android.test.InstrumentationTestCase
import io.github.relaynet.roost.*

class EvidenceTests : InstrumentationTestCase() {
    fun testEvidenceFieldsCrossThePackageBoundary() {
        val domainKey = DomainSigningKey(
            "test-domain".encodeToByteArray(),
            bytes("6556ea860463d074ae046736e1f20f6c78f7a26e31518d1fe7f645150aaa6ad1"),
            DomainSigningKeyStatus.TRUSTED,
        )
        val verified = verifyEvidenceBundle(
            bytes("01011009090909090909090909090909090909bc7cbcb5636375fa1d82434d466724d92377f53b980695dd49d26d0ce1" +
                "2205a50221000000004000800000000000000081fca7ffbc3100200000001b636f6e7465787420666f7220616e206162" +
                "757365207265706f7274007b41657468657253656e64657243657274696669636174652d76323a020b746573742d646f" +
                "6d61696e0a72656c61792e7465737457f24707f390532aa6598f61c43f3bc2dae984525359cb43fc13db947e93f63e65" +
                "e8f9b0bc6eae124169f0576f97362d295a8cf5f770b45e14357ce647d33eec00000000655577a0409ed439e636de0aab" +
                "b9ec5e6c0562d1c2892d1a1679897b3f4d1583bcc4abfc014821307b3bb6e35ec85fe66bca2c2e46ffd56f0bd533e434" +
                "69659c01f985450640340624ac228577b9d3823baec5f873a09708c2e88d4f83ce4f2660911f7606b89a1b0b0f82399c" +
                "1137947e6fc1c1bec30553cfdc435559c1d76007741143e40f"), EvidenceProfile.MESSAGE_TRANSCRIPT_VERSION1,
            listOf(domainKey), "relay.test".encodeToByteArray(),
            1700000000uL, 0uL,
        )
        // Pin the decoded bytes across Kotlin's signed ByteArray boundary. Swift owns
        // independent native signature verification; API 36 cannot import these Ed25519 keys.
        check(verified.evidenceIdentity.contentEquals(bytes("a7af7974cb7c5c3c770baf1b946f8d7d69a295068d99b78425b1df538a6aa489")))
        check(verified.sessionKey.contentEquals(ByteArray(16) { 9 }))
        check(verified.recipientIdentityVerifyingKey.contentEquals(bytes("bc7cbcb5636375fa1d82434d466724d92377f53b980695dd49d26d0ce12205a5")))
        check(verified.messageId.contentEquals(bytes("02210000000040008000000000000000")))
        check(verified.plaintext.contentEquals("context for an abuse report".encodeToByteArray()))
        check(verified.senderIdentityKey.ed25519VerifyingKey.contentEquals(bytes("65e8f9b0bc6eae124169f0576f97362d295a8cf5f770b45e14357ce647d33eec")))
        check(verified.senderIdentityKey.x25519PublicKey.contentEquals(bytes("57f24707f390532aa6598f61c43f3bc2dae984525359cb43fc13db947e93f63e")))
        check(verified.relayDomain.contentEquals("relay.test".encodeToByteArray()))
        check(verified.timestampMillis == 1700000300545uL)
        check(verified.certificateExpiresAtSecs == 1700100000uL)
        check(verified.profile == EvidenceProfile.MESSAGE_TRANSCRIPT_VERSION1)
        check(verified.payloadKind == PayloadKind.ENCRYPTED_TEXT)
    }

    private fun bytes(hex: String): ByteArray = hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
