package com.example.taller3firebase

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val tuBoton: Button = findViewById(R.id.Login)
        val tuBoton1: Button = findViewById(R.id.Singup)
        tuBoton.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        tuBoton1.setOnClickListener {
            val intent = Intent(this, Singup::class.java)
            startActivity(intent)
        }
    }

}