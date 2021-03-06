package com.example.proj4_kotlin_ver.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proj4_kotlin_ver.R
import com.example.proj4_kotlin_ver.SearchRecyclerViewAdapter
import com.example.proj4_kotlin_ver.SearchViewHolder
import com.example.proj4_kotlin_ver.data.StationData
import com.example.proj4_kotlin_ver.data.Stations
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity(), SearchViewHolder.ItemClickListener {

    private lateinit var stationData: StationData
    private lateinit var stations: Array<String>
    private lateinit var lines: Array<String>
    private lateinit var lats: Array<Double>
    private lateinit var lngs: Array<Double>
    private lateinit var adapter: SearchRecyclerViewAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        setSupportActionBar(search_tool_bar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // SearchViewを初期状態で開いたままにする
        search_view.isIconified = false
        // SearchViewの閉じる（×ボタン）を消す
        search_view.isIconifiedByDefault = false
        // SearchViewの左に検索ボタンを表示させる
        search_view.isSubmitButtonEnabled = true
        search_view.queryHint = getString(R.string.search_query_hint)

        search_view.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            // 検索文字列が変更された(変更されるたびに呼び出されるので注意)
            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText.isNullOrEmpty()) {
                    example_text.text = getString(R.string.search_description)
                    example_text.visibility = View.VISIBLE
                    search_progress_bar.visibility = View.GONE

                    // リストの中身を空にする
                    adapter = SearchRecyclerViewAdapter(StationData(Stations(emptyList())), this@SearchActivity)
                    search_result_list.adapter = adapter
                }
                return false
            }

            // 検索ボタン押下
            override fun onQueryTextSubmit(query: String?): Boolean {
                example_text.visibility = View.GONE
                search_progress_bar.visibility = View.VISIBLE

                val getStationUrl = "https://express.heartrails.com/api/json?method=getStations&name=${query}"
                getStationUrl.httpGet().responseString { _, _, result ->
                    when (result) {
                        is Result.Success -> {
                            search_progress_bar.visibility = View.GONE

                            val regex = Regex("error")
                            // 検索文字列の駅名があるならresult.value内にerror文がないのでfalse
                            if(regex.containsMatchIn(result.value)) {
                                // ユーザーに検索文字列の駅がないことをわかりやすくするためにtextを変更する
                                example_text.text = getString(R.string.search_notfound_description, query)
                                example_text.visibility = View.VISIBLE

                            } else {

                                val mapper = jacksonObjectMapper()
                                stationData = mapper.readValue(result.value)

                                lines = emptyArray()
                                stations = emptyArray()
                                lats = emptyArray()
                                lngs = emptyArray()

                                stationData.response.station.forEach {
                                    lines += it.line
                                    stations += it.name
                                    lats += it.y
                                    lngs += it.x
                                }
                                adapter = SearchRecyclerViewAdapter(stationData, this@SearchActivity)
                                search_result_list.adapter = adapter

                                val itemDecoration = DividerItemDecoration(this@SearchActivity, DividerItemDecoration.VERTICAL)
                                search_result_list.addItemDecoration(itemDecoration)
                            }
                        }
                        is Result.Failure -> { }
                    }
                }
                            return false
            }
        })
    }

    override fun onStart() {
        super.onStart()
        layoutManager = LinearLayoutManager(this)
        search_result_list.layoutManager = layoutManager
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) finish()

        return super.onOptionsItemSelected(item)
    }

    override fun onItemClick(itemView: View, position: Int) {
        val intent = Intent()
        intent.putExtra("station", stations[position])
        intent.putExtra("line", lines[position])
        intent.putExtra("lat", lats[position])
        intent.putExtra("lng", lngs[position])
        setResult(RESULT_OK, intent)
        finish()
    }
}