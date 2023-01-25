package com.android.trustmanagementapp.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MasterAccountDetail(
    var id: String = "",
    var groupName: String = "",
    var memberAdminEmail: String= "",
    var year : Int = 0,
    var month : String = "",
    var income : Int= 0,
    var monthExpense : String = "",
    var expenseAmount : Int = 0,
    var code : String = ""

) : Parcelable