package com.sonhvp.kryptographer.key.spec

import java.util.*
import javax.security.auth.x500.X500Principal

abstract class CryptographicKeySpec {
    abstract var alias: String
    abstract var keySize: Int
    abstract var subject: X500Principal
    abstract var startTime: Date
    abstract var endTime: Date
}