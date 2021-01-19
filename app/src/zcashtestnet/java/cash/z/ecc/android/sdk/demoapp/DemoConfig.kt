package cash.z.ecc.android.sdk.demoapp

import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed

data class DemoConfig(
    val alias: String = "SdkDemo",
    val host: String = "lightwalletd.testnet.electriccoin.co",
    val port: Int = 9067,
    val birthdayHeight: Int = 954_500,
    val utxoEndHeight: Int = 1075590,
    val sendAmount: Double = 0.0,

    // corresponds to address: ztestsapling1zhqvuq8zdwa8nsnde7074kcfsat0w25n08jzuvz5skzcs6h9raxu898l48xwr8fmkny3zqqrgd9
    //val initialSeedWords: String = "wish puppy smile loan doll curve hole maze file ginger hair nose key relax knife witness cannon grab despair throw review deal slush frame",
    //sval initialSeedWords: String = "wish puppy smile loan doll curve hole maze file syrup hair nose key relax knife witness cannon grab despair throw review deal slush frame",

    var initialSeed: ByteArray?=null,
    // corresponds to seed: urban kind wise collect social marble riot primary craft lucky head cause syrup odor artist decorate rhythm phone style benefit portion bus truck top
    val toAddress: String = "ztestsapling1ddttvrm6ueug4vwlczs8daqjaul60aur4udnvcz9qdnjt9ekt2tsxheqvv3mn50wvhmzj4ge9rl"
)