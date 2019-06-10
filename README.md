## Cryptographer [![](https://jitpack.io/v/com.sonhvp/cryptographer.svg)](https://jitpack.io/#com.sonhvp/cryptographer)
### Gradle Setup
In your project level build.gradle
```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```
In your app level build.gradle
```gradle
dependencies {
    implementation 'com.sonhvp:cryptographer:1.0.1'
}
```
### Basic Usage
By default, library uses asymmetric key with RSA/ECB/PKCS1Padding in Android API 19-22 and symmetric key with AES/CBC/PKCS7Padding in Android API 23 and higher to encrypt/decrypt data.  
```kotlin
class MainActivity : AppCompatActivity() {

    private val cryptographer by lazy { initCryptographer() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val text = "123$%^{}@abc"
        val encryptedText = cryptographer.encrypt(text)
        val decryptedText = cryptographer.decrypt(encryptedText)
    }
}
```

| API 19-22 | API 23+ |
| --- | --- |
| Asymmetric Key (2048 bits) | Symmetric Key (256 bits) |
| RSA/ECB/PKCS1Padding | AES/CBC/PKCS7Padding |

### Advanced Usage
If symmetricKey isn't initialized, only use asymmetric key to encrypt/decrypt.
```kotlin
val startTime = Calendar.getInstance().time
val endTime = Calendar.getInstance().apply { add(Calendar.YEAR, 20) }.time

val cryptographer = initCryptographer {
    asymmetricKey {
        setAlias("MyAsymmetricKey")
        setSubject(X500Principal("CN=Default"))
        setSerialNumber(BigInteger.ONE)
        setKeySize(2048)
        setStartDate(startTime)
        setEndDate(endTime)
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        symmetricKey("MySymmetricKey", KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT) {
            setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            setKeySize(256)
            setRandomizedEncryptionRequired(false)
            setKeyValidityStart(startTime)
            setKeyValidityEnd(endTime)
            setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
        }
    }
}

//Use defaultAsymmetricKey() or defaultSymmetricKey() to init default key
```
```kotlin
//You can use your IV, only AES/CBC/PKCS7Padding cipher use IV to encrypt/decrypt
cryptographer.encrypt("data", iv)
cryptographer.decrypt("data", iv)

//Get key aliases
val keyAliases = KeyManager.getKeyAliases()

//Delete key
KeyManager.deleteAllKey()
KeyManager.deleteKey("alias")
```

### License
```
Apache 2.0
```
