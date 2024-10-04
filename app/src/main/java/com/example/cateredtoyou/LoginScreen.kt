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
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            val call = DatabaseApi.retrofitService.loginCheck(username, password)
            call.enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>){
                    if(response.isSuccessful) {
                        val rawResponse = response.body()
                        val message = response.message().toString()
                        if (rawResponse?.status == true) {
                            val intent = Intent(this@LoginScreen, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@LoginScreen, response.message(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
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
