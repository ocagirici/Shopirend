package com.shopirend.android.data

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopirendRepository @Inject constructor(private val api: ShopirendApi, private val session: SessionManager) {
    suspend fun restoreSession() = session.restore()
    suspend fun logout() = session.clear()

    suspend fun register(name: String, email: String, password: String): AuthResponse =
        api.post<AuthResponse, RegisterRequest>("auth/register", RegisterRequest(email, password, name)).also { session.save(it.accessToken) }

    suspend fun login(email: String, password: String): AuthResponse =
        api.post<AuthResponse, LoginRequest>("auth/login", LoginRequest(email, password)).also { session.save(it.accessToken) }

    suspend fun profile(): User = api.get("profile")
    suspend fun updateProfile(name: String): User = api.put<User, UpdateProfileRequest>("profile", UpdateProfileRequest(name))
    suspend fun registerDeviceToken(token: String) = api.postNoContent("profile/device-token", DeviceTokenRequest(token))

    suspend fun friends(): List<Friendship> = api.get("friends")
    suspend fun addFriend(email: String): Friendship = api.post<Friendship, AddFriendRequest>("friends", AddFriendRequest(email))
    suspend fun acceptFriend(id: String): Friendship = api.post<Friendship, Unit>("friends/$id/accept", Unit)
    suspend fun removeFriend(id: String) = api.delete("friends/$id")

    suspend fun stores(): List<Store> = api.get("stores")
    suspend fun home(): Home = api.get("home")
    suspend fun trips(): List<TripSummary> = api.get("trips")
    suspend fun startTrip(storeCode: String, recipients: Set<String>): Trip = api.post<Trip, StartTripRequest>("trips", StartTripRequest(storeCode, recipients))
    suspend fun trip(id: String): Trip = api.get("trips/$id")
    suspend fun addTripItem(id: String, name: String, quantity: Int, note: String?): TripItem = api.post<TripItem, AddTripItemRequest>("trips/$id/requests", AddTripItemRequest(name, quantity, note))
    suspend fun updateTripItem(tripId: String, itemId: String, status: TripItemStatus): TripItem = api.patch<TripItem, UpdateTripItemRequest>("trips/$tripId/requests/$itemId", UpdateTripItemRequest(status))
    suspend fun closeTrip(id: String, status: TripStatus): Trip = api.patch<Trip, UpdateTripRequest>("trips/$id", UpdateTripRequest(status))

    suspend fun shoppingLists(): List<ShoppingListModel> = api.get("lists")
    suspend fun createShoppingList(name: String): ShoppingListModel = api.post<ShoppingListModel, CreateNamedRequest>("lists", CreateNamedRequest(name))
    suspend fun addListItem(listId: String, name: String, quantity: Int): ShoppingListModel = api.post<ShoppingListModel, AddListItemRequest>("lists/$listId/items", AddListItemRequest(name, quantity))
    suspend fun checkListItem(listId: String, itemId: String, checked: Boolean): ShoppingListModel = api.patch<ShoppingListModel, UpdateListItemRequest>("lists/$listId/items/$itemId", UpdateListItemRequest(checked))
    suspend fun addListItemToTrip(listId: String, itemId: String, tripId: String): TripItem = api.post<TripItem, Unit>("lists/$listId/items/$itemId/trip/$tripId", Unit)

    suspend fun wishlists(): List<WishlistSummary> = api.get("wishlists")
    suspend fun wishlist(id: String): Wishlist = api.get("wishlists/$id")
    suspend fun createWishlist(name: String): Wishlist = api.post<Wishlist, CreateNamedRequest>("wishlists", CreateNamedRequest(name))
    suspend fun addWishlistItem(id: String, name: String, description: String?, storeId: String?): Wishlist = api.post<Wishlist, AddWishlistItemRequest>("wishlists/$id/items", AddWishlistItemRequest(name, description, storeId))
    suspend fun shareWishlist(id: String, memberIds: Set<String>): Wishlist = api.put<Wishlist, ShareWishlistRequest>("wishlists/$id/members", ShareWishlistRequest(memberIds))
    suspend fun updateWishlistItem(wishlistId: String, itemId: String, status: WishlistItemStatus): Wishlist = api.patch<Wishlist, UpdateWishlistItemRequest>("wishlists/$wishlistId/items/$itemId", UpdateWishlistItemRequest(status))
}
