package com.example.proj4_kotlin_ver

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private var uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    private var ringtone = RingtoneManager.getRingtone(this, uri)
    private var ringtone_String: String? = null
    private lateinit var ringtone_uri: Uri
    private val REQUEST_CODE = 101

    private val mapsFragment = MapsFragment()
    private val alarmStopFragment = AlarmStopFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(tool_bar)

        val fineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        /*
        位置情報の取得に関して利用者に同意を求める
        android 10 以降ではService等で位置情報を求める場合にはACCESS_BACKGROUND_LOCATIONのパーミッションが必要となる
         */
        if(fineLocationPermission) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val backgroundPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED

                if(!backgroundPermission) {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_CODE)
                }
            }
        } else {
            var permissionArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) permissionArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
            requestPermissions(permissionArray, REQUEST_CODE)
        }

    }

    override fun onResume() {
        super.onResume()

        // アプリを開いた時にアラームがセットされていればストップ機能を有したFragmentを表示する
        if(isServiceWorking(GeoFencingService::class.java)) {
            replaceFragment(alarmStopFragment)
        }else{
            replaceFragment(mapsFragment)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            REQUEST_CODE -> {
                if((grantResults.isNotEmpty()) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return
                } else {
                    Toast.makeText(this, "権限の非許可", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            else -> {
                Toast.makeText(this, "権限の非許可", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.setting -> {
                val sharedPref = getPreferences(Context.MODE_PRIVATE)
                ringtone_String = sharedPref.getString(getString(R.string.saved_ringtone), null)
                if (ringtone_String != null) {
                    ringtone_uri = Uri.parse(ringtone_String)
                }

                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.settingTitle))
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
            R.id.stationsDetail -> {
                startActivity(Intent(this, StationsDetailActivity::class.java))
                return true
            }
            R.id.aboutApp -> {
                startActivity(Intent(this, OssLicensesMenuActivity::class.java))
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
            println(ringtone.getTitle(this))
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