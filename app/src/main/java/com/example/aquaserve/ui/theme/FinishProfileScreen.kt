package com.example.aquaserve.ui.theme

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaserve.network.ProfileUpdateResult
import com.example.aquaserve.network.ProfileViewModel

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun FinishProfileScreen(
    profileViewModel: ProfileViewModel,
    onFinish: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var phone_number by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var postalCode by remember { mutableStateOf("") }
    var extra by remember { mutableStateOf("") }

    val areaOptions = listOf("Jay Nagar", "Jadavji Nagar", "PramukhSwami Nagar")
    var areaExpanded by remember { mutableStateOf(false) }
    var selectedArea by remember { mutableStateOf(areaOptions[0]) }

    val context = LocalContext.current
    val updateResult by profileViewModel.updateResult.collectAsState()

    LaunchedEffect(updateResult) {
        if (updateResult is ProfileUpdateResult.Success) {
            Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()
            profileViewModel.reset()
            onFinish()
        } else if (updateResult is ProfileUpdateResult.Error) {
            Toast.makeText(context, (updateResult as ProfileUpdateResult.Error).error, Toast.LENGTH_LONG).show()
            profileViewModel.reset()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                backgroundColor = Color.Blue,
                contentColor = Color.White,
                modifier = Modifier.height(70.dp)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Update Your Information",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = phone_number,
                    onValueChange = { phone_number = it },
                    shape = RoundedCornerShape(16.dp),
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    shape = RoundedCornerShape(16.dp),
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = areaExpanded,
                    onExpandedChange = { areaExpanded = !areaExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedArea,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Area") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Select Area") },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = areaExpanded,
                        onDismissRequest = { areaExpanded = false }
                    ) {
                        areaOptions.forEach { option ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedArea = option
                                    areaExpanded = false
                                }
                            ) {
                                Text(text = option)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = postalCode,
                    onValueChange = { postalCode = it },
                    shape = RoundedCornerShape(16.dp),
                    label = { Text("Postal Code") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = extra,
                    onValueChange = { extra = it },
                    shape = RoundedCornerShape(16.dp),
                    label = { Text("Extra (e.g., Landmark)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        profileViewModel.updateProfile(context, phone_number, address, selectedArea, postalCode, extra)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.Blue,
                        contentColor = Color.White
                    ),
                    enabled = updateResult !is ProfileUpdateResult.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (updateResult is ProfileUpdateResult.Loading) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Text("Submit")
                    }
                }
            }
        }
    }
}