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
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.ritika.foodigo.R
import com.ritika.foodigo.util.ConnectionManager
import org.json.JSONObject
import java.lang.Exception

class RegisterActivity : AppCompatActivity() {

    lateinit var etRName: EditText
    lateinit var etREmail: EditText
    lateinit var etRMobileNumber: EditText
    lateinit var etRAddress: EditText
    lateinit var etRPassword: EditText
    lateinit var etRPasswordConfirm: EditText
    lateinit var btnRRegister: Button

    lateinit var sharedPreferences: SharedPreferences

    lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        sharedPreferences = getSharedPreferences("Foodigo Preferences", Context.MODE_PRIVATE)

        toolbar = findViewById(R.id.toolbar)
        setUpToolbar(toolbar)

        etRName = findViewById(R.id.etRName)
        etREmail = findViewById(R.id.etREmail)
        etRMobileNumber = findViewById(R.id.etRMobileNumber)
        etRAddress = findViewById(R.id.etRAddress)
        etRPassword = findViewById(R.id.etRPassword)
        etRPasswordConfirm = findViewById(R.id.etRPasswordConfirm)
        btnRRegister = findViewById(R.id.btnRRegister)

        btnRRegister.setOnClickListener {

            val name = etRName.text.toString()
            val email = etREmail.text.toString()
            val mobileNumber = etRMobileNumber.text.toString()
            val address = etRAddress.text.toString()
            val password = etRPassword.text.toString()
            val passwordConfirm = etRPasswordConfirm.text.toString()

            if ((password == passwordConfirm) && (mobileNumber.length == 10) && (password.length > 3)) {

                val queue = Volley.newRequestQueue(this@RegisterActivity)
                val url = "http://13.235.250.119/v2/register/fetch_result"

                val jsonParams = JSONObject()
                jsonParams.put("name", name)
                jsonParams.put("mobile_number", mobileNumber)
                jsonParams.put("password", password)
                jsonParams.put("address", address)
                jsonParams.put("email", email)

                if (ConnectionManager().checkConnectivity(this@RegisterActivity)) {

                    val jsonRequest = object: JsonObjectRequest(Request.Method.POST, url, jsonParams, Response.Listener {

                        try {

                            val mainData = it.getJSONObject("data")
                            val success = mainData.getBoolean("success")
                            if (success) {

                                val data = mainData.getJSONObject("data")
                                val userId = data.getString("user_id")

                                val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                                savePreferences(userId, name, email, mobileNumber, address, password)
                                startActivity(intent)

                            } else {
                                Toast.makeText(this@RegisterActivity, "Some error occurred", Toast.LENGTH_SHORT).show()
                            }

                        } catch (e: Exception) {
                            Toast.makeText(this@RegisterActivity, "Catch error occurred", Toast.LENGTH_SHORT).show()
                        }

                    }, Response.ErrorListener {
                        Toast.makeText(this@RegisterActivity, "Volley error occurred", Toast.LENGTH_SHORT).show()

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
                    val dialog = AlertDialog.Builder(this@RegisterActivity)
                    dialog.setTitle("Error")
                    dialog.setMessage("Internet Connection Is Not Found")

                    dialog.setPositiveButton("Open settings") { text, listener ->
                        val settingsIntent = Intent (Settings.ACTION_WIRELESS_SETTINGS)
                        startActivity(settingsIntent)
                        finish()
                    }

                    dialog.setNegativeButton("Exit") { text, listener ->
                        ActivityCompat.finishAffinity(this@RegisterActivity)
                    }
                    dialog.create()
                    dialog.show()
                }

            } else {
                Toast.makeText(this@RegisterActivity, "Invalid input", Toast.LENGTH_SHORT).show()
            }

        }

    }

    fun savePreferences(
        userId:String,
        name: String,
        email: String,
        mobileNumber: String,
        address: String,
        password: String
    ) {
        sharedPreferences.edit().putBoolean("Data", true).apply()
        sharedPreferences.edit().putString("UserId", userId).apply()
        sharedPreferences.edit().putString("Name", name).apply()
        sharedPreferences.edit().putString("Email", email).apply()
        sharedPreferences.edit().putString("MobileNumber", mobileNumber).apply()
        sharedPreferences.edit().putString("Address", address).apply()
        sharedPreferences.edit().putString("Password", password).apply()

    }

    fun setUpToolbar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Register Yourself"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}