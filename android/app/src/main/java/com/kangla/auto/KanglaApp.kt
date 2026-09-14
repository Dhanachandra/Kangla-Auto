package com.kangla.auto

import android.app.Application
import com.kangla.auto.data.Repository

class KanglaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Repository.init(this)
    }
}