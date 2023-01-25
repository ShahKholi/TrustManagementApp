package com.android.trustmanagementapp.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MemberClass(
    var groupName : String = "",
    var memberName : String = "",
    var memberEmail : String = "",
    var memberPhone : String = "",
    var memberAdminUID : String ="",
    var memberAdminEmail : String= "",
    var profileImage:  String = "",
    var id : String = "",
    var adminEnableCode : Int = 0
) : Parcelable