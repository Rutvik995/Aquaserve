package com.example.aquaserve

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aquaserve.network.AuthViewModel
import com.example.aquaserve.network.DriverViewModel
import com.example.aquaserve.network.OrderViewModel
import com.example.aquaserve.network.OwnerViewModel
import com.example.aquaserve.network.ProfileViewModel
import com.example.aquaserve.network.WalletViewModel
import com.example.aquaserve.ui.theme.CoolerOrderScreen
import com.example.aquaserve.ui.theme.DriverHomeScreen
import com.example.aquaserve.ui.theme.FinishProfileScreen
import com.example.aquaserve.ui.theme.ForgotPasswordScreen
import com.example.aquaserve.ui.theme.GetStartedScreen
import com.example.aquaserve.ui.theme.HomeScreen
import com.example.aquaserve.ui.theme.LoginScreen
import com.example.aquaserve.ui.theme.OrderPlacedScreen
import com.example.aquaserve.ui.theme.OrderSummaryScreen
import com.example.aquaserve.ui.theme.OwnerHomeScreen
import com.example.aquaserve.ui.theme.ProductOrderScreen
import com.example.aquaserve.ui.theme.ProfileScreen
import com.example.aquaserve.ui.theme.ResetPasswordScreen
import com.example.aquaserve.ui.theme.SignupScreen
import com.example.aquaserve.ui.theme.WalletScreen
import com.razorpay.PaymentResultListener
import com.example.aquaserve.network.AnnouncementViewModel
import com.example.aquaserve.ui.theme.AnnouncementsScreen import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.aquaserve.util.SessionManager
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import com.example.aquaserve.ui.theme.BoxOrderScreen
import com.example.aquaserve.network.SubscriptionViewModel
import com.example.aquaserve.ui.theme.SubscriptionScreen
import com.example.aquaserve.ui.theme.SubscriptionDetailsScreen

class MainActivity : ComponentActivity(), PaymentResultListener {

    private val walletViewModel: WalletViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AquaServeApp(walletViewModel = walletViewModel)
        }
    }

    override fun onPaymentSuccess(paymentId: String?) {
        Toast.makeText(this, "Payment Successful! Updating wallet...", Toast.LENGTH_SHORT).show()
        walletViewModel.confirmPaymentSuccess(this)
    }
    override fun onPaymentError(code: Int, description: String?) {
        Toast.makeText(this, "Payment Failed: $description", Toast.LENGTH_LONG).show()
    }
}

@Composable
fun AquaServeApp(walletViewModel: WalletViewModel) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val savedToken = sessionManager.fetchAuthToken()
    val savedRole = sessionManager.fetchUserRole()

    val initialRoute = remember {
        if (savedToken != null && savedRole != null) {
            when (savedRole) {
                "owner" -> "owner_home"
                "driver" -> "driver_home"
                else -> "home"
            }
        } else {
            "get_started"
        }
    }

    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val orderViewModel: OrderViewModel = viewModel()
    val ownerViewModel: OwnerViewModel = viewModel()
    val driverViewModel: DriverViewModel = viewModel()
    val announcementViewModel: AnnouncementViewModel = viewModel()
    val subscriptionViewModel: SubscriptionViewModel = viewModel()


    NavHost(navController, startDestination = initialRoute) {

        composable("get_started") { GetStartedScreen { navController.navigate("login") } }

        composable("signup") {
            SignupScreen(
                authViewModel = authViewModel,
                onSignupSuccess = {
                    navController.navigate("login") {
                        popUpTo("signup") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login")
                }
            )
        }

        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = { response ->
                    val destination = when (response.role) {
                        "owner" -> "owner_home"
                        "driver" -> "driver_home"
                        else -> "home"
                    }
                    navController.navigate(destination) {
                        popUpTo("get_started") { inclusive = true }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate("signup") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate("forgot_password")
                }
            )
        }

        composable("forgot_password") {
            ForgotPasswordScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onCodeSent = { email ->
                    navController.navigate("reset_password/$email")
                }
            )
        }

        composable(
            route = "reset_password/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            ResetPasswordScreen(
                email = email,
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onResetSuccess = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("finish_profile") {
            FinishProfileScreen(
                profileViewModel = profileViewModel,
                onFinish = {
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("home") {
            HomeScreen(
                onProductClick = { navController.navigate("product_order") },
                onNewProductClick = { navController.navigate("cooler_order") },
                onBox1LClick = {
                    navController.navigate("box_order/1 Litre Box/200")
                },
                onBox250mlClick = {
                    navController.navigate("box_order/250ml Box/200")
                },
                onNavigateToOrderSummary = {
                    navController.navigate("order_summary") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate("profile") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToWallet = { navController.navigate("wallet") },
                onNavigateToAnnouncements = { navController.navigate("announcements") },
                onNavigateToSubscription = { navController.navigate("buy_subscription") },
                onNavigateToSubscriptionDetails = { navController.navigate("subscription_details") },


                onNavigateToEditProfile = {
                    navController.navigate("finish_profile")
                },
                walletViewModel = walletViewModel,
                profileViewModel = profileViewModel,
                orderViewModel = orderViewModel,
                announcementViewModel = announcementViewModel,
                subscriptionViewModel = subscriptionViewModel
            )
        }

        composable("buy_subscription") {
            SubscriptionScreen(
                subscriptionViewModel = subscriptionViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("subscription_details") {
            SubscriptionDetailsScreen(
                subscriptionViewModel = subscriptionViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("announcements") {
            AnnouncementsScreen(
                viewModel = announcementViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("profile") {
            ProfileScreen(
                profileViewModel = profileViewModel,
                onNavigateToEditProfile = { navController.navigate("finish_profile") },
                onNavigateHome = {
                    navController.navigate("home") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToOrderSummary = {
                    navController.navigate("order_summary") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    sessionManager.clearSession()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("wallet") {
            WalletScreen(
                walletViewModel = walletViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }


        composable("owner_home") {
            OwnerHomeScreen(
                ownerViewModel = ownerViewModel,
                announcementViewModel = announcementViewModel,
                onLogout = {
                    sessionManager.clearSession()
                    navController.navigate("login") { popUpTo("owner_home") { inclusive = true } }
                }
            )
        }

        composable("driver_home") {
            DriverHomeScreen(
                driverViewModel = driverViewModel,
                announcementViewModel = announcementViewModel,
                onNavigateToAnnouncements = { navController.navigate("announcements") },
                onLogout = {
                    sessionManager.clearSession()
                    navController.navigate("login") { popUpTo("driver_home") { inclusive = true } }
                }
            )
        }

        composable("product_order") {
            ProductOrderScreen(
                orderViewModel = orderViewModel,
                onPlaceOrder = { navController.navigate("order_placed") },
                onNavigateBack = { navController.popBackStack() }
            )
        }


        composable("cooler_order") {
            CoolerOrderScreen(
                orderViewModel = orderViewModel,
                onPlaceOrder = { navController.navigate("order_placed") },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "box_order/{name}/{price}",
            arguments = listOf(
                navArgument("name") { type = NavType.StringType },
                navArgument("price") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val productName = backStackEntry.arguments?.getString("name") ?: "Box"
            val price = backStackEntry.arguments?.getInt("price") ?: 0

            BoxOrderScreen(
                orderViewModel = orderViewModel,
                productName = productName,
                pricePerBox = price,
                onPlaceOrder = { navController.navigate("order_placed") },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("order_placed") {
            OrderPlacedScreen {
                navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                }
            }
        }

        composable("order_summary") {
            OrderSummaryScreen(
                orderViewModel = orderViewModel,
                onNavigateHome = {
                    navController.navigate("home") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToProfile = {
                    navController.navigate("profile") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}