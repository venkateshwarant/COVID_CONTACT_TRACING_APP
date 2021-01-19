package cash.z.ecc.android.sdk.demoapp

import androidx.lifecycle.ViewModel
import cash.z.ecc.android.bip39.Mnemonics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Shared mutable state for the demo
 */
class SharedViewModel : ViewModel() {

    private val config = App.instance.defaultConfig
  //  private val _seedPhrase = MutableStateFlow(config.initialSeedWords)
    private val _seedPhraseBytes = MutableStateFlow(config.initialSeed)

    // publicly, this is read-only
    //val seedPhrase: StateFlow<String> get() = _seedPhrase
    val seedPhraseBytes: MutableStateFlow<ByteArray?> get() = _seedPhraseBytes

   /* fun updateSeedPhrase(newPhrase: String?): Boolean {
        return if (isValidSeedPhrase(newPhrase)) {
            _seedPhrase.value = newPhrase!!
            true
        } else {
            false
        }
    }*/
   fun updateSeedPhraseBytes(newSeed: ByteArray?) {
       seedPhraseBytes.value = newSeed
   }
    private fun isValidSeedPhrase(phrase: String?): Boolean {
        if (phrase.isNullOrEmpty()) return false
        return try {
            Mnemonics.MnemonicCode(phrase).validate()
            true
        } catch (t: Throwable){ false }
    }
}
