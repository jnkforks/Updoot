package com.ducktapedapps.updoot.ui

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.widget.ViewPager2.ORIENTATION_HORIZONTAL
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.UpdootApplication
import com.ducktapedapps.updoot.backgroundWork.cacheCleanUp.enqueueCleanUpWork
import com.ducktapedapps.updoot.databinding.ActivityMainBinding
import com.ducktapedapps.updoot.ui.navDrawer.NavDrawerPagerAdapter
import com.ducktapedapps.updoot.ui.navDrawer.ScrimVisibilityAdjuster
import com.ducktapedapps.updoot.ui.navDrawer.ToolbarMenuSwapper
import com.ducktapedapps.updoot.ui.navDrawer.accounts.AccountsAdapter
import com.ducktapedapps.updoot.ui.navDrawer.destinations.NavDrawerDestinationAdapter
import com.ducktapedapps.updoot.ui.navDrawer.subscriptions.SubscriptionsAdapter
import com.ducktapedapps.updoot.utils.accountManagement.IRedditClient
import com.ducktapedapps.updoot.utils.accountManagement.RedditClient
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject


class MainActivity : AppCompatActivity(), IRedditClient.AccountChangeListener, NavController.OnDestinationChangedListener {
    @Inject
    lateinit var redditClient: RedditClient

    @Inject
    lateinit var activityVMFactory: ActivityVMFactory
    private val viewModel by lazy { ViewModelProvider(this@MainActivity, activityVMFactory).get(ActivityVM::class.java) }

    private lateinit var binding: ActivityMainBinding
    private val navController by lazy { findNavController(R.id.nav_host_fragment) }
    private val accountsAdapter = AccountsAdapter(object : AccountsAdapter.AccountAction {

        override fun login() = navController.navigate(R.id.loginActivity)

        override fun switch(accountName: String) {
            viewModel.setCurrentAccount(accountName)
            if (bottomNavigationDrawer.isInFocus()) bottomNavigationDrawer.collapse()
        }

        override fun logout(accountName: String) = viewModel.logout(accountName)

        override fun toggleEntryMenu() = viewModel.expandOrCollapseAccountsMenu()
    })
    private val subscriptionAdapter = SubscriptionsAdapter(object : SubscriptionsAdapter.ClickHandler {
        override fun goToSubreddit(subredditName: String) = navController.navigate(R.id.SubredditDestination)
    })
    private val navDrawerDestinationAdapter = NavDrawerDestinationAdapter()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_settings -> {
                navController.navigate(R.id.SettingsDestination)
                true
            }
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as UpdootApplication).updootComponent.inject(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpViews()

        setUpViewModel()

        redditClient.attachListener(this)

        setUpStatusBarColors()
    }

    private fun setUpViewModel() {
        viewModel.apply {
            accounts.observe(this@MainActivity) {
                accountsAdapter.submitList(it)
            }
            navigationEntries.observe(this@MainActivity) {
                navDrawerDestinationAdapter.submitList(it)
            }
            subredditSubscription.observe(this@MainActivity) {
                subscriptionAdapter.submitList(it)
            }
        }
    }

    private fun setUpStatusBarColors() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            window.statusBarColor = Color.BLACK
        }
    }

    override fun onBackPressed() {
        with(binding.bottomNavigationDrawer) {
            if (isInFocus()) collapse()
            else super.onBackPressed()
        }
    }

    private fun setUpViews() {
        binding.apply {
            navController.addOnDestinationChangedListener(this@MainActivity)

            scrimView.setOnClickListener { bottomNavigationDrawer.toggleState() }

            bottomNavigationDrawer.apply {

                addOnSlideAction(ScrimVisibilityAdjuster(scrimView))

                addOnStateChangeAction(ToolbarMenuSwapper(binding.toolbar, ::getCurrentDestinationMenu))

                binding.toolbar.apply {
                    setSupportActionBar(this)
                    setupWithNavController(navController)
                    setOnClickListener { bottomNavigationDrawer.toggleState() }
                }

                binding.viewPager.apply {
                    orientation = ORIENTATION_HORIZONTAL
                    adapter = NavDrawerPagerAdapter(
                            pageOneAdapter = listOf(accountsAdapter, navDrawerDestinationAdapter),
                            pageTwoAdapter = listOf(subscriptionAdapter)
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        setUpWorkers()
        redditClient.detachListener()
    }

    override fun currentAccountChanged() = viewModel.reloadContent()

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        when (destination.id) {
            R.id.SubredditDestination -> {
                bottomNavigationDrawer.show()
                navController.currentDestination?.label = arguments?.getString("subreddit").run { if (isNullOrEmpty()) "Updoot" else this }
            }
            R.id.CommentsDestination -> {
                bottomNavigationDrawer.show()
                navController.currentDestination?.label = "Comments"
            }
            R.id.SettingsDestination -> {
                bottomNavigationDrawer.show()
                navController.currentDestination?.label = "Settings"
            }
            R.id.ExploreDestination -> {
                bottomNavigationDrawer.hide()
                navController.currentDestination?.label = "Explore"
            }
            else -> navController.currentDestination?.label = "Updoot"
        }
        bottomNavigationDrawer.post { /*to let behaviour be initialized*/
            if (bottomNavigationDrawer.isInFocus()) bottomNavigationDrawer.collapse()
        }
    }

    private fun setUpWorkers() = enqueueCleanUpWork(this)

    private fun getCurrentDestinationMenu(): Int? =
            when (navController.currentDestination?.id) {
                R.id.SubredditDestination -> R.menu.subreddit_screen_menu
                R.id.CommentsDestination -> R.menu.comment_screen_menu
                else -> null
            }
}