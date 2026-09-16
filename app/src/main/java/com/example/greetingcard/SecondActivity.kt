package com.example.greetingcard

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_second)

        val challengesList = arrayOf("1. Device & OS Fragmentation",
                                        "2. Unstable & Dynamic Environment",
                                        "3. Rapid Changes",
                                        "4. Limited Tool Support",
                                        "5. Low Awareness of Security & Privacy")

        val listView = findViewById<ListView>(R.id.listView)
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, challengesList)

        val returnButton = findViewById<Button>(R.id.buttonReturn)
        returnButton.setOnClickListener {
            finish()
        }
    }
}