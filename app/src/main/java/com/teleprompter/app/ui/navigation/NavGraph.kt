package com.teleprompter.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.teleprompter.app.ui.screens.editor.ScriptEditorScreen
import com.teleprompter.app.ui.screens.scriptlist.ScriptListScreen
import com.teleprompter.app.ui.screens.settings.SettingsScreen

object Routes {
    const val SCRIPT_LIST = "script_list"
    const val SCRIPT_EDITOR = "script_editor/{scriptId}"
    const val SCRIPT_EDITOR_NEW = "script_editor/new"
    const val SETTINGS = "settings"

    fun scriptEditor(id: Long) = "script_editor/$id"
    fun newScript() = "script_editor/new"
}

@Composable
fun TeleprompterNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SCRIPT_LIST
    ) {
        composable(Routes.SCRIPT_LIST) {
            ScriptListScreen(
                onNavigateToEditor = { scriptId ->
                    navController.navigate(Routes.scriptEditor(scriptId))
                },
                onNavigateToNewScript = {
                    navController.navigate(Routes.newScript())
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        composable(
            route = Routes.SCRIPT_EDITOR,
            arguments = listOf(
                navArgument("scriptId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val scriptId = backStackEntry.arguments?.getLong("scriptId") ?: return@composable
            ScriptEditorScreen(
                scriptId = scriptId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SCRIPT_EDITOR_NEW) {
            ScriptEditorScreen(
                scriptId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
