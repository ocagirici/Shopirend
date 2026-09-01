package com.shopirend.api

import com.shopirend.security.AuthenticatedUser
import com.shopirend.service.*
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/auth")
class AuthController(private val auth: AuthService) {
    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody request: RegisterRequest) = auth.register(request)

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest) = auth.login(request)
}

@RestController
@RequestMapping("/profile")
class ProfileController(private val auth: AuthService, private val deviceTokens: DeviceTokenService) {
    @GetMapping fun get(@AuthenticationPrincipal user: AuthenticatedUser) = auth.profile(user.id)
    @PutMapping fun update(@AuthenticationPrincipal user: AuthenticatedUser, @Valid @RequestBody request: UpdateProfileRequest) = auth.updateProfile(user.id, request)
    @PostMapping("/device-token") @ResponseStatus(HttpStatus.NO_CONTENT)
    fun token(@AuthenticationPrincipal user: AuthenticatedUser, @Valid @RequestBody request: DeviceTokenRequest) = deviceTokens.register(user.id, request)
}

@RestController
@RequestMapping("/friends")
class FriendController(private val service: FriendshipService) {
    @GetMapping fun list(@AuthenticationPrincipal user: AuthenticatedUser) = service.list(user.id)
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    fun add(@AuthenticationPrincipal user: AuthenticatedUser, @Valid @RequestBody request: AddFriendRequest) = service.add(user.id, request)
    @PostMapping("/{id}/accept") fun accept(@AuthenticationPrincipal user: AuthenticatedUser, @PathVariable id: UUID) = service.accept(user.id, id)
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    fun remove(@AuthenticationPrincipal user: AuthenticatedUser, @PathVariable id: UUID) = service.remove(user.id, id)
}

@RestController
@RequestMapping("/stores")
class StoreController(private val trips: TripService) {
    @GetMapping fun list() = trips.stores()
}

@RestController
class TripController(private val trips: TripService) {
    @GetMapping("/home") fun home(@AuthenticationPrincipal user: AuthenticatedUser) = trips.home(user.id)
    @GetMapping("/trips") fun list(@AuthenticationPrincipal user: AuthenticatedUser) = trips.visible(user.id)
    @PostMapping("/trips") @ResponseStatus(HttpStatus.CREATED)
    fun start(@AuthenticationPrincipal user: AuthenticatedUser, @Valid @RequestBody request: StartTripRequest) = trips.start(user.id, request)
    @GetMapping("/trips/{id}") fun details(@AuthenticationPrincipal user: AuthenticatedUser, @PathVariable id: UUID) = trips.details(user.id, id)
    @PostMapping("/trips/{id}/requests") @ResponseStatus(HttpStatus.CREATED)
    fun request(@AuthenticationPrincipal user: AuthenticatedUser, @PathVariable id: UUID, @Valid @RequestBody request: AddTripRequest) = trips.addRequest(user.id, id, request)
    @PatchMapping("/trips/{tripId}/requests/{requestId}")
    fun updateRequest(@AuthenticationPrincipal user: AuthenticatedUser, @PathVariable tripId: UUID, @PathVariable requestId: UUID, @RequestBody request: UpdateTripRequestStatusRequest) = trips.updateRequest(user.id, tripId, requestId, request)
    @PatchMapping("/trips/{id}")
    fun updateTrip(@AuthenticationPrincipal user: AuthenticatedUser, @PathVariable id: UUID, @RequestBody request: UpdateTripStatusRequest) = trips.updateTrip(user.id, id, request)
}

@RestController
@RequestMapping("/lists")
class ShoppingListController(private val lists: ShoppingListService) {
    @GetMapping fun list(@AuthenticationPrincipal user: AuthenticatedUser) = lists.list(user.id)
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    fun create(@AuthenticationPrincipal user: AuthenticatedUser, @Valid @RequestBody request: CreateNamedRequest) = lists.create(user.id, request)
    @PostMapping("/{listId}/items")
    fun addItem(@AuthenticationPrincipal user: AuthenticatedUser, @PathVariable listId: UUID, @Valid @RequestBody request: AddListItemRequest) = lists.addItem(user.id, listId, request)
    @PatchMapping("/{listId}/items/{itemId}")
    fun check(@AuthenticationPrincipal user: AuthenticatedUser, @PathVariable listId: UUID, @PathVariable itemId: UUID, @RequestBody request: UpdateListItemRequest) = lists.checkItem(user.id, listId, itemId, request)
    @DeleteMapping("/{listId}/items/{itemId}") @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@AuthenticationPrincipal user: AuthenticatedUser, @PathVariable listId: UUID, @PathVariable itemId: UUID) = lists.deleteItem(user.id, listId, itemId)
    @PostMapping("/{listId}/items/{itemId}/trip/{tripId}")
    fun addToTrip(@AuthenticationPrincipal user: AuthenticatedUser, @PathVariable listId: UUID, @PathVariable itemId: UUID, @PathVariable tripId: UUID) = lists.addToTrip(user.id, listId, itemId, tripId)
}

@RestController
@RequestMapping("/wishlists")
class WishlistController(private val wishlists: WishlistService) {
    @GetMapping fun list(@AuthenticationPrincipal user: AuthenticatedUser) = wishlists.list(user.id)
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    fun create(@AuthenticationPrincipal user: AuthenticatedUser, @Valid @RequestBody request: CreateNamedRequest) = wishlists.create(user.id, request)
    @GetMapping("/{id}") fun details(@AuthenticationPrincipal user: AuthenticatedUser, @PathVariable id: UUID) = wishlists.details(user.id, id)
    @PutMapping("/{id}/members") fun share(@AuthenticationPrincipal user: AuthenticatedUser, @PathVariable id: UUID, @RequestBody request: ShareWishlistRequest) = wishlists.share(user.id, id, request)
    @PostMapping("/{id}/items") fun addItem(@AuthenticationPrincipal user: AuthenticatedUser, @PathVariable id: UUID, @Valid @RequestBody request: AddWishlistItemRequest) = wishlists.addItem(user.id, id, request)
    @PatchMapping("/{wishlistId}/items/{itemId}")
    fun updateItem(@AuthenticationPrincipal user: AuthenticatedUser, @PathVariable wishlistId: UUID, @PathVariable itemId: UUID, @RequestBody request: UpdateWishlistItemRequest) = wishlists.updateItem(user.id, wishlistId, itemId, request)
}

