package com.bangkit.batikloka.utils.helper

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bangkit.batikloka.R
import com.bangkit.batikloka.ui.adapter.ImageSourceAdapter
import com.yalantis.ucrop.UCrop
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImagePickerHelper(private val activity: Activity) {
    companion object {
        const val PICK_IMAGE_REQUEST = 1
        const val CAMERA_PERMISSION_REQUEST = 2
        const val CAMERA_CAPTURE_REQUEST = 3
    }

    private var currentPhotoPath: String = ""
    private var onImageSelectedListener: ((File) -> Unit)? = null

    fun setOnImageSelectedListener(listener: (File) -> Unit) {
        onImageSelectedListener = listener
    }

    fun showImageSourceOptions() {
        val options = arrayOf("Take photo from camera", "Choose from gallery")
        val icons = intArrayOf(R.drawable.ic_camera_outlined, R.drawable.ic_photo)

        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Choose Image Source")

        val dialogView = activity.layoutInflater.inflate(R.layout.dialog_image_source, null)
        val listView: ListView = dialogView.findViewById(R.id.listView)

        val adapter = ImageSourceAdapter(activity, options, icons)
        listView.adapter = adapter

        builder.setView(dialogView)

        val dialog = builder.create()

        dialog.setOnShowListener { dialogInterface ->
            val alertDialog = dialogInterface as AlertDialog
            alertDialog.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog_background)
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            when (position) {
                0 -> openCamera()
                1 -> openGallery()
            }
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        } else {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val photoFile = createImageFile()
            val photoURI = FileProvider.getUriForFile(
                activity,
                "${activity.packageName}.fileprovider",
                photoFile
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            activity.startActivityForResult(intent, CAMERA_CAPTURE_REQUEST)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun startCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File(activity.cacheDir, "cropped_image.jpg"))
        UCrop.of(uri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(500, 500)
            .start(activity)
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode == Activity.RESULT_OK -> {
                when (requestCode) {
                    PICK_IMAGE_REQUEST -> {
                        data?.data?.let { uri ->
                            startCrop(uri)
                        }
                    }

                    CAMERA_CAPTURE_REQUEST -> {
                        val photoFile = File(currentPhotoPath)
                        val photoURI = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            FileProvider.getUriForFile(
                                activity,
                                "${activity.packageName}.fileprovider",
                                photoFile
                            )
                        } else {
                            Uri.fromFile(photoFile)
                        }
                        startCrop(photoURI)
                    }

                    UCrop.REQUEST_CROP -> {
                        val resultUri = UCrop.getOutput(data!!)
                        resultUri?.let { uri ->
                            val file = File(uri.path!!)
                            onImageSelectedListener?.invoke(file)
                        }
                    }

                    else -> {
                        Toast.makeText(activity, "Unhandled request code", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

            resultCode == UCrop.RESULT_ERROR -> {
                val cropError = UCrop.getError(data!!)
                Toast.makeText(activity, "Crop error: ${cropError?.message}", Toast.LENGTH_SHORT)
                    .show()
            }

            else -> {
                Toast.makeText(activity, "Operation cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}