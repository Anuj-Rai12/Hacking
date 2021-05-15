package com.uptodd.uptoddapp.doctor.dashboard

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.uptodd.uptoddapp.R

class CarouselAdapter(private var context: Context,
                      private val onClickListener: OnClickListener, val doctorsReferred: String, val doctorsEnrolled: String, val patientsReferred: String, val patientsEnrolled: String): PagerAdapter()
{
    private lateinit var layoutInflater: LayoutInflater

    override fun getCount(): Int {
        return 2
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }



    @SuppressLint("SetTextI18n")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater= LayoutInflater.from(context)
        val view: View =layoutInflater.inflate(R.layout.carousel_item,container,false)

        if(position==1){
            view.findViewById<TextView>(R.id.doctor_dashboard_patients_referred_text).text = "Doctors Referred Count"
            view.findViewById<TextView>(R.id.doctor_dashboard_patients_enrolled_text).text = "Doctors Enrolled Count"
            view.findViewById<TextView>(R.id.doctor_dashboard_patients_referred_count).text = doctorsReferred
            view.findViewById<TextView>(R.id.doctor_dashboard_patients_enrolled_count).text = doctorsEnrolled
            view.findViewById<LinearLayout>(R.id.doctor_dashboard_patients_referred).setOnClickListener {
                onClickListener.onClickDoctorsReferred()
            }
            view.findViewById<LinearLayout>(R.id.doctor_dashboard_patients_enrolled).setOnClickListener {
                onClickListener.onClickDoctorsEnrolled()
            }
        }
        else{
            view.findViewById<TextView>(R.id.doctor_dashboard_patients_referred_text).text = "Patients Referred Count"
            view.findViewById<TextView>(R.id.doctor_dashboard_patients_enrolled_text).text = "Patients Enrolled Count"
            view.findViewById<TextView>(R.id.doctor_dashboard_patients_referred_count).text = patientsReferred
            view.findViewById<TextView>(R.id.doctor_dashboard_patients_enrolled_count).text = patientsEnrolled
            view.findViewById<LinearLayout>(R.id.doctor_dashboard_patients_referred).setOnClickListener {
                onClickListener.onClickPatientsReferred()
            }
            view.findViewById<LinearLayout>(R.id.doctor_dashboard_patients_enrolled).setOnClickListener {
                onClickListener.onClickPatientsEnrolled()
            }
        }

        container.addView(view,0)

        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    interface OnClickListener {
        fun onClickDoctorsReferred()
        fun onClickDoctorsEnrolled()
        fun onClickPatientsReferred()
        fun onClickPatientsEnrolled()
    }

}