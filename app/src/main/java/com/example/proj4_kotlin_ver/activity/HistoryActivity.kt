package com.example.proj4_kotlin_ver.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
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

class HistoryActivity : Fragment(), HistoryViewHolder.ItemClickListener {

    private lateinit var realm: Realm
    private lateinit var sortedResults: RealmResults<HistoryData>
    private lateinit var adapter: HistoryRecyclerViewAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        realm = Realm.getDefaultInstance()
    }

    override fun onStart() {
        super.onStart()
        sortedResults = realm.where(HistoryData::class.java).findAll().sort("dateTime", Sort.DESCENDING)
        layoutManager = LinearLayoutManager(activity)
        recycleView.layoutManager = layoutManager

        adapter = HistoryRecyclerViewAdapter(sortedResults, this)
        recycleView.adapter = this.adapter

        val itemDecoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
        recycleView.addItemDecoration(itemDecoration)

        if (adapter.itemCount == 0) {
            no_history_description.visibility = View.VISIBLE
        } else {
            no_history_description.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    override fun onItemClick(itemView: View, position: Int) {
        sortedResults[position]?.let {
            setFragmentResult("stationData", bundleOf(
                "station" to it.station,
                "line" to it.line,
                "lat" to it.lat,
                "lng" to it.lng,
                "radius" to it.radius
            ))

            Toast.makeText(activity, "アラームセット画面にデータを設定しました", Toast.LENGTH_SHORT).show()
        }
    }
}