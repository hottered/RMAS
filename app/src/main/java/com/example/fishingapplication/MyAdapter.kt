package com.example.fishingapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView

class MyAdapter(private val locationsList: ArrayList<MarkerData>) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.location_item,
            parent,false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val currentItem = locationsList[position]

        holder.title.text = currentItem.title
        Glide.with(holder.itemView.context)
            .load(currentItem.imageMarker) // Replace 'imageUrl' with the actual URL of the image
            .into(holder.image)

    }

    override fun getItemCount(): Int {
        return locationsList.size
    }

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {

        val title : TextView = itemView.findViewById(R.id.name_recycler_textview)
        val image : CircleImageView = itemView.findViewById(R.id.image_recycler_item)

    }

}