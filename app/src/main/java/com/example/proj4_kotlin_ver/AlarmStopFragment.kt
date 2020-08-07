package com.example.proj4_kotlin_ver

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_alarm_stop.*
import java.util.zip.Inflater

class AlarmStopFragment : Fragment() {

    companion object {
        fun newInstance(): AlarmStopFragment {
            val fragment = AlarmStopFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_alarm_stop, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        val savedStation = sharedPref.getString(getString(R.string.saved_station), "aaa")
        val savedRadius = sharedPref.getInt(getString(R.string.saved_radius), 0)

        detailText.text = "${savedStation}駅\n${savedRadius}mでアラームをセット中です"

        alarmStop.setOnClickListener {
            val serviceIntent = Intent(activity, GeoFencingService::class.java)
            activity?.stopService(serviceIntent)

            val transaction = activity?.supportFragmentManager?.beginTransaction().apply {
                val fragmentManager = fragmentManager
                if(fragmentManager != null) {
                    val transaction = fragmentManager.beginTransaction()
                    transaction.replace(R.id.container, MapsFragment.newInstance())
                    transaction.commit()
                }
            }
        }
    }
}