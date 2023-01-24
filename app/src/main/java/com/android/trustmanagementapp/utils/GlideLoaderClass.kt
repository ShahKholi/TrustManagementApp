package com.android.trustmanagementapp.utils

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.android.trustmanagementapp.R
import com.bumptech.glide.Glide
import java.io.IOException

class GlideLoaderClass(val context : Context) {

    fun loadGroupIcon(image: Any, imageView: ImageView){
        try{
            Glide.with(context)
              //.load(Uri.parse(imageUri.toString()))
                .load(image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_placeholder)
                .into(imageView)
        }catch (e:IOException){
            e.printStackTrace()
        }
    }

    fun loadGroupPictures(groupImage: Any, imageView: ImageView?) {
        try{
            Glide.with(context)
                //.load(Uri.parse(imageUri.toString()))
                .load(groupImage)
                .centerCrop()
                .into(imageView!!)
        }catch (e:IOException){
            e.printStackTrace()
        }
    }

    fun loadGuestProfilePictures(groupImage: Any, imageView: ImageView?) {
        try{
            Glide.with(context)
                //.load(Uri.parse(imageUri.toString()))
                .load(groupImage)
                .fitCenter()
                .into(imageView!!)
        }catch (e:IOException){
            e.printStackTrace()
        }
    }
}