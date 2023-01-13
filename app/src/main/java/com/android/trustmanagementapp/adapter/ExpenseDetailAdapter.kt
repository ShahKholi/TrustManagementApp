package com.android.trustmanagementapp.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.activities.AddExpenseActivity
import com.android.trustmanagementapp.firestore.FireStoreClass

import com.android.trustmanagementapp.model.MonthExpense
import com.android.trustmanagementapp.utils.Constants
import com.android.trustmanagementapp.utils.GlideLoaderClass
import com.android.trustmanagementapp.utils.MSPEditText
import com.android.trustmanagementapp.utils.MSPTextViewBold

class ExpenseDetailAdapter(
    private val context: Context,
    private val expenseList: ArrayList<MonthExpense>,
    private val activity: Activity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ExpenseDetailAdapter.MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_expense_view_rc_layout,
                parent,
                false
            )
        )
    }

    @SuppressLint("SetTextI18n", "CutPasteId")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = expenseList[position]
        if (holder is MyViewHolder) {

            GlideLoaderClass(context).loadGroupPictures(
                model.groupImage,
                holder.itemView.findViewById(R.id.cv_trust_image_exp)
            )

            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_view_account_group_name_exp).text =
                model.groupName
            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_view_month_name_exp).text =
                "${model.month} Expense"
            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_view_month_amount_exp).text =
                model.expenseAmount.toString()


            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_expense_detail)
                .setOnClickListener {
                    val llLayout: LinearLayout =
                        holder.itemView.findViewById(R.id.ll_expense_detail)

                    if (!llLayout.isVisible) {
                        llLayout.visibility = View.VISIBLE
                        holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_brief_expense).text =
                            model.detail

                    } else {
                        llLayout.visibility = View.GONE
                    }

                }

            holder.itemView.findViewById<ImageView>(R.id.iv_edit_expense_group).setOnClickListener {
                val builder = AlertDialog.Builder(
                    context,
                    R.style.AlertDialogTheme
                )
                val defaultAmount =
                    holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_view_month_amount_exp).text.toString()

                val customerLayout: View =
                    LayoutInflater.from(context)
                        .inflate(R.layout.custom_dilog_box_edit_text, null)
                builder.setView(customerLayout)
                builder.setTitle("Edit Amount")
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                val editText: MSPEditText =
                    customerLayout.findViewById(R.id.et_user_amount_edit)
                editText.setText(defaultAmount)
                val currentAmount : Int = defaultAmount.toInt()

                when(activity){
                    is AddExpenseActivity -> {
                        builder.setPositiveButton("SAVE"){dialogInterface, _ ->
                            val amount = editText.text.toString()
                            val adminEmail = model.memberAdminEmail
                            val groupName = model.groupName
                            val month = model.month
                            val totalAmount = amount.toInt()
                            val userHashMap = HashMap<String, Any>()
                            userHashMap[Constants.EXPENSE_AMOUNT] = totalAmount
                            activity.showProgressDialog()
                            FireStoreClass().updateCurrentAmountExpense(
                                activity,
                                userHashMap,
                                groupName,
                                month,
                                adminEmail,
                                currentAmount,
                                activity.currentYear()
                            )

                        }
                        builder.setNegativeButton("CANCEL") { dialogInterface, _ ->
                            dialogInterface.dismiss()
                        }
                        val alertDialog: AlertDialog? = builder.create()
                        // Set other dialog properties
                        alertDialog!!.setCancelable(false)
                        alertDialog.show()
                    }
                }

            }

        }
    }

    override fun getItemCount(): Int {
        return expenseList.size
    }

    class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

    }
}