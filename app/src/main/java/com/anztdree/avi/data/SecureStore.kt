package com.anztdree.avi.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Penyimpanan terenkripsi memakai AndroidKeyStore (AES-GCM).
 * Dipakai untuk API key. Kalau KeyStore gagal (perangkat eksotis),
 * pemanggil fallback ke plaintext — aplikasi tetap jalan.
 */
object SecureStore {

    private const val KEY_ALIAS = "avi_master_key"
    private const val TRANSFORM = "AES/GCM/NoPadding"
    private const val PREFIX = "enc1:"

    private fun getOrCreateKey(): SecretKey? = try {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(KEY_ALIAS, null) as? SecretKey) ?: run {
            val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            gen.init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
            gen.generateKey()
        }
    } catch (_: Exception) {
        null
    }

    fun encrypt(plain: String): String? = try {
        val key = getOrCreateKey() ?: return null
        val cipher = Cipher.getInstance(TRANSFORM)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val ct = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        PREFIX + Base64.encodeToString(iv + ct, Base64.NO_WRAP)
    } catch (_: Exception) {
        null
    }

    fun decrypt(stored: String): String? = try {
        if (!stored.startsWith(PREFIX)) stored
        else {
            val key = getOrCreateKey() ?: return ""
            val raw = Base64.decode(stored.removePrefix(PREFIX), Base64.NO_WRAP)
            val spec = GCMParameterSpec(128, raw, 0, 12)
            val cipher = Cipher.getInstance(TRANSFORM)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            String(cipher.doFinal(raw, 12, raw.size - 12), Charsets.UTF_8)
        }
    } catch (_: Exception) {
        // K14 — nilai terenkripsi rusak / kunci KeyStore hilang → kosongkan
        // supaya UI jelas "key belum ada" dan pemilik menempel ulang, BUKAN
        // mengirim sampah terenkripsi ke Google sebagai API key.
        if (stored.startsWith(PREFIX)) "" else null
    }
}
