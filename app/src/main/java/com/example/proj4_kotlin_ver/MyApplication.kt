package com.example.proj4_kotlin_ver

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

class MyApplication: Application() {

    // Realmを初期化する
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        // .deleteRealmIfMigrationNeeded()は開発中のみつけておく
        val config = RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build()
        Realm.setDefaultConfiguration(config)
    }
}