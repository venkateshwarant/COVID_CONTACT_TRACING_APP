package cash.z.ecc.android.sdk.demoapp.fragments

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import cash.z.ecc.android.sdk.Initializer
import cash.z.ecc.android.sdk.Synchronizer
import cash.z.ecc.android.sdk.block.CompactBlockProcessor
import cash.z.ecc.android.sdk.db.entity.*
import cash.z.ecc.android.sdk.demoapp.App
import cash.z.ecc.android.sdk.demoapp.BaseDemoFragment
import cash.z.ecc.android.sdk.demoapp.R
import cash.z.ecc.android.sdk.demoapp.adapter.ClientAdapter
import cash.z.ecc.android.sdk.demoapp.databinding.FragmentHomeBinding
import cash.z.ecc.android.sdk.demoapp.pojo.User
import cash.z.ecc.android.sdk.demoapp.util.RSA
import cash.z.ecc.android.sdk.demoapp.util.TinyDB
import cash.z.ecc.android.sdk.demoapp.util.mainActivity
import cash.z.ecc.android.sdk.ext.*
import cash.z.ecc.android.sdk.tool.DerivationTool
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.messages.Message
import com.google.android.gms.nearby.messages.MessageListener
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment :
    BaseDemoFragment<FragmentHomeBinding>(),
    AbsListView.OnScrollListener {

    private lateinit var synchronizer: Synchronizer
    private lateinit var spendingKey: String

    private val progressBar: ProgressBar? = null
    private var mHandler: Handler? = null
    var mMessageListener: MessageListener? = null
    var mMessage: Message? = null
    var data = ArrayList<String?>()
    val handler = Handler()
    private var thread: Thread? = null
    private val set = setOf<String>()
    private var itemsAdapter: ClientAdapter? = null
    private var killSocketServer = false
    private var isPreviousTransactionMined= false
    private var balance = CompactBlockProcessor.WalletBalance()
        set(value) {
            field = value
        }
    private var isSending = false
        set(value) {
            field = value
            if (value) Twig.sprout("Sending") else Twig.clip("Sending")
        }
    private var isSyncing = true
        set(value) {
            field = value
        }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mHandler = Handler()
        var tinyDB : TinyDB = TinyDB(activity?.applicationContext)
        if(tinyDB.getListString("addressList").isNotEmpty()){
            data = tinyDB.getListString("addressList")
        }
        itemsAdapter= ClientAdapter(context as FragmentActivity?, data)
        binding.offerList.adapter = itemsAdapter
        binding.sendData.setOnClickListener {
                when(deviceNumber){
                    1-> sendMessageToServers(clientPort1)
                    2-> sendMessageToServers(clientPort2)
                    3-> sendMessageToServers(clientPort3)
                    4-> sendMessageToServers(clientPort4)
                }
            }
        binding.clearData.setOnClickListener{
            var tinyDB : TinyDB = TinyDB(activity?.applicationContext)
            tinyDB.remove("addressList")
            itemsAdapter?.clear()
        }
        binding.resetDevice.setOnClickListener{
            var tinyDB : TinyDB = TinyDB(activity?.applicationContext)
            tinyDB.remove("testedPositive")
        }
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("ddMMyy")
        val dateStr = dateFormat.format(calendar.time)
        binding.currentDate.setText(dateStr)
        binding.testedPositiveButton.setOnClickListener{
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Tested positive")
            builder.setMessage("Are you sure that you are tested positive for COVID-19?")

            builder.setPositiveButton("No") { dialog, which ->
                Toast.makeText(requireContext().applicationContext,
                    "No", Toast.LENGTH_SHORT).show()
            }

            builder.setNegativeButton("Yes") { dialog, which ->
                var tinyDB : TinyDB = TinyDB(activity?.applicationContext)
                val list= tinyDB.getListString("addressList")
                tinyDB.putBoolean("testedPositive", true)
                for(content in list){
                    val array= content.split(",")
                    val zAddress =array[0]
                    val memo =array[1]+","+array[2]
                    onSend(zAddress, memo)
                    break;
                }
                list.removeAt(0)
                tinyDB.putListString("toSendAddressList",list)
            }

            builder.show()
        }

        changeStatusBarColor(requireActivity())
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        thread = Thread(Runnable {
            try {
                while (!killSocketServer) {
                    try{
                        if (server == null) {
                            when(deviceNumber){
                                1-> serverPort = serverPort1
                                2-> serverPort = serverPort2
                                3-> serverPort = serverPort3
                                4-> serverPort = serverPort4
                            }
                            server = ServerSocket(serverPort)
                            server?.soTimeout = 100000
                        }
                        Log.d(TAG_MOCK_NEARBY, "Waiting for the client request")
                        val socket = server!!.accept()
                        val ois = ObjectInputStream(socket.getInputStream())
                        val message = ois.readObject() as String
                        Log.d(TAG_MOCK_NEARBY, "Message Received: $message")
                        var cont = activity?.applicationContext
                        if(cont!=null && activity!=null){
                            var tinyDB : TinyDB = TinyDB(cont)
                            activity?.runOnUiThread{
                                if(tinyDB.getListString("addressList").isEmpty()){
                                    val list= arrayListOf(message)
                                    tinyDB.putListString("addressList",list)
                                }
                                else{
                                    val list= tinyDB.getListString("addressList")
                                    list.add(message)
                                    tinyDB.putListString("addressList",list)
                                }
                                itemsAdapter?.add(message)
                            }
                        }
                        ois.close()
                        socket.close()
                    }catch ( e: java.lang.Exception){
                        server?.close()
                        server=null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
        thread!!.start()
        mMessageListener = object : MessageListener() {
            override fun onFound(message: Message) {
                Log.d(TAG, "Found message: " + String(message.content))
                data.add(String(message.content))
                val itemsAdapter: ArrayAdapter<String> = ArrayAdapter<String>(requireActivity(), android.R.layout.simple_list_item_1, data)
                binding.offerList.adapter = itemsAdapter
            }

            override fun onLost(message: Message) {
                Log.d(TAG, "Lost sight of message: " + String(message.content))
            }
        }
        val delay = 30000
        handler.postDelayed(object : Runnable {
            override fun run() {
                /*val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("ddMMyy")
                val dateStr = dateFormat.format(calendar.time)*/
                var dateStr = binding.currentDate.text.toString()
                val cipher = RSA(context)
                val array = ByteArray(7) // length is bounded by 7
                Random().nextBytes(array)
                val salt = Base64.encodeToString(array, Base64.DEFAULT)
                val signedDate = cipher.sign("$dateStr,$salt", "SHA512withRSA")
                var fromUser = User()
                val  seed = sharedViewModel.seedPhraseBytes.value
                fromUser.setSeedByteArr(seed)
                fromUser.setViewingKey(DerivationTool.deriveViewingKeys(fromUser.getSeedByteArr()).first())
                fromUser.setSaplingKeyAddress(DerivationTool.deriveShieldedAddress(fromUser.getViewingKey()))
                fromUser.setSpendingKey(DerivationTool.deriveSpendingKeys(fromUser.getSeedByteArr()).first())
                val message= fromUser.getSaplingKeyAddress()+","+signedDate+","+salt
                mMessage = Message(message.toByteArray())
                Nearby.getMessagesClient(requireActivity()).publish(mMessage!!)
                handler.postDelayed(this, delay.toLong())
            }
        }, delay.toLong())
    }

    override fun onStart() {
        super.onStart()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("ddMMyy")
        val dateStr = dateFormat.format(calendar.time)
        binding.currentDate.setText(dateStr)
        val cipher = RSA(context)
        val array = ByteArray(7) // length is bounded by 7
        Random().nextBytes(array)
        val salt = Base64.encodeToString(array, Base64.DEFAULT)
        val signedDate = cipher.sign("$dateStr,$salt", "SHA512withRSA")
        var fromUser = User()
        val  seed = sharedViewModel.seedPhraseBytes.value
        fromUser.setSeedByteArr(seed)
        fromUser.setViewingKey(DerivationTool.deriveViewingKeys(fromUser.getSeedByteArr()).first())
        fromUser.setSaplingKeyAddress(DerivationTool.deriveShieldedAddress(fromUser.getViewingKey()))
        fromUser.setSpendingKey(DerivationTool.deriveSpendingKeys(fromUser.getSeedByteArr()).first())
        val message= fromUser.getSaplingKeyAddress()+","+signedDate+","+salt
        mMessage = Message(message.toByteArray())
        Nearby.getMessagesClient(requireActivity()).publish(mMessage!!)
        Nearby.getMessagesClient(requireActivity()).subscribe(mMessageListener!!)
    }

    override fun onStop() {
        Nearby.getMessagesClient(requireActivity()).unpublish(mMessage!!)
        Nearby.getMessagesClient(requireActivity()).unsubscribe(mMessageListener!!)
        handler.removeCallbacksAndMessages(null)
        killSocketServer=true
        server?.close()
        super.onStop()
    }

    override fun onScroll(
        view: AbsListView,
        firstVisibleItem: Int,
        visibleItemCount: Int,
        totalItemCount: Int
    ) {
        if (firstVisibleItem + visibleItemCount == totalItemCount /*&& !adapter.endReached()*/ && !hasCallback) { //check if we've reached the bottom
//            mHandler.postDelayed(showMore, 300);
            hasCallback = true
        }
    }

    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {}
    private var hasCallback = false


    override fun inflateBinding(layoutInflater: LayoutInflater): FragmentHomeBinding =
        FragmentHomeBinding.inflate(layoutInflater)

    private fun setup() {
        val seed = sharedViewModel.seedPhraseBytes.value
        App.instance.defaultConfig.let { config ->
            Initializer(App.instance) {
                it.importWallet(seed!!, config.birthdayHeight)
                it.server(config.host, config.port)
            }.let { initializer ->
                synchronizer = Synchronizer(initializer)
            }
            spendingKey = DerivationTool.deriveSpendingKeys(seed!!).first()
        }
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setup()
    }


    private fun monitorChanges() {
        synchronizer.status.collectWith(lifecycleScope, ::onStatus)
        synchronizer.progress.collectWith(lifecycleScope, ::onProgress)
        synchronizer.processorInfo.collectWith(lifecycleScope, ::onProcessorInfoUpdated)
        synchronizer.balances.collectWith(lifecycleScope, ::onBalance)
    }

    private fun onStatus(status: Synchronizer.Status) {
        binding.textStatus.text = "Status: $status"
        isSyncing = status != Synchronizer.Status.SYNCED
        if (status == Synchronizer.Status.SCANNING) {
            binding.textBalance.text = "Calculating balance..."
        } else {
            if (!isSyncing) {
                onBalance(balance)
            }
        }
    }

    private fun onProgress(i: Int) {
        if (i < 100) {
            binding.textStatus.text = "Downloading blocks...$i%"
            binding.textBalance.visibility = View.INVISIBLE
        } else {
            binding.textBalance.visibility = View.VISIBLE
        }
    }

    private fun onProcessorInfoUpdated(info: CompactBlockProcessor.ProcessorInfo) {
        if (info.isScanning) binding.textStatus.text = "Scanning blocks...${info.scanProgress}%"
    }

    private fun onBalance(balance: CompactBlockProcessor.WalletBalance) {
        this.balance = balance
        if (!isSyncing) {
            binding.textBalance.text = """
                Available balance: ${balance.availableZatoshi.convertZatoshiToZecString(12)}
                Total balance: ${balance.totalZatoshi.convertZatoshiToZecString(12)}
            """.trimIndent()
            if(balance.availableZatoshi>0){
                var tinyDB : TinyDB = TinyDB(activity?.applicationContext)
                val list= tinyDB.getListString("toSendAddressList")
                if(isPreviousTransactionMined && list.size>0){
                    for(content in list){
                        val array= content.split(",")
                        val zAddress =array[0]
                        val memo =array[1]+","+array[2]
                        onSend(zAddress, memo)
                        isPreviousTransactionMined=false
                        break
                    }
                    list.removeAt(0)
                    tinyDB.putListString("toSendAddressList",list)
                }
            }
        }
    }

    private fun onSend(toAddress:String, memo:String) {
       // isSending = true
        val amount = 0.toDouble().convertZecToZatoshi()
        val toAddress= toAddress
        synchronizer.sendToAddress(
            spendingKey,
            amount,
            toAddress,
            memo
        ).collectWith(lifecycleScope, ::onPendingTxUpdated)
        mainActivity()?.hideKeyboard()
    }

    private fun onPendingTxUpdated(pendingTransaction: PendingTransaction?) {
        val id = pendingTransaction?.id ?: -1
        val message = when {
            pendingTransaction == null -> "Transaction not found"
            pendingTransaction.isMined() -> "Transaction Mined (id: $id)!\n\nSEND COMPLETE".also {
                isSending = false
                isPreviousTransactionMined=true
            }
            pendingTransaction.isSubmitSuccess() -> "Successfully submitted transaction!\nAwaiting confirmation..."
            pendingTransaction.isFailedEncoding() -> "ERROR: failed to encode transaction! (id: $id)".also { isSending = false }
            pendingTransaction.isFailedSubmit() -> "ERROR: failed to submit transaction! (id: $id)".also { isSending = false }
            pendingTransaction.isCreated() -> "Transaction creation complete! (id: $id)"
            pendingTransaction.isCreating() -> "Creating transaction!".also { onResetInfo() }
            else -> "Transaction updated!".also { twig("Unhandled TX state: $pendingTransaction") }
        }
        twig("Pending TX Updated: $message")
        binding.textInfo.apply {
            text = "$text\n$message"
        }
    }

    private fun sendMessageToServers(clientPorts: ArrayList<Int>){
        for (clientPort in clientPorts){
            sendMessageToOneServer(clientPort)
        }
    }
    private fun sendMessageToOneServer(clientPort: Int){
        val thread = Thread(Runnable {
            try {
                val socket = Socket("10.0.2.2", clientPort)
                val oos = ObjectOutputStream(socket.getOutputStream())
                Log.d(TAG_MOCK_NEARBY, "Sending request to Socket Server")

               /* val calendar = Calendar.getInstance()
                val dateFormat = SimpleDateFormat("ddMMyy")
                val dateStr = dateFormat.format(calendar.time)*/
                var dateStr = binding.currentDate.text.toString()
                val cipher = RSA(context)
                val array = ByteArray(8) // length is bounded by 7
                Random().nextBytes(array)
                val salt = Base64.encodeToString(array, Base64.DEFAULT)
                val signedDate = cipher.sign("$dateStr,$salt", "SHA512withRSA")
                var fromUser = User()
                val  seed = sharedViewModel.seedPhraseBytes.value
                fromUser.setSeedByteArr(seed)
                fromUser.setViewingKey(DerivationTool.deriveViewingKeys(fromUser.getSeedByteArr()).first())
                fromUser.setSaplingKeyAddress(DerivationTool.deriveShieldedAddress(fromUser.getViewingKey()))
                fromUser.setSpendingKey(DerivationTool.deriveSpendingKeys(fromUser.getSeedByteArr()).first())
                val message= fromUser.getSaplingKeyAddress()+","+signedDate+","+salt
                oos.writeObject(message)
                Log.d(TAG_MOCK_NEARBY, "Message: $message")
                oos.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
        thread.start()
    }

    private fun onResetInfo() {
        binding.textInfo.text = "Active Transaction:"
    }
    override fun onResume() {
        super.onResume()
        // the lifecycleScope is used to dispose of the synchronizer when the fragment dies
        synchronizer.start(lifecycleScope)
        monitorChanges()
    }
    companion object {
        private const val TAG = "NEARBY API"
        private const val TAG_MOCK_NEARBY = "SOCKET SERVER API"
        private var server: ServerSocket? = null
        private var serverPort = 60101
        //private const val clientPort = 6100

        private const val deviceNumber =4

        private const val serverPort1 = 60101
        private val clientPort1 = arrayListOf(60102, 60104)

        private const val serverPort2 = 60102
        private val clientPort2 = arrayListOf(60101, 60103, 60104)

        private const val serverPort3 = 60103
        private val clientPort3 = arrayListOf(60102)

        private const val serverPort4 = 60104
        private val clientPort4 = arrayListOf(60101, 60102)

        /*val message: String
            get() {
                val timeStamp =
                    SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date())
                return "ferfdcv $timeStamp"
            }
*/
        @RequiresApi(api = Build.VERSION_CODES.M)
        fun changeStatusBarColor(activity: Activity) {
            val window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.statusBarColor = activity.resources.getColor(R.color.colorWhite)
            }
        }
    }
}