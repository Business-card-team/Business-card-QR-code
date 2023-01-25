package com.example.projet_business_card

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.transition.Fade
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {

    var firstAct: Activity? = null

    lateinit var  btnGetStarted:Button
    lateinit var sharedImage3:ImageView
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        firstAct=this
        //check if user is Logged In
        firebaseAuth=FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser?.isEmailVerified==true)
        {
            startActivity(Intent(this,homeActivity::class.java))
            finish()
        }



        // on below line we are creating
        // a variable for our fade.
        val fade = Fade()

        // on below line we are excluding the target
        // which we dont want to animate such as our
        // status background and navigation background.
        fade.excludeTarget(android.R.id.statusBarBackground, true)
        fade.excludeTarget(android.R.id.navigationBarBackground, true)

        // on below line we are specifying
        // enter transition as fade.
        window.enterTransition = fade

        // on below line we are specifying
        // exit transition as fade.
        window.exitTransition = fade

        // on below line we are initializing
        // our image view with its id.


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


        // Start Btn
        btnGetStarted=findViewById(R.id.btnGetStarted)
        sharedImage3=findViewById(R.id.sharedImage3)
        btnGetStarted.setOnClickListener(View.OnClickListener {

            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this@MainActivity, sharedImage3, ViewCompat.getTransitionName(sharedImage3)!!
            )

            startActivity(Intent(this,loginActivity::class.java))
            finish()
        })
    }
}