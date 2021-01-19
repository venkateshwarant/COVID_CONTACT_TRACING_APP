package cash.z.ecc.android.sdk.demoapp.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import cash.z.ecc.android.sdk.Initializer
import cash.z.ecc.android.sdk.SdkSynchronizer
import cash.z.ecc.android.sdk.Synchronizer
import cash.z.ecc.android.sdk.block.CompactBlockProcessor
import cash.z.ecc.android.sdk.db.entity.ConfirmedTransaction
import cash.z.ecc.android.sdk.db.entity.Transaction
import cash.z.ecc.android.sdk.demoapp.App
import cash.z.ecc.android.sdk.demoapp.BaseDemoFragment
import cash.z.ecc.android.sdk.demoapp.R
import cash.z.ecc.android.sdk.demoapp.databinding.FragmentListTransactionsBinding
import cash.z.ecc.android.sdk.demoapp.databinding.FragmentVulnerableBinding
import cash.z.ecc.android.sdk.demoapp.util.RSA
import cash.z.ecc.android.sdk.demoapp.util.TinyDB
import cash.z.ecc.android.sdk.ext.collectWith
import cash.z.ecc.android.sdk.ext.twig
import cash.z.ecc.android.sdk.tool.DerivationTool
import java.text.SimpleDateFormat
import java.util.*

/**
 * List all transactions related to the given seed, since the given birthday. This begins by
 * downloading any missing blocks and then validating and scanning their contents. Once scan is
 * complete, the transactions are available in the database and can be accessed by any SQL tool.
 * By default, the SDK uses a PagedTransactionRepository to provide transaction contents from the
 * database in a paged format that works natively with RecyclerViews.
 */
class VulnerableFragment : BaseDemoFragment<FragmentVulnerableBinding>() {
    private lateinit var initializer: SdkSynchronizer.SdkInitializer
    private lateinit var synchronizer: Synchronizer
    private lateinit var adapter: TransactionAdapter<ConfirmedTransaction>
    private lateinit var address: String
    private var status: Synchronizer.Status? = null
    private val isSynced get() = status == Synchronizer.Status.SYNCED


    /**
     * Initialize the required values that would normally live outside the demo but are repeated
     * here for completeness so that each demo file can serve as a standalone example.
     */
    private fun setup() {
        // defaults to the value of `DemoConfig.seedWords` but can also be set by the user
       // var seedPhrase = sharedViewModel.seedPhrase.value

        // Use a BIP-39 library to convert a seed phrase into a byte array. Most wallets already
        // have the seed stored
        //val seed = Mnemonics.MnemonicCode(seedPhrase).toSeed()
        val  seed = sharedViewModel.seedPhraseBytes.value!!

        App.instance.defaultConfig.let { config ->
            initializer = Initializer(App.instance) {
                it.importWallet(seed, config.birthdayHeight)
                it.server(config.host, config.port)
            }
            address = DerivationTool.deriveShieldedAddress(seed)
        }
        synchronizer = Synchronizer(initializer)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initTransactionUI() {
        binding.recyclerTransactions.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        adapter = TransactionAdapter()
        binding.recyclerTransactions.adapter = adapter
        binding.vulnerability.text="Not vulnerable"
        binding.vulnerability.setTextColor(requireContext().getColor(R.color.colorGreen))
        var tinyDB : TinyDB = TinyDB(activity?.applicationContext)
        if(tinyDB.getBoolean("testedPositive")){
            binding.vulnerability.text= "Tested positive!!!"
            binding.vulnerability.setTextColor(requireContext().getColor(R.color.colorRed))
        }
    }

    private fun monitorChanges() {
        // the lifecycleScope is used to stop everything when the fragment dies
        synchronizer.status.collectWith(lifecycleScope, ::onStatus)
        synchronizer.processorInfo.collectWith(lifecycleScope, ::onProcessorInfoUpdated)
        synchronizer.progress.collectWith(lifecycleScope, ::onProgress)
        synchronizer.clearedTransactions.collectWith(lifecycleScope, ::onTransactionsUpdated)
    }


    //
    // Change listeners
    //

    private fun onProcessorInfoUpdated(info: CompactBlockProcessor.ProcessorInfo) {
        if (info.isScanning) binding.textInfo.text = "Scanning blocks...${info.scanProgress}%"
    }

    private fun onProgress(i: Int) {
        if (i < 100) binding.textInfo.text = "Downloading blocks...$i%"
    }

    private fun onStatus(status: Synchronizer.Status) {
        this.status = status
        binding.textStatus.text = "Status: $status"
        if (isSynced) onSyncComplete()
    }

    private fun onSyncComplete() {
        binding.textInfo.visibility = View.INVISIBLE
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun onTransactionsUpdated(transactions: PagedList<ConfirmedTransaction>) {
        twig("got a new paged list of transactions")
        adapter.submitList(transactions)
        for(transaction in transactions){
            for(dayIndex in 0..15){
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -dayIndex);
                val dateFormat = SimpleDateFormat("ddMMyy")
                val dateStr = dateFormat.format(calendar.time)
                val cipher = RSA(context)
                val memo = getMemoString(transaction).split(",")
                if(memo.size>=2){
                    val signedDate = cipher.sign(dateStr+","+memo[1].substring(0,13), "SHA512withRSA")
                    if(memo[0].contains(signedDate)){
                        binding.vulnerability.text= "Vulnerable!!!"
                        binding.vulnerability.setTextColor(requireContext().getColor(R.color.colorRed))
                        break
                    }
                }
            }

        }
        // show message when there are no transactions
        if (isSynced) {
            binding.textInfo.apply {
                if (transactions.isEmpty()) {
                    visibility = View.VISIBLE
                    text =
                        "No transactions found. Try to either change the seed words " +
                                "or send funds to this address (tap the FAB to copy it):\n\n $address"
                } else {
                    visibility = View.INVISIBLE
                    text = ""
                }
            }
        }
    }


    //
    // Android Lifecycle overrides
    //

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setup()
    }
    private fun getMemoString(transaction: Transaction?): String {
        return transaction?.memo?.takeUnless { it[0] < 0 }?.let { String(it) } ?: "no memo"
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTransactionUI()
    }

    override fun onResume() {
        super.onResume()
        // the lifecycleScope is used to dispose of the synchronizer when the fragment dies
        synchronizer.start(lifecycleScope)
        monitorChanges()
    }


    //
    // Base Fragment overrides
    //

    override fun onActionButtonClicked() {
        if (::address.isInitialized) copyToClipboard(address)
    }

    override fun inflateBinding(layoutInflater: LayoutInflater): FragmentVulnerableBinding =
        FragmentVulnerableBinding.inflate(layoutInflater)

}
