package com.shopirend.api

import com.shopirend.model.*
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant
import java.util.UUID

data class RegisterRequest(@field:Email val email: String, @field:Size(min = 8, max = 100) val password: String, @field:NotBlank @field:Size(max = 100) val displayName: String)
data class LoginRequest(@field:Email val email: String, @field:NotBlank val password: String)
data class AuthResponse(val accessToken: String, val user: UserDto)
data class UserDto(val id: UUID, val email: String, val displayName: String)
data class UpdateProfileRequest(@field:NotBlank @field:Size(max = 100) val displayName: String)

data class AddFriendRequest(@field:Email val email: String)
data class FriendshipDto(val id: UUID, val user: UserDto, val status: FriendshipStatus, val incoming: Boolean)

data class StoreDto(val id: UUID, val code: String, val name: String)
data class StartTripRequest(@field:NotBlank val storeCode: String, val recipientIds: Set<UUID>)
data class TripSummaryDto(val id: UUID, val store: StoreDto, val shopper: UserDto, val status: TripStatus, val startedAt: Instant, val requestCount: Int, val mine: Boolean)
data class TripDto(val id: UUID, val store: StoreDto, val shopper: UserDto, val status: TripStatus, val startedAt: Instant, val completedAt: Instant?, val recipientIds: List<UUID>, val requests: List<TripRequestDto>, val mine: Boolean)
data class AddTripRequest(@field:NotBlank @field:Size(max = 160) val itemName: String, @field:Min(1) @field:Max(99) val quantity: Int = 1, @field:Size(max = 500) val note: String? = null)
data class UpdateTripRequestStatusRequest(val status: TripRequestStatus)
data class UpdateTripStatusRequest(val status: TripStatus)
data class TripRequestDto(val id: UUID, val requestingUser: UserDto, val itemName: String, val quantity: Int, val note: String?, val status: TripRequestStatus, val createdAt: Instant)
data class HomeDto(val activeTrips: List<TripSummaryDto>, val friendsTrips: List<TripSummaryDto>, val recentActivity: List<String>)

data class CreateNamedRequest(@field:NotBlank @field:Size(max = 100) val name: String)
data class AddListItemRequest(@field:NotBlank @field:Size(max = 160) val name: String, @field:Min(1) @field:Max(99) val quantity: Int = 1)
data class UpdateListItemRequest(val checked: Boolean)
data class ShoppingListDto(val id: UUID, val name: String, val items: List<ShoppingListItemDto>)
data class ShoppingListItemDto(val id: UUID, val name: String, val quantity: Int, val checked: Boolean)

data class ShareWishlistRequest(val memberIds: Set<UUID>)
data class AddWishlistItemRequest(@field:NotBlank @field:Size(max = 160) val name: String, @field:Size(max = 500) val description: String? = null, val preferredStoreId: UUID? = null)
data class UpdateWishlistItemRequest(val status: WishlistItemStatus)
data class WishlistSummaryDto(val id: UUID, val name: String, val owner: UserDto, val itemCount: Int, val mine: Boolean)
data class WishlistDto(val id: UUID, val name: String, val owner: UserDto, val memberIds: List<UUID>, val items: List<WishlistItemDto>, val mine: Boolean)
data class WishlistItemDto(val id: UUID, val name: String, val description: String?, val preferredStore: StoreDto?, val status: WishlistItemStatus)

data class DeviceTokenRequest(@field:NotBlank @field:Size(max = 512) val token: String)

fun UserEntity.toDto() = UserDto(id, email, displayName)
fun StoreEntity.toDto() = StoreDto(id, code, name)

