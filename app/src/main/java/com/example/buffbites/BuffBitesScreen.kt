package com.example.buffbites.ui

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.buffbites.R
import com.example.buffbites.data.Datasource
import com.example.buffbites.ui.theme.BuffBitesTheme

enum class BuffBitesScreen(@StringRes val title: Int) {
    Start(title = R.string.app_name),
    Meal(title = R.string.choose_meal),
    Delivery(title = R.string.choose_delivery_time),
    Summary(title = R.string.order_summary)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuffBitesAppBar(
    currentScreen: BuffBitesScreen,
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(stringResource(id = currentScreen.title)) },
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
fun BuffBitesApp(
    viewModel: OrderViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = BuffBitesScreen.valueOf(
        backStackEntry?.destination?.route ?: BuffBitesScreen.Start.name
    )

    Scaffold(
        topBar = {
            BuffBitesAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()

        NavHost(
            navController = navController,
            startDestination = BuffBitesScreen.Start.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = BuffBitesScreen.Start.name) {
                StartOrderScreen(
                    restaurantOptions = Datasource.restaurants,
                    onRestaurantSelected = {
                        viewModel.setRestaurant(it)
                        navController.navigate(BuffBitesScreen.Meal.name)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }
            composable(route = BuffBitesScreen.Meal.name) {
                ChooseMenuScreen(
                    options = Datasource.restaurants[0].menuItems,
                    onSelectionChanged = { selectedItem ->
                        viewModel.updateMeal(selectedItem)
                    },
                    onNextButtonClicked = { navController.navigate(BuffBitesScreen.Delivery.name) },
                    onCancelButtonClicked = {
                        cancelOrderAndNavigateToStart(viewModel, navController)
                    },
                    modifier = Modifier.fillMaxHeight()
                )
            }
            composable(route = BuffBitesScreen.Delivery.name) {
                ChooseDeliveryTimeScreen(
                    subtotal = uiState.orderSubtotal,
                    options = listOf(
                        "Mon Sep 18 6:00 PM",
                        "Mon Sep 18 7:00 PM",
                        "Mon Sep 18 8:00 PM",
                        "Mon Sep 18 9:00 PM"
                    ),
                    onSelectionChanged = { selectedTime ->
                        viewModel.updateDeliveryTime(selectedTime)
                    },
                    onCancelButtonClicked = {
                        navController.popBackStack()
                    },
                    onNextButtonClicked = {
                        navController.navigate(BuffBitesScreen.Summary.name)
                    },
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp)
                )
            }
            composable(route = BuffBitesScreen.Summary.name) {
                val context = LocalContext.current
                OrderSummaryScreen(
                    orderUiState = uiState,
                    onCancelButtonClicked = {
                        cancelOrderAndNavigateToStart(viewModel, navController)
                    },
                    onSubmitButtonClicked = {
                        cancelOrderAndNavigateToStart(viewModel, navController)
                    },
                    modifier = Modifier.fillMaxHeight()
                )
            }
        }
    }
}

private fun cancelOrderAndNavigateToStart(
    viewModel: OrderViewModel,
    navController: NavHostController
) {
    viewModel.resetOrder()
    navController.popBackStack(BuffBitesScreen.Start.name, inclusive = false)
}


