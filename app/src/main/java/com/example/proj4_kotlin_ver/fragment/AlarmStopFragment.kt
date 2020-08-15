package com.example.proj4_kotlin_ver.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.proj4_kotlin_ver.service.GeoFencingService
import com.example.proj4_kotlin_ver.R
import kotlinx.android.synthetic.main.activity_alarm_stop.*

class AlarmStopFragment : Fragment() {

    companion object {
        fun newInstance(): AlarmStopFragment {
            val fragment =
                AlarmStopFragment()
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

        detailText.text = getString(R.string.alertStop_text, savedStation, savedRadius)

        alarmStop.setOnClickListener {
            val serviceIntent = Intent(activity, GeoFencingService::class.java)
            activity?.stopService(serviceIntent)

            activity?.supportFragmentManager?.beginTransaction().apply {
                val fragmentManager = fragmentManager
                if(fragmentManager != null) {
                    val transaction = fragmentManager.beginTransaction()
                    transaction.replace(
                        R.id.container,
                        MapsFragment.newInstance()
                    )
                    transaction.commit()
                }
            }
        }
    }
}