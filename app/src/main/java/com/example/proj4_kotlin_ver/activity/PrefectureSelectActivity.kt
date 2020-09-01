package com.example.proj4_kotlin_ver.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.example.proj4_kotlin_ver.R
import com.example.proj4_kotlin_ver.dialog.ProgressDialogFragment
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result
import kotlinx.android.synthetic.main.activity_prefecture_select.*
import kotlinx.android.synthetic.main.activity_stations_detail.detail_tool_bar
import org.json.JSONArray
import org.json.JSONObject

class PrefectureSelectActivity : AppCompatActivity() {

    private lateinit var prefectures: Array<String>
    private val progressDialog = ProgressDialogFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prefecture_select)

        setSupportActionBar(detail_tool_bar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        prefectures = resources.getStringArray(R.array.prefectures)
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, prefectures)
        line_list.adapter = adapter

        line_list.setOnItemClickListener { _, _, position, _ ->
            progressDialog.show(supportFragmentManager, "progress")
            val getLineUrl = "https://express.heartrails.com/api/json?method=getLines&prefecture=${prefectures[position]}"
            getLineUrl.httpGet().responseJson { _, _, result ->
                when(result) {
                    is Result.Success -> {
                        val responseJson = result.get()
                        var lineArray: Array<String> = emptyArray()
                        val lineObj: JSONArray = (responseJson.obj()["response"] as JSONObject).get("line") as JSONArray
                        lineArray = Array(lineObj.length()) {
                            lineObj.getString(it)
                        }

                        val intent = Intent(this, LineSelectActivity::class.java)
                        intent.putExtra("lines", lineArray)
                        startActivityForResult(intent, 0)
                    }
                    is Result.Failure -> { }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        /*
        *   戻るボタンで戻ってきた際にプログレスバーが表示されっぱなしにならないようにする
        *   progressDialog.dismiss()とするとRuntimeErrorになるので注意
        *   findFragmentByTagでtagが同じDialogがあればdismissするという感じ
        */
        supportFragmentManager.findFragmentByTag("progress")?.let {
            (it as DialogFragment).dismiss()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) finish()

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 0 && resultCode == Activity.RESULT_OK) {
            val extras = data?.extras
            if(extras != null) {
                val text = extras.getString("text")
                val intent = Intent()
                intent.putExtra("station", extras.getString("station"))
                intent.putExtra("lat", extras.getDouble("lat"))
                intent.putExtra("lng", extras.getDouble("lng"))
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }
}