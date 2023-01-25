package com.example.projet_business_card

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.Image
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter

import java.io.File

class Card_Details : AppCompatActivity() {

    lateinit var logoImage:ImageView
    lateinit var backgroundImageCard:ImageView

    lateinit var ownerNameTxt:TextView
    lateinit var ownerJobTxt:TextView
    lateinit var ownerPhoneFinal:TextView
    lateinit var ownerEmailFinal:TextView
    lateinit var ownerWebSiteFinal:TextView
    lateinit var ownerAddressFinal:TextView

    lateinit var qrCodeImage:ImageView
    lateinit var qrCodeImageZommer:ImageView
     var lightOn :Boolean=false
    lateinit var  bmp:Bitmap
    lateinit var dialog:Dialog
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
        ownerNameTxt=findViewById(R.id.ownerNameTxt)
        ownerJobTxt=findViewById(R.id.ownerJobTxt)
        ownerPhoneFinal=findViewById(R.id.ownerPhoneFinal)
        ownerEmailFinal=findViewById(R.id.ownerEmailFinal)
        ownerWebSiteFinal=findViewById(R.id.ownerWebSiteFinal)
        ownerAddressFinal=findViewById(R.id.ownerAddressFinal)

        //-----------------

        //QR Zoomer Dialog
        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.qr_zommer)


        logoImage=findViewById(R.id.logoImage)
        backgroundImageCard=findViewById(R.id.backgroundImageCard)


        qrCodeImage=findViewById(R.id.qrCodeImg)







        // Get Intent Informations
        val cardId:String? = intent.getStringExtra("cardId")
        val activeUserId:String? = intent.getStringExtra("activeUserId")
        val ownerName:String? = intent.getStringExtra("ownerName")
        val ownerJob:String? = intent.getStringExtra("ownerJob")
        val ownerAddress:String? = intent.getStringExtra("ownerAddress")
        val ownerEmail:String? = intent.getStringExtra("ownerEmail")
        val ownerPhone:String? = intent.getStringExtra("ownerPhone")
        val ownerWebSite:String? = intent.getStringExtra("ownerWebSite")
        val background:String? = intent.getStringExtra("background")
        val logo:String? = intent.getStringExtra("logo")

        ownerNameTxt.setText(ownerName)
        ownerJobTxt.setText(ownerJob)
        ownerPhoneFinal.setText(ownerEmail)
        ownerEmailFinal.setText(ownerPhone)
        ownerWebSiteFinal.setText(ownerWebSite)
        ownerAddressFinal.setText(ownerAddress)



        //Generate Code QR
        val dataQR=""+ownerWebSite

        val writer=QRCodeWriter()
        try {
            val bitMatrix=writer.encode(dataQR,BarcodeFormat.QR_CODE,512,512)
            val width=bitMatrix.width
            val height=bitMatrix.height
            bmp=Bitmap.createBitmap(width,height,Bitmap.Config.RGB_565)

            for(x in 0 until width)
            {
                for(y in 0 until height)
                {
                    bmp.setPixel(x,y,if(bitMatrix[x,y]) Color.BLACK else Color.WHITE)
                }
            }
            qrCodeImage.setImageBitmap(bmp)

        }catch (e:WriterException)
        {
            Toast.makeText(this,""+e.message,Toast.LENGTH_LONG).show()
        }






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

        //Image QR Click
        qrCodeImage.setOnClickListener(View.OnClickListener {
            dialog.findViewById<ImageView>(R.id.qrZommer).setImageBitmap(bmp)
            dialog.show()
        })
    }
}