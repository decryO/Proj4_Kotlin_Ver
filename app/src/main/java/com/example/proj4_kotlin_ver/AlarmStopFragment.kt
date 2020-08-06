package com.example.proj4_kotlin_ver

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_alarm_stop.*
import java.util.zip.Inflater

class AlarmStopFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_alarm_stop, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        alarmStop.setOnClickListener {
            val serviceIntent = Intent(activity, GeoFencingService::class.java)
            activity?.stopService(serviceIntent)

            fragmentManager?.popBackStack()
        }
    }
}