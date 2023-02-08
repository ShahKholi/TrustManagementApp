package com.android.trustmanagementapp.firestore

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.android.trustmanagementapp.activities.*
import com.android.trustmanagementapp.activities.fragments.TimelineFragment
import com.android.trustmanagementapp.model.*
import com.android.trustmanagementapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class FireStoreClass {
    private val mFirestoreInstance = FirebaseFirestore.getInstance()
    private var userExit: Boolean = false


    fun registerUser(activity: RegisterActivity, userInfo: UserClass) {
        mFirestoreInstance.collection(Constants.USER)
            .document(userInfo.id)
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.userRegistrationSuccess(userInfo.firstName)
            }
            .addOnFailureListener { e ->
                activity.cancelProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while registering the user.",
                    e
                )
            }
    }

    fun createAboutGroup(
        adminAboutGroupActivity: AdminAboutGroupActivity,
        aboutGroup: AboutGroup
    ) {
        val document = mFirestoreInstance.collection(Constants.ABOUT_GROUP).document()
        aboutGroup.id = document.id
        val set = document.set(aboutGroup, SetOptions.merge())
        set.addOnSuccessListener {
            adminAboutGroupActivity.successCreateAboutGroup(aboutGroup)
        }
        set.addOnFailureListener { exception ->
            adminAboutGroupActivity.cancelProgressDialog()
            Log.e(
                adminAboutGroupActivity.javaClass.simpleName,
                "Error while creating about group",
                exception
            )
        }
    }

    fun saveDocumentID(activity: Activity, timeline: Timeline) {
        val document = mFirestoreInstance.collection(Constants.TIMELINE_DETAIL).document()
        timeline.id = document.id
        val set = document.set(timeline, SetOptions.merge())
        set.addOnSuccessListener {
            when (activity) {
                is AddTimelineActivity -> {
                    activity.successSavedDocumentID(timeline)
                }
            }
        }
        set.addOnFailureListener { exception ->
            when (activity) {
                is AddTimelineActivity -> {
                    activity.cancelProgressDialog()
                    Log.e(
                        activity.javaClass.simpleName,
                        "Error while saving documentID",
                        exception
                    )
                }

            }
        }
    }

    suspend fun checkGroupAvailble(mGroupName: String, getEmailId: String?): String {
        var groupString: String = ""
        mFirestoreInstance.collection(Constants.GROUP)
            .whereEqualTo("groupName", mGroupName)
            .whereEqualTo("email", getEmailId)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    groupString = i.get("groupName").toString()
                }
            }.await()
        return groupString
    }

    fun createGroup(createGroupActivity: CreateGroupActivity, createGroup: GroupNameClass) {
        mFirestoreInstance.collection(Constants.GROUP)
            .document()
            .set(createGroup, SetOptions.merge())
            .addOnSuccessListener {
                createGroupActivity.groupRegistrationSuccess(createGroup.groupName)
            }
            .addOnFailureListener { e ->
                createGroupActivity.cancelProgressDialog()
                Log.e(
                    createGroupActivity.javaClass.simpleName,
                    "Error while creating group.",
                    e
                )
            }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun createAccountDetailToFirestore(
        activity: Activity,
        memberAccountDetail: MemberAccountDetail
    ) {

        mFirestoreInstance.collection(Constants.MEMBER_ACCOUNT_DETAIL)
            .document()
            .set(memberAccountDetail, SetOptions.merge())
            .addOnSuccessListener {
                when (activity) {
                    is AddAccountActivity -> {
                        activity.accountRegistrationSuccess(memberAccountDetail)
                    }
                }
            }.addOnFailureListener { e ->
                when (activity) {
                    is AddAccountActivity -> {
                        activity.cancelProgressDialog()
                        Log.e(
                            activity.javaClass.simpleName,
                            "Error while creating Account Detail.",
                            e
                        )
                    }
                }
            }
    }

    suspend fun registerExpenseDetail(
        activity: Activity,
        monthExpenseAmount: MonthExpense
    ) {
        mFirestoreInstance.collection(Constants.GROUP_EXPENSE_DETAIL)
            .document()
            .set(monthExpenseAmount, SetOptions.merge())
            .addOnSuccessListener {
                when (activity) {
                    is AddExpenseActivity -> {
                        activity.lifecycleScope.launch {
                            activity.groupExpenseDetailSuccess(monthExpenseAmount)
                        }

                    }
                }
            }.addOnFailureListener { e ->
                when (activity) {
                    is AddExpenseActivity -> {
                        activity.cancelProgressDialog()
                        Log.e(
                            activity.javaClass.simpleName,
                            "Error while creating expense"
                        )
                    }
                }
            }
    }

    fun registerMasterAccount(
        addAccountActivity: AddAccountActivity,
        masterAccountDetail: MasterAccountDetail
    ) {
        mFirestoreInstance.collection(Constants.MASTER_ACCOUNT_DETAIL)
            .document()
            .set(masterAccountDetail, SetOptions.merge())
            .addOnSuccessListener {
                addAccountActivity.masterAccountRegisterSuccess()
            }.addOnFailureListener { e ->
                Log.e(
                    addAccountActivity.javaClass.simpleName,
                    "Error while creating Master Account.",
                    e
                )
            }

    }

    fun createMemberToFirestore(activity: Activity, createMember: MemberClass) {
        mFirestoreInstance.collection(Constants.MEMBER)
            .document()
            .set(createMember, SetOptions.merge())
            .addOnSuccessListener {
                when (activity) {
                    is AddMemberActivity -> {
                        activity.groupRegistrationSuccess(createMember.memberName)
                    }
                }
            }.addOnFailureListener { e ->
                when (activity) {
                    is AddMemberActivity -> {
                        activity.cancelProgressDialog()
                        Log.e(
                            activity.javaClass.simpleName,
                            "Error while creating group.",
                            e
                        )
                    }
                }
            }
    }

    fun getCurrentUserID(): String {
        val current = FirebaseAuth.getInstance().currentUser
        var currentUserUID = ""
        if (current != null) {
            currentUserUID = current.uid
        }
        return currentUserUID
    }

    suspend fun validateCode(activity: RegisterActivity, code: String): ArrayList<String> {
        val userList: ArrayList<String> = ArrayList()
        var user: UserClass
        mFirestoreInstance.collection(Constants.USER)
            .whereEqualTo("code", code)
            .get()
            .addOnSuccessListener { document ->
                for (i in document) {
                    user = i.toObject(UserClass::class.java)
                    //group.groupImage = i.get("groupImage").toString()
                    user.code = i.get("code").toString()
                    userList.add(user.code)
                }
            }.await()
        return userList
    }

    fun codeValidateUserDetail(activity: Activity, code: String, email: String) {
        mFirestoreInstance.collection(Constants.USER)
            .whereEqualTo(Constants.CODE, code)
            .get()
            .addOnSuccessListener { document ->
                val userEmailList: ArrayList<UserClass> = ArrayList()
                for (i in document) {
                    val user = i.toObject(UserClass::class.java)
                    user.id = i.id
                    userEmailList.add(user)
                }
                when (activity) {
                    is LoginActivity -> {
                        if (document.documents.size == 0) {
                            activity.failureMemberLogin()
                        } else {
                            activity.successMemberLogin(userEmailList, email)
                        }

                    }
                }
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is LoginActivity -> {
                        Log.e("Exception find user", e.message.toString())
                    }
                }
            }
    }

    fun validateUserAdminORNonAdmin(activity: Activity, email: String) {
        Log.e("checkadmin initial", email)
        mFirestoreInstance.collection(Constants.USER)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener { document ->
                when (activity) {
                    is LoginActivity -> {
                        activity.cancelProgressDialog()
                        if (document.documents.isNotEmpty()) {
                            userExit = true
                            activity.userValueReturn(userExit)

                        } else {
                            activity.userValueReturn(userExit)

                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is LoginActivity -> {
                        activity.cancelProgressDialog()
                        Log.e("Exception find user", e.message.toString())
                    }
                }
            }
    }

    fun updateUploadImageToCloudStorage(
        activity: Activity,
        mSelectedGroupImageUri: Uri, groupImage: String
    ) {
        val storageReference: StorageReference = FirebaseStorage.getInstance().reference.child(
            groupImage + System.currentTimeMillis() + "." +
                    Constants.getMimeType(activity, mSelectedGroupImageUri)
        )
        storageReference.putFile(mSelectedGroupImageUri).addOnSuccessListener { taskSnapShot ->
            Log.i(
                "FireBase image url ",
                taskSnapShot.metadata!!.reference!!.downloadUrl.toString()
            )
            taskSnapShot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                Log.i("FireBase Download url ", uri.toString())
                when (activity) {
                    is CreateGroupActivity -> {
                        activity.updateGroupProfile(uri)
                    }
                    is AddMemberActivity -> {
                        activity.updateGroupProfile(uri)
                    }
                }
            }.addOnFailureListener { exception ->
                when (activity) {
                    is CreateGroupActivity -> {
                        activity.cancelProgressDialog()
                    }
                    is AddMemberActivity -> {
                        activity.cancelProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    exception.message,
                    exception
                )
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun uploadImageToCloudStorage(
        activity: Activity,
        mSelectedGroupImageUri: Uri, groupImage: String, groupImage2: String
    ) {
        val storageReference: StorageReference = FirebaseStorage.getInstance().reference.child(
            groupImage + System.currentTimeMillis() + "." +
                    Constants.getMimeType(activity, mSelectedGroupImageUri)
        )
        storageReference.putFile(mSelectedGroupImageUri).addOnSuccessListener { taskSnapShot ->
            Log.i(
                "FireBase image url ",
                taskSnapShot.metadata!!.reference!!.downloadUrl.toString()
            )
            taskSnapShot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                Log.i("FireBase Download url ", uri.toString())
                when (activity) {
                    is CreateGroupActivity -> {
                        activity.successGroupIconUpload(uri)
                    }
                    is AddMemberActivity -> {
                        activity.successGroupIconUpload(uri)
                    }

                    is GuestProfileActivity -> {
                        activity.lifecycleScope.launch {
                            activity.successGroupIconUpload(uri)
                        }

                    }

                    is AddTimelineActivity -> {
                        activity.lifecycleScope.launch {
                            activity.successGroupIconUpload(uri)
                        }

                    }
                    is AddExpenseActivity -> {

                        activity.lifecycleScope.launch {
                            activity.successGroupIconUpload(uri, groupImage2)
                        }

                    }
                }
            }.addOnFailureListener { exception ->
                when (activity) {
                    is CreateGroupActivity -> {
                        activity.cancelProgressDialog()
                    }
                    is AddMemberActivity -> {
                        activity.cancelProgressDialog()
                    }
                    is AddExpenseActivity -> {
                        activity.cancelProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    exception.message,
                    exception
                )
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadAllTimelineDetailFragment(timelineFragment: TimelineFragment) {
        mFirestoreInstance.collection(Constants.TIMELINE_DETAIL)
            .get()
            .addOnSuccessListener { document ->
                val timelineList: ArrayList<Timeline> = ArrayList()
                for (i in document.documents) {
                    val timeline = i.toObject(Timeline::class.java)
                    timeline!!.id = i.id
                    timelineList.add(timeline)
                }
                timelineFragment.successTimelineListFromFirestore(timelineList)
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadAllTimelineDetail(activity: Activity) {
        mFirestoreInstance.collection(Constants.TIMELINE_DETAIL)
            .get()
            .addOnSuccessListener { document ->
                val timelineList: ArrayList<Timeline> = ArrayList()
                for (i in document.documents) {
                    val timeline = i.toObject(Timeline::class.java)
                    timeline!!.id = i.id
                    timelineList.add(timeline)
                }
                when (activity) {
                    is ViewTimeLineActivity -> {
                        activity.successTimelineListFromFirestore(timelineList)
                    }

                }

            }.addOnFailureListener { exception ->
                when (activity) {
                    is ViewTimeLineActivity -> {
                        activity.cancelProgressDialog()
                        Log.e(
                            activity.javaClass.simpleName,
                            "Error load timeline detail.",
                            exception
                        )
                    }
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadTimelineDetail(activity: Activity, adminEmailId: String) {
        mFirestoreInstance.collection(Constants.TIMELINE_DETAIL)
            .whereEqualTo("adminEmail", adminEmailId)
            .get()
            .addOnSuccessListener { document ->
                val timelineList: ArrayList<Timeline> = ArrayList()
                for (i in document.documents) {
                    val timeline = i.toObject(Timeline::class.java)
                    timeline!!.id = i.id
                    timelineList.add(timeline)
                }
                when (activity) {
                    is AddTimelineActivity -> {
                        activity.lifecycleScope.launch {
                            activity.successTimelineListFromFirestore(timelineList)
                        }

                    }
                }

            }.addOnFailureListener { exception ->
                when (activity) {
                    is AddTimelineActivity -> {
                        activity.cancelProgressDialog()
                        Log.e(
                            activity.javaClass.simpleName,
                            "Error load expense group.",
                            exception
                        )
                    }

                }
            }

    }

    fun loadExpenseDetail(activity: Activity, adminEmail: String) {
        mFirestoreInstance.collection(Constants.GROUP_EXPENSE_DETAIL)
            .whereEqualTo("memberAdminEmail", adminEmail)
            .get()
            .addOnSuccessListener { document ->
                val expenseList: ArrayList<MonthExpense> = ArrayList()
                for (i in document.documents) {
                    val expense = i.toObject(MonthExpense::class.java)
                    expense!!.id = i.id
                    expenseList.add(expense)
                }

                when (activity) {
                    is AddExpenseActivity -> {
                        activity.successExpenseListFromFirestore(expenseList)
                    }
                    is GuestViewGroupExpenseActivity -> {
                        activity.successExpenseListFromFirestore(expenseList)
                    }
                }

            }.addOnFailureListener { exception ->
                when (activity) {
                    is AddExpenseActivity -> {
                        activity.cancelProgressDialog()
                        Log.e(
                            activity.javaClass.simpleName,
                            "Error load expense group.",
                            exception
                        )
                    }
                }
            }
    }

    suspend fun getGroupPreviousBalance(
        activity: Activity,
        mGroupName: String, adminEmailId: String?
    ): Int {
        var previousAmount: Int = 0
        var group: GroupNameClass
        mFirestoreInstance.collection(Constants.GROUP)
            .whereEqualTo("email", adminEmailId)
            .whereEqualTo("groupName", mGroupName)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    group = i.toObject(GroupNameClass::class.java)!!
                    group.previousBalance = i.get("groupPreviousBalance").toString().toInt()
                    previousAmount = group.previousBalance
                }
            }.await()

        return previousAmount
    }

    fun getAssignedGroupList(activity: Activity, assignedAdminEmail: String) {
        mFirestoreInstance.collection(Constants.GROUP)
            .whereEqualTo("email", assignedAdminEmail)
            .get()
            .addOnSuccessListener { document ->
                val groupList: ArrayList<GroupNameClass> = ArrayList()
                for (i in document.documents) {
                    val group = i.toObject(GroupNameClass::class.java)
                    group!!.id = i.id
                    groupList.add(group)
                }
                when (activity) {
                    is AdminScreenActivity -> {
                        activity.successGroupListFromFirestore(groupList)
                    }
                    is AddMemberActivity -> {
                        activity.successGroupListFromFirestore(groupList)
                    }
                    is AddAccountActivity -> {
                        activity.successGroupListFromFirestore(groupList)
                    }
                    is PreViewAccountActivity -> {
                        activity.successGroupListFromFirestore(groupList)
                    }
                    is AddExpenseActivity -> {
                        activity.successGroupListFromFirestore(groupList)
                    }
                    is PreViewMemberActivity -> {
                        activity.successGroupListFromFirestore(groupList)
                    }
                }
            }.addOnFailureListener { exception ->
                when (activity) {
                    is AdminScreenActivity -> {
                        activity.cancelProgressDialog()
                        Log.e(
                            activity.javaClass.simpleName,
                            "Error while checking group.",
                            exception
                        )
                    }
                }
            }

    }

    fun getGroupDetail(
        activity: Activity,
        storeAdminEmail: String,
        storeGroupName: String
    ) {
        val groupList: ArrayList<GroupNameClass> = ArrayList()
        mFirestoreInstance.collection(Constants.GROUP)
            .whereEqualTo("email", storeAdminEmail)
            .whereEqualTo("groupName", storeGroupName)
            .get()
            .addOnSuccessListener{document->
                for (i in document.documents){
                    val group = i.toObject(GroupNameClass::class.java)
                    group!!.id = i.id
                    groupList.add(group)
                }
                when(activity){
                    is AboutGroupFullViewActivity -> {
                        activity.successGroupDetail(groupList)
                    }

                }

            }.addOnFailureListener { exception->
                when(activity){
                    is AboutGroupFullViewActivity -> {
                        activity.cancelProgressDialog()
                    }

                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while checking group.",
                    exception
                )
            }
    }


    fun getGroupList(activity: Activity, adminEmail: String) {
        mFirestoreInstance.collection(Constants.GROUP)
            .whereEqualTo("email", adminEmail)
            .get()
            .addOnSuccessListener { document ->
                val groupList: ArrayList<GroupNameClass> = ArrayList()
                for (i in document.documents) {
                    val group = i.toObject(GroupNameClass::class.java)
                    group!!.id = i.id
                    groupList.add(group)
                }
                when (activity) {
                    is AdminScreenActivity -> {
                        activity.successGroupListFromFirestore(groupList)
                    }
                    is AddMemberActivity -> {
                        activity.successGroupListFromFirestore(groupList)
                    }
                    is AddAccountActivity -> {
                        activity.successGroupListFromFirestore(groupList)
                    }
                    is PreViewAccountActivity -> {
                        activity.successGroupListFromFirestore(groupList)
                    }
                    is AddExpenseActivity -> {
                        activity.successGroupListFromFirestore(groupList)
                    }
                    is PreViewMemberActivity -> {
                        activity.successGroupListFromFirestore(groupList)
                    }
                    is AddTimelineActivity -> {
                        activity.successGroupListFromFirestore(groupList)
                    }
                    is AdminAboutGroupActivity -> {
                        activity.successGroupListFromFirestore(groupList)
                    }
                }
            }.addOnFailureListener { exception ->
                when (activity) {
                    is AdminScreenActivity -> {
                        activity.cancelProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while checking group.",
                    exception
                )
            }

    }


    fun memberDeleteUpdateMasterAccount(
        activity: Activity,
        adminEmailId: String,
        groupName: String,
        month: String,
        currentYear: Int,
        userHashMap: HashMap<String, Any>

    ) {
        Log.e("updateMasterAccount", "updated started")
        mFirestoreInstance.collection(Constants.MASTER_ACCOUNT_DETAIL)
            .whereEqualTo("groupName", groupName)
            .whereEqualTo("memberAdminEmail", adminEmailId)
            .whereEqualTo("month", month)
            .whereEqualTo("year", currentYear)
            .get()
            .addOnSuccessListener { document ->
                Log.e("userHashMap", userHashMap.toString())
                for (i in document) {
                    mFirestoreInstance.collection(Constants.MASTER_ACCOUNT_DETAIL)
                        .document(i.id)
                        .set(userHashMap, SetOptions.merge())
                    when (activity) {
                        is AddAccountActivity -> {
                            activity.updateMasterAmountSuccess()
                        }
                        is MemberDetailedActivity -> {
                            activity.updateMemberMasterAmountSuccess()
                        }
                        is AddExpenseActivity -> {
                            activity.updateMasterAmountSuccess()
                        }
                        is ViewAccountActivity -> {
                            activity.updateMasterAccountZeroSuccess()
                        }
                    }
                }

            }.addOnFailureListener { exception ->
                when (activity) {
                    is MemberDetailedActivity -> {
                        activity.cancelProgressDialog()
                    }
                    is AddAccountActivity -> {
                        activity.cancelProgressDialog()
                    }
                    is AddExpenseActivity -> {
                        activity.cancelProgressDialog()
                    }
                }
            }
    }

    fun updateMasterAccountGroup(
        createGroupActivity: CreateGroupActivity,
        emailId: String?,
        userHashMap: HashMap<String, Any>,
        mainGroupOldName: String
    ) {
        Log.e("updateMasterAccount", "updated started for change group in master Account")
        mFirestoreInstance.collection(Constants.MASTER_ACCOUNT_DETAIL)
            .whereEqualTo("memberAdminEmail", emailId.toString())
            .whereEqualTo("groupName", mainGroupOldName)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    mFirestoreInstance.collection(Constants.MASTER_ACCOUNT_DETAIL)
                        .document(i.id)
                        .set(userHashMap, SetOptions.merge())
                    createGroupActivity.masterUpdateAccountSuccess()
                }

            }.addOnFailureListener { exception ->
                createGroupActivity.cancelProgressDialog()
                Log.e(
                    createGroupActivity.javaClass.simpleName,
                    "Error updating Member.",
                    exception
                )
            }
    }

    fun updateExpenseAccountGroup(
        createGroupActivity: CreateGroupActivity,
        emailId: String?,
        userHashMap: HashMap<String, Any>,
        mainGroupOldName: String
    ) {
        Log.e("updateExpenseAccount", "updated started for change group in expense Account")

        mFirestoreInstance.collection(Constants.GROUP_EXPENSE_DETAIL)
            .whereEqualTo("memberAdminEmail", emailId.toString())
            .whereEqualTo("groupName", mainGroupOldName)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    mFirestoreInstance.collection(Constants.GROUP_EXPENSE_DETAIL)
                        .document(i.id)
                        .set(userHashMap, SetOptions.merge())
                    createGroupActivity.expenseUpdateAccountSuccess()
                }

            }.addOnFailureListener { exception ->
                createGroupActivity.cancelProgressDialog()
                Log.e(
                    createGroupActivity.javaClass.simpleName,
                    "Error updating Member.",
                    exception
                )
            }

    }

    fun updateMemberAccountGroup(
        createGroupActivity: CreateGroupActivity,
        emailId: String?,
        userHashMap: HashMap<String, Any>,
        mainGroupOldName: String
    ) {
        Log.e("updateMemberAccount", "updated started for change group in member Account")
        mFirestoreInstance.collection(Constants.MEMBER_ACCOUNT_DETAIL)
            .whereEqualTo("adminEmail", emailId.toString())
            .whereEqualTo("groupName", mainGroupOldName)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    mFirestoreInstance.collection(Constants.MEMBER_ACCOUNT_DETAIL)
                        .document(i.id)
                        .set(userHashMap, SetOptions.merge())
                    createGroupActivity.memberUpdateAccountSuccess()
                }

            }.addOnFailureListener { exception ->
                createGroupActivity.cancelProgressDialog()
                Log.e(
                    createGroupActivity.javaClass.simpleName,
                    "Error updating Member.",
                    exception
                )
            }
    }

    fun updateMemberGroup(
        createGroupActivity: CreateGroupActivity,
        emailId: String?,
        userHashMap: HashMap<String, Any>,
        mainGroupOldName: String
    ) {
        Log.e("updateMember", "updated started for change group in member detail")
        mFirestoreInstance.collection(Constants.MEMBER)
            .whereEqualTo("memberAdminEmail", emailId.toString())
            .whereEqualTo("groupName", mainGroupOldName)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    mFirestoreInstance.collection(Constants.MEMBER)
                        .document(i.id)
                        .set(userHashMap, SetOptions.merge())
                    createGroupActivity.memberUpdateSuccess()
                }

            }.addOnFailureListener { exception ->
                createGroupActivity.cancelProgressDialog()
                Log.e(
                    createGroupActivity.javaClass.simpleName,
                    "Error updating Member.",
                    exception
                )
            }
    }

    fun compareFinalMasterAccountUpdate(
        activity: Activity,
        adminEmailId: String,
        groupName: String,
        month: String,
        currentYear: Int,
        userHashMap: HashMap<String, Any>

    ) {
        Log.e("miss match found", "updating master account")
        mFirestoreInstance.collection(Constants.MASTER_ACCOUNT_DETAIL)
            .whereEqualTo("groupName", groupName)
            .whereEqualTo("memberAdminEmail", adminEmailId)
            .whereEqualTo("month", month)
            .whereEqualTo("year", currentYear)
            .get()
            .addOnSuccessListener { document ->
                Log.e("userHashMap", userHashMap.toString())
                for (i in document) {
                    mFirestoreInstance.collection(Constants.MASTER_ACCOUNT_DETAIL)
                        .document(i.id)
                        .set(userHashMap, SetOptions.merge())
                    when (activity) {
                        is AddAccountActivity -> {
                            activity.updateMasterAmountSuccess()
                        }
                        is MemberDetailedActivity -> {
                            activity.updateMasterAmountSuccess()
                        }
                        is AddExpenseActivity -> {
                            activity.updateMasterAmountSuccess()
                        }
                        is ViewAccountActivity -> {
                            activity.updateMasterAmountSuccess()
                        }
                    }
                }

            }.addOnFailureListener { exception ->
                when (activity) {
                    is MemberDetailedActivity -> {
                        activity.cancelProgressDialog()
                    }
                    is AddAccountActivity -> {
                        activity.cancelProgressDialog()
                    }
                    is AddExpenseActivity -> {
                        activity.cancelProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error updating Member.",
                    exception
                )
            }
    }

    fun updateMasterAccount(
        activity: Activity,
        adminEmailId: String,
        groupName: String,
        month: String,
        currentYear: Int,
        userHashMap: HashMap<String, Any>

    ) {
        Log.e("updateMasterAccount", "updated started")
        mFirestoreInstance.collection(Constants.MASTER_ACCOUNT_DETAIL)
            .whereEqualTo("groupName", groupName)
            .whereEqualTo("memberAdminEmail", adminEmailId)
            .whereEqualTo("month", month)
            .whereEqualTo("year", currentYear)
            .get()
            .addOnSuccessListener { document ->
                Log.e("userHashMap", userHashMap.toString())
                for (i in document) {
                    mFirestoreInstance.collection(Constants.MASTER_ACCOUNT_DETAIL)
                        .document(i.id)
                        .set(userHashMap, SetOptions.merge())
                    when (activity) {
                        is AddAccountActivity -> {
                            activity.updateMasterAmountSuccess()
                        }
                        is MemberDetailedActivity -> {
                            activity.updateMasterAmountSuccess()
                        }
                        is AddExpenseActivity -> {
                            activity.updateMasterAmountSuccess()
                        }
                    }
                }

            }.addOnFailureListener { exception ->
                when (activity) {
                    is MemberDetailedActivity -> {
                        activity.cancelProgressDialog()
                    }
                    is AddAccountActivity -> {
                        activity.cancelProgressDialog()
                    }
                    is AddExpenseActivity -> {
                        activity.cancelProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error updating Member.",
                    exception
                )
            }
    }


    fun updateCurrentAmountExpense(
        activity: Activity, userHashMap: HashMap<String, Any>,
        groupName: String, month: String, adminEmail: String, currentAmount: Int,
        currentYear: Int
    ) {

        mFirestoreInstance.collection(Constants.GROUP_EXPENSE_DETAIL)
            .whereEqualTo("memberAdminEmail", adminEmail)
            .whereEqualTo("groupName", groupName)
            .whereEqualTo("year", currentYear)
            .whereEqualTo("month", month)
            .whereEqualTo("expenseAmount", currentAmount)
            .get()
            .addOnSuccessListener { document ->
                val monthExpenseList: ArrayList<MonthExpense> = ArrayList()
                for (i in document) {
                    mFirestoreInstance.collection(Constants.GROUP_EXPENSE_DETAIL)
                        .document(i.id)
                        .set(userHashMap, SetOptions.merge())
                    val monthExpense = i.toObject(MonthExpense::class.java)
                    monthExpense.id = i.id
                    monthExpenseList.add(monthExpense)

                }
                when (activity) {
                    is AddExpenseActivity -> {
                        activity.lifecycleScope.launch {
                            activity.expenseUpdateSuccess(monthExpenseList)
                        }

                    }
                }
            }.addOnFailureListener { exception ->
                when (activity) {
                    is AddExpenseActivity -> {
                        activity.cancelProgressDialog()
                        Log.e(
                            activity.javaClass.simpleName,
                            "Error updating Member.",
                            exception
                        )
                    }
                }
            }

    }

    fun updateGroupDetail(
        activity: CreateGroupActivity,
        mGroupName: String,
        emailId: String,
        updateGroup: GroupNameClass
    ) {

        val groupList: ArrayList<GroupNameClass> = ArrayList()
        // var group = GroupNameClass()
        mFirestoreInstance.collection(Constants.GROUP)
            .whereEqualTo("groupName", mGroupName)
            .whereEqualTo("email", emailId)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    mFirestoreInstance.collection(Constants.GROUP)
                        .document(i.id)
                        .set(updateGroup, SetOptions.merge())
                    val group = i.toObject(GroupNameClass::class.java)!!
                    group.id = i.id
                    groupList.add(group)
                }
                activity.lifecycleScope.launch {
                    activity.groupUpdateSuccess()
                }


            }.addOnFailureListener { exception ->
                activity.cancelProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error updating group.",
                    exception
                )
            }
    }


    suspend fun updateMemberProfile(
        activity: Activity,
        updateMember: MemberClass,
        mUserGroupName: String,
        mUserAdminEmail: String,
        memberEmail: String

    ) {

        val memberList: ArrayList<MemberClass> = ArrayList()
        var member = MemberClass()
        mFirestoreInstance.collection(Constants.MEMBER)
            .whereEqualTo("groupName", mUserGroupName)
            .whereEqualTo("memberAdminEmail", mUserAdminEmail)
            .whereEqualTo("memberEmail", memberEmail)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    mFirestoreInstance.collection(Constants.MEMBER)
                        .document(i.id)
                        .set(updateMember, SetOptions.merge())
                    member = i.toObject(MemberClass::class.java)!!
                    member.id = i.id

                }
                memberList.add(member)
                when (activity) {
                    is AddMemberActivity -> {
                        activity.lifecycleScope.launch {
                            activity.successUpdatedMemberList(
                                memberList,
                                memberEmail,
                                mUserAdminEmail,
                                mUserGroupName
                            )
                        }

                    }
                    is GuestProfileActivity -> {
                        activity.lifecycleScope.launch {
                            activity.successUpdatedMemberList(
                                memberList,
                                memberEmail,
                                mUserAdminEmail,
                                mUserGroupName
                            )
                        }

                    }
                }
            }.await()
    }


    suspend fun updateMemberAccount(
        activity: Activity,
        memberEmail: String,
        mUserAdminEmail: String,
        mUserGroupName: String,
        memberHashMap: HashMap<String, String>
    ) {

        mFirestoreInstance.collection(Constants.MEMBER_ACCOUNT_DETAIL)
            .whereEqualTo("groupName", mUserGroupName)
            .whereEqualTo("memberEmail", memberEmail)
            .whereEqualTo("adminEmail", mUserAdminEmail)
            .get()
            .addOnSuccessListener { document ->
                val memberAccountList: ArrayList<MemberAccountDetail> = ArrayList()
                for (i in document.documents) {

                    mFirestoreInstance.collection(Constants.MEMBER_ACCOUNT_DETAIL)
                        .document(i.id)
                        .set(memberHashMap, SetOptions.merge())
                    val memberAccount = i.toObject(MemberAccountDetail::class.java)
                    memberAccount!!.id = i.id
                    memberAccountList.add(memberAccount)
                }
                when (activity) {
                    is AddMemberActivity -> {
                        activity.successUpdateMemberAccount()
                    }
                    is GuestProfileActivity -> {
                        activity.lifecycleScope.launch {
                            activity.successUpdateMemberAccount()
                        }

                    }
                }

            }.await()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun updateCurrentAmountMember(
        activity: Activity,
        userHashMap: HashMap<String, Any>,
        groupName: String,
        month: String,
        memberEmail: String,
        memberName: String,
        adminEmail: String
    ) {

        mFirestoreInstance.collection(Constants.MEMBER_ACCOUNT_DETAIL)
            .whereEqualTo("groupName", groupName)
            .whereEqualTo("month", month)
            .whereEqualTo("memberEmail", memberEmail)
            .whereEqualTo("memberName", memberName)
            .whereEqualTo("adminEmail", adminEmail)
            .get()
            .addOnSuccessListener { document ->
                val memberAccountList: ArrayList<MemberAccountDetail> = ArrayList()
                for (i in document) {
                    mFirestoreInstance.collection(Constants.MEMBER_ACCOUNT_DETAIL)
                        .document(i.id)
                        .set(userHashMap, SetOptions.merge())
                    val memberAccount = i.toObject(MemberAccountDetail::class.java)
                    memberAccount.id = i.id
                    memberAccountList.add(memberAccount)

                    when (activity) {
                        is AddAccountActivity -> {
                            activity.cancelProgressDialog()
                            activity.amountUpdateSuccess()
                        }

                        is MemberDetailedActivity -> {
                            activity.lifecycleScope.launch {
                                activity.amountUpdateSuccess(memberAccountList)
                            }

                        }
                    }

                }

            }.addOnFailureListener { e ->
                Log.e("Update failed", e.message.toString())

            }

    }

    fun createExpenseAmountToMasterAccount(
        activity: Activity, groupName: String,
        memberAdminEmail: String,
        hashMapUp: java.util.HashMap<String, Any>,
        month: String,
        currentYear: Int
    ) {
        mFirestoreInstance.collection(Constants.MASTER_ACCOUNT_DETAIL)
            .whereEqualTo("groupName", groupName)
            .whereEqualTo("memberAdminEmail", memberAdminEmail)
            .whereEqualTo("month", month)
            .whereEqualTo("year", currentYear)
            .get()
            .addOnSuccessListener { document ->
                for (i in document) {
                    mFirestoreInstance.collection(Constants.MASTER_ACCOUNT_DETAIL)
                        .document(i.id)
                        .set(hashMapUp, SetOptions.merge())
                }
                when (activity) {
                    is AddExpenseActivity -> {
                        activity.successUpdateToMaster()
                    }
                }

            }.addOnFailureListener { exception ->
                when (activity) {
                    is AddExpenseActivity -> {
                        activity.cancelProgressDialog()
                        Log.e(
                            activity.javaClass.simpleName,
                            "Error updating master.",
                            exception
                        )
                    }
                }
            }
    }

    suspend fun checkAccountAvailableFromFirestore(
        activity: MemberDetailedActivity, mUserAdminEmail: String, groupName: String,
        memberEmail: String,
    ): ArrayList<String> {
        val memberAccountList: ArrayList<String> = ArrayList()
        mFirestoreInstance.collection(Constants.MEMBER_ACCOUNT_DETAIL)
            .whereEqualTo("groupName", groupName)
            .whereEqualTo("memberEmail", memberEmail)
            .whereEqualTo("adminEmail", mUserAdminEmail)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    val memberAccount = i.toObject(MemberAccountDetail::class.java)
                    memberAccount!!.id = i.id
                    memberAccountList.add(memberAccount.id)

                }

            }.await()
        return memberAccountList
    }

    suspend fun checkMemberEmailAvailableFirestore(
        email: String,
        adminEmail: String,
        mGroupName: String
    ): ArrayList<MemberClass> {
        val memberList: ArrayList<MemberClass> = ArrayList()
        var member: MemberClass
        mFirestoreInstance.collection(Constants.MEMBER)
            .whereEqualTo("memberAdminEmail", adminEmail)
            .whereEqualTo("memberEmail", email)
            .whereEqualTo("groupName", mGroupName)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    member = i.toObject(MemberClass::class.java)!!
                    member.id = i.id
                    memberList.add(member)
                }

            }.await()

        return memberList
    }

    fun checkCurrentEmailMemberAvailableFirestore(
        activity: Activity,
        email: String,
        adminEmail: String
    ) {
        val memberList: ArrayList<MemberClass> = ArrayList()
        var member: MemberClass
        mFirestoreInstance.collection(Constants.MEMBER)
            .whereEqualTo("memberAdminEmail", adminEmail)
            .whereEqualTo("memberEmail", email)
            .get()
            .addOnSuccessListener { document ->

                if (document.size() == 0) {
                    when (activity) {
                        is LoginActivity -> {
                            activity.emailNotAvailable()
                        }
                    }
                } else {
                    for (i in document.documents) {
                        member = i.toObject(MemberClass::class.java)!!
                        member.id = i.id
                        memberList.add(member)

                        when (activity) {
                            is LoginActivity -> {
                                activity.userAvailableInMember(memberList)
                            }
                        }
                    }
                }

            }.addOnFailureListener { exception ->
                when (activity) {
                    is LoginActivity -> {
                        activity.cancelProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while checking Member.",
                    exception
                )
            }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun checkMonthAvailableForThisMember(
        addAccountActivity: AddAccountActivity, groupName: String,
        memberName: String, memberEmail: String, month: String
    ) {

        mFirestoreInstance.collection(Constants.MEMBER_ACCOUNT_DETAIL)
            .whereEqualTo("groupName", groupName)
            .whereEqualTo("month", month)
            .whereEqualTo("memberEmail", memberEmail)
            .whereEqualTo("memberName", memberName)
            .get()
            .addOnSuccessListener { document ->

                if (document.size() == 0) {
                    addAccountActivity.createMemberAccount()
                } else {
                    val memberAccountList: ArrayList<MemberAccountDetail> = ArrayList()
                    for (i in document.documents) {
                        val memberAccount = i.toObject(MemberAccountDetail::class.java)
                        memberAccount!!.id = i.id
                        val amount = memberAccount.currentAmount
                        memberAccountList.add(memberAccount)
                        addAccountActivity.updateMemberMonthAmount(amount)
                    }

                }

            }.addOnFailureListener { exception ->
                addAccountActivity.cancelProgressDialog()
                Log.e(
                    addAccountActivity.javaClass.simpleName,
                    "Error while checking Member.",
                    exception
                )
            }
    }

    suspend fun getAdminCodeFromUserFireStore(email: String): String {
        var user: UserClass = UserClass()
        mFirestoreInstance.collection(Constants.USER)
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    user = i.toObject(UserClass::class.java)!!
                    user.code = i.get("code").toString()
                }
            }.await()
        return user.code
    }

    fun getAdminEnableMemberFirestore(email: String): Int {
        var memberClass: MemberClass = MemberClass()
        mFirestoreInstance.collection(Constants.MEMBER)
            .whereEqualTo("memberEmail", email)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    memberClass = i.toObject(MemberClass::class.java)!!
                    memberClass.adminEnableCode = i.get("adminEnableCode").toString().toInt()
                }

            }
        return memberClass.adminEnableCode
    }

    suspend fun getAdminEmailFromMemberFireStore(email: String): String {
        var memberClass: MemberClass = MemberClass()
        mFirestoreInstance.collection(Constants.MEMBER)
            .whereEqualTo("memberEmail", email)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    memberClass = i.toObject(MemberClass::class.java)!!
                    memberClass.memberAdminEmail = i.get("memberAdminEmail").toString()

                }

            }.await()
        return memberClass.memberAdminEmail
    }

    suspend fun getCurrentUpdatedMemberDetail(memberEmail: String, mGroupName: String):
            ArrayList<MemberClass> {
        val memberList: ArrayList<MemberClass> = ArrayList()
        var memberAccount: MemberClass
        mFirestoreInstance.collection(Constants.MEMBER)
            .whereEqualTo("groupName", mGroupName)
            .whereEqualTo("memberEmail", memberEmail)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    memberAccount = i.toObject(MemberClass::class.java)!!
                    memberAccount.id = i.id
                    memberList.add(memberAccount)
                }

            }.await()

        return memberList
    }

    fun getCurrentGuestMemberDetailFromFirestore(
        activity: Activity,
        memberEmail: String?,
        mCurrentGroupName: String
    ) {
        mFirestoreInstance.collection(Constants.MEMBER)
            .whereEqualTo("groupName", mCurrentGroupName)
            .whereEqualTo("memberEmail", memberEmail)
            .get()
            .addOnSuccessListener { document ->
                val memberList: ArrayList<MemberClass> = ArrayList()
                for (i in document.documents) {
                    val member = i.toObject(MemberClass::class.java)
                    member!!.id = i.id
                    memberList.add(member)
                }
                when (activity) {
                    is GuestProfileActivity -> {
                        activity.successGettingMemberList(memberList)
                    }
                }

            }.addOnFailureListener { exception ->
                when (activity) {
                    is GuestProfileActivity -> {
                        activity.cancelProgressDialog()
                        Log.e(
                            activity.javaClass.simpleName,
                            "Error while getting Member.",
                            exception
                        )
                    }
                }
            }
    }

    fun getDefaultMemberAccountFromFirestore(mainActivity: MainActivity, memberEmail: String?) {
        mFirestoreInstance.collection(Constants.MEMBER)
            .whereEqualTo("memberEmail", memberEmail)
            .get()
            .addOnSuccessListener { document ->
                val memberList: ArrayList<MemberClass> = ArrayList()
                for (i in document.documents) {
                    val member = i.toObject(MemberClass::class.java)
                    member!!.id = i.id
                    memberList.add(member)
                }
                mainActivity.successMemberList(memberList)

            }.addOnFailureListener { exception ->
                Log.e("E", exception.toString())
            }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getMemberEmailFromFireStore(
        activity: Activity,
        adminEmail: String, groupName: String, memberName: String
    ) {
        mFirestoreInstance.collection(Constants.MEMBER)
            .whereEqualTo("memberAdminEmail", adminEmail)
            .whereEqualTo("groupName", groupName)
            .whereEqualTo("memberName", memberName)
            .get()
            .addOnSuccessListener { document ->
                val memberList: ArrayList<MemberClass> = ArrayList()
                for (i in document.documents) {
                    val member = i.toObject(MemberClass::class.java)
                    member!!.id = i.id
                    memberList.add(member)
                }
                when (activity) {
                    is AddAccountActivity -> {
                        activity.successMemberEmail(memberList)
                    }
                }

            }.addOnFailureListener { exception ->
                when (activity) {
                    is AddAccountActivity -> {
                        activity.cancelProgressDialog()
                    }
                }
                Log.e("E", exception.toString())
            }
    }

    suspend fun getMasterImageFromGroupFirestore(
        activity: Activity,
        mGroupName: String, mAdminEmail: String
    ): String {
        var imageUrl: String = ""
        var group: GroupNameClass
        mFirestoreInstance.collection(Constants.GROUP)
            .whereEqualTo("email", mAdminEmail)
            .whereEqualTo("groupName", mGroupName)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    group = i.toObject(GroupNameClass::class.java)!!
                    group.groupImage = i.get("groupImage").toString()
                    imageUrl = group.groupImage
                }
            }.await()
        return imageUrl
    }

    suspend fun getGroupDate(mAdmin: String, mGroupName: String): String {
        var groupOrgDate: String = ""

        mFirestoreInstance.collection(Constants.GROUP)
            .whereEqualTo("groupName", mGroupName)
            .whereEqualTo("email", mAdmin)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    groupOrgDate = i.get("groupCreatedDate").toString()
                }

            }.await()
        return groupOrgDate
    }


    suspend fun getGroupImageFromFirestore(
        currentGroupNameSelect: String,
        adminEmail: String
    ): String {
        val groupList: ArrayList<String> = ArrayList()
        var group: GroupNameClass
        var imageString: String = ""

        mFirestoreInstance.collection(Constants.GROUP)
            //.whereEqualTo("id", getCurrentUserID())
            .whereEqualTo("groupName", currentGroupNameSelect)
            .whereEqualTo("email", adminEmail)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    group = i.toObject(GroupNameClass::class.java)!!
                    imageString = i.get("groupImage").toString()
                    //group.id = i.id

                }
            }.await()
        return imageString
    }

    suspend fun getMasterAccountMonth(
        activity: Activity,
        mGroupName: String,
        adminEmailId: String,
        currentYear: Int
    ): ArrayList<String> {
        val masterMonthList: ArrayList<String> = ArrayList()
        var masterAccount: MasterAccountDetail
        mFirestoreInstance.collection(Constants.MASTER_ACCOUNT_DETAIL)
            .whereEqualTo("groupName", mGroupName)
            .whereEqualTo("memberAdminEmail", adminEmailId)
            .whereEqualTo("year", currentYear)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    masterAccount = i.toObject(MasterAccountDetail::class.java)!!
                    masterAccount.month = i.get("month").toString()
                    masterMonthList.add(masterAccount.month)
                }

            }.await()
        return masterMonthList
    }

    fun getAllMasterAccountFromFirestore(
        activity: Activity,
        groupName: String, adminEmailId: String?, currentYear: Int
    ) {

        val masterAccountList: ArrayList<MasterAccountDetail> = ArrayList()
        var masterAccount: MasterAccountDetail
        mFirestoreInstance.collection(Constants.MASTER_ACCOUNT_DETAIL)
            .whereEqualTo("groupName", groupName)
            .whereEqualTo("memberAdminEmail", adminEmailId)
            .whereEqualTo("year", currentYear)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    masterAccount = i.toObject(MasterAccountDetail::class.java)!!
                    masterAccount.id = i.id
                    masterAccountList.add(masterAccount)
                }
                when (activity) {
                    is ViewAccountActivity -> {
                        activity.lifecycleScope.launch {
                            activity.successMasterList(masterAccountList)
                        }
                    }
                    is GuestViewAccountActivity -> {
                        activity.lifecycleScope.launch {
                            activity.successMasterList(masterAccountList)
                        }

                    }
                }

            }.addOnFailureListener { exception ->
                when (activity) {
                    is ViewAccountActivity -> {
                        activity.cancelProgressDialog()
                        Log.e(
                            activity.javaClass.simpleName,
                            "Error while master detail.",
                            exception
                        )
                    }
                    is GuestViewAccountActivity -> {
                        activity.cancelProgressDialog()
                        Log.e(
                            activity.javaClass.simpleName,
                            "Error while master detail.",
                            exception
                        )
                    }
                }
            }
    }

    suspend fun getAllMasterAccount(
        groupName: String,
        adminEmailId: String?,
        month: String
    ):
            ArrayList<MasterAccountDetail> {
        val masterAccountList: ArrayList<MasterAccountDetail> = ArrayList()
        var masterAccount: MasterAccountDetail

        mFirestoreInstance.collection(Constants.MASTER_ACCOUNT_DETAIL)
            .whereEqualTo("groupName", groupName)
            .whereEqualTo("memberAdminEmail", adminEmailId)
            .whereEqualTo("month", month)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    masterAccount = i.toObject(MasterAccountDetail::class.java)!!
                    masterAccount.id = i.id
                    masterAccountList.add(masterAccount)
                }
            }.await()


        return masterAccountList
    }

    suspend fun allAvailableMonthList(groupName: String, getAdminEmailId: String?):
            ArrayList<MemberAccountDetail> {
        val monthList: ArrayList<MemberAccountDetail> = ArrayList()
        var month: MemberAccountDetail
        mFirestoreInstance.collection(Constants.MEMBER_ACCOUNT_DETAIL)
            .whereEqualTo("groupName", groupName)
            .whereEqualTo("adminEmail", getAdminEmailId)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    month = i.toObject(MemberAccountDetail::class.java)!!
                    month.id = i.id
                    monthList.add(month)
                }
            }.await()
        return monthList
    }

    suspend fun masterAccountAmountForCurrentMonthFireStore(
        grpName: String,
        adminEmailId: String?,
        month: String,
        currentYear: Int

    ):
            ArrayList<Int> {
        val masterAccountList: ArrayList<Int> = ArrayList()
        var masterAccount: MasterAccountDetail
        mFirestoreInstance.collection(Constants.MASTER_ACCOUNT_DETAIL)
            .whereEqualTo("groupName", grpName)
            .whereEqualTo("memberAdminEmail", adminEmailId)
            .whereEqualTo("month", month)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    masterAccount = i.toObject(MasterAccountDetail::class.java)!!
                    masterAccount.income = i.get("income").toString().toInt()
                    masterAccountList.add(masterAccount.income)
                }
            }.await()
        return masterAccountList
    }

    suspend fun checkMasterAccountForSameMonth(
        grpName: String,
        adminEmailId: String?,
        month: String
    ):
            ArrayList<MasterAccountDetail> {
        val masterAccountList: ArrayList<MasterAccountDetail> = ArrayList()
        var masterAccount: MasterAccountDetail
        mFirestoreInstance.collection(Constants.MASTER_ACCOUNT_DETAIL)
            .whereEqualTo("groupName", grpName)
            .whereEqualTo("memberAdminEmail", adminEmailId)
            .whereEqualTo("month", month)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    masterAccount = i.toObject(MasterAccountDetail::class.java)!!
                    masterAccount.id = i.id
                    masterAccountList.add(masterAccount)
                }
            }.await()
        return masterAccountList
    }

    suspend fun getFullTotalAmount(
        mGroupName: String,
        mAdminEmail: String,
        currentYear: Int
    ): ArrayList<Int> {
        val amountList: ArrayList<Int> = ArrayList()
        var masterClass: MasterAccountDetail
        mFirestoreInstance.collection(Constants.MASTER_ACCOUNT_DETAIL)
            .whereEqualTo("memberAdminEmail", mAdminEmail)
            .whereEqualTo("groupName", mGroupName)
            .whereEqualTo("year", currentYear)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    masterClass = i.toObject(MasterAccountDetail::class.java)!!
                    masterClass.income = i.get("income").toString().toInt()
                    amountList.add(masterClass.income)
                }
            }.await()
        return amountList
    }

    suspend fun getFullExpenseAmount(
        mGroupName: String,
        mAdminEmail: String,
        currentYear: Int
    ): ArrayList<Int> {
        val amountList: ArrayList<Int> = ArrayList()
        var masterClass: MasterAccountDetail
        mFirestoreInstance.collection(Constants.MASTER_ACCOUNT_DETAIL)
            .whereEqualTo("memberAdminEmail", mAdminEmail)
            .whereEqualTo("groupName", mGroupName)
            .whereEqualTo("year", currentYear)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    masterClass = i.toObject(MasterAccountDetail::class.java)!!
                    masterClass.expenseAmount = i.get("expenseAmount").toString().toInt()
                    amountList.add(masterClass.expenseAmount)
                }
            }.await()
        return amountList
    }

    suspend fun memberAccountTotalAmountFirestore(
        mGroupName: String,
        mAdmin: String,
        mMemberEmail: String
    ): ArrayList<Int> {
        val memberAccountList: ArrayList<Int> = ArrayList()
        var memberAccount: MemberAccountDetail

        mFirestoreInstance.collection(Constants.MEMBER_ACCOUNT_DETAIL)
            .whereEqualTo("groupName", mGroupName)
            .whereEqualTo("adminEmail", mAdmin)
            .whereEqualTo("memberEmail", mMemberEmail)
            .get()
            .addOnSuccessListener { document ->
                for (it in document.documents) {
                    memberAccount = it.toObject(MemberAccountDetail::class.java)!!
                    memberAccount.currentAmount = it.get("currentAmount").toString().toInt()
                    memberAccountList.add(memberAccount.currentAmount)
                }
            }.await()
        return memberAccountList
    }

    suspend fun memberAccountAmountForCurrentMonthFirestore(
        month: String,
        mGroupName: String,
        adminEmailId: String,
        currentYear: Int
    ): ArrayList<Int> {

        val memberAccountList: ArrayList<Int> = ArrayList()
        var memberAccount: MemberAccountDetail

        mFirestoreInstance.collection(Constants.MEMBER_ACCOUNT_DETAIL)
            .whereEqualTo("groupName", mGroupName)
            .whereEqualTo("adminEmail", adminEmailId)
            .whereEqualTo("month", month)
            .whereEqualTo("year", currentYear.toString())
            .get()
            .addOnSuccessListener { document ->
                for (it in document.documents) {
                    memberAccount = it.toObject(MemberAccountDetail::class.java)!!
                    memberAccount.currentAmount = it.get("currentAmount").toString().toInt()
                    memberAccountList.add(memberAccount.currentAmount)

                }
            }.await()
        return memberAccountList
    }

    fun monthWiseMemberAccountDetail(
        activity: Activity,
        mGroupName: String,
        mMonthName: String,
        mAdminEmail: String,
        currentYear: Int
    ) {
        val memberAccountList: ArrayList<MemberAccountDetail> = ArrayList()
        var memberAccount: MemberAccountDetail
        mFirestoreInstance.collection(Constants.MEMBER_ACCOUNT_DETAIL)
            .whereEqualTo("groupName", mGroupName)
            .whereEqualTo("adminEmail", mAdminEmail)
            .whereEqualTo("month", mMonthName)
            .whereEqualTo("year", currentYear.toString())
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    memberAccount = i.toObject(MemberAccountDetail::class.java)!!
                    memberAccount.id = i.id
                    memberAccountList.add(memberAccount)
                }
                when (activity) {
                    is AccountMonthWiseDetailedActivity -> {
                        activity.lifecycleScope.launch {
                            activity.successMemberList(memberAccountList)
                        }
                    }
                    is MemberNotPaidListActivity -> {
                        activity.successMemberList(memberAccountList)
                    }
                }

            }.addOnFailureListener { exception ->
                when (activity) {
                    is AccountMonthWiseDetailedActivity -> {
                        activity.cancelProgressDialog()
                        Log.e(
                            activity.javaClass.simpleName,
                            "Error while month wise detail.",
                            exception
                        )
                    }
                    is MemberNotPaidListActivity -> {
                        activity.cancelProgressDialog()
                        Log.e(
                            activity.javaClass.simpleName,
                            "Error while month wise detail.",
                            exception
                        )
                    }
                }
            }

    }

    suspend fun memberAccountMonthCheckFromFirestore(
        month: String,
        mGroupName: String,
        adminEmailId: String,
        currentYear: Int
    ): ArrayList<String> {

        val memberAccountList: ArrayList<String> = ArrayList()
        var memberAccount: MemberAccountDetail

        mFirestoreInstance.collection(Constants.MEMBER_ACCOUNT_DETAIL)
            .whereEqualTo("groupName", mGroupName)
            .whereEqualTo("adminEmail", adminEmailId)
            .whereEqualTo("month", month)
            .whereEqualTo("year", currentYear.toString())
            .get()
            .addOnSuccessListener { document ->
                for (it in document.documents) {
                    memberAccount = it.toObject(MemberAccountDetail::class.java)!!
                    memberAccount.month = it.get("month").toString()
                    memberAccountList.add(memberAccount.month)

                }
            }.await()
        return memberAccountList
    }

    suspend fun checkAmountMasterAccountForSameMonth(
        grpName: String,
        adminEmailId: String?,
        month: String,
        currentYear: String
    ):
            ArrayList<Int> {
        val masterAccountList: ArrayList<Int> = ArrayList()
        var masterAccount: MemberAccountDetail
        mFirestoreInstance.collection(Constants.MEMBER_ACCOUNT_DETAIL)
            .whereEqualTo("groupName", grpName)
            .whereEqualTo("adminEmail", adminEmailId)
            .whereEqualTo("month", month)
            .whereEqualTo("year", currentYear)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    masterAccount = i.toObject(MemberAccountDetail::class.java)!!
                    masterAccount.currentAmount = i.get("currentAmount").toString().toInt()
                    masterAccountList.add(masterAccount.currentAmount)
                }
            }.await()
        return masterAccountList
    }

    suspend fun getAllExpenseDetailForCurrentMonth(
        memberAdminEmail: String, month: String,
        groupName: String, year: Int
    ): ArrayList<Int> {
        val expenseList: ArrayList<Int> = ArrayList()
        var expenseClass: MonthExpense
        mFirestoreInstance.collection(Constants.GROUP_EXPENSE_DETAIL)
            .whereEqualTo("memberAdminEmail", memberAdminEmail)
            .whereEqualTo("groupName", groupName)
            .whereEqualTo("month", month)
            .whereEqualTo("year", year)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    expenseClass = i.toObject(MonthExpense::class.java)!!
                    expenseClass.expenseAmount = i.get("expenseAmount").toString().toInt()
                    expenseList.add(expenseClass.expenseAmount)
                }
            }.await()
        return expenseList
    }


    suspend fun getMemberAccountFromFirestore(
        mUserAdminEmail: String,
        mUserGroupName: String,
        mUserMemberEmail: String
    ): ArrayList<String> {
        val memberList: ArrayList<String> = ArrayList()
        var monthClass: MemberAccountDetail

        mFirestoreInstance.collection(Constants.MEMBER_ACCOUNT_DETAIL)
            .whereEqualTo("adminEmail", mUserAdminEmail)
            .whereEqualTo("groupName", mUserGroupName)
            .whereEqualTo("memberEmail", mUserMemberEmail)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    monthClass = i.toObject(MemberAccountDetail::class.java)!!
                    monthClass.memberEmail = i.get("memberEmail").toString()
                    memberList.add(monthClass.memberEmail)
                }
            }.await()

        return memberList
    }

    suspend fun beforeDeleteGetMemberAccountDetailFromFirestore(
        mUserAdminEmail: String,
        mUserGroupName: String
    ): ArrayList<MemberAccountDetail> {
        val memberList: ArrayList<MemberAccountDetail> = ArrayList()
        var monthClass: MemberAccountDetail

        mFirestoreInstance.collection(Constants.MEMBER_ACCOUNT_DETAIL)
            .whereEqualTo("adminEmail", mUserAdminEmail)
            .whereEqualTo("groupName", mUserGroupName)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    monthClass = i.toObject(MemberAccountDetail::class.java)!!
                    monthClass.id = i.id
                    memberList.add(monthClass)
                }
            }.await()

        return memberList
    }

    suspend fun checkMasterGroupAvailableInFirestore(
        emailId: String?,
        mainGroupOldName: String
    ): ArrayList<String> {
        val masterAccountList: ArrayList<String> = ArrayList()
        var masterClass: MasterAccountDetail
        mFirestoreInstance.collection(Constants.MASTER_ACCOUNT_DETAIL)
            .whereEqualTo("memberAdminEmail", emailId.toString())
            .whereEqualTo("groupName", mainGroupOldName)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    masterClass = i.toObject(MasterAccountDetail::class.java)!!
                    masterClass.groupName = i.get("groupName").toString()
                    masterAccountList.add(masterClass.groupName)
                }
            }.await()
        return masterAccountList
    }

    suspend fun checkExpenseGroupAvailableInFirestore(
        emailId: String?,
        mainGroupOldName: String
    ): ArrayList<String> {
        val expenseAccountList: ArrayList<String> = ArrayList()
        var expenseClass: MonthExpense
        mFirestoreInstance.collection(Constants.GROUP_EXPENSE_DETAIL)
            .whereEqualTo("memberAdminEmail", emailId.toString())
            .whereEqualTo("groupName", mainGroupOldName)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    expenseClass = i.toObject(MonthExpense::class.java)!!
                    expenseClass.groupName = i.get("groupName").toString()
                    expenseAccountList.add(expenseClass.groupName)
                }
            }.await()
        return expenseAccountList
    }

    suspend fun checkGroupAvailableinMemberAccountFirestore(
        emailId: String?,
        mainGroupOldName: String
    ): ArrayList<String> {
        val memberAccountList: ArrayList<String> = ArrayList()
        var memberAccountClass: MemberAccountDetail
        mFirestoreInstance.collection(Constants.MEMBER_ACCOUNT_DETAIL)
            .whereEqualTo("adminEmail", emailId.toString())
            .whereEqualTo("groupName", mainGroupOldName)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    memberAccountClass = i.toObject(MemberAccountDetail::class.java)!!
                    memberAccountClass.groupName = i.get("groupName").toString()
                    memberAccountList.add(memberAccountClass.groupName)
                }
            }.await()
        return memberAccountList
    }

    suspend fun checkGroupAvailableinMemberFirestore(
        emailId: String?,
        mainGroupOldName: String
    ): ArrayList<String> {
        val memberList: ArrayList<String> = ArrayList()
        var memberClass: MemberClass
        mFirestoreInstance.collection(Constants.MEMBER)
            .whereEqualTo("memberAdminEmail", emailId.toString())
            .whereEqualTo("groupName", mainGroupOldName)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    memberClass = i.toObject(MemberClass::class.java)!!
                    memberClass.groupName = i.get("groupName").toString()
                    memberList.add(memberClass.groupName)
                }
            }.await()
        return memberList
    }

    suspend fun getTotalAmount(
        groupName: String, getAdminEmailId: String?,
        month: String
    ):
            ArrayList<Int> {
        val monthList: ArrayList<Int> = ArrayList()
        var monthClass: MemberAccountDetail
        mFirestoreInstance.collection(Constants.MEMBER_ACCOUNT_DETAIL)
            .whereEqualTo("groupName", groupName)
            .whereEqualTo("adminEmail", getAdminEmailId)
            .whereEqualTo("month", month)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    monthClass = i.toObject(MemberAccountDetail::class.java)!!
                    monthClass.currentAmount = i.get("currentAmount").toString().toInt()
                    monthList.add(monthClass.currentAmount)
                }
            }.await()
        return monthList
    }

    fun guestGetIncomeDetailFromFirestore(
        requireActivity: Activity,
        mGroupName: String,
        mAdmin: String,
        mMemberEmail: String,
        currentYear: Int
    ) {
        val monthList: ArrayList<MemberAccountDetail> = ArrayList()
        var monthClass: MemberAccountDetail
        mFirestoreInstance.collection(Constants.MEMBER_ACCOUNT_DETAIL)
            .whereEqualTo("groupName", mGroupName)
            .whereEqualTo("adminEmail", mAdmin)
            .whereEqualTo("memberEmail", mMemberEmail)
            .whereEqualTo("year", currentYear.toString())
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    monthClass = i.toObject(MemberAccountDetail::class.java)!!
                    monthClass.id = i.id
                    monthList.add(monthClass)
                }
                when (requireActivity) {
                    is GuestMemberTransactionDetailActivity -> {
                        requireActivity.successMemberAccount(monthList)
                    }
                }

            }.addOnFailureListener { exception ->
                when (requireActivity) {
                    is GuestMemberTransactionDetailActivity -> {
                        requireActivity.cancelProgressDialog()
                    }
                }
                Log.e(
                    requireActivity.javaClass.simpleName,
                    "Error while getting income detail.",
                    exception
                )
            }
    }

    fun getIncomeDetailFromFirestore(
        activity: Activity, mUserGroupName: String,
        mUserAdminEmail: String,
        mUserMemberEmail: String, mUserMemberName: String, currentYear: Int
    ) {

        val monthList: ArrayList<MemberAccountDetail> = ArrayList()
        var monthClass: MemberAccountDetail
        mFirestoreInstance.collection(Constants.MEMBER_ACCOUNT_DETAIL)
            .whereEqualTo("groupName", mUserGroupName)
            .whereEqualTo("adminEmail", mUserAdminEmail)
            .whereEqualTo("memberEmail", mUserMemberEmail)
            //.whereEqualTo("memberName", mUserMemberName)
            .whereEqualTo("year", currentYear.toString())
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    monthClass = i.toObject(MemberAccountDetail::class.java)!!
                    monthClass.id = i.id
                    monthList.add(monthClass)
                }
                when (activity) {
                    is MemberDetailedActivity -> {
                        activity.successIncomeDetail(monthList)
                    }
                    is GuestAllMemberDetailedActivity -> {
                        activity.successIncomeDetail(monthList)
                    }
                }
            }.addOnFailureListener { exception ->
                when (activity) {
                    is MemberDetailedActivity -> {
                        activity.cancelProgressDialog()
                        Log.e(
                            activity.javaClass.simpleName,
                            "Error while getting income detail.",
                            exception
                        )
                    }
                }
            }

    }

    suspend fun checkPreviousAmountForCurrentMonthInMaster(
        groupName: String,
        memberAdminEmail: String, month: String,
        year: Int
    ): Int {
        var amount: Int = 0
        mFirestoreInstance.collection(Constants.MASTER_ACCOUNT_DETAIL)
            .whereEqualTo("memberAdminEmail", memberAdminEmail)
            .whereEqualTo("groupName", groupName)
            .whereEqualTo("year", year)
            .whereEqualTo("month", month)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    val value = i.get("monthExpense").toString()
                    if ((value.isNotEmpty() && value != "null")) {
                        val expAmount = i.get("expenseAmount").toString()
                        amount = expAmount.toInt()
                    } else {
                        amount = 0
                    }
                }
            }.await()
        return amount
    }


    suspend fun checkPreviousAmountBalanceFromGroup(
        groupName: String, memberAdminEmail: String,
        year: Int
    ): Int {
        var amount: Int = 0

        mFirestoreInstance.collection(Constants.GROUP)
            .whereEqualTo("groupName", groupName)
            .whereEqualTo("email", memberAdminEmail)
            //.whereEqualTo("year", year)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    val value = i.get("groupPreviousBalance").toString()
                    if ((value.isNotEmpty() && value != "null")) {
                        amount = value.toInt()
                    } else {
                        amount = 0
                    }
                }
            }.await()
        return amount
    }

    fun getAdminDetail(activity: Activity, id: String) {
        val userList: ArrayList<UserClass> = ArrayList()
        mFirestoreInstance.collection(Constants.USER)
            .whereEqualTo("id", id)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    val group = i.toObject(UserClass::class.java)
                    group!!.id = i.id
                    userList.add(group)
                }
                when (activity) {
                    is AdminScreenActivity -> {
                        activity.successAdminUserList(userList)
                    }
                }
            }.addOnFailureListener { e ->
                when (activity) {
                    is AdminScreenActivity -> {
                        activity.cancelProgressDialog()
                        e.printStackTrace()
                    }
                }
            }
    }

    fun getAssignedGroupAdmin(activity: Activity, id: String) {
        mFirestoreInstance.collection(Constants.USER)
            .whereEqualTo("id", id)
            .get()
            .addOnSuccessListener { document ->
                var adminEmail: String = ""
                for (i in document.documents) {
                    val group = i.toObject(UserClass::class.java)
                    group!!.adminEmail = i.get("adminEmail").toString()
                    adminEmail = group.adminEmail
                }
                when (activity) {
                    is AdminScreenActivity -> {
                        activity.successAdminList(adminEmail)
                    }

                }
            }.addOnFailureListener { exception ->
                when (activity) {
                    is AdminScreenActivity -> {
                        Log.e(
                            activity.javaClass.simpleName,
                            "Error while checking Member.",
                            exception
                        )
                    }
                }
            }
    }

    fun getGroupAdmin(activity: Activity, adminEmail: String) {
        mFirestoreInstance.collection(Constants.USER)
            .whereEqualTo("email", adminEmail)
            .get()
            .addOnSuccessListener { document ->
                val groupList: ArrayList<UserClass> = ArrayList()
                for (i in document.documents) {
                    val group = i.toObject(UserClass::class.java)
                    group!!.id = i.id
                    groupList.add(group)
                }
                when (activity) {
                    is ViewMemberAccountActivity -> {
                        activity.successAdminList(groupList)
                    }
                    is GuestAllMemberListDetailActivity -> {
                        activity.successAdminList(groupList)
                    }

                }
            }.addOnFailureListener { exception ->
                when (activity) {
                    is ViewMemberAccountActivity -> {
                        Log.e(
                            activity.javaClass.simpleName,
                            "Error while checking Member.",
                            exception
                        )
                    }
                }
            }
    }

    fun getDeleteItemDocumentIDGroupExpenseDetail(
        activity: Activity,
        groupName: String,
        memberAdminEmail: String,
        month: String,
        expenseAmount: Int,
        year: Int
    ) {
        mFirestoreInstance.collection(Constants.GROUP_EXPENSE_DETAIL)
            .whereEqualTo("expenseAmount", expenseAmount)
            .whereEqualTo("groupName", groupName)
            .whereEqualTo("memberAdminEmail", memberAdminEmail)
            .whereEqualTo("month", month)
            .whereEqualTo("year", year)
            .get()
            .addOnSuccessListener { document ->
                val expenseList: ArrayList<MonthExpense> = ArrayList()
                for (i in document.documents) {
                    val monthExpense = i.toObject(MonthExpense::class.java)
                    monthExpense!!.id = i.id
                    expenseList.add(monthExpense)
                }
                when (activity) {
                    is AddExpenseActivity -> {
                        activity.showProgressDialog()
                        activity.successGettingDocumentID(expenseList, expenseAmount, month)
                    }
                }

            }.addOnFailureListener { exception ->
                when (activity) {
                    is AddExpenseActivity -> {
                        Log.e(
                            activity.javaClass.simpleName,
                            "Error while deleting month.",
                            exception
                        )
                    }
                }
            }
    }

    fun getDeleteItemDocumentIDMemberAccountDetail(
        activity: Activity,
        month: String,
        groupName: String,
        adminEmail: String,
        memberEmail: String,
        year: String,
        currentAmount: Int
    ) {

        mFirestoreInstance.collection(Constants.MEMBER_ACCOUNT_DETAIL)
            .whereEqualTo("groupName", groupName)
            .whereEqualTo("adminEmail", adminEmail)
            .whereEqualTo("memberEmail", memberEmail)
            .whereEqualTo("month", month)
            .whereEqualTo("year", year)
            .get()
            .addOnSuccessListener { document ->
                val memberList: ArrayList<MemberAccountDetail> = ArrayList()
                for (i in document.documents) {
                    val account = i.toObject(MemberAccountDetail::class.java)
                    account!!.id = i.id
                    memberList.add(account)
                }
                when (activity) {

                    is MemberDetailedActivity -> {
                        activity.showProgressDialog()
                        activity.successGettingDocumentID(memberList, currentAmount, month)
                    }
                }

            }.addOnFailureListener { exception ->
                when (activity) {
                    is MemberDetailedActivity -> {
                        Log.e(
                            activity.javaClass.simpleName,
                            "Error while deleting month.",
                            exception
                        )
                    }
                }
            }
    }

    fun deleteMasterAboutGroup(email: String, groupName: String) {
        mFirestoreInstance.collection(Constants.ABOUT_GROUP)
            .whereEqualTo("groupName", groupName)
            .whereEqualTo("adminEmail", email)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    mFirestoreInstance.collection(Constants.ABOUT_GROUP)
                        .document(i.id)
                        .delete()
                }
            }

    }

    fun deleteAboutGroup(activity: AdminAboutGroupActivity, id: String) {
        mFirestoreInstance.collection(Constants.ABOUT_GROUP)
            .document(id)
            .delete()
            .addOnSuccessListener {
                activity.deletionSuccess()
            }.addOnFailureListener { exception ->
                activity.cancelProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while deleting about group.",
                    exception
                )
            }
    }

    fun deleteCurrentTimeline(activity: Activity, id: String) {
        mFirestoreInstance.collection(Constants.TIMELINE_DETAIL)
            .document(id)
            .delete()
            .addOnSuccessListener {
                when (activity) {
                    is AddTimelineActivity -> {
                        activity.deletionSuccess()
                    }
                }

            }.addOnFailureListener { exception ->
                when (activity) {
                    is AddTimelineActivity -> {
                        activity.cancelProgressDialog()
                        Log.e(
                            activity.javaClass.simpleName,
                            "Error while deleting timeline.",
                            exception
                        )
                    }
                }
            }

    }


    fun deleteExpenseMonthWise(
        activity: Activity,
        documentId: String,
        expenseAmount: Int,
        month: String
    ) {
        mFirestoreInstance.collection(Constants.GROUP_EXPENSE_DETAIL)
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                when (activity) {
                    is AddExpenseActivity -> {
                        activity.lifecycleScope.launch {
                            activity.deletionSuccess(expenseAmount, month)
                        }

                    }
                }
            }.addOnFailureListener { exception ->
                when (activity) {
                    is AddExpenseActivity -> {
                        activity.cancelProgressDialog()
                    }
                }
            }
    }

    suspend fun deleteMasterAccount(email: String, groupName: String) {
        mFirestoreInstance.collection(Constants.MASTER_ACCOUNT_DETAIL)
            .whereEqualTo("groupName", groupName)
            .whereEqualTo("memberAdminEmail", email)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    mFirestoreInstance.collection(Constants.MASTER_ACCOUNT_DETAIL)
                        .document(i.id)
                        .delete()
                }

            }.await()
    }

    suspend fun deleteExpenseAccount(email: String, groupName: String) {
        mFirestoreInstance.collection(Constants.GROUP_EXPENSE_DETAIL)
            .whereEqualTo("groupName", groupName)
            .whereEqualTo("memberAdminEmail", email)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    mFirestoreInstance.collection(Constants.GROUP_EXPENSE_DETAIL)
                        .document(i.id)
                        .delete()
                }

            }.await()
    }

    suspend fun deleteMemberAccount(email: String, groupName: String) {
        mFirestoreInstance.collection(Constants.MEMBER_ACCOUNT_DETAIL)
            .whereEqualTo("groupName", groupName)
            .whereEqualTo("adminEmail", email)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    mFirestoreInstance.collection(Constants.MEMBER_ACCOUNT_DETAIL)
                        .document(i.id)
                        .delete()
                }

            }.await()
    }

    suspend fun deleteMemberDetail(email: String, groupName: String) {
        mFirestoreInstance.collection(Constants.MEMBER)
            .whereEqualTo("groupName", groupName)
            .whereEqualTo("memberAdminEmail", email)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    mFirestoreInstance.collection(Constants.MEMBER)
                        .document(i.id)
                        .delete()
                }

            }.await()
    }

    fun deleteCurrentGroup(context: Context, groupName: String, email: String) {
        mFirestoreInstance.collection(Constants.GROUP)
            .whereEqualTo("groupName", groupName)
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    mFirestoreInstance.collection(Constants.GROUP)
                        .document(i.id)
                        .delete()
                }
                when (context) {
                    is AdminScreenActivity -> {
                        context.lifecycleScope.launch {
                            context.successGroupDeleted(groupName, email)
                        }

                    }
                }
            }.addOnFailureListener { exception ->
                when (context) {
                    is AdminScreenActivity -> {
                        context.cancelProgressDialog()
                    }
                }
                Log.e(
                    context.javaClass.simpleName,
                    "Error while deleting group.",
                    exception
                )
            }
    }

    fun deleteMemberAccountDetail(
        activity: Activity,
        mUserAdminEmail: String,
        mUserGroupName: String,
        mUserMemberEmail: String
    ) {
        mFirestoreInstance.collection(Constants.MEMBER_ACCOUNT_DETAIL)
            .whereEqualTo("adminEmail", mUserAdminEmail)
            .whereEqualTo("groupName", mUserGroupName)
            .whereEqualTo("memberEmail", mUserMemberEmail)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    mFirestoreInstance.collection(Constants.MEMBER_ACCOUNT_DETAIL)
                        .document(i.id)
                        .delete()
                }
                when (activity) {
                    is MemberDetailedActivity -> {
                        activity.successDeleteFromMemberAccount()
                    }
                }

            }.addOnFailureListener { exception ->
                when (activity) {
                    is MemberDetailedActivity -> {
                        activity.cancelProgressDialog()
                    }
                }

                Log.e(
                    activity.javaClass.simpleName,
                    "Error while deleting member.",
                    exception
                )
            }
    }

    fun deleteMonthWiseMemberAccountDetail(
        activity: Activity,
        documentId: String,
        currentAmount: Int,
        month: String
    ) {
        mFirestoreInstance.collection(Constants.MEMBER_ACCOUNT_DETAIL)
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                when (activity) {
                    is MemberDetailedActivity -> {
                        activity.lifecycleScope.launch {
                            activity.deletionSuccess(currentAmount, month)
                        }

                    }
                }

            }.addOnFailureListener { exception ->
                when (activity) {
                    is MemberDetailedActivity -> {
                        activity.cancelProgressDialog()
                        Log.e(
                            activity.javaClass.simpleName,
                            "Error while deleting month.",
                            exception
                        )
                    }
                }
            }
    }

    suspend fun deleteMemberFromMemberList(
        activity: Activity,
        mUserAdminEmail: String,
        mUserGroupName: String,
        mUserMemberEmail: String
    ) {

        mFirestoreInstance.collection(Constants.MEMBER)
            .whereEqualTo("memberAdminEmail", mUserAdminEmail)
            .whereEqualTo("groupName", mUserGroupName)
            .whereEqualTo("memberEmail", mUserMemberEmail)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    mFirestoreInstance.collection(Constants.MEMBER)
                        .document(i.id)
                        .delete()

                    when (activity) {
                        is MemberDetailedActivity -> {
                            activity.lifecycleScope.launch {
                                activity.successDeleteFromMember()
                            }

                        }
                    }
                }


            }.await()
    }

    fun getMemberList(activity: Activity, groupName: String, adminEmail: String) {
        mFirestoreInstance.collection(Constants.MEMBER)
            .whereEqualTo("memberAdminEmail", adminEmail)
            .whereEqualTo("groupName", groupName)
            .get()
            .addOnSuccessListener { document ->
                val memberList: ArrayList<MemberClass> = ArrayList()
                for (i in document.documents) {
                    val group = i.toObject(MemberClass::class.java)
                    group!!.id = i.id
                    memberList.add(group)
                }
                when (activity) {
                    is AddAccountActivity -> {
                        activity.successMemberListFromFirestore(memberList)
                    }
                    is ViewMemberAccountActivity -> {
                        activity.successMemberListFromFirestore(memberList)
                    }
                    is GuestAllMemberListDetailActivity -> {
                        activity.successMemberListFromFirestore(memberList)
                    }
                    is MemberNotPaidListActivity -> {
                        activity.successMemberListFromFirestore(memberList)
                    }
                }
            }.addOnFailureListener { exception ->
                when (activity) {

                    is AddAccountActivity -> {
                        activity.cancelProgressDialog()
                        Log.e(
                            activity.javaClass.simpleName,
                            "Error while checking Member.",
                            exception
                        )
                    }
                    is ViewMemberAccountActivity -> {
                        activity.cancelProgressDialog()
                    }
                    is GuestAllMemberListDetailActivity -> {
                        activity.cancelProgressDialog()
                    }
                    is MemberNotPaidListActivity -> {
                        activity.cancelProgressDialog()
                    }

                }
            }
    }

    fun getFullAboutDetail(
        activity: Activity,
        adminEmailId: String,
        groupName: String
    ) {
        mFirestoreInstance.collection(Constants.ABOUT_GROUP)
            .whereEqualTo("adminEmail", adminEmailId)
            .whereEqualTo("groupName", groupName)
            .get()
            .addOnSuccessListener { document ->
                val aboutGroupList: ArrayList<AboutGroup> = ArrayList()
                for (i in document.documents) {
                    val aboutGroup = i.toObject(AboutGroup::class.java)!!
                    aboutGroup.id = i.id
                    aboutGroupList.add(aboutGroup)
                }
                when (activity) {
                    is AboutGroupFullViewActivity -> {
                        activity.successLoadAboutList(aboutGroupList)
                    }
                }

            }.addOnFailureListener { exception ->
                when (activity) {
                    is AboutGroupFullViewActivity -> {
                        activity.cancelProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while checking about detail.",
                    exception
                )


            }
    }

    fun getAboutDetail(
        activity: Activity,
        adminEmailId: String
    ) {
        mFirestoreInstance.collection(Constants.ABOUT_GROUP)
            .whereEqualTo("adminEmail", adminEmailId)
            .get()
            .addOnSuccessListener { document ->
                val aboutGroupList: ArrayList<AboutGroup> = ArrayList()
                for (i in document.documents) {
                    val aboutGroup = i.toObject(AboutGroup::class.java)!!
                    aboutGroup.id = i.id
                    aboutGroupList.add(aboutGroup)
                }
                when (activity) {
                    is AdminAboutGroupActivity -> {
                        activity.successLoadAboutList(aboutGroupList)
                    }
                }

            }.addOnFailureListener { exception ->
                when (activity) {
                    is AdminAboutGroupActivity -> {
                        activity.cancelProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while checking about detail.",
                    exception
                )


            }
    }

    suspend fun checkAboutGroupAvailableinFirestore(
        adminEmailId: String,
        groupName: String
    ): String {
        var groupFound: String = ""
        mFirestoreInstance.collection(Constants.ABOUT_GROUP)
            .whereEqualTo("adminEmail", adminEmailId)
            .whereEqualTo("groupName", groupName)
            .get()
            .addOnSuccessListener { document ->
                for (i in document.documents) {
                    groupFound = i.get("groupName").toString()
                }

            }.await()
        return groupFound
    }

    fun getExpenseImageDetail(
        activity: Activity,
        mGroupName: String,
        mMonthName: String,
        mAdminEmail: String
    ) {
        mFirestoreInstance.collection(Constants.GROUP_EXPENSE_DETAIL)
            .whereEqualTo("memberAdminEmail", mAdminEmail)
            .whereEqualTo("month", mMonthName)
            .whereEqualTo("groupName", mGroupName)
            .get()
            .addOnSuccessListener { document ->
                val expenseList: ArrayList<MonthExpense> = ArrayList()
                for (i in document.documents) {
                    val month = i.toObject(MonthExpense::class.java)
                    month!!.id = i!!.id
                    expenseList.add(month)
                }
                when (activity) {
                    is PreExpenseImageActivity -> {
                        activity.expenseImageSuccess(expenseList)
                    }
                }

            }.addOnFailureListener { exception ->
                when (activity) {
                    is PreExpenseImageActivity -> {
                        activity.cancelProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while checking expense detail.",
                    exception
                )
            }
    }


}