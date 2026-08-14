package com.example.aquaserve.ui.theme

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaserve.R
import com.example.aquaserve.network.AnnouncementViewModel
import com.example.aquaserve.network.Order
import com.example.aquaserve.network.OrderViewModel
import com.example.aquaserve.network.ProfileViewModel
import com.example.aquaserve.network.WalletViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.example.aquaserve.network.SubscriptionViewModel
import com.example.aquaserve.network.SubscriptionResponse

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    onProductClick: () -> Unit,
    onNewProductClick: () -> Unit,
    onBox1LClick: () -> Unit,
    onBox250mlClick: () -> Unit,
    onNavigateToOrderSummary: () -> Unit,
    onNavigateToAnnouncements: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    walletViewModel: WalletViewModel,
    profileViewModel: ProfileViewModel,
    orderViewModel: OrderViewModel,
    announcementViewModel: AnnouncementViewModel,
    subscriptionViewModel: SubscriptionViewModel,
    onNavigateToSubscription: () -> Unit,
    onNavigateToSubscriptionDetails: () -> Unit,
) {
    val context = LocalContext.current
    val balance by walletViewModel.balance.collectAsState()
    val profile by profileViewModel.profile.collectAsState()
    val orders by orderViewModel.orders.collectAsState()
    val recentOrder = orders.firstOrNull()

    val newAnnouncementCount by announcementViewModel.newAnnouncementCount.collectAsState()
    val activeSub by subscriptionViewModel.activeSubscription.collectAsState()

    LaunchedEffect(key1 = Unit) {
        walletViewModel.getWalletBalance(context)
        profileViewModel.getProfile(context)
        orderViewModel.getOrderHistory(context)
        announcementViewModel.checkNewAnnouncements(context)
        subscriptionViewModel.fetchSubscriptionStatus(context)
    }

    fun checkProfileAndOrder(onOrderAction: () -> Unit) {
        val user = profile
        if (user == null) {
            Toast.makeText(context, "Loading profile...", Toast.LENGTH_SHORT).show()
            return
        }
        if (user.phoneNumber.isNullOrBlank() || user.address.isNullOrBlank() || user.postalCode.isNullOrBlank()) {
            Toast.makeText(context, "Please complete your profile to place an order.", Toast.LENGTH_LONG).show()
            onNavigateToEditProfile()
        } else {
            onOrderAction()
        }
    }

    val quotes = listOf(
        "\"Water is the driving force of all nature.\"",
        "\"AquaServe: Hydration delivered, naturally.\"",
        "\"Sip smarter, live better.\""
    )
    val pagerState = rememberPagerState(pageCount = { quotes.size })

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Hello, ${profile?.name?.split(" ")?.firstOrNull() ?: ""}!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier.height(70.dp),
                backgroundColor = Color.Blue,
                contentColor = Color.White,
                actions = {
                    IconButton(onClick = onNavigateToAnnouncements) {
                        BadgedBox(
                            badge = {
                                if (newAnnouncementCount > 0) {
                                    Badge(backgroundColor = Color.Red) {
                                        Text("$newAnnouncementCount", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Announcements")
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigation(backgroundColor = Color.Blue) {
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = true,
                    onClick = { },
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color.White.copy(alpha = 0.6f)
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Order History") },
                    label = { Text("History") },
                    selected = false,
                    onClick = onNavigateToOrderSummary,
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color.White.copy(alpha = 0.6f)
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    selected = false,
                    onClick = onNavigateToProfile,
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                contentPadding = PaddingValues(horizontal = 40.dp),
                pageSpacing = 12.dp
            ) { page ->
                QuoteCard(quote = quotes[page])
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToWallet() },
                    elevation = 4.dp,
                    shape = RoundedCornerShape(12.dp),
                    backgroundColor = Color(0xFFE3F2FD)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Wallet Balance", style = MaterialTheme.typography.body2)
                            Text(text = "₹ %.2f".format(balance), style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
                        }
                        Text("Add Money >", color = Color.Blue, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (activeSub != null) {
                    SubscriptionStatusCard(
                        sub = activeSub!!,
                        onClick = onNavigateToSubscriptionDetails
                    )
                } else {
                    Button(
                        onClick = { checkProfileAndOrder { onNavigateToSubscription() } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50))
                    ) {
                        Text("Buy Monthly Subscription", color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Your Recent Order", fontSize = 20.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(12.dp))

                if (recentOrder != null) {
                    RecentOrderItem(order = recentOrder)
                } else {
                    Text("You haven't placed any orders yet.", color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Our Products", fontSize = 20.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ProductItem(
                        modifier = Modifier.weight(1f),
                        imageRes = R.drawable.bottle,
                        name = "20L Bottle",
                        price = "20 ₹ / Unit",
                        onClick = { checkProfileAndOrder { onProductClick() } }
                    )
                    ProductItem(
                        modifier = Modifier.weight(1f),
                        imageRes = R.drawable.onelitre,
                        name = "1 Litre Box",
                        price = "200 ₹ / Box",
                        onClick = { checkProfileAndOrder { onBox1LClick() } }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ProductItem(
                        modifier = Modifier.weight(1f),
                        imageRes = R.drawable.cooler,
                        name = "Cooler Bottle",
                        price = "50 ₹ / Unit",
                        onClick = { checkProfileAndOrder { onNewProductClick() } }
                    )
                    ProductItem(
                        modifier = Modifier.weight(1f),
                        imageRes = R.drawable.twofiftyml,
                        name = "250ml Box",
                        price = "200 ₹ / Box",
                        onClick = { checkProfileAndOrder { onBox250mlClick() } }
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SubscriptionStatusCard(sub: SubscriptionResponse, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color(0xFFE8F5E9)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Active Subscription", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("View Details >", color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(sub.productName, fontWeight = FontWeight.SemiBold)
                Column(horizontalAlignment = Alignment.End) {
                    Text("Start: ${sub.startDate}", fontSize = 12.sp, color = Color.DarkGray)
                    Text("End: ${sub.endDate}", fontSize = 12.sp, color = Color.DarkGray)
                }
            }
        }
    }
}

@Composable
fun ProductItem(
    modifier: Modifier = Modifier,
    imageRes: Int,
    name: String,
    price: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        elevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = name,
                modifier = Modifier.size(90.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = name, fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
            Text(text = price, fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue, contentColor = Color.White),
                modifier = Modifier.fillMaxWidth().height(36.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("Order", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun QuoteCard(quote: String) {
    Card(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        elevation = 2.dp,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color(0xFFF0F4FF)
    ) {
        Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
            Text(text = quote, style = MaterialTheme.typography.body1, fontStyle = FontStyle.Italic, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun RecentOrderItem(order: Order) {
    val (date, time) = formatDateTime(order.createdAt)

    val statusColor = when(order.status) {
        "Delivered" -> Color(0xFF388E3C)
        "Accepted" -> Color(0xFFFF9800)
        else -> Color.Gray
    }

    Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp, shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {

                Text(text = date, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = order.status,
                    color = statusColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(order.productName ?: "Product", fontWeight = FontWeight.Bold)
                    Text("Quantity: ${order.quantity}")
                }
                Text(text = "₹ ${order.billAmount}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colors.primary)
            }
        }
    }
}

private fun formatDateTime(isoString: String): Pair<String, String> {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val dateObj = inputFormat.parse(isoString) ?: return Pair("", "")
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        Pair(dateFormat.format(dateObj), timeFormat.format(dateObj))
    } catch (e: Exception) {
        Pair("", "")
    }
}