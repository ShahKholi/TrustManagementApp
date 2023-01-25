package com.android.trustmanagementapp.model

import android.os.Parcel
import android.os.Parcelable

data class MonthExpense(
    var groupName : String = "",
    var memberAdminUID : String ="",
    var memberAdminEmail : String= "",
    var expenseAmount : Int = 0,
    var month : String = "",
    var detail : String = "",
    var groupImage : String = "",
    var id : String = "",
    var year: Int = 0,
    var spendImage : String = "",
    var code : String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(groupName)
        parcel.writeString(memberAdminUID)
        parcel.writeString(memberAdminEmail)
        parcel.writeInt(expenseAmount)
        parcel.writeString(month)
        parcel.writeString(detail)
        parcel.writeString(groupImage)
        parcel.writeString(id)
        parcel.writeInt(year)
        parcel.writeString(spendImage)
        parcel.writeString(code)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MonthExpense> {
        override fun createFromParcel(parcel: Parcel): MonthExpense {
            return MonthExpense(parcel)
        }

        override fun newArray(size: Int): Array<MonthExpense?> {
            return arrayOfNulls(size)
        }
    }
}