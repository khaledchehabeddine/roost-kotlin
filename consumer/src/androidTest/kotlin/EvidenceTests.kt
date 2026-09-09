package io.github.relaynet.roost.consumer

import android.test.InstrumentationTestCase
import io.github.relaynet.roost.*
import org.json.JSONObject

class EvidenceTests : InstrumentationTestCase() {
    fun testCanonicalEvidenceCrossesThePackageBoundary() {
        val domainKey = DomainSigningKey(
            bytes("domain_signing_key.identifier_hex"),
            bytes("domain_signing_key.verifying_key_hex"),
            DomainSigningKeyStatus.TRUSTED,
        )
        val verified = verifyEvidenceBundle(
            bytes("valid.bundle_hex"), EvidenceProfile.MESSAGE_TRANSCRIPT_VERSION1,
            listOf(domainKey), bytes("expected_relay_domain_hex"),
            value("accepted_timestamp_secs").toULong(), value("grace_window_secs").toULong(),
        )
        // Pin the decoded bytes across Kotlin's signed ByteArray boundary. Swift owns
        // independent native signature verification; API 36 cannot import these Ed25519 keys.
        for ((field, actual) in listOf(
            "evidence_identity_hex" to verified.evidenceIdentity,
            "session_key_hex" to verified.sessionKey,
            "recipient_identity_verifying_key_hex" to verified.recipientIdentityVerifyingKey,
            "message_id_hex" to verified.messageId,
            "plaintext_hex" to verified.plaintext,
            "sender_identity_ed25519_verifying_key_hex" to verified.senderIdentityKey.ed25519VerifyingKey,
            "sender_identity_x25519_public_key_hex" to verified.senderIdentityKey.x25519PublicKey,
            "relay_domain_hex" to verified.relayDomain,
        )) {
            check(actual.contentEquals(bytes("valid.decoded.$field"))) { field }
        }
        check(verified.timestampMillis == value("valid.decoded.timestamp_millis").toULong())
        check(verified.certificateExpiresAtSecs == value("valid.decoded.certificate_expires_at_secs").toULong())
        check(verified.profile == EvidenceProfile.MESSAGE_TRANSCRIPT_VERSION1)
        check(verified.payloadKind == PayloadKind.ENCRYPTED_TEXT)
    }

    private val fixture by lazy {
        instrumentation.context.assets.open("evidence_bundles_v1.json").bufferedReader().use {
            JSONObject(it.readText())
        }
    }
    private fun value(path: String): String = path.split('.').fold(fixture as Any) { value, key ->
        (value as JSONObject).get(key)
    }.toString()
    private fun bytes(key: String): ByteArray = value(key).chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
