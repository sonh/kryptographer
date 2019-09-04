package com.sonhvp.kryptographer.key

abstract class CryptographicKey {

    abstract val alias: String
    abstract val type: String
    abstract val algorithm: String

    /** Encrypt with AES/CBC/PKCS7Padding Cipher
     *  @param data String(UTF8)
     *  @param iv If IV is null, default IV will use to encrypt, RSA cipher doesn't need IV
     *  @return encoded String (Base64)
     */
    abstract fun encrypt(data: String, iv: ByteArray? = null): String

    /** Decrypt with AES/CBC/PKCS7Padding Cipher. <br />
     *  @param encryptedData encoded String (Base64)
     *  @param iv If IV is null, default IV will use to decrypt, RSA cipher doesn't need IV
     *  @return String(UTF-8)
     */
    abstract fun decrypt(encryptedData: String, iv: ByteArray? = null): String

    abstract fun isInsideSecureHardware(): Boolean
}