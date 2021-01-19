package cash.z.ecc.android.sdk.demoapp

import android.app.Application
import androidx.fragment.app.activityViewModels
import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import cash.z.ecc.android.sdk.demoapp.util.SharedPreferencesHelper
import cash.z.ecc.android.sdk.ext.TroubleshootingTwig
import cash.z.ecc.android.sdk.ext.Twig
import java.util.*

class App : Application() {

    var defaultConfig = DemoConfig()

    override fun onCreate() {
        instance = this

        if(SharedPreferencesHelper.get(applicationContext, "seedphrase","").toString().isEmpty()){
            val initialSeed: ByteArray = Mnemonics.MnemonicCode(Mnemonics.WordCount.COUNT_24).toSeed()
            SharedPreferencesHelper.put(applicationContext, "seedphrase", Arrays.toString(initialSeed))
            defaultConfig.initialSeed = initialSeed
        }else{
            var stringArray= SharedPreferencesHelper.get(applicationContext, "seedphrase","").toString()
            if (stringArray != null) {
                val split: Array<String> =
                    stringArray.substring(1, stringArray.length - 1).split(", ").toTypedArray()
                val array = ByteArray(split.size)
                for (i in split.indices) {
                    array[i] = split[i].toByte()
                }
                defaultConfig.initialSeed=array
            }
        }
        super.onCreate()
        Twig.plant(TroubleshootingTwig())
    }

    companion object {
        lateinit var instance: App
    }
}
