package com.ritika.foodigo.activity

import android.app.AlertDialog
import android.content.Intent
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

class ForgotPasswordActivity : AppCompatActivity() {

    lateinit var etFPMobileNumber: EditText
    lateinit var etFPEmail: EditText
    lateinit var btnFPSend: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        supportActionBar?.hide()

        etFPMobileNumber = findViewById(R.id.etFPMobileNumber)
        etFPEmail = findViewById(R.id.etFPEmail)
        btnFPSend = findViewById(R.id.btnFPSend)

        btnFPSend.setOnClickListener{

            val mobileNumber = etFPMobileNumber.text.toString()
            val email = etFPEmail.text.toString()

            val queue = Volley.newRequestQueue(this@ForgotPasswordActivity)
            val url = "http://13.235.250.119/v2/forgot_password/fetch_result"

            val jsonParams = JSONObject()
            jsonParams.put("mobile_number", mobileNumber)
            jsonParams.put("email", email)

            if (ConnectionManager().checkConnectivity(this@ForgotPasswordActivity)) {

                val jsonRequest = object: JsonObjectRequest(Request.Method.POST, url, jsonParams, Response.Listener {

                    try {

                        val mainData = it.getJSONObject("data")
                        val success = mainData.getBoolean("success")
                        if (success) {

                            val intent = Intent(this@ForgotPasswordActivity, ResetPasswordActivity::class.java)
                            val firstTry = mainData.getBoolean("first_try")

                            if (firstTry) {
                                Toast.makeText(this@ForgotPasswordActivity, "Sending OTP...", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@ForgotPasswordActivity, "OTP sent", Toast.LENGTH_SHORT).show()
                            }

                            intent.putExtra("mobileNumber", mobileNumber)
                            startActivity(intent)

                        } else {
                            Toast.makeText(this@ForgotPasswordActivity, "Some error occurred", Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: Exception) {
                        Toast.makeText(this@ForgotPasswordActivity, "Catch error occurred", Toast.LENGTH_SHORT).show()
                    }

                }, Response.ErrorListener {
                    Toast.makeText(this@ForgotPasswordActivity, "Volley error occurred", Toast.LENGTH_SHORT).show()

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
                val dialog = AlertDialog.Builder(this@ForgotPasswordActivity)
                dialog.setTitle("Error")
                dialog.setMessage("Internet Connection Is Not Found")

                dialog.setPositiveButton("Open settings") { text, listener ->
                    val settingsIntent = Intent (Settings.ACTION_WIRELESS_SETTINGS)
                    startActivity(settingsIntent)
                    finish()
                }

                dialog.setNegativeButton("Exit") { text, listener ->
                    ActivityCompat.finishAffinity(this@ForgotPasswordActivity)
                }
                dialog.create()
                dialog.show()
            }


        }

    }

}
