package com.example.cateredtoyou

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.cateredtoyou.apifiles.AddUserResponse
import com.example.cateredtoyou.apifiles.DatabaseApi
import com.example.cateredtoyou.apifiles.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var btnAdd: Button
    private lateinit var btnView: Button
    private lateinit var etA: EditText
    private lateinit var etB: EditText
    lateinit var resultTv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        // Back button setup
        val backButton: Button = findViewById(R.id.back_to_MainActivity)
        backButton.setOnClickListener { finish() }

        btnAdd = findViewById(R.id.btn_add)
        btnView = findViewById(R.id.btn_view)
        etA = findViewById(R.id.et_a)
        etB = findViewById(R.id.et_b)
        resultTv = findViewById(R.id.result_tv)

        btnAdd.setOnClickListener(this)
        btnView.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val a = sanitizeInput(etA.text.toString())
        val b = sanitizeInput(etB.text.toString())
        when (v?.id) {
            R.id.btn_view -> getUsers()
            R.id.btn_add -> AddUser(a, b)
        }
    }

    private fun sanitizeInput(input: String): String {
        return input.trim().replace(Regex("[^a-zA-Z0-9@._-]"), "")
    }

    private fun AddUser(username: String, password: String) {
        val call = DatabaseApi.retrofitService.addUser(username, password)
        call.enqueue(object : Callback<AddUserResponse> {
            override fun onResponse(call: Call<AddUserResponse>, response: Response<AddUserResponse>) {
                if (response.isSuccessful) {
                    resultTv.text = response.body()?.message ?: "Unknown error"
                } else {
                    resultTv.text = "Failed with code: ${response.code()}"
                }
            }

            override fun onFailure(call: Call<AddUserResponse>, t: Throwable) {
                Log.e("UserActivity", "Error: ${t.localizedMessage}", t)
            }
        })
    }

    private fun getUsers() {
        DatabaseApi.retrofitService.getUser().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful) {
                    resultTv.text = response.body()?.joinToString("\n") {
                        "ID: ${sanitizeInput(it.userId.toString())}, Username: ${sanitizeInput(it.username)}"
                    } ?: "No users found."
                } else {
                    Log.e("UserActivity", "Error code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.e("UserActivity", "Error: ${t.localizedMessage}", t)
            }
        })
    }
}
