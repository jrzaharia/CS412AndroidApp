package com.example.greetingcard

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.greetingcard.ui.theme.GreetingCardTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

private const val MAX_DIMENSION = 1600

private enum class PermissionDialog { NONE, DENIED, SETTINGS }

class ThirdActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GreetingCardTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CaptureImageScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun CaptureImageScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = LocalActivity.current

    // rememberSaveable keeps these across screen rotation (and process death)
    var dialog by rememberSaveable { mutableStateOf(PermissionDialog.NONE) }
    var askedOnStart by rememberSaveable { mutableStateOf(false) }
    var pendingUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var shownUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var image by remember { mutableStateOf<ImageBitmap?>(null) }

    fun hasCameraPermission() =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = pendingUri
        pendingUri = null
        if (success && uri != null) {
            shownUri = uri
        } else if (uri != null) {
            context.contentResolver.delete(uri, null, null) // cancelled: remove empty entry
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            val canAskAgain = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    it, Manifest.permission.CAMERA
                )
            } ?: false
            // Denied once: explain and ask again. Permanently denied: only Settings can help.
            dialog = if (canAskAgain) PermissionDialog.DENIED else PermissionDialog.SETTINGS
        }
    }

    fun launchCamera() {
        val uri = createImageUri(context)
        if (uri == null) {
            Toast.makeText(context, "Could not create image file", Toast.LENGTH_SHORT).show()
            return
        }
        pendingUri = uri
        takePicture.launch(uri)
    }

    // Ask for permission when the activity first starts (not again after rotation)
    LaunchedEffect(Unit) {
        if (!askedOnStart) {
            askedOnStart = true
            if (!hasCameraPermission()) {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    // Decode the captured image off the main thread whenever the Uri changes
    LaunchedEffect(shownUri) {
        val uri = shownUri ?: return@LaunchedEffect
        image = withContext(Dispatchers.IO) {
            try {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    val sample = maxOf(info.size.width, info.size.height) / MAX_DIMENSION
                    if (sample > 1) decoder.setTargetSampleSize(sample)
                }.asImageBitmap()
            } catch (e: IOException) {
                null
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = {
                if (hasCameraPermission()) launchCamera()
                else permissionLauncher.launch(Manifest.permission.CAMERA)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Capture Image")
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 16.dp)
        ) {
            image?.let {
                Image(
                    bitmap = it,
                    contentDescription = "Captured image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Button(
            onClick = { activity?.finish() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Return to Main Activity")
        }
    }

    when (dialog) {
        PermissionDialog.DENIED -> PermissionInfoDialog(
            title = "Camera access needed",
            message = "The Capture Image feature needs camera access to take a photo. " +
                    "Without this permission, it will not work.",
            confirmText = "Try again",
            onConfirm = {
                dialog = PermissionDialog.NONE
                permissionLauncher.launch(Manifest.permission.CAMERA)
            },
            onDismiss = { dialog = PermissionDialog.NONE }
        )

        PermissionDialog.SETTINGS -> PermissionInfoDialog(
            title = "Camera access disabled",
            message = "The Capture Image feature needs camera access to take a photo and " +
                    "will not work while it is disabled. You can enable it in the app settings.",
            confirmText = "Open settings",
            onConfirm = {
                dialog = PermissionDialog.NONE
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                )
            },
            onDismiss = { dialog = PermissionDialog.NONE }
        )

        PermissionDialog.NONE -> Unit
    }
}

@Composable
private fun PermissionInfoDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmText) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun createImageUri(context: android.content.Context): Uri? {
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "CS412_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CS412AndroidApp")
    }
    return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
}