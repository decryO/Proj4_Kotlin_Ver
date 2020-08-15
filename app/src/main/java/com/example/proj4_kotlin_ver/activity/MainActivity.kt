package com.example.proj4_kotlin_ver.activity

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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.proj4_kotlin_ver.service.GeoFencingService
import com.example.proj4_kotlin_ver.fragment.MapsFragment
import com.example.proj4_kotlin_ver.dialog.PermissionDENIEDDialogFragment
import com.example.proj4_kotlin_ver.R
import com.example.proj4_kotlin_ver.dialog.DescriptionDialogFragment
import com.example.proj4_kotlin_ver.fragment.AlarmStopFragment
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), DescriptionDialogFragment.DescriptionDialogListener,
    PermissionDENIEDDialogFragment.PermissionDENIEDDialogListener {

    private var uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    private var ringtone = RingtoneManager.getRingtone(this, uri)
    private var ringtoneString: String? = null
    private lateinit var ringtoneUri: Uri
    private val requestCode = 101

    private val mapsFragment =
        MapsFragment()
    private val alarmStopFragment =
        AlarmStopFragment()
    private var permissionArray: Array<String> = arrayOf()
    private val descriptionDialog =
        DescriptionDialogFragment()
    private val DENIEDDialog =
        PermissionDENIEDDialogFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(tool_bar)

        val args = Bundle()
        val fineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        /*
        位置情報の取得に関して利用者に同意を求める
        android 10 以降ではService等で位置情報を求める場合にはACCESS_BACKGROUND_LOCATIONのパーミッションが必要となる
         */
        if(fineLocationPermission) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val backgroundPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED

                if(!backgroundPermission) {
                    permissionArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    args.putInt("descriptionNumber", 1)
                    descriptionDialog.arguments = args
                    descriptionDialog.show(supportFragmentManager, "simple")
                }
            }
        } else {
            permissionArray += Manifest.permission.ACCESS_FINE_LOCATION
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissionArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                args.putInt("descriptionNumber", 1)
            } else args.putInt("descriptionNumber", 0)
            descriptionDialog.arguments = args
            descriptionDialog.show(supportFragmentManager, "simple")
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
            this.requestCode -> {
                if((grantResults.isNotEmpty()) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return
                } else {
                    DENIEDDialog.show(supportFragmentManager, "simple2")
                }
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
                ringtoneString = sharedPref.getString(getString(R.string.saved_ringtone), null)
                if (ringtoneString != null) {
                    ringtoneUri = Uri.parse(ringtoneString)
                }

                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.settingTitle))
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false) // サイレントは見せない

                intent.putExtra(
                    RingtoneManager.EXTRA_RINGTONE_TYPE,
                    RingtoneManager.TYPE_ALARM
                ) // アラーム音

                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false) // デフォルトは表示しない

                if (ringtoneString != null) {
                    intent.putExtra(
                        RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                        ringtoneUri
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

    override fun onDescriptionDialogClick() {
        requestPermissions(permissionArray, requestCode)
    }

    // 位置情報の取得が許可されておらずアラームをセットしても何時までたっても起動しないため、乗り過ごし防止のためにアプリを閉じる
    override fun onDENIEDDialogClick() {
        finish()
    }
}