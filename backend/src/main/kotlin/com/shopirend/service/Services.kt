package com.shopirend.service

import com.shopirend.api.*
import com.shopirend.model.*
import com.shopirend.repository.*
import com.shopirend.security.JwtService
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

class ApiException(val status: HttpStatus, override val message: String) : RuntimeException(message)

private fun notFound(resource: String): Nothing = throw ApiException(HttpStatus.NOT_FOUND, "$resource not found")
private fun forbidden(message: String = "You do not have access to this resource"): Nothing = throw ApiException(HttpStatus.FORBIDDEN, message)

@Service
class AuthService(
    private val users: UserRepository,
    private val encoder: PasswordEncoder,
    private val jwt: JwtService,
) {
    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        val email = request.email.trim().lowercase()
        if (users.existsByEmailIgnoreCase(email)) throw ApiException(HttpStatus.CONFLICT, "Email is already registered")
        val passwordHash = requireNotNull(encoder.encode(request.password)) { "Password encoder returned no hash" }
        val user = users.save(UserEntity(email = email, passwordHash = passwordHash, displayName = request.displayName.trim()))
        return AuthResponse(jwt.create(user.id, user.email), user.toDto())
    }

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): AuthResponse {
        val user = users.findByEmailIgnoreCase(request.email.trim()) ?: throw ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password")
        if (!encoder.matches(request.password, user.passwordHash)) throw ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password")
        return AuthResponse(jwt.create(user.id, user.email), user.toDto())
    }

    @Transactional(readOnly = true)
    fun profile(userId: UUID) = (users.findById(userId).orElse(null) ?: notFound("User")).toDto()

    @Transactional
    fun updateProfile(userId: UUID, request: UpdateProfileRequest): UserDto {
        val user = users.findById(userId).orElse(null) ?: notFound("User")
        user.displayName = request.displayName.trim()
        return users.save(user).toDto()
    }
}

@Service
class FriendshipService(private val friendships: FriendshipRepository, private val users: UserRepository) {
    @Transactional(readOnly = true)
    fun list(userId: UUID): List<FriendshipDto> {
        val accepted = friendships.findForUser(userId, FriendshipStatus.ACCEPTED).map { it.toDtoFor(userId) }
        val incoming = friendships.findIncoming(userId).map { it.toDtoFor(userId) }
        return incoming + accepted
    }

    @Transactional
    fun add(userId: UUID, request: AddFriendRequest): FriendshipDto {
        val me = users.findById(userId).orElse(null) ?: notFound("User")
        val other = users.findByEmailIgnoreCase(request.email.trim()) ?: notFound("User")
        if (me.id == other.id) throw ApiException(HttpStatus.BAD_REQUEST, "You cannot add yourself")
        if (friendships.findBetween(me.id, other.id).isNotEmpty()) throw ApiException(HttpStatus.CONFLICT, "A friendship or request already exists")
        return friendships.save(FriendshipEntity(requester = me, addressee = other)).toDtoFor(userId)
    }

    @Transactional
    fun accept(userId: UUID, friendshipId: UUID): FriendshipDto {
        val friendship = friendships.findById(friendshipId).orElse(null) ?: notFound("Friend request")
        if (friendship.addressee.id != userId || friendship.status != FriendshipStatus.PENDING) forbidden("Only the recipient can accept this request")
        friendship.status = FriendshipStatus.ACCEPTED
        friendship.updatedAt = Instant.now()
        return friendships.save(friendship).toDtoFor(userId)
    }

    @Transactional
    fun remove(userId: UUID, friendshipId: UUID) {
        val friendship = friendships.findById(friendshipId).orElse(null) ?: notFound("Friendship")
        if (friendship.requester.id != userId && friendship.addressee.id != userId) forbidden()
        friendships.delete(friendship)
    }

    @Transactional(readOnly = true)
    fun requireAccepted(userId: UUID, friendIds: Collection<UUID>) {
        friendIds.forEach { friendId ->
            if (friendships.findBetween(userId, friendId).none { it.status == FriendshipStatus.ACCEPTED }) {
                throw ApiException(HttpStatus.BAD_REQUEST, "Every trip recipient must be an accepted friend")
            }
        }
    }

    private fun FriendshipEntity.toDtoFor(userId: UUID): FriendshipDto {
        val other = if (requester.id == userId) addressee else requester
        return FriendshipDto(id, other.toDto(), status, status == FriendshipStatus.PENDING && addressee.id == userId)
    }
}

@Service
class TripService(
    private val users: UserRepository,
    private val stores: StoreRepository,
    private val trips: ShoppingTripRepository,
    private val recipients: TripRecipientRepository,
    private val requests: TripRequestRepository,
    private val friendships: FriendshipService,
    private val notifications: NotificationService,
) {
    @Transactional(readOnly = true)
    fun stores() = stores.findAllByOrderByNameAsc().map { it.toDto() }

    @Transactional
    fun start(userId: UUID, request: StartTripRequest): TripDto {
        friendships.requireAccepted(userId, request.recipientIds)
        val shopper = users.findById(userId).orElse(null) ?: notFound("User")
        val store = stores.findByCode(request.storeCode.uppercase()) ?: notFound("Store")
        val trip = trips.save(ShoppingTripEntity(shopper = shopper, store = store))
        request.recipientIds.map { recipientId ->
            val user = users.findById(recipientId).orElse(null) ?: notFound("Friend")
            recipients.save(TripRecipientEntity(trip = trip, recipient = user))
        }
        notifications.send(request.recipientIds, "${shopper.displayName} is going shopping", "${shopper.displayName} started a trip to ${store.name}.", mapOf("tripId" to trip.id.toString(), "type" to "TRIP_STARTED"))
        return toDto(trip, userId)
    }

    @Transactional(readOnly = true)
    fun visible(userId: UUID) = trips.findVisibleActive(userId).map { toSummary(it, userId) }

    @Transactional(readOnly = true)
    fun details(userId: UUID, tripId: UUID): TripDto {
        val trip = trips.findById(tripId).orElse(null) ?: notFound("Trip")
        requireVisible(trip, userId)
        return toDto(trip, userId)
    }

    @Transactional
    fun addRequest(userId: UUID, tripId: UUID, request: AddTripRequest): TripRequestDto {
        val trip = trips.findById(tripId).orElse(null) ?: notFound("Trip")
        requireVisible(trip, userId)
        if (trip.status != TripStatus.ACTIVE) throw ApiException(HttpStatus.CONFLICT, "This trip is no longer active")
        val user = users.findById(userId).orElse(null) ?: notFound("User")
        val item = requests.save(TripRequestEntity(trip = trip, requestingUser = user, itemName = request.itemName.trim(), quantity = request.quantity, note = request.note?.trim()?.ifBlank { null }))
        if (trip.shopper.id != userId) notifications.send(setOf(trip.shopper.id), "New request for ${trip.store.name}", "${user.displayName} requested ${request.quantity}× ${request.itemName.trim()}.", mapOf("tripId" to trip.id.toString(), "type" to "REQUEST_ADDED"))
        return item.toDto()
    }

    @Transactional
    fun updateRequest(userId: UUID, tripId: UUID, requestId: UUID, update: UpdateTripRequestStatusRequest): TripRequestDto {
        val trip = trips.findById(tripId).orElse(null) ?: notFound("Trip")
        if (trip.shopper.id != userId) forbidden("Only the shopper can update requested items")
        if (trip.status != TripStatus.ACTIVE) throw ApiException(HttpStatus.CONFLICT, "This trip is no longer active")
        val item = requests.findById(requestId).orElse(null)?.takeIf { it.trip.id == tripId } ?: notFound("Request")
        item.status = update.status
        item.updatedAt = Instant.now()
        notifications.send(setOf(item.requestingUser.id), "Request updated", "${item.itemName} is now ${update.status.name.lowercase().replace('_', ' ')}.", mapOf("tripId" to trip.id.toString(), "type" to "REQUEST_UPDATED"))
        return requests.save(item).toDto()
    }

    @Transactional
    fun updateTrip(userId: UUID, tripId: UUID, update: UpdateTripStatusRequest): TripDto {
        val trip = trips.findById(tripId).orElse(null) ?: notFound("Trip")
        if (trip.shopper.id != userId) forbidden("Only the shopper can close this trip")
        if (update.status == TripStatus.ACTIVE) throw ApiException(HttpStatus.BAD_REQUEST, "A closed trip cannot be reopened")
        trip.status = update.status
        trip.completedAt = Instant.now()
        return toDto(trips.save(trip), userId)
    }

    @Transactional(readOnly = true)
    fun home(userId: UUID): HomeDto {
        val visible = trips.findVisibleActive(userId)
        val mine = visible.filter { it.shopper.id == userId }.map { toSummary(it, userId) }
        val friends = visible.filter { it.shopper.id != userId }.map { toSummary(it, userId) }
        val activity = visible.take(5).map { trip ->
            if (trip.shopper.id == userId) "Your ${trip.store.name} trip has ${requests.findByTripIdOrderByCreatedAtAsc(trip.id).size} requests"
            else "${trip.shopper.displayName} is shopping at ${trip.store.name}"
        }
        return HomeDto(mine, friends, activity)
    }

    private fun requireVisible(trip: ShoppingTripEntity, userId: UUID) {
        if (trip.shopper.id != userId && !recipients.existsByTripIdAndRecipientId(trip.id, userId)) forbidden()
    }

    private fun toSummary(trip: ShoppingTripEntity, userId: UUID) = TripSummaryDto(trip.id, trip.store.toDto(), trip.shopper.toDto(), trip.status, trip.startedAt, requests.findByTripIdOrderByCreatedAtAsc(trip.id).size, trip.shopper.id == userId)

    private fun toDto(trip: ShoppingTripEntity, userId: UUID) = TripDto(
        trip.id, trip.store.toDto(), trip.shopper.toDto(), trip.status, trip.startedAt, trip.completedAt,
        recipients.findByTripId(trip.id).map { it.recipient.id }, requests.findByTripIdOrderByCreatedAtAsc(trip.id).map { it.toDto() }, trip.shopper.id == userId,
    )

    private fun TripRequestEntity.toDto() = TripRequestDto(id, requestingUser.toDto(), itemName, quantity, note, status, createdAt)
}

@Service
class ShoppingListService(
    private val users: UserRepository,
    private val lists: ShoppingListRepository,
    private val items: ShoppingListItemRepository,
    private val trips: TripService,
) {
    @Transactional(readOnly = true)
    fun list(userId: UUID) = lists.findByOwnerIdOrderByCreatedAtDesc(userId).map { it.toDto() }

    @Transactional
    fun create(userId: UUID, request: CreateNamedRequest): ShoppingListDto {
        val owner = users.findById(userId).orElse(null) ?: notFound("User")
        return lists.save(ShoppingListEntity(owner = owner, name = request.name.trim())).toDto()
    }

    @Transactional
    fun addItem(userId: UUID, listId: UUID, request: AddListItemRequest): ShoppingListDto {
        val list = owned(userId, listId)
        items.save(ShoppingListItemEntity(list = list, name = request.name.trim(), quantity = request.quantity))
        return list.toDto()
    }

    @Transactional
    fun checkItem(userId: UUID, listId: UUID, itemId: UUID, request: UpdateListItemRequest): ShoppingListDto {
        val list = owned(userId, listId)
        val item = items.findById(itemId).orElse(null)?.takeIf { it.list.id == listId } ?: notFound("List item")
        item.checked = request.checked
        items.save(item)
        return list.toDto()
    }

    @Transactional
    fun deleteItem(userId: UUID, listId: UUID, itemId: UUID) {
        owned(userId, listId)
        val item = items.findById(itemId).orElse(null)?.takeIf { it.list.id == listId } ?: notFound("List item")
        items.delete(item)
    }

    @Transactional
    fun addToTrip(userId: UUID, listId: UUID, itemId: UUID, tripId: UUID): TripRequestDto {
        owned(userId, listId)
        val item = items.findById(itemId).orElse(null)?.takeIf { it.list.id == listId } ?: notFound("List item")
        return trips.addRequest(userId, tripId, AddTripRequest(item.name, item.quantity))
    }

    private fun owned(userId: UUID, listId: UUID) = lists.findById(listId).orElse(null)?.takeIf { it.owner.id == userId } ?: forbidden("Shopping lists are private")
    private fun ShoppingListEntity.toDto() = ShoppingListDto(id, name, items.findByListIdOrderByNameAsc(id).map { ShoppingListItemDto(it.id, it.name, it.quantity, it.checked) })
}

@Service
class WishlistService(
    private val users: UserRepository,
    private val wishlists: WishlistRepository,
    private val members: WishlistMemberRepository,
    private val items: WishlistItemRepository,
    private val stores: StoreRepository,
    private val friendships: FriendshipService,
) {
    @Transactional(readOnly = true)
    fun list(userId: UUID) = wishlists.findVisible(userId).map { w -> WishlistSummaryDto(w.id, w.name, w.owner.toDto(), items.findByWishlistIdOrderByCreatedAtDesc(w.id).size, w.owner.id == userId) }

    @Transactional(readOnly = true)
    fun details(userId: UUID, wishlistId: UUID): WishlistDto {
        val wishlist = visible(userId, wishlistId)
        return wishlist.toDto(userId)
    }

    @Transactional
    fun create(userId: UUID, request: CreateNamedRequest): WishlistDto {
        val owner = users.findById(userId).orElse(null) ?: notFound("User")
        return wishlists.save(WishlistEntity(owner = owner, name = request.name.trim())).toDto(userId)
    }

    @Transactional
    fun share(userId: UUID, wishlistId: UUID, request: ShareWishlistRequest): WishlistDto {
        val wishlist = owned(userId, wishlistId)
        friendships.requireAccepted(userId, request.memberIds)
        members.deleteByWishlistId(wishlistId)
        request.memberIds.forEach { memberId -> members.save(WishlistMemberEntity(wishlist = wishlist, member = users.findById(memberId).orElse(null) ?: notFound("Friend"))) }
        return wishlist.toDto(userId)
    }

    @Transactional
    fun addItem(userId: UUID, wishlistId: UUID, request: AddWishlistItemRequest): WishlistDto {
        val wishlist = owned(userId, wishlistId)
        val store = request.preferredStoreId?.let { stores.findById(it).orElse(null) ?: notFound("Store") }
        items.save(WishlistItemEntity(wishlist = wishlist, name = request.name.trim(), description = request.description?.trim()?.ifBlank { null }, preferredStore = store))
        return wishlist.toDto(userId)
    }

    @Transactional
    fun updateItem(userId: UUID, wishlistId: UUID, itemId: UUID, request: UpdateWishlistItemRequest): WishlistDto {
        val wishlist = owned(userId, wishlistId)
        val item = items.findById(itemId).orElse(null)?.takeIf { it.wishlist.id == wishlistId } ?: notFound("Wishlist item")
        item.status = request.status
        items.save(item)
        return wishlist.toDto(userId)
    }

    private fun visible(userId: UUID, id: UUID): WishlistEntity = wishlists.findById(id).orElse(null)?.takeIf { it.owner.id == userId || members.existsByWishlistIdAndMemberId(id, userId) } ?: forbidden()
    private fun owned(userId: UUID, id: UUID): WishlistEntity = wishlists.findById(id).orElse(null)?.takeIf { it.owner.id == userId } ?: forbidden("Only the wishlist owner can make changes")
    private fun WishlistEntity.toDto(userId: UUID) = WishlistDto(id, name, owner.toDto(), members.findByWishlistId(id).map { it.member.id }, items.findByWishlistIdOrderByCreatedAtDesc(id).map { WishlistItemDto(it.id, it.name, it.description, it.preferredStore?.toDto(), it.status) }, owner.id == userId)
}

@Service
class DeviceTokenService(private val users: UserRepository, private val tokens: DeviceTokenRepository) {
    @Transactional
    fun register(userId: UUID, request: DeviceTokenRequest) {
        val user = users.findById(userId).orElse(null) ?: notFound("User")
        val token = tokens.findByToken(request.token) ?: DeviceTokenEntity(user = user, token = request.token)
        token.user = user
        token.updatedAt = Instant.now()
        tokens.save(token)
    }
}
