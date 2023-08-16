package com.example.fishingapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.math.floor

class MyAdapter(private val locationsList: ArrayList<MarkerData>) :
    RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    private lateinit var mListener:onItemClickListener
    interface onItemClickListener {
        fun onItemClick(position : Int)
    }
    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.location_item,
            parent,false)
        return MyViewHolder(itemView,mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val currentItem = locationsList[position]

        holder.title.text = currentItem.title
        Glide.with(holder.itemView.context)
            .load(currentItem.imageMarker) // Replace 'imageUrl' with the actual URL of the image
            .into(holder.image)
        holder.ratingBar.rating = floor(currentItem.rating ?: 0.0).toFloat()

    }

    override fun getItemCount(): Int {
        return locationsList.size
    }

    class MyViewHolder(itemView : View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {

        val title : TextView = itemView.findViewById(R.id.name_recycler_textview)
        val image : CircleImageView = itemView.findViewById(R.id.image_recycler_item)
        val ratingBar : RatingBar = itemView.findViewById(R.id.ratingBar_adapter)

        init {
            itemView.setOnClickListener{
                listener.onItemClick(adapterPosition)
            }
        }
    }

}