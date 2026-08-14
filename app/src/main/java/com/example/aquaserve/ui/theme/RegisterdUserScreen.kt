package com.example.aquaserve.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaserve.network.OwnerViewModel
import com.example.aquaserve.network.User

@Composable
fun RegisteredUsersScreen(ownerViewModel: OwnerViewModel) {
    val users by ownerViewModel.users.collectAsState()
    val isLoading by ownerViewModel.usersLoading.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        ownerViewModel.fetchUsers(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
        } else if (users.isEmpty()) {
            Text(
                text = "No registered users found.",
                modifier = Modifier.padding(top = 24.dp)
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(users) { user ->
                    UserListItem(user = user)
                    Divider()
                }
            }
        }
    }
}

@Composable
fun UserListItem(user: User) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = user.name, fontSize = 18.sp, fontWeight = FontWeight.Medium)

            if (user.isSubscriber) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = Color(0xFF9C27B0),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "SUB",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Text(text = user.email, fontSize = 14.sp, color = Color.Gray)
    }
}