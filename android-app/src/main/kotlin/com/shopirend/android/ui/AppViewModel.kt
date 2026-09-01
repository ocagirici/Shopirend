package com.shopirend.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopirend.android.data.*
import com.shopirend.android.notification.FirebaseTokenRegistrar
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppUiState(
    val restoringSession: Boolean = true,
    val authenticated: Boolean = false,
    val busy: Boolean = false,
    val error: String? = null,
    val user: User? = null,
    val home: Home? = null,
    val friends: List<Friendship> = emptyList(),
    val stores: List<Store> = emptyList(),
    val shoppingLists: List<ShoppingListModel> = emptyList(),
    val wishlists: List<WishlistSummary> = emptyList(),
    val activeTrip: Trip? = null,
    val activeWishlist: Wishlist? = null,
)

@HiltViewModel
class AppViewModel @Inject constructor(
    private val repository: ShopirendRepository,
    private val firebaseTokens: FirebaseTokenRegistrar,
) : ViewModel() {
    private val _state = MutableStateFlow(AppUiState())
    val state: StateFlow<AppUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val token = repository.restoreSession()
            if (token == null) _state.update { it.copy(restoringSession = false) }
            else runCatching { refreshAllInternal() }
                .onSuccess { _state.update { it.copy(authenticated = true, restoringSession = false) } }
                .onFailure { repository.logout(); _state.update { it.copy(restoringSession = false, error = "Your session expired. Please sign in again.") } }
        }
    }

    fun clearError() = _state.update { it.copy(error = null) }

    fun login(email: String, password: String) = action {
        val auth = repository.login(email.trim(), password)
        _state.update { it.copy(authenticated = true, user = auth.user) }
        refreshAllInternal()
    }

    fun register(name: String, email: String, password: String) = action {
        val auth = repository.register(name.trim(), email.trim(), password)
        _state.update { it.copy(authenticated = true, user = auth.user) }
        refreshAllInternal()
    }

    fun logout() = viewModelScope.launch {
        repository.logout()
        _state.value = AppUiState(restoringSession = false)
    }

    fun refreshAll() = action { refreshAllInternal() }

    private suspend fun refreshAllInternal() = coroutineScope {
        val profile = async { repository.profile() }
        val home = async { repository.home() }
        val friends = async { repository.friends() }
        val stores = async { repository.stores() }
        val lists = async { repository.shoppingLists() }
        val wishlists = async { repository.wishlists() }
        val profileResult = profile.await()
        val homeResult = home.await()
        val friendsResult = friends.await()
        val storesResult = stores.await()
        val listsResult = lists.await()
        val wishlistsResult = wishlists.await()
        _state.update {
            it.copy(
                authenticated = true,
                user = profileResult,
                home = homeResult,
                friends = friendsResult,
                stores = storesResult,
                shoppingLists = listsResult,
                wishlists = wishlistsResult,
            )
        }
        firebaseTokens.registerIfConfigured()
    }

    fun addFriend(email: String, done: () -> Unit = {}) = action(done) {
        repository.addFriend(email.trim())
        _state.update { it.copy(friends = repository.friends()) }
    }

    fun acceptFriend(id: String) = action {
        repository.acceptFriend(id)
        _state.update { it.copy(friends = repository.friends()) }
    }

    fun removeFriend(id: String) = action {
        repository.removeFriend(id)
        _state.update { it.copy(friends = repository.friends()) }
    }

    fun startTrip(storeCode: String, recipientIds: Set<String>, done: (String) -> Unit) = action {
        val trip = repository.startTrip(storeCode, recipientIds)
        _state.update { it.copy(activeTrip = trip, home = repository.home()) }
        done(trip.id)
    }

    fun openTrip(id: String) = action {
        _state.update { it.copy(activeTrip = repository.trip(id)) }
    }

    fun refreshTrip(id: String) = viewModelScope.launch {
        runCatching { repository.trip(id) }.onSuccess { trip -> _state.update { it.copy(activeTrip = trip) } }
    }

    fun addTripItem(tripId: String, name: String, quantity: Int, note: String?, done: () -> Unit = {}) = action(done) {
        repository.addTripItem(tripId, name.trim(), quantity, note?.trim())
        _state.update { it.copy(activeTrip = repository.trip(tripId), home = repository.home()) }
    }

    fun updateTripItem(tripId: String, itemId: String, status: TripItemStatus) = action {
        repository.updateTripItem(tripId, itemId, status)
        _state.update { it.copy(activeTrip = repository.trip(tripId)) }
    }

    fun closeTrip(tripId: String, status: TripStatus, done: () -> Unit) = action(done) {
        repository.closeTrip(tripId, status)
        _state.update { it.copy(activeTrip = null, home = repository.home()) }
    }

    fun createList(name: String) = action {
        repository.createShoppingList(name.trim())
        _state.update { it.copy(shoppingLists = repository.shoppingLists()) }
    }

    fun addListItem(listId: String, name: String, quantity: Int) = action {
        repository.addListItem(listId, name.trim(), quantity)
        _state.update { it.copy(shoppingLists = repository.shoppingLists()) }
    }

    fun checkListItem(listId: String, itemId: String, checked: Boolean) = action {
        repository.checkListItem(listId, itemId, checked)
        _state.update { it.copy(shoppingLists = repository.shoppingLists()) }
    }

    fun addListItemToTrip(listId: String, itemId: String, tripId: String) = action {
        repository.addListItemToTrip(listId, itemId, tripId)
        _state.update { it.copy(activeTrip = repository.trip(tripId)) }
    }

    fun createWishlist(name: String, done: (String) -> Unit) = action {
        val wishlist = repository.createWishlist(name.trim())
        _state.update { it.copy(activeWishlist = wishlist, wishlists = repository.wishlists()) }
        done(wishlist.id)
    }

    fun openWishlist(id: String) = action { _state.update { it.copy(activeWishlist = repository.wishlist(id)) } }

    fun addWishlistItem(id: String, name: String, description: String?, storeId: String?) = action {
        _state.update { it.copy(activeWishlist = repository.addWishlistItem(id, name.trim(), description?.trim(), storeId), wishlists = repository.wishlists()) }
    }

    fun shareWishlist(id: String, memberIds: Set<String>) = action {
        _state.update { it.copy(activeWishlist = repository.shareWishlist(id, memberIds)) }
    }

    fun updateWishlistItem(wishlistId: String, itemId: String, status: WishlistItemStatus) = action {
        _state.update { it.copy(activeWishlist = repository.updateWishlistItem(wishlistId, itemId, status)) }
    }

    fun updateProfile(name: String) = action {
        _state.update { it.copy(user = repository.updateProfile(name.trim())) }
    }

    private fun action(done: () -> Unit = {}, block: suspend () -> Unit) = viewModelScope.launch {
        _state.update { it.copy(busy = true, error = null) }
        runCatching { block() }
            .onSuccess { done() }
            .onFailure { error -> _state.update { it.copy(error = error.message ?: "Something went wrong") } }
        _state.update { it.copy(busy = false) }
    }
}
