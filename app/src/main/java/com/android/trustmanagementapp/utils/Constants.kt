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
    const val TIMELINE_IMAGE: String = "timeline_image"
    const val USER_IMAGE: String = "userImage"
    const val EXPENSE_IMAGE: String = "expenseImage"
    const val TIMELINE_DETAIL = "timelineDetail"
    const val TIMELINE_LIKED_DETAIL = "timelineLikedDetail"
    const val LIKE_ID = "likedEmailList"

    //pref detail
    const val STORE_EMAIL_ID: String = "store_email_id"
    const val STORE_ASSIGNED_ADMIN_EMAIL_ID: String = "store_assigned_admin_email_id"
    const val STORE_MEMBER_EMAIL_ID: String = "store_member_email_id"
    const val STORE_TIMELINE_MEMBER_NAME: String = "store_member_name"
    const val STORE_MEMBER_PHONE_ID: String = "store_member_phone_id"
    const val STORE_GROUP_NAME_ID: String = "store_group_name_id"
    const val STORE_MEMBER_PROFILE_ID: String = "store_profile_image_id"
    const val TIMELINE_USER_EMAIL: String = "time_line_user_email"





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

    const val FINISH : String = "finish"

    const val MEMBER_DELETE_SUCCESS : String = "memberDeleteSuccess"

    const val CURRENT_AMOUNT: String = "currentAmount"

    const val ADMIN_EMAIL: String = "adminEmail"

    const val CREATED_DATE : String = "createdDate"

    //const val GROUP_PREVIOUS_BALANCE : String = "groupPreviousBalance"
    const val GROUP_PREVIOUS_BALANCE : Int = 0


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