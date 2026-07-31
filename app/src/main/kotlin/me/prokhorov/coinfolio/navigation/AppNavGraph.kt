package me.prokhorov.coinfolio.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.prokhorov.coinfolio.screen.coindetail.CoinDetailScreen
import me.prokhorov.coinfolio.screen.coinlist.CoinListScreen
import me.prokhorov.coinfolio.ui.overlay.TopFadeOverlay

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = ScreenRoute.CoinList,
        ) {
            composable<ScreenRoute.CoinList> {
                CoinListScreen(
                    onCoinClick = { coinId ->
                        navController.navigate(ScreenRoute.CoinDetail(id = coinId))
                    },
                )
            }
            composable<ScreenRoute.CoinDetail> {
                CoinDetailScreen(onBackClick = { navController.navigateUp() })
            }
        }
        TopFadeOverlay()
    }
}
