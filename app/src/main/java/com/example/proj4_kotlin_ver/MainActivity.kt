package com.example.proj4_kotlin_ver

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    private var ringtone = RingtoneManager.getRingtone(this, uri)
    private var ringtone_String: String? = null
    private lateinit var ringtone_uri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapsFragment = MapsFragment()
        val alarmStopFragment = AlarmStopFragment()

        setSupportActionBar(tool_bar)

        // アプリを開いた時にアラームがセットされていればストップ機能を有したFragmentを表示する
        if(isServiceWorking(GeoFencingService::class.java)) {
            replaceFragment(alarmStopFragment)
        }else{
            replaceFragment(mapsFragment)
        }

        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        ringtone_String = sharedPref.getString(getString(R.string.saved_ringtone), null)
        if (ringtone_String != null) {
            ringtone_uri = Uri.parse(ringtone_String)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.setting -> {
                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "タイトル")
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false) // サイレントは見せない

                intent.putExtra(
                    RingtoneManager.EXTRA_RINGTONE_TYPE,
                    RingtoneManager.TYPE_ALARM
                ) // アラーム音

                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false) // デフォルトは表示しない

                if (ringtone_String != null) {
                    intent.putExtra(
                        RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                        ringtone_uri
                    ) //Preferenceがあった場合の選択済み
                } else if (uri != null) {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, uri) // 選択済みを選択する
                }
                startActivityForResult(intent, 1)
                return true
            }
            R.id.aboutApp -> {
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == 1 && data != null) {
            uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            ringtone = RingtoneManager.getRingtone(this, uri)
            val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
            with(sharedPref.edit()) {
                putString(getString(R.string.saved_ringtone), uri.toString())
                commit()
            }
        }else{
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commit()
    }

    // サービスが動いていればTrue、動いていなければFalse
    private fun isServiceWorking(clazz: Class<*>): Boolean {
        val manager = this.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return manager.getRunningServices(Integer.MAX_VALUE).any { clazz.name == it.service.className }
    }
}