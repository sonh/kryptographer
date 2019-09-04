package com.sonhvp.kryptographer.key

import android.os.Build
import android.security.keystore.KeyInfo
import android.util.Base64
import androidx.annotation.RequiresApi
import com.sonhvp.kryptographer.ANDROID_KEYSTORE
import com.sonhvp.kryptographer.Kryptographer
import com.sonhvp.kryptographer.log
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher

data class AsymmetricKey (
    override val alias: String,
    override val type: String ,
    override val algorithm: String,
    val publicKey: PublicKey,
    val privateKey: PrivateKey
) : CryptographicKey() {

    override fun encrypt(data: String, iv: ByteArray?): String {
        if (iv != null) log("RSA cipher doesn't need IV")
        Kryptographer.rsaCipher.init(Cipher.ENCRYPT_MODE, this@AsymmetricKey.publicKey)

        val encryptedBytes = Kryptographer.rsaCipher.doFinal(data.toByteArray())
        return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
    }

    override fun decrypt(encryptedData: String, iv: ByteArray?): String {
        if (iv != null) log("RSA cipher doesn't need IV")
        Kryptographer.rsaCipher.init(Cipher.DECRYPT_MODE, this@AsymmetricKey.privateKey)

        val encryptedBytes = Base64.decode(encryptedData, Base64.NO_WRAP)
        val decryptedBytes = Kryptographer.rsaCipher.doFinal(encryptedBytes)
        return String(decryptedBytes)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun isInsideSecureHardware(): Boolean {
        val factory = KeyFactory.getInstance(privateKey.algorithm, ANDROID_KEYSTORE)
        val keyInfo = factory.getKeySpec(privateKey, KeyInfo::class.java)
        return keyInfo.isInsideSecureHardware
    }

}

/** Default RSA Cipher. */
private const val RSA_CIPHER = "RSA/ECB/PKCS1Padding"