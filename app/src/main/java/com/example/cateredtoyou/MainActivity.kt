package com.example.cateredtoyou

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cateredtoyou.APIFiles.DatabaseApi
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
//        lateinit var result: String
        when(v?.id){

            R.id.btn_view -> {
                DatabaseApi.retrofitService.getUsers().enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>){
                        if(response.isSuccessful){
                            val rawResponse = response.body()
                            resultTv.text = rawResponse
                        } else{
                            Log.e("MainActivity", "Response failed with code: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Log.e("MainActivity", "Failed to fetch users", t)
                    }
                })
            }

        }
    }
}