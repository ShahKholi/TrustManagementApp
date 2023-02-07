package com.android.trustmanagementapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AboutGroup(
    val groupName : String = "",
    val adminEmail : String = "",
    val address : String = "",
    val fullDetail : String = "",
    var id : String = ""
) : Parcelable
