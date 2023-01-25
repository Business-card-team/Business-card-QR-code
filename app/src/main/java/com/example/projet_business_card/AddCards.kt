package com.example.projet_business_card

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

class AddCards : AppCompatActivity() {

    lateinit var companyCardTxt:EditText
    lateinit var emailCardTxt:EditText
    lateinit var addressCardTxt:EditText
    lateinit var webSiteCardTxt:EditText
    lateinit var phoneCardTxt:EditText
    lateinit var jobCardTxt:EditText

    lateinit var selectBackgroundBtn:Button
    lateinit var selectLogoBtn:Button
    lateinit var addCardBtn:Button

    lateinit var uriImage:Uri
    lateinit var uriImage2:Uri
    lateinit var dialog: Dialog

     var image1Select:Boolean=false
     var image2Select:Boolean=false



    lateinit var dataRef: DatabaseReference
    lateinit var dataRef2: DatabaseReference
    lateinit var firebaseAuth: FirebaseAuth

    lateinit var activeUserId:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_cards)


        companyCardTxt=findViewById(R.id.companyCardTxt)
        emailCardTxt=findViewById(R.id.emailCardTxt)
        addressCardTxt=findViewById(R.id.addressCardTxt)
        webSiteCardTxt=findViewById(R.id.webSiteCardTxt)
        phoneCardTxt=findViewById(R.id.phoneCardTxt)
        jobCardTxt=findViewById(R.id.jobCardTxt)

        selectBackgroundBtn=findViewById(R.id.selectBackgroundBtn)
        selectLogoBtn=findViewById(R.id.selectLogoBtn)
        addCardBtn=findViewById(R.id.addCardBtn)





        //DataBase SetUp
        dataRef=FirebaseDatabase.getInstance().getReference("Cards")
        firebaseAuth=FirebaseAuth.getInstance()

        //---------------------------------------------------------------------------------------------------------
        dataRef2=FirebaseDatabase.getInstance().getReference("Users")


        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
               if (dataSnapshot.exists())
               {
                   for(dataSnap in dataSnapshot.children)
                   {
                       val user=dataSnap.getValue(Users::class.java)
                       if(user?.email.toString()==firebaseAuth.currentUser?.email.toString())
                           activeUserId=user?.userID.toString()
                   }
               }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
            }
        }
        dataRef2.addValueEventListener(postListener)

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

        // Dialog Loading
        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.alert_dialog_loading)


        // Select BackGround
        selectBackgroundBtn.setOnClickListener(View.OnClickListener {
            val intent=Intent()
            intent.type="image/*"
            intent.action=Intent.ACTION_GET_CONTENT
            @Suppress("DEPRECATION")
            startActivityForResult(intent,100)
        })

        // Select Logo
        selectLogoBtn.setOnClickListener(View.OnClickListener {
            val intent=Intent()
            intent.type="image/*"
            intent.action=Intent.ACTION_GET_CONTENT
            @Suppress("DEPRECATION")
            startActivityForResult(intent,101)
        })



        // Add Card Btn
        addCardBtn.setOnClickListener(View.OnClickListener {

            var ownerName=companyCardTxt.text.trim().toString()
            var address=addressCardTxt.text.trim().toString()
            var email=emailCardTxt.text.trim().toString()
            var phone=phoneCardTxt.text.trim().toString()
            var webSite=webSiteCardTxt.text.trim().toString()
            var job=jobCardTxt.text.trim().toString()




            if(ownerName.isEmpty())
            {
                companyCardTxt.error="Please Insert Owner Name"
                Toast.makeText(this,"Please Insert Owner Name",Toast.LENGTH_SHORT).show()
            }
            else if(job.isEmpty())
            {
                jobCardTxt.error="Please Insert Owner Job"
                Toast.makeText(this,"Please Insert Owner Job",Toast.LENGTH_SHORT).show()
            }
            else if(email.isEmpty())
            {
                emailCardTxt.error="Please Insert Owner Email"
                Toast.makeText(this,"Please Insert Owner Email",Toast.LENGTH_SHORT).show()
            }
            else if(address.isEmpty())
            {
                addressCardTxt.error="Please Insert Owner Address"
                Toast.makeText(this,"Please Insert Owner Address",Toast.LENGTH_SHORT).show()
            }
            else if(webSite.isEmpty())
            {
                webSiteCardTxt.error="Please Insert Owner WebSite"
                Toast.makeText(this,"Please Insert Owner WebSite",Toast.LENGTH_SHORT).show()
            }
            else if(phone.isEmpty())
            {
                phoneCardTxt.error="Please Insert Owner Phone"
                Toast.makeText(this,"Please Insert Owner Phone",Toast.LENGTH_SHORT).show()
            }
            else if(image1Select==false)
            {
                Toast.makeText(this,"Please Choose Background Pic",Toast.LENGTH_SHORT).show()
            }
            else if(image2Select==false)
            {
                Toast.makeText(this,"Please Choose Logo Pic",Toast.LENGTH_SHORT).show()
            }
            else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
            {
                emailCardTxt.error="Incorrect Email Address"
                Toast.makeText(this,"Incorrect Email Address",Toast.LENGTH_SHORT).show()
            }
            else if(!android.util.Patterns.WEB_URL.matcher(webSite).matches())
            {
                webSiteCardTxt.error="Incorrect WebSite Address"
                Toast.makeText(this,"Incorrect WebSite Address",Toast.LENGTH_SHORT).show()
            }
            else if(!android.util.Patterns.PHONE.matcher(phone).matches())
            {
                phoneCardTxt.error="Incorrect Phone Number"
                Toast.makeText(this,"Incorrect Phone Number",Toast.LENGTH_SHORT).show()
            }

            else
            {
                dialog.show()
                val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
                val now=Date()
                val fileName=formatter.format(now)+"_file01"
                val fileName2=formatter.format(now)+"_file02"
                //Upload Background Image
                val storageRef=FirebaseStorage.getInstance().getReference("cards/"+fileName)
                storageRef.putFile(uriImage).addOnCompleteListener(
                    {
                        if(it.isSuccessful)
                        {
                            //Upload Logo Image
                            val storageRef=FirebaseStorage.getInstance().getReference("cards/"+fileName2)
                            storageRef.putFile(uriImage2).addOnCompleteListener({
                                if (it.isSuccessful)
                                {
                                    dialog.dismiss()
                                    //Add Data Card To DataBase
                                    val cardId = dataRef.push().key!!
                                    val card=Cards(cardId,activeUserId,ownerName,address,email,phone,webSite,fileName,fileName2,job)
                                    dataRef.child(cardId).setValue(card).addOnCompleteListener {
                                        if(it.isSuccessful)
                                        {
                                            Toast.makeText(this,"Card Added Succesfuly",Toast.LENGTH_LONG).show()
                                            var intent=Intent(this,Card_Details::class.java)
                                            intent.putExtra("cardId",cardId)
                                            intent.putExtra("activeUserId",activeUserId)
                                            intent.putExtra("ownerName",ownerName)
                                            intent.putExtra("ownerJob",job)
                                            intent.putExtra("ownerAddress",address)
                                            intent.putExtra("ownerEmail",email)
                                            intent.putExtra("ownerPhone",phone)
                                            intent.putExtra("ownerWebSite",webSite)
                                            intent.putExtra("background",fileName)
                                            intent.putExtra("logo",fileName2)
                                            startActivity(intent)
                                            finish()
                                        }
                                        else
                                        {
                                            Toast.makeText(this,""+it.exception,Toast.LENGTH_LONG).show()
                                        }
                                    }

                                }
                                else
                                {
                                    dialog.dismiss()
                                    Toast.makeText(this,it.exception?.message,Toast.LENGTH_LONG).show()
                                }
                            })
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

    }
    //Activity Result For Selecting The Image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==100 && resultCode== RESULT_OK)
        {
            uriImage=data?.data!!
            image1Select=true
        }
        else if(requestCode==101 && resultCode== RESULT_OK)
        {
            uriImage2=data?.data!!
            image2Select=true
        }
    }


}