package com.example.aquaserve.ui.theme

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.example.aquaserve.R


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun OrderPlacedScreen(onNavigateHome: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000L)
        onNavigateHome()
    }

    Scaffold {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.orderplacedimage),
                    contentDescription = "orderplacedimage",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(bottom = 24.dp)
                )
                Text(
                    "ORDER PLACED",
                    style = MaterialTheme.typography.h4,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
