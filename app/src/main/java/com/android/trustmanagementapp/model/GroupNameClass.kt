package com.android.trustmanagementapp.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GroupNameClass(
    var groupName: String = "",
    var groupPreviousBalance : Int = 0,
    var groupCreatedDate: String = "",
    var email: String = "",
    var groupImage:  String = "",
    var id: String = "",
    var previousBalance : Int = 0,
    var code : String = ""
) : Parcelable