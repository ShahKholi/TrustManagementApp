package com.android.trustmanagementapp.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserClass(
    var id : String = "",
    val firstName : String = "",
    val lastName : String = "",
    var email : String = "",
    val mobile : String = "",
    var code : String = "",
    val adminFlag : Int = 1,
    val premiumEnabled : Int =0,
    var adminEmail : String = ""
) : Parcelable