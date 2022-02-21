package com.projects.wallet.utils

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.projects.wallet.R
import kotlinx.android.synthetic.main.bottomsheet_select.*

object CommonUtils {


    fun openSelection(activity: Activity, student: () -> Unit,document:()->Unit){
        val view = LayoutInflater.from(activity).inflate(R.layout.bottomsheet_select,null)
        val mBottomSheetDialog = Dialog(activity, R.style.MaterialDialogSheet)
        with(mBottomSheetDialog){
            setContentView(view)
            setCancelable(false)
            setCanceledOnTouchOutside(false)
            window!!.setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            window!!.setGravity(Gravity.BOTTOM)

            show()

            tvStudent.setOnClickListener {
                student()
            }
            tvdocument.setOnClickListener {
                document()
            }
        }
    }
    enum class NEWPASSWORD{
        CANCEL,CONFIRM
    }
}