package com.vocalrange.analyzer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vocalrange.analyzer.data.VoiceRepository
import com.vocalrange.analyzer.ui.screens.history.HistoryScreen
import com.vocalrange.analyzer.ui.screens.home.HomeScreen
import com.vocalrange.analyzer.ui.screens.rangeresult.RangeResultScreen
import com.vocalrange.analyzer.ui.screens.rangetest.RangeTestScreen
import com.vocalrange.analyzer.ui.screens.vibratoresult.VibratoResultScreen
import com.vocalrange.analyzer.ui.screens.vibratotest.VibratoTestScreen

private object Routes {
    const val HOME = "home"
    const val RANGE_TEST = "rangeTest"
    const val RANGE_RESULT = "rangeResult/{sessionId}"
    const val VIBRATO_TEST = "vibratoTest"
    const val VIBRATO_RESULT = "vibratoResult/{sessionId}"
    const val HISTORY = "history"

    fun rangeResult(id: Long) = "rangeResult/$id"
    fun vibratoResult(id: Long) = "vibratoResult/$id"
}

@Composable
fun VoiceRangeNavGraph(repository: VoiceRepository) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateRangeTest = { navController.navigate(Routes.RANGE_TEST) },
                onNavigateVibratoTest = { navController.navigate(Routes.VIBRATO_TEST) },
                onNavigateHistory = { navController.navigate(Routes.HISTORY) }
            )
        }

        composable(Routes.RANGE_TEST) {
            RangeTestScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onFinished = { id ->
                    navController.navigate(Routes.rangeResult(id)) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }

        composable(
            route = Routes.RANGE_RESULT,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: -1L
            RangeResultScreen(
                repository = repository,
                sessionId = sessionId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.VIBRATO_TEST) {
            VibratoTestScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onFinished = { id ->
                    navController.navigate(Routes.vibratoResult(id)) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }

        composable(
            route = Routes.VIBRATO_RESULT,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getLong("sessionId") ?: -1L
            VibratoResultScreen(
                repository = repository,
                sessionId = sessionId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                repository = repository,
                onBack = { navController.popBackStack() },
                onOpenRangeSession = { id -> navController.navigate(Routes.rangeResult(id)) },
                onOpenVibratoSession = { id -> navController.navigate(Routes.vibratoResult(id)) }
            )
        }
    }
}
