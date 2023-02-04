package com.example.projet_business_card

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.transition.Fade
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class loginActivity : AppCompatActivity() {

    lateinit var firebaseAuth: FirebaseAuth

    lateinit var btnNewAccount:TextView
    lateinit var forgotPassBtn:TextView

    lateinit var emailLoginTxt:EditText
    lateinit var passwordLoginTxt:EditText

    lateinit var signInBtn:Button
    lateinit var signInWithGoogleBtn:Button

    private lateinit var googleSignInClient : GoogleSignInClient
    private lateinit var dialog:Dialog

    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Firebase Instance

        val fade = Fade()

        // on below line we are excluding our target such
        // as status bar background and navigation bar
        // background from animation.
        fade.excludeTarget(android.R.id.statusBarBackground, true)
        fade.excludeTarget(android.R.id.navigationBarBackground, true)

        // on below line we are setting enter
        // and exit transition as fade.
        window.enterTransition = fade
        window.exitTransition = fade



        //FireBase Instance
        firebaseAuth = FirebaseAuth.getInstance()

        //Firebase DataBase
        dbRef = FirebaseDatabase.getInstance().getReference("Users")

        //Set Up Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("144939753159-on84gl3hnft71idfgipro8abnf1a3pt6.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this , gso)
        googleSignInClient.signOut()

        //Get Elemets
        emailLoginTxt=findViewById(R.id.emailLoginTxt)
        passwordLoginTxt=findViewById(R.id.passwordLoginTxt)
        signInBtn=findViewById(R.id.signInBtn)
        forgotPassBtn=findViewById(R.id.forgotPassBtn)
        signInWithGoogleBtn=findViewById(R.id.signInWithGoogleBtn)

        // Dialog Loading

        dialog = Dialog(this)
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

        // New Account Btn
        btnNewAccount=findViewById(R.id.btnNewAccount)
        btnNewAccount.setOnClickListener(View.OnClickListener {

            startActivity(Intent(this,signUpActivity::class.java))
            //finish()
        })



        //sign In Button Click

        signInBtn.setOnClickListener(View.OnClickListener {
            val email=emailLoginTxt.text.trim().toString()
            val password=passwordLoginTxt.text.trim().toString()

            if(email.isEmpty())
            {
                emailLoginTxt.error="insert this field"
                Toast.makeText(this,"Please Insert Email", Toast.LENGTH_SHORT).show()
            }
            else if(password.isEmpty())
            {
                passwordLoginTxt.error="insert this field"
                Toast.makeText(this,"Please Insert Password", Toast.LENGTH_SHORT).show()
            }
            else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            {
                emailLoginTxt.error="Please insert correct Email"
                Toast.makeText(this,"Please insert correct Email",Toast.LENGTH_SHORT).show()
            }
            else
            {
                dialog.show()
                    //check if exist
                    firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(
                        {
                            if(it.isSuccessful)
                            {
                                if(it.result.user?.isEmailVerified==true)
                                {
                                    dialog.dismiss()
                                    val intent=Intent(this,homeActivity::class.java)
                                    startActivity(intent)
                                    //(MainActivity()::class as Activity).finish()
                                    finish()
                                }
                                else
                                {
                                    dialog.dismiss()
                                    Toast.makeText(this,"Please Verify your email",Toast.LENGTH_LONG).show()

                                }
                            }
                            else
                            {
                                dialog.dismiss()
                                Toast.makeText(this,it.exception?.message,Toast.LENGTH_LONG).show()
                            }
                        }
                    )

            }

        })


        //Forgot Password Btn

        forgotPassBtn.setOnClickListener(View.OnClickListener {
            val intent=Intent(this,forgot_Password_Activity::class.java)
            startActivity(intent)
            //finish()
        })


        // Sign In With Google

        signInWithGoogleBtn.setOnClickListener(View.OnClickListener {
            dialog.show()
            signInGoogle()
        })



    }

    private fun signInGoogle() {

        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        dialog.dismiss()
        if (result.resultCode == Activity.RESULT_OK){

            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResults(task)

        }
    }

    private fun handleResults(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful){
            val account : GoogleSignInAccount? = task.result
            if (account != null){
                dialog.show()
                updateUI(account)
            }
        }else{
            dialog.dismiss()
            Toast.makeText(this, task.exception.toString() , Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken , null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful){
                dialog.dismiss()
                val intent : Intent = Intent(this , homeActivity::class.java)
                intent.putExtra("email" , account.email)
                intent.putExtra("name" , account.displayName)
                val emailGoogle=account.email as String
                val userIdGoogle=account.id as String
                val usernameGoogle=account.displayName as String
                creatUserDBwithGoogle(userIdGoogle,usernameGoogle,emailGoogle)
            }else{
                dialog.dismiss()
                Toast.makeText(this, it.exception.toString() , Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun creatUserDBwithGoogle(userId:String,username:String,email:String)
    {
        val user = Users(userId, username, email,"gs://projet-business-card-977f7.appspot.com/Users/profile.png")
        //Insert The User To The DataBase

        dbRef.child(userId).setValue(user)
            .addOnCompleteListener {
                dialog.dismiss()
                val intent=Intent(this,homeActivity::class.java)
                startActivity(intent)
                //(MainActivity()::class as Activity).finish()
                finish()

            }.addOnFailureListener { err ->
                dialog.dismiss()
                Toast.makeText(this, "Error ${err.message}", Toast.LENGTH_LONG).show()
            }
    }
}