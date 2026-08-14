package com.example.aquaserve.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaserve.network.ProfileViewModel
import com.example.aquaserve.network.UserProfile

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    onNavigateToEditProfile: () -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateToOrderSummary: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val profile by profileViewModel.profile.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()

    LaunchedEffect(key1 = true) {
        profileViewModel.getProfile(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                backgroundColor = Color.Blue,
                contentColor = Color.White,
                modifier = Modifier.height(70.dp),
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigation(backgroundColor = Color.Blue) {
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = false,
                    onClick = onNavigateHome,
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
                    selected = true,
                    onClick = { /* Already here */ },
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (profile == null) {
                Text(
                    "Could not load profile. Please try again.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                ProfileDetails(
                    profile = profile!!,
                    onNavigateToEditProfile = onNavigateToEditProfile
                )
            }
        }
    }
}

@Composable
fun ProfileDetails(profile: UserProfile, onNavigateToEditProfile: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile Icon",
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally),
            tint = Color.Blue
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = profile.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(24.dp))

        ProfileInfoRow(icon = Icons.Default.Email, text = profile.email)
        ProfileInfoRow(icon = Icons.Default.Phone, text = profile.phoneNumber.takeIf { !it.isNullOrBlank() } ?: "Not set")
        ProfileInfoRow(icon = Icons.Default.Home, text = profile.address.takeIf { !it.isNullOrBlank() } ?: "Not set")
        ProfileInfoRow(icon = Icons.Default.LocationOn, text = profile.postalCode.takeIf { !it.isNullOrBlank() } ?: "Not set")

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNavigateToEditProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
        ) {
            Text("Edit Profile", color = Color.White, fontSize = 18.sp)
        }
    }
}

@Composable
fun ProfileInfoRow(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, fontSize = 16.sp)
    }
}
