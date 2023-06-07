package com.example.piashop.ui.views

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.piashop.R
import com.example.piashop.integration.ShopTabViewModelApi


/**
 * Routes(screens) or navigation items for the home screen
 */
private sealed class Tab(
    val route: String,
    @StringRes val labelResourceId: Int,
    val icon: ImageVector
) {
    object Shop : Tab("shop", R.string.shop, Icons.Filled.ShoppingCart)
    object Merchant : Tab("merchant", R.string.merchant, Icons.Filled.Home)
    object User : Tab("user", R.string.user, Icons.Filled.Person)
    object SdkUi : Tab("sdk_ui", R.string.sdk_ui, Icons.Filled.Settings)
}

/** Contains bottom tab navigation views. */
@Composable
fun NavigationView(
    shopTabViewModelApi: ShopTabViewModelApi,
    cardEntryView: @Composable () -> Unit
) {

    val bottomNavController: NavHostController = rememberNavController()

    // Tabs will appear in the order seen here, left to right.
    val tabs: Set<Tab> = setOf(Tab.Shop, Tab.Merchant, Tab.User, Tab.SdkUi)

    val renderTab: @Composable (Tab) -> Unit = { tab ->
        when(tab) {
            Tab.Shop -> ShopTab(
                viewModelApi = shopTabViewModelApi,
                cardEntryView = cardEntryView
            )
            Tab.Merchant -> MerchantSettingsTab()
            Tab.SdkUi -> UiCustomizationTab()
            Tab.User -> UserSettingsTab()
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(bottomNavController = bottomNavController, navigationItems = tabs.toList())
        }
    ) { innerPadding ->

        NavHost(
            navController = bottomNavController,
            startDestination = tabs.first().route,
            modifier = Modifier.padding(innerPadding)
        ) {
            tabs.forEach { tab -> composable(tab.route, content = { renderTab(tab) }) }
        }
    }
}

/** Bottom navigation bar for the home screen */
@Composable
private fun NavigationBar(
    bottomNavController: NavHostController,
    navigationItems: List<Tab>
) {

    BottomNavigation {
        val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        navigationItems.forEach { tab ->

            BottomNavigationItem(
                icon = {
                    Icon(tab.icon, contentDescription = stringResource(id = tab.labelResourceId))
                },
                label = { Text(text = stringResource(id = tab.labelResourceId)) },
                selectedContentColor = MaterialTheme.colors.primaryVariant,
                unselectedContentColor = LocalContentColor.current.copy(0.4f),
                selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                onClick = { bottomNavController.onSelected(tab = tab) }
            )
        }
    }
}

private fun NavHostController.onSelected(tab: Tab) {
    // Below code is referred from
    // https://developer.android.com/jetpack/compose/navigation#bottom-nav
    navigate(tab.route) {

        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        // on the back stack as users select items
        popUpTo(graph.findStartDestination().id) {
            this.saveState = true
        }

        // Avoid multiple copies of the same destination when
        // re-selecting the same item
        launchSingleTop = true
        // Restore state when re-selecting a previously selected item
        restoreState = true
    }
}