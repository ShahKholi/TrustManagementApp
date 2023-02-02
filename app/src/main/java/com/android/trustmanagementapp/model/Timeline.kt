package com.android.trustmanagementapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Timeline(
    val groupName : String = "",
    val adminEmail : String = "",
    val detail : String = "",
    val timeLineImage : String = "",
    val dateTime: String = "",
    val groupImage : String = "",
    var id : String = ""

) : Parcelable
