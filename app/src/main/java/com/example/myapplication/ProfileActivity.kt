package com.example.myapplication


import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore

import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class ProfileActivity : AppCompatActivity() {
    private lateinit var imageProfile: ImageView
    private lateinit var cardChangeProfile: CardView
    private lateinit var cardBack: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        imageProfile = findViewById(R.id.imageProfile)
        cardChangeProfile = findViewById(R.id.cardChangeProfile)
        cardBack = findViewById(R.id.cardBack)

        loadProfileImage()

        cardChangeProfile.setOnClickListener {
            pickImage()
        }

        cardBack.setOnClickListener {
            finish()
        }
    }

    private fun loadProfileImage() {
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val imageUriString = sharedPreferences.getString("profile_image", null)
        if (imageUriString != null) {
            imageProfile.setImageURI(Uri.parse(imageUriString))
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            val imageUri = data.data
            imageProfile.setImageURI(imageUri)

            // Zapisz wybrane zdjęcie w SharedPreferences
            val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            sharedPreferences.edit().putString("profile_image", imageUri.toString()).apply()
        }
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1001
    }
}

