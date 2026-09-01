package com.shopirend.android.data

import kotlinx.serialization.Serializable

@Serializable data class User(val id: String, val email: String, val displayName: String)
@Serializable data class AuthResponse(val accessToken: String, val user: User)
@Serializable data class RegisterRequest(val email: String, val password: String, val displayName: String)
@Serializable data class LoginRequest(val email: String, val password: String)
@Serializable data class UpdateProfileRequest(val displayName: String)

@Serializable enum class FriendshipStatus { PENDING, ACCEPTED }
@Serializable data class Friendship(val id: String, val user: User, val status: FriendshipStatus, val incoming: Boolean)
@Serializable data class AddFriendRequest(val email: String)

@Serializable data class Store(val id: String, val code: String, val name: String)
@Serializable enum class TripStatus { ACTIVE, COMPLETED, CANCELLED }
@Serializable enum class TripItemStatus { REQUESTED, BOUGHT, NOT_FOUND, CANCELLED }
@Serializable data class StartTripRequest(val storeCode: String, val recipientIds: Set<String>)
@Serializable data class TripSummary(val id: String, val store: Store, val shopper: User, val status: TripStatus, val startedAt: String, val requestCount: Int, val mine: Boolean)
@Serializable data class TripItem(val id: String, val requestingUser: User, val itemName: String, val quantity: Int, val note: String? = null, val status: TripItemStatus, val createdAt: String)
@Serializable data class Trip(val id: String, val store: Store, val shopper: User, val status: TripStatus, val startedAt: String, val completedAt: String? = null, val recipientIds: List<String>, val requests: List<TripItem>, val mine: Boolean)
@Serializable data class AddTripItemRequest(val itemName: String, val quantity: Int, val note: String? = null)
@Serializable data class UpdateTripItemRequest(val status: TripItemStatus)
@Serializable data class UpdateTripRequest(val status: TripStatus)
@Serializable data class Home(val activeTrips: List<TripSummary>, val friendsTrips: List<TripSummary>, val recentActivity: List<String>)

@Serializable data class CreateNamedRequest(val name: String)
@Serializable data class ShoppingListItem(val id: String, val name: String, val quantity: Int, val checked: Boolean)
@Serializable data class ShoppingListModel(val id: String, val name: String, val items: List<ShoppingListItem>)
@Serializable data class AddListItemRequest(val name: String, val quantity: Int)
@Serializable data class UpdateListItemRequest(val checked: Boolean)

@Serializable enum class WishlistItemStatus { WANTED, ACQUIRED, ARCHIVED }
@Serializable data class WishlistSummary(val id: String, val name: String, val owner: User, val itemCount: Int, val mine: Boolean)
@Serializable data class WishlistItem(val id: String, val name: String, val description: String? = null, val preferredStore: Store? = null, val status: WishlistItemStatus)
@Serializable data class Wishlist(val id: String, val name: String, val owner: User, val memberIds: List<String>, val items: List<WishlistItem>, val mine: Boolean)
@Serializable data class AddWishlistItemRequest(val name: String, val description: String? = null, val preferredStoreId: String? = null)
@Serializable data class ShareWishlistRequest(val memberIds: Set<String>)
@Serializable data class UpdateWishlistItemRequest(val status: WishlistItemStatus)

@Serializable data class DeviceTokenRequest(val token: String)
@Serializable data class ErrorResponse(val status: Int? = null, val message: String = "Something went wrong")

