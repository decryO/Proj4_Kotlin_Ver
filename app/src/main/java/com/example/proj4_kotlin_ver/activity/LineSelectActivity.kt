package com.example.proj4_kotlin_ver.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proj4_kotlin_ver.R
import com.example.proj4_kotlin_ver.SelectionRecyclerViewAdapter
import com.example.proj4_kotlin_ver.SelectionViewHolder
import com.example.proj4_kotlin_ver.data.StationData
import com.example.proj4_kotlin_ver.dialog.ProgressDialogFragment
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import kotlinx.android.synthetic.main.activity_line_select.* import kotlinx.android.synthetic.main.activity_stations_detail.detail_tool_bar

class LineSelectActivity : AppCompatActivity(), SelectionViewHolder.ItemClickListener {

    private lateinit var lines: Array<String>
    private lateinit var stationData: StationData
    private lateinit var lats: Array<Double>
    private lateinit var lngs: Array<Double>
    private val progressDialog = ProgressDialogFragment()
    private var selectedLine = ""
    private lateinit var adapter: SelectionRecyclerViewAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_line_select)

        setSupportActionBar(detail_tool_bar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        lines = intent.extras?.getStringArray("lines") as Array<String>
    }

    override fun onStart() {
        super.onStart()
        layoutManager = LinearLayoutManager(this)
        line_list.layoutManager = layoutManager

        adapter = SelectionRecyclerViewAdapter(lines, this)
        line_list.adapter = this.adapter

        val itemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        line_list.addItemDecoration(itemDecoration)
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

    override fun onItemClick(itemView: View, position: Int) {
        progressDialog.show(supportFragmentManager, "progress")
        selectedLine = lines[position]
        val getStationUrl = "https://express.heartrails.com/api/json?method=getStations&line=${selectedLine}"
        getStationUrl.httpGet().responseString { _, _, result ->
            when(result) {
                is Result.Success -> {
                    val mapper = jacksonObjectMapper()
                    stationData = mapper.readValue(result.value)
                    var stations: Array<String> = emptyArray()
                    lats = emptyArray()
                    lngs = emptyArray()
                    stationData.response.station.forEach {
                        stations += it.name
                        lats += it.y
                        lngs += it.x
                    }

                    val intent = Intent(this, StationSelectActivity::class.java)
                    intent.putExtra("stations", stations)
                    intent.putExtra("lats", lats)
                    intent.putExtra("lngs", lngs)
                    startActivityForResult(intent, 0)
                }
                is Result.Failure -> {
                    Toast.makeText(this, "通信にエラーが生じました。もう一度お試しください", Toast.LENGTH_SHORT).show()
                    supportFragmentManager.findFragmentByTag("progress")?.let {
                        (it as DialogFragment).dismiss()
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 0 && resultCode == Activity.RESULT_OK) {
            val extras = data?.extras
            if(extras != null) {
                val position = extras.getInt("position")
                val intent = Intent()
                intent.putExtra("station", extras.getString("station"))
                intent.putExtra("line", selectedLine)
                intent.putExtra("lat", lats[position])
                intent.putExtra("lng", lngs[position])
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }
}