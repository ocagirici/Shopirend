package com.shopirend.repository

import com.shopirend.model.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface UserRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmailIgnoreCase(email: String): UserEntity?
    fun existsByEmailIgnoreCase(email: String): Boolean
}

interface FriendshipRepository : JpaRepository<FriendshipEntity, UUID> {
    @Query("select f from FriendshipEntity f where (f.requester.id = :userId or f.addressee.id = :userId) and f.status = :status order by f.updatedAt desc")
    fun findForUser(userId: UUID, status: FriendshipStatus): List<FriendshipEntity>

    @Query("select f from FriendshipEntity f where f.addressee.id = :userId and f.status = 'PENDING' order by f.createdAt desc")
    fun findIncoming(userId: UUID): List<FriendshipEntity>

    @Query("select f from FriendshipEntity f where (f.requester.id = :a and f.addressee.id = :b) or (f.requester.id = :b and f.addressee.id = :a)")
    fun findBetween(a: UUID, b: UUID): List<FriendshipEntity>
}

interface StoreRepository : JpaRepository<StoreEntity, UUID> {
    fun findByCode(code: String): StoreEntity?
    fun findAllByOrderByNameAsc(): List<StoreEntity>
}

interface ShoppingTripRepository : JpaRepository<ShoppingTripEntity, UUID> {
    fun findByShopperIdAndStatusOrderByStartedAtDesc(userId: UUID, status: TripStatus): List<ShoppingTripEntity>

    @Query("select distinct t from ShoppingTripEntity t left join TripRecipientEntity r on r.trip = t where (t.shopper.id = :userId or r.recipient.id = :userId) and t.status = :status order by t.startedAt desc")
    fun findVisibleActive(userId: UUID, status: TripStatus = TripStatus.ACTIVE): List<ShoppingTripEntity>
}

interface TripRecipientRepository : JpaRepository<TripRecipientEntity, UUID> {
    fun findByTripId(tripId: UUID): List<TripRecipientEntity>
    fun existsByTripIdAndRecipientId(tripId: UUID, recipientId: UUID): Boolean
}

interface TripRequestRepository : JpaRepository<TripRequestEntity, UUID> {
    fun findByTripIdOrderByCreatedAtAsc(tripId: UUID): List<TripRequestEntity>
}

interface ShoppingListRepository : JpaRepository<ShoppingListEntity, UUID> {
    fun findByOwnerIdOrderByCreatedAtDesc(ownerId: UUID): List<ShoppingListEntity>
}

interface ShoppingListItemRepository : JpaRepository<ShoppingListItemEntity, UUID> {
    fun findByListIdOrderByNameAsc(listId: UUID): List<ShoppingListItemEntity>
}

interface WishlistRepository : JpaRepository<WishlistEntity, UUID> {
    @Query("select distinct w from WishlistEntity w left join WishlistMemberEntity m on m.wishlist = w where w.owner.id = :userId or m.member.id = :userId order by w.createdAt desc")
    fun findVisible(userId: UUID): List<WishlistEntity>
}

interface WishlistMemberRepository : JpaRepository<WishlistMemberEntity, UUID> {
    fun findByWishlistId(wishlistId: UUID): List<WishlistMemberEntity>
    fun deleteByWishlistId(wishlistId: UUID)
    fun existsByWishlistIdAndMemberId(wishlistId: UUID, memberId: UUID): Boolean
}

interface WishlistItemRepository : JpaRepository<WishlistItemEntity, UUID> {
    fun findByWishlistIdOrderByCreatedAtDesc(wishlistId: UUID): List<WishlistItemEntity>
}

interface DeviceTokenRepository : JpaRepository<DeviceTokenEntity, UUID> {
    fun findByToken(token: String): DeviceTokenEntity?
    fun findByUserIdIn(userIds: Collection<UUID>): List<DeviceTokenEntity>
}

