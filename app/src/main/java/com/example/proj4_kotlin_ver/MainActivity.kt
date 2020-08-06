package com.example.proj4_kotlin_ver

import android.app.ActivityManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    // サービスがすでに起動されている場合TrueになるFlag
    private var serviceRunningFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapsFragment = MapsFragment()
        val alarmStopFragment = AlarmStopFragment()

        serviceRunningFlag = isServiceWorking(GeoFencingService::class.java)
        if(serviceRunningFlag) {
            replacceFragment(alarmStopFragment)
        }else{
            replacceFragment(mapsFragment)
        }
    }

    private fun replacceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commit()
    }

    private fun isServiceWorking(clazz: Class<*>): Boolean {
        val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE).any { clazz.name == it.service.className }
    }
}