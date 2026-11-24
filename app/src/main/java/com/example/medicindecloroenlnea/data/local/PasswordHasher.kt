package com.example.medicindecloroenlnea.data.local

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

internal object PasswordHasher {
    private const val SALT_BYTES = 16
    private const val PBKDF2_ITERATIONS = 120_000
    private const val HASH_BITS = 256

    fun crearSal(): ByteArray = ByteArray(SALT_BYTES).also(SecureRandom()::nextBytes)

    fun hash(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, HASH_BITS)
        return try {
            Base64.encodeToString(
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded,
                Base64.NO_WRAP
            )
        } finally {
            spec.clearPassword()
        }
    }

    fun coincide(expected: String, actual: String): Boolean {
        val expectedBytes = expected.toByteArray(Charsets.UTF_8)
        val actualBytes = actual.toByteArray(Charsets.UTF_8)
        var result = expectedBytes.size xor actualBytes.size
        for (index in 0 until maxOf(expectedBytes.size, actualBytes.size)) {
            result = result or (
                (expectedBytes.getOrElse(index) { 0 }.toInt()) xor
                    (actualBytes.getOrElse(index) { 0 }.toInt())
                )
        }
        return result == 0
    }
}
