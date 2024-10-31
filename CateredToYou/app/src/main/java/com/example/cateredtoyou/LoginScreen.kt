package com.example.cateredtoyou

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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
        val signupTextView: TextView = findViewById(R.id.signup_text)

        // Set up the clickable "Sign up!" text
        val spannableString = SpannableString("Don't have an account? Sign up!")

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Navigate to SignUpActivity
                val intent = Intent(this@LoginScreen, UserActivity::class.java)
                startActivity(intent)
            }
        }

        spannableString.setSpan(clickableSpan, 23, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        signupTextView.text = spannableString
        signupTextView.movementMethod = LinkMovementMethod.getInstance()

        loginButton.setOnClickListener {
            // turn the input into strings
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            // connecting to the api using functions created in ApiConnect.kt
            val call = DatabaseApi.retrofitService.loginCheck(username, password)
            call.enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>){
                    if(response.isSuccessful) {
                        val rawResponse = response.body()
                        val message = rawResponse?.message
                        if (rawResponse?.status == true) {
                            val intent = Intent(this@LoginScreen, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@LoginScreen, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Log.e("LoginScreen", "Failed to authorize user", t)
                }
            })
        }
    }
}
