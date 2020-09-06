package com.example.proj4_kotlin_ver.fragment

import android.os.Bundle
import android.view.*
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
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_history.*

class HistoryFragment : Fragment(), HistoryViewHolder.ItemClickListener {

    private lateinit var realm: Realm
    private lateinit var sortedResults: RealmResults<HistoryData>
    private lateinit var adapter: HistoryRecyclerViewAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.activity_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        realm = Realm.getDefaultInstance()
    }

    override fun onStart() {
        super.onStart()
        sortedResults = realm.where<HistoryData>().findAll().sort("dateTime", Sort.DESCENDING)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.history_option, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.delete_history -> {
                realm.executeTransaction {
                    realm.where<HistoryData>().findAll().deleteAllFromRealm()
                }
                recycleView.visibility = View.GONE
                no_history_description.visibility = View.VISIBLE
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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