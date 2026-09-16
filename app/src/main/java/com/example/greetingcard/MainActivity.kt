package com.example.greetingcard

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.greetingcard.ui.theme.GreetingCardTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GreetingCardTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val context = LocalContext.current
                    Column(modifier = Modifier.padding(innerPadding)) {
                        Greeting(
                            name = "Johnnie",
                            studentID = 1419478,
                            modifier = Modifier.padding(innerPadding)
                        )

                        Button(onClick = {
                            val intent = Intent(context, SecondActivity::class.java)
                            context.startActivity(intent)
                        }) {
                            Text("Start Activity Explicitly")
                        }

                        Button(onClick = {
                            val intent = Intent("com.example.greetingcard.SHOW_LIST")
                            intent.setPackage(context.packageName)
                            context.startActivity(intent)
                        }) {
                            Text("Start Activity Implicitly")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, studentID: Int, modifier: Modifier = Modifier) {
    Surface(color = Color.hsl(hue=259F,saturation=1F,lightness=0.884F)) {
        Text(
            text = "Hi, my name is $name!\nMy student ID is $studentID.\n",
            modifier = modifier.padding(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GreetingCardTheme {
        Greeting("Johnnie", studentID = 1419478)
    }
}