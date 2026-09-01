package com.shopirend.android.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

private data class Destination(val route: String, val label: String, val icon: ImageVector)
private val bottomDestinations = listOf(
    Destination("home", "Home", Icons.Outlined.Home),
    Destination("friends", "Friends", Icons.Outlined.People),
    Destination("lists", "Lists", Icons.Outlined.Checklist),
    Destination("wishlists", "Wishes", Icons.Outlined.FavoriteBorder),
    Destination("profile", "Profile", Icons.Outlined.Person),
)

@Composable
fun ShopirendApp(viewModel: AppViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AnimatedContent(state.restoringSession to state.authenticated, label = "session") { (restoring, authenticated) ->
        when {
            restoring -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            !authenticated -> AuthScreen(state.busy, state.error, viewModel::clearError, viewModel::login, viewModel::register)
            else -> AuthenticatedApp(state, viewModel)
        }
    }
}

@Composable
private fun AuthenticatedApp(state: AppUiState, viewModel: AppViewModel) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar(it); viewModel.clearError() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (currentRoute in bottomDestinations.map { it.route }) {
                NavigationBar {
                    bottomDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                nav.navigate(destination.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, destination.label) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            NavHost(nav, startDestination = "home") {
                composable("home") { HomeScreen(state, onRefresh = viewModel::refreshAll, onStart = { nav.navigate("start-trip") }, onTrip = { nav.navigate("trip/$it") }) }
                composable("friends") { FriendsScreen(state.friends, state.busy, onAdd = { nav.navigate("add-friend") }, onAccept = viewModel::acceptFriend, onRemove = viewModel::removeFriend) }
                composable("add-friend") { AddFriendScreen(state.busy, onBack = nav::popBackStack, onAdd = { email -> viewModel.addFriend(email) { nav.popBackStack() } }) }
                composable("start-trip") { StartTripScreen(state.stores, state.friends, state.busy, onBack = nav::popBackStack, onStart = { store, friends -> viewModel.startTrip(store, friends) { nav.navigate("trip/$it") { popUpTo("home") } } }) }
                composable("trip/{id}") { entry ->
                    val id = entry.arguments?.getString("id").orEmpty()
                    TripScreen(id, state.activeTrip, state.busy, viewModel::openTrip, viewModel::refreshTrip, viewModel::addTripItem, viewModel::updateTripItem, onClose = { status -> viewModel.closeTrip(id, status) { nav.popBackStack("home", false) } }, onBack = nav::popBackStack)
                }
                composable("lists") { ShoppingListsScreen(state.shoppingLists, state.home?.activeTrips.orEmpty(), state.busy, viewModel::createList, viewModel::addListItem, viewModel::checkListItem, viewModel::addListItemToTrip) }
                composable("wishlists") { WishlistsScreen(state.wishlists, state.busy, onOpen = { nav.navigate("wishlist/$it") }, onCreate = { name -> viewModel.createWishlist(name) { nav.navigate("wishlist/$it") } }) }
                composable("wishlist/{id}") { entry ->
                    val id = entry.arguments?.getString("id").orEmpty()
                    WishlistDetailsScreen(id, state.activeWishlist, state.friends, state.stores, state.busy, onLoad = viewModel::openWishlist, onBack = nav::popBackStack, onAdd = { name, description, storeId -> viewModel.addWishlistItem(id, name, description, storeId) }, onShare = { viewModel.shareWishlist(id, it) }, onStatus = { itemId, status -> viewModel.updateWishlistItem(id, itemId, status) })
                }
                composable("profile") { ProfileScreen(state.user, state.busy, onSave = viewModel::updateProfile, onLogout = viewModel::logout) }
            }
            if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth().align(Alignment.TopCenter))
        }
    }
}
