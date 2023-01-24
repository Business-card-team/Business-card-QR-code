package com.example.projet_business_card

import android.app.Dialog
import android.content.Intent
import android.opengl.Visibility
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class forgot_Password_Activity : AppCompatActivity() {

    lateinit var sendEmailBtn:Button
    lateinit var logInForgotBtn:Button
    lateinit var emailForgotTxt:EditText
    lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        firebaseAuth=FirebaseAuth.getInstance()

        // Dialog Loading
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.alert_dialog_loading)


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

        sendEmailBtn=findViewById(R.id.sendEmailBtn)
        emailForgotTxt=findViewById(R.id.emailForgotTxt)
        logInForgotBtn=findViewById(R.id.logInForgotBtn)

        sendEmailBtn.setOnClickListener(View.OnClickListener {

            var email=emailForgotTxt.text.trim().toString()

            if(email.isEmpty())
            {
                Toast.makeText(this,"Please Insert The Email", Toast.LENGTH_SHORT).show()
            }
            else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            {
                Toast.makeText(this,"Please insert correct Email", Toast.LENGTH_SHORT).show()
            }
            else
            {
                dialog.show()
                firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener({
                    if (it.isSuccessful)
                    {
                        dialog.dismiss()
                        logInForgotBtn.visibility=View.VISIBLE
                        Toast.makeText(this,"Please Reset your Password throught your Email Then Log in",Toast.LENGTH_LONG).show()
                        /*
                        Thread.sleep(3_000)
                        val intent= Intent(this,loginActivity::class.java)
                        startActivity(intent)
                        finish()
                         */
                    }
                    else
                    {
                        dialog.dismiss()
                        Toast.makeText(this,it.exception?.message,Toast.LENGTH_LONG).show()
                    }
                })
            }

        })

        //Log In Button
        logInForgotBtn.setOnClickListener(View.OnClickListener {
            val intent= Intent(this,loginActivity::class.java)
            startActivity(intent)
            finish()
        })
    }
}