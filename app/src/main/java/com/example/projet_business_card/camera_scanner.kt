package com.example.projet_business_card

import android.Manifest.permission.CAMERA
import android.Manifest.permission_group.CAMERA
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class camera_scanner : AppCompatActivity() {
    private val CAMERA_REQUEST_CODE = 101
    private var cardId=""
    lateinit var viewDetailsBtn:Button
    private lateinit var codeScanner: CodeScanner
    lateinit var dataRef: DatabaseReference
    lateinit var dataRef2: DatabaseReference
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var activeUserId:String
     var isAdded:Boolean=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_scanner)

        viewDetailsBtn=findViewById(R.id.viewDetailsBtn)

        firebaseAuth= FirebaseAuth.getInstance()

        //DataBase SetUp
        dataRef= FirebaseDatabase.getInstance().getReference("Cards")
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
        setUpPermession()
        startScanning()
        viewDetailsBtn.setOnClickListener(View.OnClickListener {
            //Toast.makeText(this,cardId,Toast.LENGTH_SHORT).show()
            val postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get Post object and use the values to update the UI
                    if (dataSnapshot.exists())
                    {
                        for(dataSnap in dataSnapshot.children)
                        {
                            val card=dataSnap.getValue(Cards::class.java)
                            if(card?.idCard.toString()==cardId && isAdded==false)
                            {
                                isAdded=true
                                //Add Data Card To DataBase
                                val cardId2 = dataRef.push().key!!


                                val card=Cards(cardId2,activeUserId,card?.cardOwner.toString(),card?.ownerAddress.toString(),card?.ownerEmail.toString(),card?.ownerPhone.toString()
                                ,card?.ownerWebSite.toString(),card?.ownerBackground.toString(),card?.ownerImage.toString(),card?.ownerJob.toString())
                                dataRef.child(cardId2).setValue(card).addOnCompleteListener {
                                    if(it.isSuccessful)
                                    {

                                        var intent= Intent(applicationContext,Card_Details::class.java)
                                        intent.putExtra("cardId",card?.idCard.toString())
                                        intent.putExtra("activeUserId",card?.idUser.toString())
                                        intent.putExtra("ownerName",card?.cardOwner.toString())
                                        intent.putExtra("ownerJob",card?.ownerJob.toString())
                                        intent.putExtra("ownerAddress",card?.ownerAddress.toString())
                                        intent.putExtra("ownerEmail",card?.ownerEmail.toString())
                                        intent.putExtra("ownerPhone",card?.ownerPhone.toString())
                                        intent.putExtra("ownerWebSite",card?.ownerWebSite.toString())
                                        intent.putExtra("background",card?.ownerBackground.toString())
                                        intent.putExtra("logo",card?.ownerImage.toString())
                                        startActivity(intent)
                                        finish()
                                    }
                                    else
                                    {
                                        Toast.makeText(applicationContext,""+it.exception,Toast.LENGTH_LONG).show()
                                    }
                                }

                            }
                        }
                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                }
            }
            dataRef.addValueEventListener(postListener)
        })
    }
    private fun startScanning()
    {
        val scannerView = findViewById<CodeScannerView>(R.id.theScanner)

        codeScanner = CodeScanner(this, scannerView)

        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                //Toast.makeText(this, "Scan result: ${it.text}", Toast.LENGTH_LONG).show()
                viewDetailsBtn.visibility= View.VISIBLE
                cardId=it.text

            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                viewDetailsBtn.visibility= View.GONE
                Toast.makeText(this, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG).show()
            }
        }

        scannerView.setOnClickListener {
            viewDetailsBtn.visibility= View.GONE
            codeScanner.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun setUpPermession()
    {
        val permission =ContextCompat.checkSelfPermission(this,android.Manifest.permission.CAMERA)

        if (permission!=PackageManager.PERMISSION_GRANTED)
        {
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA),CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode)
        {
            CAMERA_REQUEST_CODE ->{
                if (grantResults.isEmpty() || grantResults[0]!=PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this,"This App need Camera Permession",Toast.LENGTH_LONG).show()
                }
                else
                {
                    //
                }
            }
        }
    }


}