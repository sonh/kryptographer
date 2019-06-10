package com.sonhvp.app

import android.app.Application
import com.sonhvp.kryptographer.Kryptographer

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Kryptographer.initWithDefaultKeys(this@App)
    }
    
}