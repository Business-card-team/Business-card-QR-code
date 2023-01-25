package com.example.projet_business_card

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat.startActivity
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class cardsAdaptar(private val cardsList:ArrayList<Cards>,private val context: Context,
                   private val dialogRemove:Dialog,
                   private val recyclerView: RecyclerView,private val dialogLoading:Dialog) :
     RecyclerView.Adapter<cardsAdaptar.MyViewHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.cards_items,parent,false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentCard=cardsList[position]
        holder.cardOwnerTxt.setText(""+currentCard.cardOwner)
        holder.cardJobTxt.setText(""+currentCard.ownerJob)

        //Item Click
        holder.cardItem.setOnClickListener(View.OnClickListener {
            //Toast.makeText(context,""+cardsList.get(holder.adapterPosition).cardOwner,Toast.LENGTH_LONG).show()
            var intent= Intent(context,Card_Details::class.java)
            intent.putExtra("cardId",cardsList.get(holder.adapterPosition).idCard)
            intent.putExtra("activeUserId",cardsList.get(holder.adapterPosition).idUser)
            intent.putExtra("ownerName",cardsList.get(holder.adapterPosition).cardOwner)
            intent.putExtra("ownerJob",cardsList.get(holder.adapterPosition).ownerJob)
            intent.putExtra("ownerAddress",cardsList.get(holder.adapterPosition).ownerAddress)
            intent.putExtra("ownerEmail",cardsList.get(holder.adapterPosition).ownerEmail)
            intent.putExtra("ownerPhone",cardsList.get(holder.adapterPosition).ownerPhone)
            intent.putExtra("ownerWebSite",cardsList.get(holder.adapterPosition).ownerWebSite)
            intent.putExtra("background",cardsList.get(holder.adapterPosition).ownerBackground)
            intent.putExtra("logo",cardsList.get(holder.adapterPosition).ownerImage)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)


        })
        //Show The Pics
        val storageRef= FirebaseStorage.getInstance().reference.child("cards/"+currentCard.ownerImage)
        val localFile= File.createTempFile("tempFile",null)
        storageRef.getFile(localFile).addOnCompleteListener(
            {
                if (it.isSuccessful)
                {
                    val bitMap= BitmapFactory.decodeFile(localFile.absolutePath)
                    holder.imageCardOwner.setImageBitmap(bitMap)
                }
            }
        )

        //Click For Remove Card
        holder.deletCardBtn.setOnClickListener(View.OnClickListener {
            dialogRemove.show()
            //cancel Remove
            dialogRemove.findViewById<Button>(R.id.cancelRemoveBtn).setOnClickListener {
                dialogRemove.dismiss()
            }
            //Remove the card
            dialogRemove.findViewById<Button>(R.id.yesRemoveBtn).setOnClickListener {
                dialogRemove.dismiss()
                dialogLoading.show()
                val dataRef=FirebaseDatabase.getInstance().getReference("Cards").child(cardsList.get(holder.adapterPosition).idCard.toString())
                dataRef.removeValue().addOnCompleteListener {
                    if(it.isSuccessful)
                    {
                        recyclerView?.adapter?.notifyDataSetChanged()
                        dialogLoading.dismiss()
                    }
                    else
                    {
                        dialogLoading.dismiss()
                        Toast.makeText(context.applicationContext,""+it.exception?.message,Toast.LENGTH_LONG).show()
                    }
                }

            }
        })

    }

    override fun getItemCount(): Int {
        return cardsList.size
    }

    class MyViewHolder(itemView :View) : RecyclerView.ViewHolder(itemView) {
        val imageCardOwner:ImageView =itemView.findViewById(R.id.imageCardOwner)
        val cardOwnerTxt:TextView =itemView.findViewById(R.id.cardOwnerTxt)
        val cardJobTxt:TextView =itemView.findViewById(R.id.cardJobTxt)
        val deletCardBtn:ImageButton =itemView.findViewById(R.id.deletCardBtn)
        val cardItem:LinearLayout =itemView.findViewById(R.id.cardItem)

    }
}