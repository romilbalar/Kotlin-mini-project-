package com.example.kmessenger.registerlogin

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.example.kmessenger.R
import com.example.kmessenger.messages.LatestMessagesActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        l_button.setOnClickListener {

            performLogin()

        }

        back_to_register.setOnClickListener{
            finish()
        }
    }

    private fun performLogin(){
        val email = l_email.text.toString()
        val password = l_password.text.toString()

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("RegisterActivity", "Email is: " + email)
        Log.d("RegisterActivity", "Password: $password")

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(!it.isSuccessful) return@addOnCompleteListener

                Log.d("Main", "Successfully logged in user with uid: ${it.result!!.user.uid}")

                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.d("Main", "Failed to login: ${it.message}")
                Toast.makeText(this, "Failed to login: ${it.message}", Toast.LENGTH_SHORT).show()

            }
    }

}