package com.example.projet_business_card

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.Image
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage

import java.io.File

class Card_Details : AppCompatActivity() {

    lateinit var companyNameFinal:TextView
    lateinit var comapnyEmailFinal:TextView
    lateinit var companyWebSiteFinal:TextView
    lateinit var companyPhoneFinal:TextView
    lateinit var companyAddressFinal:TextView

    lateinit var logoImage:ImageView
    lateinit var backgroundImageCard:ImageView

    lateinit var switchColorBtn:ImageView

    lateinit var icHome:ImageView
    lateinit var icEmail:ImageView
    lateinit var icWebsite:ImageView
    lateinit var icPhone:ImageView
    lateinit var icAddress:ImageView

    lateinit var companyNameView:TextView
    lateinit var comapnyEmailView:TextView
    lateinit var companyWebSiteView:TextView
    lateinit var companyPhoneView:TextView
    lateinit var companyAddressView:TextView

    lateinit var qrCodeImage:ImageView
     var lightOn :Boolean=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_details)

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


        //-----------------
        companyNameFinal=findViewById(R.id.companyNameFinal)
        comapnyEmailFinal=findViewById(R.id.comapnyEmailFinal)
        companyWebSiteFinal=findViewById(R.id.companyWebSiteFinal)
        companyPhoneFinal=findViewById(R.id.companyPhoneFinal)
        companyAddressFinal=findViewById(R.id.companyAddressFinal)

        companyNameView=findViewById(R.id.companyNameView)
        comapnyEmailView=findViewById(R.id.companyEmailView)
        companyWebSiteView=findViewById(R.id.comanyWebsiteView)
        companyPhoneView=findViewById(R.id.companyPhoneView)
        companyAddressView=findViewById(R.id.companyAddressView)

        logoImage=findViewById(R.id.logoImage)
        backgroundImageCard=findViewById(R.id.backgroundImageCard)

        switchColorBtn=findViewById(R.id.switchColorBtn)

        qrCodeImage=findViewById(R.id.qrCodeImg)



        icHome=findViewById(R.id.icHome)
        icEmail=findViewById(R.id.icEmail)
        icWebsite=findViewById(R.id.icWebsite)
        icPhone=findViewById(R.id.icPhone)
        icAddress=findViewById(R.id.icAddress)



        // Get Intent Informations
        val cardId:String? = intent.getStringExtra("cardId")
        val activeUserId:String? = intent.getStringExtra("activeUserId")
        val companyName:String? = intent.getStringExtra("companyName")
        val companyAddress:String? = intent.getStringExtra("companyAddress")
        val companyEmail:String? = intent.getStringExtra("companyEmail")
        val companyPhone:String? = intent.getStringExtra("companyPhone")
        val companyWebSite:String? = intent.getStringExtra("companyWebSite")
        val background:String? = intent.getStringExtra("background")
        val logo:String? = intent.getStringExtra("logo")


        companyNameFinal.setText(companyName)
        comapnyEmailFinal.setText(companyEmail)
        companyWebSiteFinal.setText(companyWebSite)
        companyPhoneFinal.setText(companyPhone)
        companyAddressFinal.setText(companyAddress)

        //Generate Code QR

        /*
        val dataQR="Company Name : "+companyName+" Company Email : "+companyEmail+" Company Address : "+companyAddress+" Company Phone : "+
                    companyPhone+" Company Website : "+companyWebSite

        val writer=QRCodeWriter()
        try {
            val bitMatrix=writer.encode(dataQR,BarcodeFormat.QR_CODE,512,512)
            val width=bitMatrix.width
            val height=bitMatrix.height
            val bmp=Bitmap.createBitmap(width,height,Bitmap.Config.RGB_565)
            for(x in 0 until width)
            {
                for (y in x until height)
                {
                    bmp.setPixel(x,y,if(bitMatrix[x,y]) Color.BLACK else Color.WHITE)
                }
            }
            qrCodeImage.setImageBitmap(bmp)
        }catch (ex :WriterException)
        {
            Toast.makeText(this,""+ex.message,Toast.LENGTH_LONG).show()
        }


         */



        // Get Images From Storage
        //Get LOGO First
        val storageRef=FirebaseStorage.getInstance().reference.child("cards/"+logo)
        val localFile=File.createTempFile("tempFile",null)
        storageRef.getFile(localFile).addOnCompleteListener(
            {
                if (it.isSuccessful)
                {
                    val bitMap=BitmapFactory.decodeFile(localFile.absolutePath)
                    logoImage.setImageBitmap(bitMap)
                }
                else Toast.makeText(this,""+it.exception,Toast.LENGTH_SHORT).show()
            }
        )

        //Get BackGround
        val storageRef2=FirebaseStorage.getInstance().reference.child("cards/"+background)
        val localFile2=File.createTempFile("tempFile",null)
        storageRef2.getFile(localFile2).addOnCompleteListener(
            {
                if (it.isSuccessful)
                {
                    val bitMap2=BitmapFactory.decodeFile(localFile2.absolutePath)
                    backgroundImageCard.setImageBitmap(bitMap2)
                }
                else Toast.makeText(this,""+it.exception,Toast.LENGTH_SHORT).show()
            }
        )


        //Switch Colors From Black To White
        switchColorBtn.setOnClickListener(View.OnClickListener {
            if(lightOn)
            {
                lightOn=false

                switchColorBtn.setImageResource(R.drawable.ic_dark)

                companyNameFinal.setTextColor(Color.BLACK)
                comapnyEmailFinal.setTextColor(Color.BLACK)
                companyWebSiteFinal.setTextColor(Color.BLACK)
                companyPhoneFinal.setTextColor(Color.BLACK)
                companyAddressFinal.setTextColor(Color.BLACK)

                companyNameView.setTextColor(Color.BLACK)
                comapnyEmailView.setTextColor(Color.BLACK)
                companyWebSiteView.setTextColor(Color.BLACK)
                companyPhoneView.setTextColor(Color.BLACK)
                companyAddressView.setTextColor(Color.BLACK)

                icHome.setImageResource(R.drawable.ic_company_dark)
                icEmail.setImageResource(R.drawable.ic_email_dark)
                icWebsite.setImageResource(R.drawable.ic_link_dark)
                icPhone.setImageResource(R.drawable.ic_phone_dark)
                icAddress.setImageResource(R.drawable.ic_home_dark)
            }
            else
            {
                lightOn=true
                switchColorBtn.setImageResource(R.drawable.ic_light)
                companyNameFinal.setTextColor(Color.WHITE)
                comapnyEmailFinal.setTextColor(Color.WHITE)
                companyWebSiteFinal.setTextColor(Color.WHITE)
                companyPhoneFinal.setTextColor(Color.WHITE)
                companyAddressFinal.setTextColor(Color.WHITE)

                companyNameView.setTextColor(Color.WHITE)
                comapnyEmailView.setTextColor(Color.WHITE)
                companyWebSiteView.setTextColor(Color.WHITE)
                companyPhoneView.setTextColor(Color.WHITE)
                companyAddressView.setTextColor(Color.WHITE)

                icHome.setImageResource(R.drawable.ic_company)
                icEmail.setImageResource(R.drawable.ic_email)
                icWebsite.setImageResource(R.drawable.ic_link)
                icPhone.setImageResource(R.drawable.ic_phone)
                icAddress.setImageResource(R.drawable.ic_home)
            }
        })
    }
}