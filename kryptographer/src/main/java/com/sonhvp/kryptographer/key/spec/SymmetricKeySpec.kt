@file:Suppress("unused")
package com.sonhvp.kryptographer.key.spec

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import com.sonhvp.kryptographer.ANDROID_KEYSTORE
import com.sonhvp.kryptographer.DEFAULT_SYMMETRIC_ALIAS
import com.sonhvp.kryptographer.log
import java.lang.Exception
import java.math.BigInteger
import java.util.*
import javax.crypto.KeyGenerator
import javax.security.auth.x500.X500Principal

data class SymmetricKeySpec (
    override var alias: String = DEFAULT_SYMMETRIC_ALIAS,
    override var keySize: Int = 256,
    override var subject: X500Principal = X500Principal("CN=Default"),
    override var startTime: Date = Calendar.getInstance().time,
    override var endTime: Date = Calendar.getInstance().apply { add(Calendar.YEAR, 24) }.time ) : CryptographicKeySpec()

fun symmetricKey(block: SymmetricKeySpec.() -> Unit): SymmetricKeySpec =  SymmetricKeySpec().apply(block)

@RequiresApi(Build.VERSION_CODES.M)
internal fun addSymmetricKey(symmetricKeySpec: SymmetricKeySpec) {
    when (symmetricKeySpec.keySize) {
        128, 256 -> {

        }
        else -> throw Exception("")
    }
    val keyGenParamSpec = KeyGenParameterSpec.Builder(
        symmetricKeySpec.alias,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    ).run {
        setBlockModes(KeyProperties.BLOCK_MODE_CBC)
        setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
        setKeySize(symmetricKeySpec.keySize)
        setRandomizedEncryptionRequired(false)
        setKeyValidityStart(symmetricKeySpec.startTime)
        setKeyValidityEnd(symmetricKeySpec.endTime)
        setCertificateNotBefore(symmetricKeySpec.startTime)
        setCertificateNotAfter(symmetricKeySpec.endTime)
        setCertificateSerialNumber(BigInteger(256, Random()))
        setDigests(
            KeyProperties.DIGEST_SHA256,
            KeyProperties.DIGEST_SHA512
        )
        build()
    }
    KeyGenerator.getInstance(
        KeyProperties.KEY_ALGORITHM_AES,
        ANDROID_KEYSTORE
    ).run {
        init(keyGenParamSpec)
        generateKey()
    }
    log("symmetricKey ${symmetricKeySpec.alias} is initialized")
}