package com.example.cateredtoyou

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cateredtoyou.apifiles.DatabaseApi
import com.example.cateredtoyou.apifiles.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val usernameEditText: EditText = findViewById(R.id.username)
        val passwordEditText: EditText = findViewById(R.id.password)
        val loginButton: Button = findViewById(R.id.login_button)

        loginButton.setOnClickListener {
            // turn the input into strings
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            // connecting to the api using functions created in ApiConnect.kt
            // specifically calling the loginCheck functions that takes a username and password
            val call = DatabaseApi.retrofitService.loginCheck(username, password)
            call.enqueue(object : Callback<LoginResponse> { // call to the api which requires a response based on the data classes in ApiConnect.kt
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>){
                    if(response.isSuccessful) { // only does this if the connection is a success
                        val rawResponse = response.body() // rawResponse holds the response of the json file
                        val message = rawResponse?.message // message holds the message portion of json file
                        if (rawResponse?.status == true) {
                            val intent = Intent(this@LoginScreen, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@LoginScreen, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) { // if connections fails
                    Log.e("LoginScreen", "Failed to authorize user", t)
                }
            })
            // Dummy check for demonstration purposes
//            if (username == "admin" && password == "password") {
                // Go to MainActivity after successful login
//                val intent = Intent(this, MainActivity::class.java)
//                startActivity(intent)
//                finish()
//            } else {
//                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
//            }
        }
    }
}
