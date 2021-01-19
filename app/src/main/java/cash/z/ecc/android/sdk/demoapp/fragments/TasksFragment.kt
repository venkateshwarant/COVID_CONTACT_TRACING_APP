package cash.z.ecc.android.sdk.demoapp.fragments

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import cash.z.ecc.android.sdk.demoapp.R
import cash.z.ecc.android.sdk.demoapp.demos.listtransactions.ListTransactionsFragment
import cash.z.ecc.android.sdk.demoapp.demos.send.SendFragment
import cash.z.ecc.android.sdk.demoapp.util.RSA
import java.text.SimpleDateFormat
import java.util.*

class TasksFragment : Fragment() {
    private lateinit var viewingKey: String
    private lateinit var seedByteArr: ByteArray

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_tasks, container, false)
        val date = root.findViewById<EditText>(R.id.currentDate)
        val signBtn = root.findViewById<Button>(R.id.sign)
        val verifyBtn =
            root.findViewById<Button>(R.id.verify)
   /*     val createAddress =
            root.findViewById<Button>(R.id.createAddress)*/
        val signedDate = root.findViewById<EditText>(R.id.signedDate)
        val reconstructedMessage =
            root.findViewById<TextView>(R.id.reconstructedMessage)
        val task1Layout = root.findViewById<LinearLayout>(R.id.task1Layout)
        val task1Text: CardView = root.findViewById(R.id.task1)
        val task2Layout: LinearLayout = root.findViewById(R.id.task2Layout)
        val task2Text: TextView = root.findViewById(R.id.task2)
        val task3Layout:LinearLayout = root.findViewById(R.id.task3Layout)
        val task3Text: TextView = root.findViewById(R.id.task3)
        signBtn.setOnClickListener {
            try {
                val cipher = RSA(context)
                signedDate.text.clear()
                signedDate.setText(cipher.sign(date.text.toString(), "SHA512withRSA"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        verifyBtn.setOnClickListener {
            try {
                val cipher = RSA(context)
                reconstructedMessage.text = cipher.verify(
                    signedDate.text.toString(),
                    date.text.toString(),
                    "SHA512withRSA"
                ).toString()
            } catch (e: Exception) {
                reconstructedMessage.text = "false"
                e.printStackTrace()
            }
        }
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("ddMMyy")
        val dateStr = dateFormat.format(calendar.time)
        date.setText(dateStr)
        val sendFrag = SendFragment()
        task1Text.setOnClickListener {
            if (task1Layout.visibility == View.VISIBLE) {
                task1Layout.visibility = View.GONE
            } else {
                task1Layout.visibility = View.VISIBLE
            }
        }
        task2Text.setOnClickListener{
            if (task2Layout.visibility == View.VISIBLE) {
                task2Layout.visibility = View.GONE
                this.childFragmentManager.beginTransaction().remove(sendFrag).commit()
            } else {
                this.childFragmentManager.beginTransaction().replace(R.id.SendFragmentHolder, sendFrag).commit()
                task2Layout.visibility= View.VISIBLE
            }
        }
        val listtrnFrag = ListTransactionsFragment()

        task3Text.setOnClickListener{
            if (task3Layout.visibility == View.VISIBLE) {
                task3Layout.visibility = View.GONE
                this.childFragmentManager.beginTransaction().remove(listtrnFrag).commit()
            } else {
                this.childFragmentManager.beginTransaction().replace(R.id.TransactionFragmentHolder, listtrnFrag).commit()
                task3Layout.visibility= View.VISIBLE
            }
        }
      //  showHideFragment(sendFrag)

      // this.childFragmentManager.beginTransaction().replace(R.id.SendFragmentHolder, sendFrag).commit()

     /*   createAddress.setOnClickListener {
            seedByteArr = Mnemonics.MnemonicCode(Mnemonics.WordCount.COUNT_24).toSeed()
            viewingKey = DerivationTool.deriveViewingKeys(seedByteArr).first()
            val zaddress = DerivationTool.deriveShieldedAddress(viewingKey)
            zAddress.setText(zaddress)
        }*/


        changeStatusBarColor(activity)
        return root
    }


    fun showHideFragment(fragment: Fragment) {
        val fragTransaction = this.childFragmentManager.beginTransaction()
        fragTransaction.setCustomAnimations(
            android.R.animator.fade_in,
            android.R.animator.fade_out
        )
        if (fragment.isHidden) {
            fragTransaction.show(fragment)
            Log.d("hidden", "Show")
        } else {
            fragTransaction.hide(fragment)
            Log.d("Shown", "Hide")
        }
        fragTransaction.commit()
    }


    companion object {
        @RequiresApi(api = Build.VERSION_CODES.M)
        fun changeStatusBarColor(activity: Activity?) {
            val window = activity!!.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.statusBarColor = activity.resources.getColor(R.color.colorWhite)
            }
        }

        fun hideSoftKeyboard(activity: Activity) {
            val inputMethodManager =
                activity.getSystemService(
                    Activity.INPUT_METHOD_SERVICE
                ) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                activity.currentFocus!!.windowToken, 0
            )
        }
    }
}