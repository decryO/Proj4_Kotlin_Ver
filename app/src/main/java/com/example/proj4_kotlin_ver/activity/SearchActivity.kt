package com.example.proj4_kotlin_ver.activity

import android.os.Bundle
import android.view.MenuItem
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import com.example.proj4_kotlin_ver.R
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity() {

    private lateinit var mSearchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        setSupportActionBar(search_tool_bar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // SearchViewを初期状態で開いたままにする
        search_view.isIconified = false
        // SearchViewの閉じる（×ボタン）を消す
        search_view.isIconifiedByDefault = false
        search_view.queryHint = "駅名を入力してください"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) finish()

        return super.onOptionsItemSelected(item)
    }
}