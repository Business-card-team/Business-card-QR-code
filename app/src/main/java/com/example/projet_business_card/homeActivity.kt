package com.example.projet_business_card

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList

class homeActivity : AppCompatActivity() {

    lateinit var firebaseAuth: FirebaseAuth
    lateinit var addCardActBtn:FloatingActionButton
    lateinit var optionsBtn:FloatingActionButton
    lateinit var camOpBtn:FloatingActionButton
    lateinit var logOutOpBtn:FloatingActionButton
    lateinit var searchInput:EditText
    lateinit var noCardsTxt:TextView
    var isClicked=false

    lateinit var dbRef:DatabaseReference
    lateinit var dbRef2:DatabaseReference
    lateinit var myRecycler:RecyclerView
    lateinit var listCards:ArrayList<Cards>
     lateinit var activeUserId:String

     lateinit var dialog:Dialog
     lateinit var dialog2:Dialog
     lateinit var dialog3:Dialog
    private lateinit var adapter: cardsAdaptar

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

        // Dialog Loading

        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.alert_dialog_loading)
        dialog.show()

        // Dialog Confirm Log Out
        dialog2 = Dialog(this)
        dialog2.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog2.setCancelable(false)
        dialog2.setContentView(R.layout.logout_confirmation)

        // Dialog Confirm Remove
        dialog3 = Dialog(this)
        dialog3.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog3.setCancelable(false)
        dialog3.setContentView(R.layout.remove_confirmation)


        //Get Current User
        firebaseAuth=FirebaseAuth.getInstance()

        addCardActBtn=findViewById(R.id.addCardOpBtn)
        optionsBtn=findViewById(R.id.optionsBtn)
        camOpBtn=findViewById(R.id.camOpBtn)
        logOutOpBtn=findViewById(R.id.logOutOpBtn)
        searchInput=findViewById(R.id.searchInput)
        noCardsTxt=findViewById(R.id.noCardsTxt)

        myRecycler=findViewById(R.id.myRecycler)

        //Set Up Recycler View
        myRecycler.layoutManager=LinearLayoutManager(this)
        myRecycler.setHasFixedSize(true)

        //set Up List Cards
        listCards= arrayListOf<Cards>()

        //Get Cards
        getCards()

        //Logout Btn
        logOutOpBtn.setOnClickListener(View.OnClickListener {

            dialog2.show()
            //Confirm Log Out
            dialog2.findViewById<Button>(R.id.logoutFinalBtn).setOnClickListener(
                {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this,loginActivity::class.java))
                    finish()
                }
            )

            //cancel Log Out
            dialog2.findViewById<Button>(R.id.cancelLogOut).setOnClickListener(
                {
                    dialog2.dismiss()
                }
            )

        })

        //Search For Card

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filter(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })


        //Add Cards Btn

        addCardActBtn.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this,AddCards::class.java))
        })

        //Scan Code QR Activity
        camOpBtn.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this,camera_scanner::class.java))
        })

        //Options Btn
        optionsBtn.setOnClickListener(View.OnClickListener {
            if(!isClicked)
            {
                optionsBtn.setImageResource(R.drawable.ic_baseline_close_24)
                isClicked=true
                addCardActBtn.visibility=View.VISIBLE
                addCardActBtn.isClickable=true
                camOpBtn.visibility=View.VISIBLE
                camOpBtn.isClickable=true
                logOutOpBtn.visibility=View.VISIBLE
                logOutOpBtn.isClickable=true

            }
            else
            {
                isClicked=false
                optionsBtn.setImageResource(R.drawable.ic_baseline_menu_24)
                addCardActBtn.visibility=View.INVISIBLE
                addCardActBtn.isClickable=false
                camOpBtn.visibility=View.INVISIBLE
                camOpBtn.isClickable=false
                logOutOpBtn.visibility=View.INVISIBLE
                logOutOpBtn.isClickable=false
            }
        })
    }

    private fun getCards() {

        //First Get User Id From Users DataBase
        dbRef=FirebaseDatabase.getInstance().getReference("Users")
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
                    Log.d("Current User ID ",activeUserId)
                    //Then Get Cards Of Current User DataBase
                    dbRef2=FirebaseDatabase.getInstance().getReference("Cards")
                    val postListener2 = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            // Get Post object and use the values to update the UI
                            if (dataSnapshot.exists())
                            {
                                dialog.dismiss()
                                listCards.clear()
                                for(dataSnap in dataSnapshot.children)
                                {
                                    val card=dataSnap.getValue(Cards::class.java)
                                    if(card?.idUser.toString()==activeUserId)
                                        listCards.add(card!!)
                                }
                                adapter=cardsAdaptar(listCards,applicationContext,dialog3,myRecycler,dialog)
                                myRecycler.adapter=adapter

                            }
                            else
                            {
                                dialog.dismiss()
                                listCards.clear()
                                myRecycler?.adapter?.notifyDataSetChanged()

                            }

                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Getting Post failed, log a message
                        }
                    }
                    dbRef2.addValueEventListener(postListener2)


                }
                else
                {

                    dialog.dismiss()
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
            }
        }
        dbRef.addValueEventListener(postListener)
        if(!listCards.isEmpty())
        {
            noCardsTxt.visibility=View.GONE
        }
        else
        {
            noCardsTxt.visibility=View.VISIBLE
        }



    }

    private fun filter(text: String) {
        // creating a new array list to filter our data.
        val filteredlist = ArrayList<Cards>()

        // running a for loop to compare elements.
        for (item in listCards) {
            // checking if the entered string matched with any item of our recycler view.
            @Suppress("DEPRECATION")
            if (item.cardOwner!!.toLowerCase().contains(text.lowercase(Locale.getDefault()))) {
                // if the item is matched we are
                // adding it to our filtered list.
                filteredlist.add(item)
            }
        }
        if (filteredlist.isEmpty()) {
            // if no item is added in filtered list we are
            // displaying a toast message as no data found.
            //Toast.makeText(this, "No Data Found..", Toast.LENGTH_SHORT).show()
        } else {
            // at last we are passing that filtered
            // list to our adapter class.
            adapter.filterList(filteredlist)
        }
    }


}