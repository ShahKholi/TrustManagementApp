package com.android.trustmanagementapp.model

import android.os.Parcel
import android.os.Parcelable

data class UserClass(
    var id : String = "",
    val firstName : String = "",
    val lastName : String = "",
    var email : String = "",
    val mobile : String = "",
    val code : String = "",
    val adminFlag : Int = 1,
    val premiumEnabled : Int =0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(email)
        parcel.writeString(mobile)
        parcel.writeString(code)
        parcel.writeInt(adminFlag)
        parcel.writeInt(premiumEnabled)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserClass> {
        override fun createFromParcel(parcel: Parcel): UserClass {
            return UserClass(parcel)
        }

        override fun newArray(size: Int): Array<UserClass?> {
            return arrayOfNulls(size)
        }
    }
}