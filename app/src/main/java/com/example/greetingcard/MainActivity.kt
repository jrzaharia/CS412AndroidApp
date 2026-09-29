package com.example.greetingcard

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.greetingcard.ui.theme.GreetingCardTheme


class MainActivity : ComponentActivity() {

    private var gradeService: GradeService? = null
    private var isBound = false
    private val myReceiver = MyBroadcastReceiver()
    private var grade by mutableStateOf("")

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as GradeService.LocalBinder
            gradeService = binder.getService()
            isBound = true

            grade = gradeService?.getMyGrade() ?: ""
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            gradeService = null
            isBound = false
        }
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()
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

                        Button(onClick = {
                            val intent = Intent(context, GradeService::class.java)
                            ContextCompat.startForegroundService(context, intent)
                        }) {
                            Text("Start Service")
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(onClick = {
                                val intent = Intent(context, GradeService::class.java)
                                context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
                            }) {
                                Text("Bind Service")
                            }

                            Text(
                                text = grade,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }

                        Button(onClick = {
                            val intent = Intent(MyBroadcastReceiver.ACTION)
                            intent.setPackage(context.packageName)
                            context.sendBroadcast(intent)
                        }) {
                            Text("Send Broadcast")
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Register dynamically for the custom action
        val filter = IntentFilter(MyBroadcastReceiver.ACTION)
        ContextCompat.registerReceiver(
            this,
            myReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(myReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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