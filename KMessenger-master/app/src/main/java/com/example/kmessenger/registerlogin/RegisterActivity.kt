package com.example.kmessenger.registerlogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.kmessenger.messages.LatestMessagesActivity
import com.example.kmessenger.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*
import com.example.kmessenger.models.User

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        r_button.setOnClickListener {
            performRegister()

        }

        already.setOnClickListener {
            Log.d("RegisterActivity", "Try to show login activity")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        r_photo.setOnClickListener {
            Log.d("RegisterActivity", "Try to show photo selector")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("RegisterActivity", "Photo was selected")

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            r_select_photo_imageview.setImageBitmap(bitmap)

            r_photo.alpha = 0f
//            val bitmapDrawable = BitmapDrawable(bitmap)
//            r_photo.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun performRegister() {
        val email = r_email.text.toString()
        val password = r_password.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("RegisterActivity", "Email is: " + email)
        Log.d("RegisterActivity", "Password: $password")

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener

                    Log.d("Main", "Successfully created user with uid: ${it.result!!.user.uid}")

                    uploadImageToFirebaseStorage()
                }
                .addOnFailureListener {
                    Log.d("Main", "Failed to create user: ${it.message}")
                    Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT).show()

                }
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d("Register", "Successfully upload image: ${it.metadata?.path}")

                    ref.downloadUrl.addOnSuccessListener {
                        it.toString()
                        Log.d("RegisterActivity", "File LocationL $it")

                        saveUserToFirebaseDatabase(it.toString())
                    }
                }
                .addOnFailureListener{
                    Log.d("RegisterActivity", "x")
                }

    }


    private fun saveUserToFirebaseDatabase(profileImageUrl: String){
        val uid = FirebaseAuth.getInstance().uid?:""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, r_username.text.toString(), profileImageUrl)
        ref.setValue(user)
                .addOnSuccessListener {
                    Log.d("RegisterActivity", "Finally we saved the user to Firebase Database")

                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Log.d("RegisterActivity", "Failed to set value to database: ${it.message}")
                }
    }
}
//
//class User(val uid: String, val username: String, val profileImageUrl: String){
//    constructor(): this("", "", "")
//}