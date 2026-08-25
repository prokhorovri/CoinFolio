package me.prokhorov.coinfolio.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.prokhorov.coinfolio.screen.coindetail.CoinDetailScreen
import me.prokhorov.coinfolio.screen.coinlist.CoinListScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
) {
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
}
