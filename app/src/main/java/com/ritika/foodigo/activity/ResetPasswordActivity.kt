package com.ritika.foodigo.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.ritika.foodigo.R
import com.ritika.foodigo.util.ConnectionManager
import org.json.JSONObject

class ResetPasswordActivity : AppCompatActivity() {

    lateinit var etRPotp: EditText
    lateinit var etRPNewPassword: EditText
    lateinit var etRPConfirmPassword: EditText
    lateinit var btnRPSubmit: Button

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        supportActionBar?.hide()

        etRPotp = findViewById(R.id.etRPotp)
        etRPNewPassword = findViewById(R.id.etRPNewPassword)
        etRPConfirmPassword = findViewById(R.id.etRPConfirmPassword)
        btnRPSubmit = findViewById(R.id.btnRPSubmit)

        val sharedPreferences = getSharedPreferences("Foodigo Preferences", Context.MODE_PRIVATE)

        var mobileNumber = "empty"
        if(intent != null) {
            mobileNumber = intent.getStringExtra("mobileNumber")
        }

        btnRPSubmit.setOnClickListener {

            val otp = etRPotp.text.toString()
            val newPassword = etRPNewPassword.text.toString()
            val confirmPassword = etRPConfirmPassword.text.toString()

            if (newPassword == confirmPassword) {

                val queue = Volley.newRequestQueue(this@ResetPasswordActivity)
                val url = "http://13.235.250.119/v2/reset_password/fetch_result"

                val jsonParams = JSONObject()
                jsonParams.put("mobile_number", mobileNumber)
                jsonParams.put("password", newPassword)
                jsonParams.put("otp", otp)

                if (ConnectionManager().checkConnectivity(this@ResetPasswordActivity)) {

                    val jsonRequest = object: JsonObjectRequest(Request.Method.POST, url, jsonParams, Response.Listener {

                        try {

                            val mainData = it.getJSONObject("data")
                            val success = mainData.getBoolean("success")
                            if (success) {

                                val intent = Intent(this@ResetPasswordActivity, LoginActivity::class.java)
                                val message = mainData.getString("successMessage")
                                Toast.makeText(this@ResetPasswordActivity, message, Toast.LENGTH_SHORT).show()
                                sharedPreferences.edit().clear().apply()
                                startActivity(intent)

                            } else {
                                Toast.makeText(this@ResetPasswordActivity, "Some error occurred", Toast.LENGTH_SHORT).show()
                            }

                        } catch (e: Exception) {
                            Toast.makeText(this@ResetPasswordActivity, "Catch error occurred", Toast.LENGTH_SHORT).show()
                        }

                    }, Response.ErrorListener {
                        Toast.makeText(this@ResetPasswordActivity, "Volley error occurred", Toast.LENGTH_SHORT).show()
                    }) {
                        override fun getHeaders(): MutableMap<String, String> {
                            val headers = HashMap<String, String>()
                            headers["Content-type"] = "application/json"
                            headers["token"] = "3e0843894b9247"
                            return headers
                        }
                    }

                    queue.add(jsonRequest)

                } else {
                    // Internet not found
                    val dialog = AlertDialog.Builder(this@ResetPasswordActivity)
                    dialog.setTitle("Error")
                    dialog.setMessage("Internet Connection Is Not Found")

                    dialog.setPositiveButton("Open settings") { text, listener ->
                        val settingsIntent = Intent (Settings.ACTION_WIRELESS_SETTINGS)
                        startActivity(settingsIntent)
                        finish()
                    }

                    dialog.setNegativeButton("Exit") { text, listener ->
                        ActivityCompat.finishAffinity(this@ResetPasswordActivity)
                    }
                    dialog.create()
                    dialog.show()
                }

            } else {
                Toast.makeText(this@ResetPasswordActivity, "Confirm password field should match", Toast.LENGTH_SHORT).show()
            }

        }

    }
}
