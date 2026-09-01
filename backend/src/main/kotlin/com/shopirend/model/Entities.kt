package com.shopirend.model

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

enum class FriendshipStatus { PENDING, ACCEPTED }
enum class TripStatus { ACTIVE, COMPLETED, CANCELLED }
enum class TripRequestStatus { REQUESTED, BOUGHT, NOT_FOUND, CANCELLED }
enum class WishlistItemStatus { WANTED, ACQUIRED, ARCHIVED }

@Entity
@Table(name = "app_users")
class UserEntity(
    @Id var id: UUID = UUID.randomUUID(),
    @Column(nullable = false, unique = true) var email: String = "",
    @Column(name = "password_hash", nullable = false) var passwordHash: String = "",
    @Column(name = "display_name", nullable = false) var displayName: String = "",
    @Column(name = "created_at", nullable = false) var createdAt: Instant = Instant.now(),
)

@Entity
@Table(name = "friendships")
class FriendshipEntity(
    @Id var id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "requester_id") var requester: UserEntity = UserEntity(),
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "addressee_id") var addressee: UserEntity = UserEntity(),
    @Enumerated(EnumType.STRING) var status: FriendshipStatus = FriendshipStatus.PENDING,
    @Column(name = "created_at") var createdAt: Instant = Instant.now(),
    @Column(name = "updated_at") var updatedAt: Instant = Instant.now(),
)

@Entity
@Table(name = "stores")
class StoreEntity(
    @Id var id: UUID = UUID.randomUUID(),
    @Column(nullable = false, unique = true) var code: String = "",
    @Column(nullable = false) var name: String = "",
)

@Entity
@Table(name = "shopping_trips")
class ShoppingTripEntity(
    @Id var id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "shopper_id") var shopper: UserEntity = UserEntity(),
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "store_id") var store: StoreEntity = StoreEntity(),
    @Enumerated(EnumType.STRING) var status: TripStatus = TripStatus.ACTIVE,
    @Column(name = "started_at") var startedAt: Instant = Instant.now(),
    @Column(name = "completed_at") var completedAt: Instant? = null,
)

@Entity
@Table(name = "trip_recipients")
class TripRecipientEntity(
    @Id var id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "trip_id") var trip: ShoppingTripEntity = ShoppingTripEntity(),
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "recipient_id") var recipient: UserEntity = UserEntity(),
)

@Entity
@Table(name = "trip_requests")
class TripRequestEntity(
    @Id var id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "trip_id") var trip: ShoppingTripEntity = ShoppingTripEntity(),
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "requesting_user_id") var requestingUser: UserEntity = UserEntity(),
    @Column(name = "item_name") var itemName: String = "",
    var quantity: Int = 1,
    var note: String? = null,
    @Enumerated(EnumType.STRING) var status: TripRequestStatus = TripRequestStatus.REQUESTED,
    @Column(name = "created_at") var createdAt: Instant = Instant.now(),
    @Column(name = "updated_at") var updatedAt: Instant = Instant.now(),
)

@Entity
@Table(name = "shopping_lists")
class ShoppingListEntity(
    @Id var id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "owner_id") var owner: UserEntity = UserEntity(),
    var name: String = "",
    @Column(name = "created_at") var createdAt: Instant = Instant.now(),
)

@Entity
@Table(name = "shopping_list_items")
class ShoppingListItemEntity(
    @Id var id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "list_id") var list: ShoppingListEntity = ShoppingListEntity(),
    var name: String = "",
    var quantity: Int = 1,
    var checked: Boolean = false,
)

@Entity
@Table(name = "wishlists")
class WishlistEntity(
    @Id var id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "owner_id") var owner: UserEntity = UserEntity(),
    var name: String = "",
    @Column(name = "created_at") var createdAt: Instant = Instant.now(),
)

@Entity
@Table(name = "wishlist_members")
class WishlistMemberEntity(
    @Id var id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "wishlist_id") var wishlist: WishlistEntity = WishlistEntity(),
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "member_id") var member: UserEntity = UserEntity(),
)

@Entity
@Table(name = "wishlist_items")
class WishlistItemEntity(
    @Id var id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "wishlist_id") var wishlist: WishlistEntity = WishlistEntity(),
    var name: String = "",
    var description: String? = null,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "preferred_store_id") var preferredStore: StoreEntity? = null,
    @Enumerated(EnumType.STRING) var status: WishlistItemStatus = WishlistItemStatus.WANTED,
    @Column(name = "created_at") var createdAt: Instant = Instant.now(),
)

@Entity
@Table(name = "device_tokens")
class DeviceTokenEntity(
    @Id var id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") var user: UserEntity = UserEntity(),
    @Column(nullable = false, unique = true) var token: String = "",
    var platform: String = "ANDROID",
    @Column(name = "updated_at") var updatedAt: Instant = Instant.now(),
)

