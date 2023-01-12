package com.android.trustmanagementapp.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import java.io.File

object Constants {
    const val GROUP_IMAGE: String = "groupImage"
    const val USER_IMAGE: String = "userImage"

    //pref detail
    const val STORE_EMAIL_ID: String = "store_email_id"
    const val READ_STORAGE_PERMISSION_CODE: Int = 1
    const val USER: String = "users"
    const val EMAIL: String = "email"
    const val CODE: String = "code"
    const val GROUP: String = "group"
    const val MEMBER: String = "memberList"
    const val MEMBER_ACCOUNT_DETAIL: String = "memberAccountDetail"
    const val MEMBER_ADMIN_EMAIL: String = "memberAdminEmail"
    const val MEMBER_EMAIL: String = "memberEmail"
    const val MEMBER_NAME : String = "memberName"
    const val MEMBER_PHONE : String = "memberPhone"
    const val PROFILE_IMAGE : String = "profileImage"
    const val GROUP_EXPENSE_DETAIL: String = "groupExpenseDetail"
    const val MASTER_ACCOUNT_DETAIL: String = "masterAccountDetail"
    const val MONTH: String = "month"

    const val INCOME: String = "income"
    const val MONTH_EXPENSE: String = "monthExpense"
    const val EXPENSE_AMOUNT: String = "expenseAmount"

    const val GROUP_NAME: String = "groupName"

    const val CURRENT_AMOUNT: String = "currentAmount"


    fun getMimeType(context: Context, uri: Uri): String? {

        //Check uri format to avoid null
        val extension: String? = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            //If scheme is a content
            val mime = MimeTypeMap.getSingleton()
            mime.getExtensionFromMimeType(context.contentResolver.getType(uri))
        } else {
            //If scheme is a File
            //This will replace white spaces with %20 and also other special characters.
            // This will avoid returning null values on file name with spaces and special characters.
            MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(File(uri.path.toString())).toString())
        }
        return extension
    }
}