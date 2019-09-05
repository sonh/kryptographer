@file:Suppress("unused")
package com.sonhvp.kryptographer.key.spec

import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import com.sonhvp.kryptographer.ANDROID_KEYSTORE
import com.sonhvp.kryptographer.DEFAULT_ASYMMETRIC_ALIAS
import com.sonhvp.kryptographer.KEY_RSA_ALGORITHM
import com.sonhvp.kryptographer.log
import java.lang.Exception
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.spec.AlgorithmParameterSpec
import java.util.*
import javax.security.auth.x500.X500Principal

data class AsymmetricKeySpec (
    override var alias: String = DEFAULT_ASYMMETRIC_ALIAS,
    override var keySize: Int = 2048,
    override var subject: X500Principal = X500Principal("CN=Default"),
    override var startTime: Date = Calendar.getInstance().time,
    override var endTime: Date = Calendar.getInstance().apply { add(Calendar.YEAR, 24) }.time ) : CryptographicKeySpec()

fun asymmetricKey(block: AsymmetricKeySpec.() -> Unit): AsymmetricKeySpec =  AsymmetricKeySpec().apply(block)

@RequiresApi(Build.VERSION_CODES.M)
internal fun addAsymmetricKey(asymmetricKeySpec: AsymmetricKeySpec) {
    val keyPairSpec = initKeySpec(asymmetricKeySpec)
    KeyPairGenerator.getInstance(
        KEY_RSA_ALGORITHM,
        ANDROID_KEYSTORE
    ).run {
        initialize(keyPairSpec)
        generateKeyPair()
    }
    log("asymmetricKey ${asymmetricKeySpec.alias} is initialized")
}

internal fun addAsymmetricKey(context: Context, asymmetricKeySpec: AsymmetricKeySpec) {
    when (asymmetricKeySpec.keySize) {
        1024, 2048 -> {}
        else -> throw Exception("Asymmetric Key with RSA only support 1024 and 2046 bit length")
    }
    val keyPairSpec: AlgorithmParameterSpec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        initKeySpec(asymmetricKeySpec)
    } else {
        KeyPairGeneratorSpec.Builder(context)
            .setAlias(asymmetricKeySpec.alias)
            .setSubject(asymmetricKeySpec.subject)
            .setSerialNumber(BigInteger.ONE)
            .setKeySize(asymmetricKeySpec.keySize)
            .setStartDate(asymmetricKeySpec.startTime)
            .setEndDate(asymmetricKeySpec.endTime)
            .build()
    }
    KeyPairGenerator.getInstance(
        KEY_RSA_ALGORITHM,
        ANDROID_KEYSTORE
    ).run {
        initialize(keyPairSpec)
        generateKeyPair()
    }
    log("asymmetricKey ${asymmetricKeySpec.alias} is initialized")
}

@RequiresApi(Build.VERSION_CODES.M)
private fun initKeySpec(asymmetricKeySpec: AsymmetricKeySpec) = KeyGenParameterSpec.Builder (
        asymmetricKeySpec.alias,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    ).run {
        setBlockModes(KeyProperties.BLOCK_MODE_ECB)
        setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
        setKeySize(asymmetricKeySpec.keySize)
        setCertificateSubject(asymmetricKeySpec.subject)
        //setRandomizedEncryptionRequired(false)
        setKeyValidityStart(asymmetricKeySpec.startTime)
        setKeyValidityEnd(asymmetricKeySpec.endTime)
        setCertificateNotBefore(asymmetricKeySpec.startTime)
        setCertificateNotAfter(asymmetricKeySpec.endTime)
        setCertificateSerialNumber(BigInteger(256, Random()))
        setDigests(
            //KeyProperties.DIGEST_NONE,
            //KeyProperties.DIGEST_MD5,
            KeyProperties.DIGEST_SHA1,
            //KeyProperties.DIGEST_SHA224,
            KeyProperties.DIGEST_SHA256
            //KeyProperties.DIGEST_SHA384,
            //KeyProperties.DIGEST_SHA512
        )
        build()
    }