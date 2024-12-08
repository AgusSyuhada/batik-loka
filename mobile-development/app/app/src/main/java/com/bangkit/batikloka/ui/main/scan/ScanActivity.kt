package com.bangkit.batikloka.ui.main.scan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bangkit.batikloka.R
import com.bangkit.batikloka.databinding.ActivityScanBinding
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScanBinding
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private var isFlashOn = false
    private var isFrontCamera = false
    private var currentImageFile: File? = null

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            RESULT_OK -> {
                currentImageFile?.let { file ->
                    Toast.makeText(this, "Photo captured", Toast.LENGTH_SHORT).show()
                }
            }

            RESULT_CANCELED -> {
                currentImageFile?.delete()
                currentImageFile = null
                Toast.makeText(this, "Camera canceled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createTempFile(): File {
        val mediaDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return File(mediaDir ?: filesDir, "scan_${System.currentTimeMillis()}.jpg")
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val outputFile = createTempFile()
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(
                        baseContext,
                        "Photo capture failed: ${exc.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    currentImageFile = outputFile
                    val savedUri = FileProvider.getUriForFile(
                        this@ScanActivity,
                        "${packageName}.fileprovider",
                        outputFile
                    )

                    // Tambahkan permission untuk membaca URI
                    val intent = Intent(this@ScanActivity, ScanResultActivity::class.java).apply {
                        putExtra("IMAGE_URI", savedUri.toString())
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    startActivity(intent)
                    finish()
                }
            }
        )
    }

    private fun uriToFile(uri: Uri): File {
        val mediaDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        val file = File(mediaDir ?: filesDir, "temp_image_${System.currentTimeMillis()}.jpg")

        contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                currentImageFile = uriToFile(uri)
                val savedUri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    currentImageFile!!
                )

                val intent = Intent(this, ScanResultActivity::class.java).apply {
                    putExtra("IMAGE_URI", savedUri.toString())
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Gallery canceled", Toast.LENGTH_SHORT).show()
            }
        }

    private fun openGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            launcherIntentCamera.launch(intent)
        }
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.CAMERA)
            } else {
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        if (!allPermissionsGranted()) {
            requestAllPermissions()
        } else {
            startCamera()
        }

        setupListeners()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun requestAllPermissions() {
        ActivityCompat.requestPermissions(
            this,
            REQUIRED_PERMISSIONS,
            REQUEST_CODE_PERMISSIONS
        )
    }

    private fun setupListeners() {
        binding.btnFlash.setOnClickListener {
            toggleFlash()
        }

        binding.btnSwitchCamera.setOnClickListener {
            isFrontCamera = !isFrontCamera
            startCamera()
        }

        binding.btnScan.setOnClickListener {
            takePhoto()
        }

        binding.btnGallery.setOnClickListener {
            openGallery()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.surfaceProvider = binding.viewFinder.surfaceProvider
                }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = if (isFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun toggleFlash() {
        imageCapture?.let { imageCapture ->
            isFlashOn = !isFlashOn
            imageCapture.flashMode = if (isFlashOn) {
                binding.btnFlash.setImageResource(R.drawable.ic_flash_on)
                ImageCapture.FLASH_MODE_ON
            } else {
                binding.btnFlash.setImageResource(R.drawable.ic_flash_off)
                ImageCapture.FLASH_MODE_OFF
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }
}