package com.android.trustmanagementapp.firestore

import android.app.Activity
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.android.trustmanagementapp.activities.*
import com.android.trustmanagementapp.model.*
import com.android.trustmanagementapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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

    private fun getCurrentUserID(): String {
        val current = FirebaseAuth.getInstance().currentUser
        var currentUserUID = ""
        if (current != null) {
            currentUserUID = current.uid
        }
        return currentUserUID
    }

    suspend fun validateCode(activity: RegisterActivity, code: String): ArrayList<UserClass> {
        val userList: ArrayList<UserClass> = ArrayList()
        var user: UserClass
        mFirestoreInstance.collection(Constants.USER)
            .whereEqualTo("code", code)
            .get()
            .addOnSuccessListener { document ->
                for (i in document) {
                    user = i.toObject(UserClass::class.java)
                    //group.groupImage = i.get("groupImage").toString()
                    user.id = i.id
                    userList.add(user)
                }
            }.await()
        return userList
    }

    fun codeValidateUserDetail(activity: Activity, code: String) {
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
                            activity.successMemberLogin(userEmailList)
                        }

                    }
                }
            }
            .addOnFailureListener {
                when (activity) {
                    is LoginActivity -> {

                    }
                }
            }
    }

    fun validateUserAdminORNonAdmin(activity: Activity, email: String) {

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

    fun uploadImageToCloudStorage(
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
                        activity.successGroupIconUpload(uri)
                    }
                    is AddMemberActivity -> {
                        activity.successGroupIconUpload(uri)
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

    fun loadExpenseDetail(activity: Activity) {
        mFirestoreInstance.collection(Constants.GROUP_EXPENSE_DETAIL)
            .whereEqualTo("memberAdminUID", getCurrentUserID())
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

    fun getGroupList(activity: Activity) {
        mFirestoreInstance.collection(Constants.GROUP)
            .whereEqualTo("id", getCurrentUserID())
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

    suspend fun updateMasterAccount(
        activity: Activity,
        adminEmailId: String,
        groupName: String,
        month: String,
        currentYear: Int,
        userHashMap: HashMap<String, Any>

    ) {
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
                    }
                }

            }.await()
    }


    fun updateCurrentAmountExpense(
        activity: Activity, userHashMap: HashMap<String, Any>,
        groupName: String, month: String, adminEmail: String, currentAmount: Int,
        currentYear: Int
    ) {
        Log.e("update current amount", currentAmount.toString())
        mFirestoreInstance.collection(Constants.GROUP_EXPENSE_DETAIL)
            .whereEqualTo("memberAdminEmail", adminEmail)
            .whereEqualTo("groupName",groupName)
            .whereEqualTo("year",currentYear)
            .whereEqualTo("month",month)
            .whereEqualTo("expenseAmount", currentAmount)
            .get()
            .addOnSuccessListener {  document->
                val monthExpenseList: ArrayList<MonthExpense> = ArrayList()
                for (i in document){
                    mFirestoreInstance.collection(Constants.GROUP_EXPENSE_DETAIL)
                        .document(i.id)
                        .set(userHashMap, SetOptions.merge())
                    val monthExpense = i.toObject(MonthExpense::class.java)
                    monthExpense.id = i.id
                    monthExpenseList.add(monthExpense)

                }
                when(activity){
                    is AddExpenseActivity -> {
                        activity.lifecycleScope.launch {
                            activity.expenseUpdateSuccess(monthExpenseList)
                        }

                    }
                }
            }.addOnFailureListener { exception->
                when(activity){
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
                when(activity){
                    is AddExpenseActivity -> {
                        activity.successUpdateToMaster()
                    }
                }

            }.addOnFailureListener {exception->
                when(activity){
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


    suspend fun getGroupImageFromFirestore(
        activity: Activity,
        currentGroupNameSelect: String
    ): String {
        val groupList: ArrayList<String> = ArrayList()
        var group: GroupNameClass
        var imageString : String = ""
        mFirestoreInstance.collection(Constants.GROUP)
            .whereEqualTo("id", getCurrentUserID())
            .whereEqualTo("groupName", currentGroupNameSelect)
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

    fun getAllMasterAccountFromFirestore(
        activity: Activity,
        groupName: String, adminEmailId: String?
    ) {
        val masterAccountList: ArrayList<MasterAccountDetail> = ArrayList()
        var masterAccount: MasterAccountDetail
        mFirestoreInstance.collection(Constants.MASTER_ACCOUNT_DETAIL)
            .whereEqualTo("groupName", groupName)
            .whereEqualTo("memberAdminEmail", adminEmailId)
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
           .whereEqualTo("memberAdminEmail",memberAdminEmail)
           .whereEqualTo("groupName", groupName)
           .whereEqualTo("month", month)
           .whereEqualTo("year",year)
           .get()
           .addOnSuccessListener { document ->
               for (i in document.documents){
                   expenseClass = i.toObject(MonthExpense::class.java)!!
                   expenseClass.expenseAmount = i.get("expenseAmount").toString().toInt()
                   expenseList.add(expenseClass.expenseAmount)
               }
           }.await()
       return expenseList
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
            .whereEqualTo("memberName", mUserMemberName)
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

    fun getDeleteItemDocumentID(
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
                }
            }
    }




}