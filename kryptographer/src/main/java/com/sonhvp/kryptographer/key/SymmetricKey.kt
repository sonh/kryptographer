package com.sonhvp.kryptographer.key

import android.os.Build
import android.security.keystore.KeyInfo
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import com.sonhvp.kryptographer.ANDROID_KEYSTORE
import com.sonhvp.kryptographer.Kryptographer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec

data class SymmetricKey (
    override val alias: String,
    override val type: String ,
    override val algorithm: String,
    val key: SecretKey
) : CryptographicKey() {

    override fun encrypt(data: String, iv: ByteArray?): String {
        val cipherIv = if (iv == null) {
            if (!Kryptographer.prefs.contains(DEFAULT_IV_PREF)) {
                //Random bytes. 128/256 bits Key use 128 bits IV (16 bytes)
                val randomIv = ByteArray(16)
                SecureRandom().nextBytes(randomIv)
                //Save default IV
                Kryptographer.prefs.edit(commit = true) {
                    putString(DEFAULT_IV_PREF, Base64.encodeToString(randomIv, Base64.NO_WRAP))
                }
            }
            //Get default IV
            val defaultIvStr = Kryptographer.prefs.getString(DEFAULT_IV_PREF, "")
            Base64.decode(defaultIvStr, Base64.NO_WRAP)
        } else {
            iv
        }

        Kryptographer.aesCipher.init(Cipher.ENCRYPT_MODE, this@SymmetricKey.key, IvParameterSpec(cipherIv))

        //Encrypt Byte Array
        val encryptedBytes = Kryptographer.aesCipher.doFinal(data.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
    }

    override fun decrypt(encryptedData: String, iv: ByteArray?): String {
        val cipherIv = if (iv == null) {
            //Get default IV
            val defaultIvStr = Kryptographer.prefs.getString(DEFAULT_IV_PREF, "")
            Base64.decode(defaultIvStr, Base64.NO_WRAP)
        } else {
            iv
        }

        Kryptographer.aesCipher.init(Cipher.DECRYPT_MODE, this@SymmetricKey.key, IvParameterSpec(cipherIv))

        val encryptedBytes = Base64.decode(encryptedData, Base64.NO_WRAP)
        val decryptedBytes = Kryptographer.aesCipher.doFinal(encryptedBytes)
        return String(decryptedBytes)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun isInsideSecureHardware(): Boolean {
        val factory = SecretKeyFactory.getInstance(key.algorithm, ANDROID_KEYSTORE)
        val keyInfo = factory.getKeySpec(key, KeyInfo::class.java) as KeyInfo
        return keyInfo.isInsideSecureHardware
    }

}

private const val AES_CIPHER = "AES/CBC/PKCS7Padding"
private const val DEFAULT_IV_PREF = "default_iv_pref"