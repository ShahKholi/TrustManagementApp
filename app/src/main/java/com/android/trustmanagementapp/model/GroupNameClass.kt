package com.android.trustmanagementapp.model

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class GroupNameClass(
    var groupName: String = "",
    var groupPreviousBalance : Int = 0,
    var groupCreatedDate: String = "",
    var email: String = "",
    var groupImage:  String = "",
    var id: String = "",
    var previousBalance : Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(groupName)
        parcel.writeInt(groupPreviousBalance)
        parcel.writeString(groupCreatedDate)
        parcel.writeString(email)
        parcel.writeString(groupImage)
        parcel.writeString(id)
        parcel.writeInt(previousBalance)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GroupNameClass> {
        override fun createFromParcel(parcel: Parcel): GroupNameClass {
            return GroupNameClass(parcel)
        }

        override fun newArray(size: Int): Array<GroupNameClass?> {
            return arrayOfNulls(size)
        }
    }
}