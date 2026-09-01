package com.shopirend.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shopirend.android.data.*
import kotlinx.coroutines.delay

@Composable
fun AuthScreen(
    busy: Boolean,
    error: String?,
    clearError: () -> Unit,
    login: (String, String) -> Unit,
    register: (String, String, String) -> Unit,
) {
    var registering by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val canSubmit = email.contains('@') && password.length >= 8 && (!registering || name.isNotBlank())

    LaunchedEffect(registering) { clearError() }
    Column(
        Modifier.fillMaxSize().padding(horizontal = 28.dp).imePadding(),
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(22.dp), modifier = Modifier.size(58.dp)) {
            Icon(Icons.Outlined.ShoppingBag, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(14.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text("Shopirend", style = MaterialTheme.typography.headlineLarge)
        Text("Shopping is better together.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))
        if (registering) {
            OutlinedTextField(name, { name = it }, label = { Text("Your name") }, leadingIcon = { Icon(Icons.Outlined.Person, null) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
        }
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, leadingIcon = { Icon(Icons.Outlined.Email, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(password, { password = it }, label = { Text("Password") }, supportingText = { if (registering) Text("At least 8 characters") }, leadingIcon = { Icon(Icons.Outlined.Lock, null) }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp))
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { if (registering) register(name, email, password) else login(email, password) },
            enabled = canSubmit && !busy,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) { Text(if (registering) "Create account" else "Sign in") }
        TextButton(onClick = { registering = !registering }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(if (registering) "Already have an account? Sign in" else "New here? Create an account")
        }
    }
}

@Composable
fun HomeScreen(state: AppUiState, onRefresh: () -> Unit, onStart: () -> Unit, onTrip: (String) -> Unit) {
    val home = state.home
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Hello, ${state.user?.displayName?.substringBefore(' ') ?: "there"}", style = MaterialTheme.typography.headlineMedium)
                    Text("Who are you shopping for today?", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onRefresh) { Icon(Icons.Outlined.Refresh, "Refresh") }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary), shape = RoundedCornerShape(28.dp)) {
                Column(Modifier.padding(22.dp)) {
                    Icon(Icons.Outlined.Storefront, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(34.dp))
                    Spacer(Modifier.height(18.dp))
                    Text("Start a shopping trip", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    Text("Pick a store, notify friends, and collect requests.", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .82f))
                    Spacer(Modifier.height(18.dp))
                    Button(onClick = onStart, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary)) {
                        Text("Let's go")
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Outlined.ArrowForward, null)
                    }
                }
            }
        }
        if (home?.activeTrips?.isNotEmpty() == true) {
            item { SectionTitle("Your active trips") }
            items(home.activeTrips, key = { it.id }) { TripSummaryCard(it, onTrip) }
        }
        item { SectionTitle("Friends shopping now") }
        if (home?.friendsTrips.isNullOrEmpty()) item { EmptyCard("No friends are shopping right now", "When a friend starts a trip and includes you, it will appear here.") }
        else items(home.friendsTrips, key = { it.id }) { TripSummaryCard(it, onTrip) }
        item { SectionTitle("Recent activity") }
        if (home?.recentActivity.isNullOrEmpty()) item { EmptyCard("Nothing yet", "Start a trip or add a friend to get going.") }
        else items(home.recentActivity) { activity ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(44.dp)) { Icon(Icons.Outlined.Notifications, null, modifier = Modifier.padding(11.dp)) }
                Text(activity, Modifier.padding(start = 12.dp))
            }
        }
    }
}

@Composable
private fun TripSummaryCard(trip: TripSummary, onClick: (String) -> Unit) {
    Card(onClick = { onClick(trip.id) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            StoreBadge(trip.store.name)
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(trip.store.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(if (trip.mine) "You are shopping" else "${trip.shopper.displayName} is shopping", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${trip.requestCount} item request${if (trip.requestCount == 1) "" else "s"}", style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Outlined.ChevronRight, null)
        }
    }
}

@Composable
fun FriendsScreen(friends: List<Friendship>, busy: Boolean, onAdd: () -> Unit, onAccept: (String) -> Unit, onRemove: (String) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenTitle("Friends", "Share trips only with people you choose.", Icons.Outlined.PersonAdd, onAdd) }
        val incoming = friends.filter { it.incoming }
        if (incoming.isNotEmpty()) {
            item { SectionTitle("Requests") }
            items(incoming, key = { it.id }) { friend ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Avatar(friend.user.displayName)
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text(friend.user.displayName, fontWeight = FontWeight.SemiBold); Text(friend.user.email, style = MaterialTheme.typography.bodySmall) }
                        Button(onClick = { onAccept(friend.id) }, enabled = !busy) { Text("Accept") }
                    }
                }
            }
        }
        item { SectionTitle("Your friends") }
        val accepted = friends.filter { it.status == FriendshipStatus.ACCEPTED }
        if (accepted.isEmpty()) item { EmptyCard("Your friend list is empty", "Add someone by the email they used to register.") }
        items(accepted, key = { it.id }) { friend ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Avatar(friend.user.displayName)
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text(friend.user.displayName, fontWeight = FontWeight.Medium); Text(friend.user.email, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
                IconButton(onClick = { onRemove(friend.id) }) { Icon(Icons.Outlined.PersonRemove, "Remove friend") }
            }
        }
    }
}

@Composable
fun AddFriendScreen(busy: Boolean, onBack: () -> Unit, onAdd: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(20.dp).imePadding()) {
        BackHeader("Add friend", onBack)
        Spacer(Modifier.height(24.dp))
        Text("Enter their Shopirend email", style = MaterialTheme.typography.titleLarge)
        Text("They will need to accept before you can share shopping trips.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(email, { email = it }, label = { Text("Email address") }, leadingIcon = { Icon(Icons.Outlined.Email, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        Button(onClick = { onAdd(email) }, enabled = email.contains('@') && !busy, modifier = Modifier.fillMaxWidth()) { Text("Send friend request") }
    }
}

@Composable
fun StartTripScreen(stores: List<Store>, friends: List<Friendship>, busy: Boolean, onBack: () -> Unit, onStart: (String, Set<String>) -> Unit) {
    var selectedStore by remember(stores) { mutableStateOf(stores.firstOrNull()?.code.orEmpty()) }
    var selectedFriends by remember { mutableStateOf(emptySet<String>()) }
    val accepted = friends.filter { it.status == FriendshipStatus.ACCEPTED }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp)) { BackHeader("Start a trip", onBack) }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Text("Where are you going?", style = MaterialTheme.typography.titleLarge) }
            item {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    stores.forEach { store ->
                        FilterChip(selected = selectedStore == store.code, onClick = { selectedStore = store.code }, label = { Text(store.name) }, leadingIcon = if (selectedStore == store.code) ({ Icon(Icons.Outlined.Check, null, Modifier.size(18.dp)) }) else null)
                    }
                }
            }
            item {
                Row(verticalAlignment = Alignment.Bottom) {
                    Column(Modifier.weight(1f)) { Text("Who should know?", style = MaterialTheme.typography.titleLarge); Text("Only selected friends can view this trip.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    TextButton(onClick = { selectedFriends = accepted.map { it.user.id }.toSet() }) { Text("Select all") }
                }
            }
            if (accepted.isEmpty()) item { EmptyCard("No friends to notify", "You can still start the trip and add your own list items.") }
            items(accepted, key = { it.user.id }) { friendship ->
                val checked = friendship.user.id in selectedFriends
                Row(Modifier.fillMaxWidth().clickable { selectedFriends = if (checked) selectedFriends - friendship.user.id else selectedFriends + friendship.user.id }.padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                    Avatar(friendship.user.displayName)
                    Text(friendship.user.displayName, Modifier.weight(1f).padding(start = 12.dp), fontWeight = FontWeight.Medium)
                    Checkbox(checked, { selectedFriends = if (it) selectedFriends + friendship.user.id else selectedFriends - friendship.user.id })
                }
            }
        }
        Surface(shadowElevation = 8.dp) {
            Button(onClick = { onStart(selectedStore, selectedFriends) }, enabled = selectedStore.isNotBlank() && !busy, modifier = Modifier.fillMaxWidth().padding(20.dp).height(52.dp)) {
                Icon(Icons.Outlined.Storefront, null)
                Spacer(Modifier.width(8.dp))
                Text("Start shopping")
            }
        }
    }
}

@Composable
fun TripScreen(
    tripId: String,
    trip: Trip?,
    busy: Boolean,
    onLoad: (String) -> Unit,
    onRefresh: (String) -> Unit,
    onAdd: (String, String, Int, String?, () -> Unit) -> Unit,
    onStatus: (String, String, TripItemStatus) -> Unit,
    onClose: (TripStatus) -> Unit,
    onBack: () -> Unit,
) {
    LaunchedEffect(tripId) { onLoad(tripId) }
    LaunchedEffect(tripId, trip?.status) {
        while (trip?.status == TripStatus.ACTIVE) { delay(5_000); onRefresh(tripId) }
    }
    if (trip == null || trip.id != tripId) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    var itemName by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var quantity by remember { mutableIntStateOf(1) }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp)) { BackHeader("${trip.store.name} trip", onBack, { onRefresh(tripId) }) }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(22.dp)) {
                    Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        StoreBadge(trip.store.name)
                        Column(Modifier.weight(1f).padding(start = 14.dp)) {
                            Text(if (trip.mine) "You're shopping" else "${trip.shopper.displayName} is shopping", fontWeight = FontWeight.SemiBold)
                            Text(if (trip.status == TripStatus.ACTIVE) "Requests update automatically" else "Trip ${trip.status.name.lowercase()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusPill(trip.status.name)
                    }
                }
            }
            item { SectionTitle("Requested items · ${trip.requests.size}") }
            if (trip.requests.isEmpty()) item { EmptyCard("No requests yet", if (trip.mine) "Friends can add items while this trip is active." else "Add the first thing you need below.") }
            items(trip.requests, key = { it.id }) { request -> TripItemCard(request, trip.mine && trip.status == TripStatus.ACTIVE, { onStatus(tripId, request.id, TripItemStatus.BOUGHT) }, { onStatus(tripId, request.id, TripItemStatus.NOT_FOUND) }) }
            if (trip.mine && trip.status == TripStatus.ACTIVE) {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(onClick = { onClose(TripStatus.CANCELLED) }, enabled = !busy, modifier = Modifier.weight(1f)) { Text("Cancel trip") }
                        Button(onClick = { onClose(TripStatus.COMPLETED) }, enabled = !busy, modifier = Modifier.weight(1f)) { Text("Finish trip") }
                    }
                }
            }
        }
        if (trip.status == TripStatus.ACTIVE) {
            Surface(shadowElevation = 10.dp) {
                Column(Modifier.fillMaxWidth().padding(16.dp).imePadding()) {
                    OutlinedTextField(itemName, { itemName = it }, label = { Text("Request an item") }, placeholder = { Text("e.g. Cola Zero") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (quantity > 1) quantity-- }) { Icon(Icons.Outlined.Remove, "Decrease") }
                        Text("$quantity×", fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = { if (quantity < 99) quantity++ }) { Icon(Icons.Outlined.Add, "Increase") }
                        OutlinedTextField(note, { note = it }, label = { Text("Note (optional)") }, singleLine = true, modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        FilledIconButton(onClick = { onAdd(tripId, itemName, quantity, note.ifBlank { null }) { itemName = ""; note = ""; quantity = 1 } }, enabled = itemName.isNotBlank() && !busy) { Icon(Icons.AutoMirrored.Outlined.Send, "Send request") }
                    }
                }
            }
        }
    }
}

@Composable
private fun TripItemCard(item: TripItem, canUpdate: Boolean, bought: () -> Unit, notFound: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text("${item.quantity}× ${item.itemName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("for ${item.requestingUser.displayName}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    item.note?.let { Text(it, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp)) }
                }
                StatusPill(item.status.name)
            }
            if (canUpdate && item.status == TripItemStatus.REQUESTED) {
                Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = bought, modifier = Modifier.weight(1f)) { Icon(Icons.Outlined.Check, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Bought") }
                    OutlinedButton(onClick = notFound, modifier = Modifier.weight(1f)) { Text("Not found") }
                }
            }
        }
    }
}

@Composable
fun ShoppingListsScreen(
    lists: List<ShoppingListModel>,
    activeTrips: List<TripSummary>,
    busy: Boolean,
    onCreate: (String) -> Unit,
    onAdd: (String, String, Int) -> Unit,
    onCheck: (String, String, Boolean) -> Unit,
    onAddToTrip: (String, String, String) -> Unit,
) {
    var newList by remember { mutableStateOf("") }
    val ownTrip = activeTrips.firstOrNull { it.mine }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { ScreenTitle("Shopping lists", "Private by default—only you can see these.") }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(newList, { newList = it }, label = { Text("New list name") }, singleLine = true, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                FilledIconButton(onClick = { onCreate(newList); newList = "" }, enabled = newList.isNotBlank() && !busy) { Icon(Icons.Outlined.Add, "Create list") }
            }
        }
        if (lists.isEmpty()) item { EmptyCard("No private lists yet", "Create one for weekly groceries, errands, or anything else.") }
        items(lists, key = { it.id }) { list -> ShoppingListCard(list, ownTrip, busy, onAdd, onCheck, onAddToTrip) }
    }
}

@Composable
private fun ShoppingListCard(list: ShoppingListModel, activeTrip: TripSummary?, busy: Boolean, onAdd: (String, String, Int) -> Unit, onCheck: (String, String, Boolean) -> Unit, onAddToTrip: (String, String, String) -> Unit) {
    var itemName by remember(list.id) { mutableStateOf("") }
    var quantity by remember(list.id) { mutableIntStateOf(1) }
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Outlined.Lock, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(8.dp)); Text(list.name, style = MaterialTheme.typography.titleLarge) }
            Spacer(Modifier.height(10.dp))
            list.items.forEach { item ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(item.checked, { onCheck(list.id, item.id, it) })
                    Text("${item.quantity}× ${item.name}", Modifier.weight(1f), color = if (item.checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
                    if (activeTrip != null) IconButton(onClick = { onAddToTrip(list.id, item.id, activeTrip.id) }) { Icon(Icons.Outlined.AddShoppingCart, "Add to active ${activeTrip.store.name} trip") }
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(itemName, { itemName = it }, label = { Text("Add item") }, singleLine = true, modifier = Modifier.weight(1f))
                IconButton(onClick = { if (quantity > 1) quantity-- }) { Icon(Icons.Outlined.Remove, null) }
                Text("$quantity")
                IconButton(onClick = { if (quantity < 99) quantity++ }) { Icon(Icons.Outlined.Add, null) }
                FilledIconButton(onClick = { onAdd(list.id, itemName, quantity); itemName = ""; quantity = 1 }, enabled = itemName.isNotBlank() && !busy) { Icon(Icons.Outlined.ArrowUpward, "Add") }
            }
        }
    }
}

@Composable
fun WishlistsScreen(wishlists: List<WishlistSummary>, busy: Boolean, onOpen: (String) -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { ScreenTitle("Wishlists", "Share inspiration with selected friends.") }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(name, { name = it }, label = { Text("New wishlist") }, singleLine = true, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                FilledIconButton(onClick = { onCreate(name); name = "" }, enabled = name.isNotBlank() && !busy) { Icon(Icons.Outlined.Add, "Create wishlist") }
            }
        }
        if (wishlists.isEmpty()) item { EmptyCard("No wishlists yet", "Create a list and choose exactly which friends can see it.") }
        items(wishlists, key = { it.id }) { wishlist ->
            Card(onClick = { onOpen(wishlist.id) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(50.dp)) { Icon(Icons.Outlined.FavoriteBorder, null, modifier = Modifier.padding(13.dp)) }
                    Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                        Text(wishlist.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("${wishlist.itemCount} items · ${if (wishlist.mine) "Yours" else "Shared by ${wishlist.owner.displayName}"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Outlined.ChevronRight, null)
                }
            }
        }
    }
}

@Composable
fun WishlistDetailsScreen(
    wishlistId: String,
    wishlist: Wishlist?,
    friends: List<Friendship>,
    stores: List<Store>,
    busy: Boolean,
    onLoad: (String) -> Unit,
    onBack: () -> Unit,
    onAdd: (String, String?, String?) -> Unit,
    onShare: (Set<String>) -> Unit,
    onStatus: (String, WishlistItemStatus) -> Unit,
) {
    LaunchedEffect(wishlistId) { onLoad(wishlistId) }
    if (wishlist == null || wishlist.id != wishlistId) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }; return }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedStore by remember { mutableStateOf<String?>(null) }
    var sharedWith by remember(wishlist.memberIds) { mutableStateOf(wishlist.memberIds.toSet()) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { BackHeader(wishlist.name, onBack) }
        item { Text(if (wishlist.mine) "Your wishlist" else "Shared by ${wishlist.owner.displayName}", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (wishlist.mine) {
            item {
                Card(shape = RoundedCornerShape(20.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Add a wish", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(name, { name = it }, label = { Text("Item name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(description, { description = it }, label = { Text("Description (optional)") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selectedStore == null, { selectedStore = null }, { Text("Any store") })
                            stores.forEach { store -> FilterChip(selectedStore == store.id, { selectedStore = store.id }, { Text(store.name) }) }
                        }
                        Button(onClick = { onAdd(name, description.ifBlank { null }, selectedStore); name = ""; description = ""; selectedStore = null }, enabled = name.isNotBlank() && !busy, modifier = Modifier.fillMaxWidth()) { Text("Add to wishlist") }
                    }
                }
            }
            item { SectionTitle("Share with friends") }
            items(friends.filter { it.status == FriendshipStatus.ACCEPTED }, key = { "share-${it.user.id}" }) { friend ->
                val checked = friend.user.id in sharedWith
                Row(Modifier.fillMaxWidth().clickable { sharedWith = if (checked) sharedWith - friend.user.id else sharedWith + friend.user.id }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Avatar(friend.user.displayName)
                    Text(friend.user.displayName, Modifier.weight(1f).padding(start = 12.dp))
                    Checkbox(checked, { sharedWith = if (it) sharedWith + friend.user.id else sharedWith - friend.user.id })
                }
            }
            item { OutlinedButton(onClick = { onShare(sharedWith) }, enabled = !busy, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Share, null); Spacer(Modifier.width(8.dp)); Text("Save sharing") } }
        }
        item { SectionTitle("Items · ${wishlist.items.size}") }
        if (wishlist.items.isEmpty()) item { EmptyCard("Nothing here yet", "Add the first item to this wishlist.") }
        items(wishlist.items, key = { it.id }) { item ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        item.description?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        item.preferredStore?.let { Text("Preferably at ${it.name}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary) }
                    }
                    if (wishlist.mine) {
                        Checkbox(item.status == WishlistItemStatus.ACQUIRED, { onStatus(item.id, if (it) WishlistItemStatus.ACQUIRED else WishlistItemStatus.WANTED) })
                    } else StatusPill(item.status.name)
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(user: User?, busy: Boolean, onSave: (String) -> Unit, onLogout: () -> Unit) {
    var name by remember(user?.displayName) { mutableStateOf(user?.displayName.orEmpty()) }
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        ScreenTitle("Profile", "Your account and app settings.")
        Spacer(Modifier.height(28.dp))
        Box(Modifier.align(Alignment.CenterHorizontally)) { Avatar(user?.displayName.orEmpty(), 78.dp) }
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(name, { name = it }, label = { Text("Display name") }, leadingIcon = { Icon(Icons.Outlined.Person, null) }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(user?.email.orEmpty(), {}, label = { Text("Email") }, leadingIcon = { Icon(Icons.Outlined.Email, null) }, readOnly = true, modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
        Text("Google sign-in can be added later without changing your account data.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(20.dp))
        Button(onClick = { onSave(name) }, enabled = name.isNotBlank() && name != user?.displayName && !busy, modifier = Modifier.fillMaxWidth()) { Text("Save changes") }
        Spacer(Modifier.weight(1f))
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Icon(Icons.AutoMirrored.Outlined.Logout, null); Spacer(Modifier.width(8.dp)); Text("Sign out") }
    }
}

@Composable
private fun ScreenTitle(title: String, subtitle: String, actionIcon: androidx.compose.ui.graphics.vector.ImageVector? = null, action: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.headlineMedium); Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (actionIcon != null && action != null) FilledIconButton(onClick = action) { Icon(actionIcon, null) }
    }
}

@Composable
private fun BackHeader(title: String, onBack: () -> Unit, refresh: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") }
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (refresh != null) IconButton(onClick = refresh) { Icon(Icons.Outlined.Refresh, "Refresh") }
    }
}

@Composable private fun SectionTitle(text: String) = Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

@Composable
private fun EmptyCard(title: String, body: String) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .55f)) {
        Column(Modifier.padding(18.dp)) { Text(title, fontWeight = FontWeight.SemiBold); Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun Avatar(name: String, size: androidx.compose.ui.unit.Dp = 46.dp) {
    val initial = name.trim().firstOrNull()?.uppercase() ?: "?"
    Surface(shape = RoundedCornerShape(size / 2), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(size)) {
        Box(contentAlignment = Alignment.Center) { Text(initial, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun StoreBadge(name: String) {
    val colors = listOf(Color(0xFF256D4A), Color(0xFFE36C3A), Color(0xFF3468A8), Color(0xFF7E57C2))
    val color = colors[kotlin.math.abs(name.hashCode()) % colors.size]
    Surface(shape = RoundedCornerShape(16.dp), color = color, modifier = Modifier.size(54.dp)) {
        Box(contentAlignment = Alignment.Center) { Text(name.take(1).uppercase(), color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun StatusPill(status: String) {
    val (background, foreground) = when (status) {
        "BOUGHT", "COMPLETED", "ACQUIRED" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        "NOT_FOUND", "CANCELLED" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
    Text(status.lowercase().replace('_', ' '), color = foreground, style = MaterialTheme.typography.labelSmall, modifier = Modifier.background(background, RoundedCornerShape(99.dp)).padding(horizontal = 9.dp, vertical = 5.dp))
}
