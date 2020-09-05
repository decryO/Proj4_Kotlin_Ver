package com.example.proj4_kotlin_ver.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proj4_kotlin_ver.HistoryRecyclerViewAdapter
import com.example.proj4_kotlin_ver.HistoryViewHolder
import com.example.proj4_kotlin_ver.R
import com.example.proj4_kotlin_ver.data.HistoryData
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_history.*

class HistoryActivity : AppCompatActivity(), HistoryViewHolder.ItemClickListener {

    private lateinit var realm: Realm
    private lateinit var sortedResults: RealmResults<HistoryData>
    private lateinit var adapter: HistoryRecyclerViewAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        setSupportActionBar(history_tool_bar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        realm = Realm.getDefaultInstance()
    }

    override fun onStart() {
        super.onStart()
        sortedResults = realm.where(HistoryData::class.java).findAll().sort("dateTime", Sort.DESCENDING)
        layoutManager = LinearLayoutManager(this)
        recycleView.layoutManager = layoutManager

        adapter = HistoryRecyclerViewAdapter(sortedResults, this)
        recycleView.adapter = this.adapter

        val itemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        recycleView.addItemDecoration(itemDecoration)

        if (adapter.itemCount == 0) {
            no_history_description.visibility = View.VISIBLE
        } else {
            no_history_description.visibility = View.GONE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) finish()

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun onItemClick(itemView: View, position: Int) {
    }
}