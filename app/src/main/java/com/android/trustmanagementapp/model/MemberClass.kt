package com.android.trustmanagementapp.model

import android.os.Parcel
import android.os.Parcelable

data class MemberClass(
    var groupName : String = "",
    var memberName : String = "",
    var memberEmail : String = "",
    var memberPhone : String = "",
    var memberAdminUID : String ="",
    var memberAdminEmail : String= "",
    var profileImage:  String = "",
    var id : String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(groupName)
        parcel.writeString(memberName)
        parcel.writeString(memberEmail)
        parcel.writeString(memberPhone)
        parcel.writeString(memberAdminUID)
        parcel.writeString(memberAdminEmail)
        parcel.writeString(profileImage)
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MemberClass> {
        override fun createFromParcel(parcel: Parcel): MemberClass {
            return MemberClass(parcel)
        }

        override fun newArray(size: Int): Array<MemberClass?> {
            return arrayOfNulls(size)
        }
    }
}