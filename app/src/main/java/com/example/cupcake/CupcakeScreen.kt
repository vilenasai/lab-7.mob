package com.example.cupcake

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.example.cupcake.data.DataSource
import com.example.cupcake.ui.OrderViewModel
import com.example.cupcake.ui.StartOrderScreen
import com.example.cupcake.ui.SelectOptionScreen
import com.example.cupcake.ui.OrderSummaryScreen

enum class CupcakeScreen(val route: String) {
    Start("start"),
    Flavor("flavor"),
    Pickup("pickup"),
    Summary("summary")
}

@Composable
fun CupcakeAppBar(
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(id = R.string.app_name)) },
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        }
    )
}

@Composable
fun CupcakeApp(
    windowSizeClass: WindowSizeClass,
    viewModel: OrderViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = backStackEntry?.destination?.route
    val canNavigateBack = currentScreen != CupcakeScreen.Start.route && currentScreen != null
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CupcakeAppBar(
                canNavigateBack = canNavigateBack,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

        val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

        NavHost(
            navController = navController,
            startDestination = CupcakeScreen.Start.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(CupcakeScreen.Start.route) {
                StartOrderScreen(
                    quantityOptions = DataSource.quantityOptions,
                    onNextButtonClicked = { quantity ->
                        viewModel.setQuantity(quantity)
                        navController.navigate(CupcakeScreen.Flavor.route)
                    }
                )
            }

            composable(CupcakeScreen.Flavor.route) {
                val flavorOptions = DataSource.flavors.map { context.getString(it) }

                if (isExpanded) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.weight(1f)) {
                            SelectOptionScreen(
                                subtotal = uiState.price,
                                options = flavorOptions,
                                onSelectionChanged = { flavor -> viewModel.setFlavor(flavor) },
                                onNextButtonClicked = { navController.navigate(CupcakeScreen.Pickup.route) },
                                onCancelButtonClicked = {
                                    viewModel.resetOrder()
                                    navController.popBackStack(CupcakeScreen.Start.route, inclusive = false)
                                }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Выберите вкус слева",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                } else {
                    SelectOptionScreen(
                        subtotal = uiState.price,
                        options = flavorOptions,
                        onSelectionChanged = { flavor -> viewModel.setFlavor(flavor) },
                        onNextButtonClicked = { navController.navigate(CupcakeScreen.Pickup.route) },
                        onCancelButtonClicked = {
                            viewModel.resetOrder()
                            navController.popBackStack(CupcakeScreen.Start.route, inclusive = false)
                        }
                    )
                }
            }

            composable(CupcakeScreen.Pickup.route) {
                SelectOptionScreen(
                    subtotal = uiState.price,
                    options = uiState.pickupOptions,
                    onSelectionChanged = { date -> viewModel.setDate(date) },
                    onNextButtonClicked = { navController.navigate(CupcakeScreen.Summary.route) },
                    onCancelButtonClicked = {
                        viewModel.resetOrder()
                        navController.popBackStack(CupcakeScreen.Start.route, inclusive = false)
                    }
                )
            }

            composable(CupcakeScreen.Summary.route) {
                OrderSummaryScreen(
                    orderUiState = uiState,
                    onSendButtonClicked = { _, _ ->
                        viewModel.resetOrder()
                        navController.popBackStack(CupcakeScreen.Start.route, inclusive = false)
                    },
                    onCancelButtonClicked = {
                        viewModel.resetOrder()
                        navController.popBackStack(CupcakeScreen.Start.route, inclusive = false)
                    }
                )
            }
        }
    }
}