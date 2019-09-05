@file:Suppress("unused")
package com.sonhvp.kryptographer

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.sonhvp.kryptographer.key.AsymmetricKey
import com.sonhvp.kryptographer.key.CryptographicKey
import com.sonhvp.kryptographer.key.SymmetricKey
import com.sonhvp.kryptographer.key.spec.*
import com.sonhvp.kryptographer.key.spec.addAsymmetricKey
import com.sonhvp.kryptographer.key.spec.addSymmetricKey
import java.lang.Exception
import java.security.*
import javax.crypto.Cipher
import javax.crypto.SecretKey

object Kryptographer {

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    internal lateinit var prefs: SharedPreferences
    //Ciphers
    internal val aesCipher: Cipher = Cipher.getInstance(AES_CIPHER)
    internal val rsaCipher: Cipher = Cipher.getInstance(RSA_CIPHER)

    var debugLogging: Boolean = true

    fun init(context: Context) { context.initPrefs() }

    fun initWithDefaultKeys(context: Context) {
        context.initPrefs()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !DEFAULT_SYMMETRIC_ALIAS.isAliasExists()) context.run {
            initKeys(SymmetricKeySpec())
        }
        if (!DEFAULT_ASYMMETRIC_ALIAS.isAliasExists()) initKeys(context, AsymmetricKeySpec())
    }

    fun initWithDefaultSymmetricKey(context: Context) {
        context.initPrefs()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !DEFAULT_SYMMETRIC_ALIAS.isAliasExists()) context.run {
            initKeys(SymmetricKeySpec())
        }
    }

    fun initWithDefaultAsymmetricKey(context: Context) {
        context.initPrefs()
        if (!DEFAULT_ASYMMETRIC_ALIAS.isAliasExists()) initKeys(context, AsymmetricKeySpec())
    }

    private fun Context.initPrefs() { prefs = getSharedPreferences(CRYPTOGRAPHER_PREFS, Context.MODE_PRIVATE) }

    fun initKeys(context: Context, vararg cryptographicKeySpecs: CryptographicKeySpec) {
        cryptographicKeySpecs.forEach {
            when (it) {
                is SymmetricKeySpec -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (it.alias.isAliasExists()) log("${it.alias} already initialized") else addSymmetricKey(it)
                }
                is AsymmetricKeySpec -> if (it.alias.isAliasExists()) log("${it.alias} is already initialized") else addAsymmetricKey(context, it)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun initKeys(vararg cryptographicKeySpecs: CryptographicKeySpec) {
        cryptographicKeySpecs.forEach {
            when (it) {
                is SymmetricKeySpec -> if (it.alias.isAliasExists()) log("${it.alias} is already initialized") else addSymmetricKey(it)
                is AsymmetricKeySpec -> if (it.alias.isAliasExists()) log("${it.alias} is already initialized") else addAsymmetricKey(it)
            }
        }
    }

    /**
     * Check this alias is exist or not
     */
    private fun String.isAliasExists(): Boolean = keyStore.containsAlias(this)

    /**
     * Check this key is exist or not
     */
    fun isKeyExists(alias: String): Boolean = keyStore.containsAlias(alias)

    /**
     * Get alias list
     * @return alias list
     */
    fun getKeyAliases(): MutableList<String> = keyStore.aliases().toList().toMutableList()

    /**
     * Get key list
     * @return key list
     */
    fun getAllKeys(): MutableList<CryptographicKey> = getKeyAliases().map { getKey(it) }.toMutableList()

    /**
     * Get key by alias
     * @param alias key's alias
     * @return asymmetric key or symmetric key
     */
    fun getKey(alias: String): CryptographicKey {
        if (keyStore.containsAlias(alias)) {
            val key = keyStore.getKey(alias, null)
            return when (key.algorithm) {
                KEY_RSA_ALGORITHM -> AsymmetricKey(alias, "asymmetric", key.algorithm, keyStore.getCertificate(alias).publicKey, key as PrivateKey)
                KEY_AES_ALGORITHM -> SymmetricKey(alias, "symmetric", key.algorithm, key as SecretKey)
                else -> throw Exception("Key algorithm is not supported")
            }
        } else {
            throw Exception("$alias key doesn't exist")
        }
    }

    /**
     * Get default symmetric key
     * Only available if default key has been initialized with initWithDefaultKeys() or initWithDefaultSymmetricKey()
     * @see initWithDefaultKeys()
     * @see initWithDefaultSymmetricKey()
     * @return SymmetricKey
     */
    fun defaultSymmetricKey(): SymmetricKey = getSymmetricKey(DEFAULT_SYMMETRIC_ALIAS)

    /**
     * Get default asymmetric key
     * Only available if default key has been initialized with initWithDefaultKeys() or initWithDefaultAsymmetricKey()
     * @see initWithDefaultKeys()
     * @see initWithDefaultAsymmetricKey()
     * @return AsymmetricKey
     */
    fun defaultAsymmetricKey(): AsymmetricKey = getAsymmetricKey(DEFAULT_ASYMMETRIC_ALIAS)

    /**
     * Get symmetric key by alias
     * @param alias key's alias
     * @return SymmetricKey
     */
    fun getSymmetricKey(alias: String): SymmetricKey = getKey(alias) as SymmetricKey
    /**
     * Get asymmetric key by alias
     * @param alias key's alias
     * @return AsymmetricKey
     */
    fun getAsymmetricKey(alias: String): AsymmetricKey = getKey(alias) as AsymmetricKey

    /**
     * Delete all keys
     * @return return true if success
     */
    fun deleteAllKeys(): Boolean {
        keyStore.aliases().iterator().forEach {
            keyStore.deleteEntry(it)
        }
        log("All keys is deleted")
        return true
    }

    /**
     * Delete key
     * @param alias enter alias for key will be deleted
     * @return return true if success
     */
    fun deleteKey(alias: String): Boolean = if (alias.isAliasExists()) {
        keyStore.deleteEntry(alias)
        log("$alias key is deleted")
        true
    } else {
        false
    }

}

internal fun log(msg: String) {
    if (BuildConfig.DEBUG && Kryptographer.debugLogging) Log.d("Kryptographer", msg)
}

internal const val DEFAULT_ASYMMETRIC_ALIAS = "DefaultAsymmetric"
internal const val DEFAULT_SYMMETRIC_ALIAS = "DefaultSymmetric"

internal const val AES_CIPHER = "AES/CBC/PKCS7Padding"
internal const val RSA_CIPHER = "RSA/ECB/PKCS1Padding"

private const val CRYPTOGRAPHER_PREFS = "cryptographer_prefs"

internal const val ANDROID_KEYSTORE = "AndroidKeyStore"
internal const val KEY_RSA_ALGORITHM = "RSA"
internal const val KEY_AES_ALGORITHM = "AES"