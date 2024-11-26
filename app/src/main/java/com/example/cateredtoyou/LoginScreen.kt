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
import com.example.cateredtoyou.apifiles.LoginRequest

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
            // Debug log
            Log.d("LoginScreen", "Login button clicked")

            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Input validation
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this@LoginScreen, "Please enter both username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Debug log
            Log.d("LoginScreen", "Attempting login with username: $username")

            val loginRequest = LoginRequest(username, password)

            DatabaseApi.retrofitService.loginCheck2(username, password).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    Log.d("LoginScreen", "Response received - Code: ${response.code()}")
                    Log.d("LoginScreen", "Response headers: ${response.headers()}")

                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        Log.d("LoginScreen", "Response body: $loginResponse")

                        if (loginResponse?.status == true) {
                            Log.d("LoginScreen", "Login successful, navigating to MainActivity")
                            val intent = Intent(this@LoginScreen, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            val message = loginResponse?.message ?: "Login failed"
                            Log.d("LoginScreen", "Login failed: $message")
                            runOnUiThread {
                                Toast.makeText(this@LoginScreen, message, Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("LoginScreen", "Login failed with error: $errorBody")
                        runOnUiThread {
                            Toast.makeText(
                                this@LoginScreen,
                                "Login failed: ${response.message()}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Log.e("LoginScreen", "Network error during login", t)
                    runOnUiThread {
                        Toast.makeText(
                            this@LoginScreen,
                            "Network error: ${t.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            })
        }
    }
}
