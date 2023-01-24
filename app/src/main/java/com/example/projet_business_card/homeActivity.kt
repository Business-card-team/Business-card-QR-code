package com.example.projet_business_card

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class homeActivity : AppCompatActivity() {

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var logOutBtn:Button
    lateinit var addCardActBtn:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //For Full Screen
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        //-----------



        //Get Current User
        firebaseAuth=FirebaseAuth.getInstance()
        Toast.makeText(this,"Email : "+firebaseAuth.currentUser?.email,Toast.LENGTH_LONG).show()

        logOutBtn=findViewById(R.id.logOutBtn)
        addCardActBtn=findViewById(R.id.addCardActBtn)


        //Logout Btn
        logOutBtn.setOnClickListener(View.OnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this,loginActivity::class.java))
            finish()
        })



        //Add Cards Btn

        addCardActBtn.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this,AddCards::class.java))
        })
    }
}