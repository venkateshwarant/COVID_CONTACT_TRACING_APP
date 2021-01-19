package cash.z.ecc.android.sdk.demoapp.fragments

import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import cash.z.ecc.android.sdk.db.entity.ConfirmedTransaction
import cash.z.ecc.android.sdk.demoapp.App
import cash.z.ecc.android.sdk.demoapp.R
import cash.z.ecc.android.sdk.ext.convertZatoshiToZecString
import java.text.SimpleDateFormat
import java.util.*

/**
 * Simple view holder for displaying confirmed transactions in the recyclerview.
 */
class TransactionViewHolder<T : ConfirmedTransaction>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val amountText = itemView.findViewById<TextView>(R.id.text_transaction_amount)
    private val infoText = itemView.findViewById<TextView>(R.id.text_transaction_info)
    private val timeText = itemView.findViewById<TextView>(R.id.text_transaction_timestamp)
    private val transactionType = itemView.findViewById<TextView>(R.id.transaction_type)
    private val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

    fun bindTo(transaction: T?) {
        val isInbound = transaction?.toAddress.isNullOrEmpty()
        amountText.text = transaction?.value.convertZatoshiToZecString()
        timeText.text =
            if (transaction == null || transaction?.blockTimeInSeconds == 0L) "Pending"
            else formatter.format(transaction.blockTimeInSeconds * 1000L)
        infoText.text = "Memo : "+getMemoString(transaction)
        if (isInbound){
            transactionType.setText("In")
        }else{
            transactionType.setText("Out")
        }
    }

    private fun getMemoString(transaction: T?): String {
        return transaction?.memo?.takeUnless { it[0] < 0 }?.let { String(it) } ?: "no memo"
    }
}
