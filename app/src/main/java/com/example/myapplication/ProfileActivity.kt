package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File



class ProfileActivity : AppCompatActivity() {

    private lateinit var imageProfile: ImageView
    private lateinit var cameraPhotoUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        imageProfile = findViewById(R.id.imageProfile)
        val cardChangePhoto = findViewById<CardView>(R.id.cardChangeProfile)
        val cardBack = findViewById<CardView>(R.id.cardBack)

        loadProfileImage()

        cardChangePhoto.setOnClickListener {
            showImagePickerDialog()
        }

        cardBack.setOnClickListener {
            finish()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Zrób zdjęcie", "Wybierz z galerii")
        android.app.AlertDialog.Builder(this)
            .setTitle("Wybierz opcję")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> openGallery()
                }
            }
            .show()
    }


    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Brak uprawnień do aparatu", Toast.LENGTH_SHORT).show()
        }
    }


    private fun openCamera() {
        val photoFile = createImageFile()
        cameraPhotoUri = FileProvider.getUriForFile(this, "$packageName.provider", photoFile)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraPhotoUri)
        cameraLauncher.launch(intent)
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageProfile.setImageURI(cameraPhotoUri)
            saveProfileImage(cameraPhotoUri)
        }
    }


    @SuppressLint("IntentReset")
    private fun openGallery() {
        pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }



    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                grantUriPermission(uri)
                imageProfile.setImageURI(uri)
                saveProfileImage(uri)
            } else {
                Toast.makeText(this, "Nie wybrano zdjęcia", Toast.LENGTH_SHORT).show()
            }
        }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            if (imageUri != null) {
                grantUriPermission(imageUri)
                imageProfile.setImageURI(imageUri)
                saveProfileImage(imageUri)
            } else {
                Toast.makeText(this, "Brak dostępu do zdjęcia", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun grantUriPermission(uri: Uri) {
        try {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }


    private fun saveProfileImage(uri: Uri) {
        val sharedPreferences = getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("profile_image_uri", uri.toString()).apply()
    }

    private fun loadProfileImage() {
        val sharedPreferences = getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
        val uriString = sharedPreferences.getString("profile_image_uri", null)
        if (uriString != null) {
            val uri = Uri.parse(uriString)


            try {
                contentResolver.openInputStream(uri)?.close()
                imageProfile.setImageURI(uri)
            } catch (e: SecurityException) {
                Toast.makeText(this, "Brak dostępu do zdjęcia. Wybierz ponownie.", Toast.LENGTH_SHORT).show()
                saveProfileImage(Uri.EMPTY)
            }
        }
    }

    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(null)
        return File.createTempFile("profile_photo", ".jpg", storageDir)
    }
}
