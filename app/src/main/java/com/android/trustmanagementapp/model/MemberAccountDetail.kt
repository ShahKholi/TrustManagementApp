package com.android.trustmanagementapp.model

import android.os.Parcel
import android.os.Parcelable



data class MemberAccountDetail(
    var year: String = "",
    val groupName: String = "",
    val memberName: String = "",
    var memberEmail : String = "",
    val adminEmail: String = "",
    val month : String = "",
    var currentAmount: Int = 0,
    var id : String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(year)
        parcel.writeString(groupName)
        parcel.writeString(memberName)
        parcel.writeString(memberEmail)
        parcel.writeString(adminEmail)
        parcel.writeString(month)
        parcel.writeInt(currentAmount)
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MemberAccountDetail> {
        override fun createFromParcel(parcel: Parcel): MemberAccountDetail {
            return MemberAccountDetail(parcel)
        }

        override fun newArray(size: Int): Array<MemberAccountDetail?> {
            return arrayOfNulls(size)
        }
    }
}