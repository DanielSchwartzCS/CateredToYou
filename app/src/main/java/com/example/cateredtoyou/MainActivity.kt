package com.example.cateredtoyou

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.cateredtoyou.apifiles.AddUserResponse
import com.example.cateredtoyou.apifiles.ApiConnect
import com.example.cateredtoyou.apifiles.DatabaseApi
import com.example.cateredtoyou.apifiles.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() , View.OnClickListener{

    lateinit var btnAdd : Button
    lateinit var btnView: Button
    lateinit var etA : EditText
    lateinit var etB : EditText
    lateinit var resultTv : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        btnAdd = findViewById(R.id.btn_add)
        btnView = findViewById(R.id.btn_view)
        etA = findViewById(R.id.et_a)
        etB = findViewById(R.id.et_b)
        resultTv = findViewById(R.id.result_tv)

        btnAdd.setOnClickListener(this)
        btnView.setOnClickListener(this)


    }

    override fun onClick(v: View?) {
        var a = etA.text.toString()
        var b = etB.text.toString()
        when(v?.id){

            R.id.btn_view -> {
                getUsers()
            }
            R.id.btn_add -> {
                AddUser(a, b)
            }

        }
    }
    private fun AddUser(username: String, password: String){
        val call = DatabaseApi.retrofitService.addUser(username, password)
        call.enqueue(object : Callback<AddUserResponse>{
            override fun onResponse(call: Call<AddUserResponse>, response: Response<AddUserResponse>) {
                if(response.isSuccessful){
                    val rawResponse = response.body()
                    if(rawResponse?.status == "success"){
                        resultTv.text = rawResponse.message
                    }else{
                        resultTv.text = rawResponse?.message
                    }
                }
            }

            override fun onFailure(call: Call<AddUserResponse>, t: Throwable) {
                Log.e("MainActivity", "Failed to add users", t)
            }
        })
    }

    private fun getUsers(){
        DatabaseApi.retrofitService.getUsers().enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if(response.isSuccessful){
                    val rawResponse = response.body()
                    resultTv.text = rawResponse?.joinToString { "\n'id': ${it.id}, 'username': ${it.username}, 'pass': ${it.pass}" }
                }else{
                    Log.e("MainActivity", "Response failed with code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Log.e("MainActivity", "Failed to fetch users", t)
            }
        })
    }



}