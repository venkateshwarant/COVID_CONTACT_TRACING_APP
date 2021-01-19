package cash.z.ecc.android.sdk.demoapp

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.getSystemService
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.viewbinding.ViewBinding
import cash.z.ecc.android.sdk.demoapp.demos.listtransactions.ListTransactionsFragment
import cash.z.ecc.android.sdk.demoapp.demos.send.SendFragment
import cash.z.ecc.android.sdk.demoapp.fragments.HomeFragment
import cash.z.ecc.android.sdk.demoapp.fragments.TasksFragment
import cash.z.ecc.android.sdk.demoapp.fragments.VulnerableFragment
import cash.z.ecc.android.sdk.service.LightWalletGrpcService
import cash.z.ecc.android.sdk.service.LightWalletService
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), ClipboardManager.OnPrimaryClipChangedListener,
    DrawerLayout.DrawerListener, View.OnClickListener {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var clipboard: ClipboardManager
    private var clipboardListener: ((String?) -> Unit)? = null
    var fabListener: BaseDemoFragment<out ViewBinding>? = null

    /**
     * The service to use for all demos that interact directly with the service. Since gRPC channels
     * are expensive to recreate, we set this up once per demo. A real app would hardly ever use
     * this object because it would utilize the synchronizer, instead, which exposes APIs that
     * automatically sync with the server.
     */
    var lightwalletService: LightWalletService? = null
    private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.addPrimaryClipChangedListener(this)
        setContentView(R.layout.activity_main)



        findViewById<View>(R.id.home_btm_ic).setOnClickListener(this)
        findViewById<View>(R.id.search_btm_ic).setOnClickListener(this)
        findViewById<View>(R.id.add_btm_ic).setOnClickListener(this)
        findViewById<View>(R.id.star_btm_ic).setOnClickListener(this)
        findViewById<View>(R.id.user_btm_ic).setOnClickListener(this)
        val homeFrag = HomeFragment()
        val fragmentManager =
            this.supportFragmentManager
        val fragmentTransaction =
            fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.HomeFragmentHolder, homeFrag)
        fragmentTransaction.commit()
        initService()
    }

    override fun onDestroy() {
        super.onDestroy()
        lightwalletService?.shutdown()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_settings) {
            val navController = findNavController(R.id.nav_host_fragment)
            navController.navigate(R.id.nav_home)
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    //
    // Private functions
    //
    
    private fun initService() {
        if (lightwalletService != null) {
            lightwalletService?.shutdown()
        }
        with(App.instance.defaultConfig) {
            lightwalletService = LightWalletGrpcService(App.instance, host, port)
        }
    }

    private fun onFabClicked(view: View) {
        fabListener?.onActionButtonClicked()
    }

    
    //
    // Helpers
    //

    fun getClipboardText(): String? {
        return with(clipboard) {
            if (!hasPrimaryClip()) return null
            return primaryClip!!.getItemAt(0)?.coerceToText(this@MainActivity)?.toString()
        }
    }

    override fun onPrimaryClipChanged() {
        clipboardListener?.invoke(getClipboardText())
    }

    fun setClipboardListener(block: (String?) -> Unit) {
        clipboardListener = block
        block(getClipboardText())
    }

    fun removeClipboardListener() {
        clipboardListener = null
    }

    fun hideKeyboard() {
        val windowToken = window.decorView.rootView.windowToken
        getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(windowToken, 0)
    }


    /* DrawerListener implementation */

    override fun onDrawerStateChanged(newState: Int) {
    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
    }

    override fun onDrawerClosed(drawerView: View) {
    }

    override fun onDrawerOpened(drawerView: View) {
        hideKeyboard()
    }

    override fun onClick(v: View?) {
        val fragmentManager =
            this.supportFragmentManager
        val fragmentTransaction =
            fragmentManager.beginTransaction()
        if (v?.id == R.id.home_btm_ic) {
            val homeFrag = HomeFragment()
            fragmentTransaction.replace(R.id.HomeFragmentHolder, homeFrag)
            (findViewById<View>(R.id.ic_home) as ImageView).setImageDrawable(
                getDrawable(R.drawable.ic_home_selected)
            )
            (findViewById<View>(R.id.ic_search) as ImageView).setImageDrawable(
                getDrawable(R.drawable.ic_search_unselected)
            )
            (findViewById<View>(R.id.ic_star) as ImageView).setImageDrawable(
                getDrawable(R.drawable.ic_star_black)
            )
            (findViewById<View>(R.id.ic_user) as ImageView).setImageDrawable(
                getDrawable(R.drawable.ic_user_unselected)
            )
        } else if (v?.id == R.id.search_btm_ic) {
            val vulFrag = VulnerableFragment()
            fragmentTransaction.replace(R.id.HomeFragmentHolder, vulFrag)
            (findViewById<View>(R.id.ic_home) as ImageView).setImageDrawable(
                getDrawable(R.drawable.ic_home_unselected)
            )
            (findViewById<View>(R.id.ic_search) as ImageView).setImageDrawable(
                getDrawable(R.drawable.ic_search_selected)
            )
            (findViewById<View>(R.id.ic_star) as ImageView).setImageDrawable(
                getDrawable(R.drawable.ic_star_black)
            )
            (findViewById<View>(R.id.ic_user) as ImageView).setImageDrawable(
                getDrawable(R.drawable.ic_user_unselected)
            )
        } else if (v?.id == R.id.add_btm_ic) {
            (findViewById<View>(R.id.ic_home) as ImageView).setImageDrawable(
                getDrawable(R.drawable.ic_home_unselected)
            )
            (findViewById<View>(R.id.ic_search) as ImageView).setImageDrawable(
                getDrawable(R.drawable.ic_search_selected)
            )
            (findViewById<View>(R.id.ic_star) as ImageView).setImageDrawable(
                getDrawable(R.drawable.ic_star_black)
            )
            (findViewById<View>(R.id.ic_user) as ImageView).setImageDrawable(
                getDrawable(R.drawable.ic_user_unselected)
            )
        } else if (v?.id == R.id.star_btm_ic) {
            val taskFrag = TasksFragment()
            fragmentTransaction.replace(R.id.HomeFragmentHolder, taskFrag)
            (findViewById<View>(R.id.ic_home) as ImageView).setImageDrawable(
                getDrawable(R.drawable.ic_home_unselected)
            )
            (findViewById<View>(R.id.ic_search) as ImageView).setImageDrawable(
                getDrawable(R.drawable.ic_search_unselected)
            )
            (findViewById<View>(R.id.ic_star) as ImageView).setImageDrawable(
                getDrawable(R.drawable.ic_star_black_selected)
            )
            (findViewById<View>(R.id.ic_user) as ImageView).setImageDrawable(
                getDrawable(R.drawable.ic_user_unselected)
            )
        } else if (v?.id == R.id.user_btm_ic) {
            val taskFrag = ListTransactionsFragment()
            fragmentTransaction.replace(R.id.HomeFragmentHolder, taskFrag)
            (findViewById<View>(R.id.ic_home) as ImageView).setImageDrawable(
                getDrawable(R.drawable.ic_home_unselected)
            )
            (findViewById<View>(R.id.ic_search) as ImageView).setImageDrawable(
                getDrawable(R.drawable.ic_search_unselected)
            )
            (findViewById<View>(R.id.ic_star) as ImageView).setImageDrawable(
                getDrawable(R.drawable.ic_star_black)
            )
            (findViewById<View>(R.id.ic_user) as ImageView).setImageDrawable(
                getDrawable(R.drawable.ic_user_selected)
            )
        } else {
            (findViewById<View>(R.id.ic_home) as ImageView).setImageDrawable(
                getDrawable(R.drawable.ic_home_selected)
            )
            (findViewById<View>(R.id.ic_search) as ImageView).setImageDrawable(
                getDrawable(R.drawable.ic_search_unselected)
            )
            (findViewById<View>(R.id.ic_star) as ImageView).setImageDrawable(
                getDrawable(R.drawable.ic_star_black)
            )
            (findViewById<View>(R.id.ic_user) as ImageView).setImageDrawable(
                getDrawable(R.drawable.ic_user_unselected)
            )
        }
        fragmentTransaction.commit()
    }
}

