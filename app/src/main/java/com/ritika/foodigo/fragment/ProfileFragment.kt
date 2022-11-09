package com.ritika.foodigo.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ritika.foodigo.R
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {

    lateinit var txtName: TextView
    lateinit var txtMobileNumber: TextView
    lateinit var txtEmail: TextView
    lateinit var txtAddress: TextView

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        sharedPreferences = context!!.getSharedPreferences("Foodigo Preferences", Context.MODE_PRIVATE)

        txtName = view.findViewById(R.id.txtName)
        txtMobileNumber = view.findViewById(R.id.txtMobileNumber)
        txtEmail = view.findViewById(R.id.txtEmail)
        txtAddress = view.findViewById(R.id.txtAddress)

        txtName.text = sharedPreferences.getString("Name", "empty")
        txtEmail.text = sharedPreferences.getString("Email", "empty")
        txtMobileNumber.text = sharedPreferences.getString("MobileNumber", "empty")
        txtAddress.text = sharedPreferences.getString("Address", "empty")

        return view
    }

}
