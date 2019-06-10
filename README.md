## Kryptographer [![](https://jitpack.io/v/com.sonhvp/cryptographer.svg)](https://jitpack.io/#com.sonhvp/cryptographer)
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
By default, library uses asymmetric key with RSA/ECB/PKCS1Padding and symmetric key with AES/CBC/PKCS7Padding to encrypt/decrypt data.

| API 19-22 | API 23+ |
| --- | --- |
| Asymmetric Key (2048 bits) | Symmetric Key (256 bits) |
| RSA/ECB/PKCS1Padding | AES/CBC/PKCS7Padding |
### Basic Usage
Initialize in your Application.onCreate() method
```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Kryptographer.initWithDefaultKeys(this@App)
    }
}
```
```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val text = "123$%^{}@abc"
        val encryptedText = Kryptographer.defaultSymmetricKey().encrypt(text)
        val decryptedText = Kryptographer.defaultSymmetricKey().decrypt(encryptedText)
    }
}
```
### Advanced Usage
```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Kryptographer.init(this@App)

        Kryptographer.initKeys(
            this@App,
            asymmetricKey {
                alias = "MyAsymmetricKey"
            },
            symmetricKey {
                alias = "MySymmetricKey"
            }
        )
    }
}
```
```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val text = "123$%^{}@abc"
        val encryptedText = Kryptographer.getKey("MyAsymmetricKey").encrypt(text)
        val decryptedText = Kryptographer.getKey("MyAsymmetricKey").decrypt(encryptedText)
    }
}
```
```kotlin
//Get key aliases
val keyAliases = Kryptographer.getKeyAliases()

//Delete key
Kryptographer.deleteAllKeys()
Kryptographer.deleteKey("alias")
```

### License
```
Apache 2.0
```
