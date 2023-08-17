package com.example.fishingapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView

class RangListAdapter(private val usersList: ArrayList<User>) :
    RecyclerView.Adapter<RangListAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.ranglist_item,parent,false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = usersList[position]

        holder.name.text = currentItem.username
        holder.score.text = currentItem.score.toString()
        Glide.with(holder.itemView.context)
            .load(currentItem.profileImageUrl) // Replace 'imageUrl' with the actual URL of the image
            .into(holder.image)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val name: TextView = itemView.findViewById(R.id.nameUser_recycler_textview)
        val image: CircleImageView = itemView.findViewById(R.id.imageUser_recycler_item)
        val score : TextView  = itemView.findViewById(R.id.score_textview_recycler)
    }
}