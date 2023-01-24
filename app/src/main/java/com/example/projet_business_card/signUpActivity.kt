package com.example.projet_business_card

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class signUpActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var emailTxt:EditText
    private lateinit var usernameTxt:EditText
    private lateinit var passwordTxt:EditText
    private lateinit var passwordConTxt:EditText

    private lateinit var signUpBtn:Button
    private lateinit var signUpGoogleBtn:Button
    private lateinit var btnHaveAccount:TextView

    private lateinit var googleSignInClient : GoogleSignInClient
    private lateinit var dialog:Dialog

    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)


        passwordTxt=findViewById(R.id.passwordTxt)
        passwordConTxt=findViewById(R.id.passwordConTxt)
        usernameTxt=findViewById(R.id.emailLoginTxt)
        emailTxt=findViewById(R.id.passwordLoginTxt)
        signUpGoogleBtn=findViewById(R.id.signUpGoogleBtn)


        signUpBtn=findViewById(R.id.signInBtn)
        btnHaveAccount=findViewById(R.id.btnHaveAccount)

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
        // Firebase
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


        //Sign Up Btn

        signUpBtn.setOnClickListener(View.OnClickListener {
            val email=emailTxt.text.trim().toString()
            val password=passwordTxt.text.toString()
            val passwordCon=passwordConTxt.text.toString()
            val username=usernameTxt.text.trim().toString()
            if (username.isEmpty())
            {
                usernameTxt.error="Please Insert Username"
                Toast.makeText(this,"Please Insert The Username",Toast.LENGTH_SHORT).show()
                usernameTxt.isSelected=true
            }
            else if(email.isEmpty())
            {
                emailTxt.error="Please Insert Email"
                Toast.makeText(this,"Please Insert The Email",Toast.LENGTH_SHORT).show()
            }
            else if(password.isEmpty())
            {
                passwordTxt.error="Please Insert Password"
                Toast.makeText(this,"Please Insert The Password",Toast.LENGTH_SHORT).show()
            }
            else if(passwordCon.isEmpty())
            {
                passwordConTxt.error="Please Insert Confirmation Password"
                Toast.makeText(this,"Please Insert The Confirmation Password",Toast.LENGTH_SHORT).show()
            }
            else
            {
                //check username if it has + 6 character
                if(username.length<6)
                {
                    usernameTxt.error="username most contain at least 6 character"
                    Toast.makeText(this, "username most contain at least 6 character", Toast.LENGTH_SHORT).show()
                }
                //Check Email If Is Correct
                else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
                {
                    emailTxt.error="Please insert correct Email"
                    Toast.makeText(this,"Please insert correct Email",Toast.LENGTH_SHORT).show()
                }
                //check password if it has + 6 character
                else if(password.length<6)
                {
                    passwordTxt.error="Passowrds most contain at least 6 character"
                    Toast.makeText(this, "Passowrds most contain at least 6 character", Toast.LENGTH_SHORT).show()
                }
                //Check Password Matching
                else if(password != passwordCon) {
                    passwordConTxt.error="Passowrds Are not Matching"
                    Toast.makeText(this, "Passowrds Are not Matching", Toast.LENGTH_SHORT).show()
                }
                else
                {
                    //Create User With Email and Password
                    dialog.show()
                    firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(
                        {
                            if(it.isSuccessful)
                            {
                                //Send Email Confirmation To This User
                                it.result.user?.sendEmailVerification()?.addOnCompleteListener {
                                    if(it.isSuccessful)
                                    {

                                        creatUserDBwithEmail(username,email)

                                    }
                                    else
                                    {
                                        //Send Error If Email Confirmation Failed
                                        dialog.dismiss()
                                        Toast.makeText(this,it.exception?.message,Toast.LENGTH_LONG).show()
                                    }
                                }

                            }
                            else
                            {
                                //Send Error For Creation with Email and Password Failed
                                dialog.dismiss()
                                Toast.makeText(this,it.exception?.message,Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }
            }
        })

        // New Account Btn
        btnHaveAccount.setOnClickListener(View.OnClickListener {


            startActivity(Intent(this,loginActivity::class.java))
            finish()
        })



        //Sign Up With Google
        signUpGoogleBtn.setOnClickListener(View.OnClickListener {
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

    //Create User In DataBase
    private fun creatUserDBwithEmail(username:String,email:String)
    {
        //User ID
        val userId = dbRef.push().key!!
        val user = Users(userId, username, email,"gs://projet-business-card-977f7.appspot.com/Users/profile.png")
        //Insert The User To The DataBase

        dbRef.child(userId).setValue(user)
            .addOnCompleteListener {
                Toast.makeText(this, "Please Verify Your Email Then Log In", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                val intent=Intent(this,loginActivity::class.java)
                startActivity(intent)
                finish()

            }.addOnFailureListener { err ->
                dialog.dismiss()
                Toast.makeText(this, "Error ${err.message}", Toast.LENGTH_LONG).show()
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
                finish()

            }.addOnFailureListener { err ->
                dialog.dismiss()
                Toast.makeText(this, "Error ${err.message}", Toast.LENGTH_LONG).show()
            }
    }
}