package com.example.androidprogram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.androidprogram.feature.cards.detail.CardDetailScreen
import com.example.androidprogram.feature.cards.detail.CardDetailViewModel
import com.example.androidprogram.feature.cards.detail.CardDetailViewModelFactory
import com.example.androidprogram.feature.cards.edit.CardEditScreen
import com.example.androidprogram.feature.cards.edit.CardEditViewModel
import com.example.androidprogram.feature.cards.edit.CardEditViewModelFactory
import com.example.androidprogram.feature.cards.list.CardListScreen
import com.example.androidprogram.feature.cards.list.CardListViewModel
import com.example.androidprogram.feature.cards.list.CardListViewModelFactory
import com.example.androidprogram.feature.login.LoginScreen
import com.example.androidprogram.feature.login.LoginViewModel
import com.example.androidprogram.feature.login.LoginViewModelFactory
import com.example.androidprogram.feature.qr.QrViewModel
import com.example.androidprogram.feature.qr.QrViewModelFactory
import com.example.androidprogram.feature.qr.QrScreen
import com.example.androidprogram.navigation.Routes
import com.example.androidprogram.ui.theme.AndroidprogramTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidprogramTheme {
                val nav = rememberNavController()
                val auth = ServiceLocator.provideAuthManager(this)
                val start = remember { mutableStateOf(if (auth.isLoggedIn()) Routes.CardList else Routes.Login) }

                LaunchedEffect(Unit) {
                    if (!auth.isLoggedIn()) nav.navigate(Routes.Login)
                }

                NavHost(navController = nav, startDestination = start.value) {
                    composable(
                        Routes.Login,
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(600, easing = EaseInOut)
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(600, easing = EaseInOut)
                            )
                        }
                    ) {
                        val vm: LoginViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = LoginViewModelFactory(auth))
                        LoginScreen(stateHolder = vm) {
                            nav.navigate(Routes.CardList) {
                                popUpTo(Routes.Login) { inclusive = true }
                            }
                        }
                    }
                    composable(
                        Routes.CardList,
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(600, easing = EaseInOut)
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(600, easing = EaseInOut)
                            )
                        }
                    ) {
                        val repo = ServiceLocator.provideCardRepository(this@MainActivity)
                        val vm: CardListViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = CardListViewModelFactory(repo))
                        CardListScreen(
                            vm = vm,
                            onAdd = { nav.navigate(Routes.CardEdit) },
                            onOpen = { id -> nav.navigate("${Routes.CardDetail}/$id") },
                            onEdit = { id -> nav.navigate("${Routes.CardEdit}/$id") },
                            onOpenQR = { nav.navigate(Routes.Qr) },
                            onOpenFavorites = { nav.navigate(Routes.Favorites) },
                            onLogout = {
                                auth.logout()
                                nav.navigate(Routes.Login) {
                                    popUpTo(Routes.CardList) { inclusive = true }
                                }
                            }
                        )
                    }
                    composable(
                        Routes.CardEdit,
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(600, easing = EaseInOut)
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(600, easing = EaseInOut)
                            )
                        }
                    ) {
                        val repo = ServiceLocator.provideCardRepository(this@MainActivity)
                        val vm: CardEditViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = CardEditViewModelFactory(repo, null))
                        CardEditScreen(vm = vm, onBack = { nav.popBackStack() }) { id ->
                            nav.popBackStack()
                        }
                    }
                    composable(
                        route = "${Routes.CardEdit}/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.LongType }),
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(600, easing = EaseInOut)
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(600, easing = EaseInOut)
                            )
                        }
                    ) { backStackEntry ->
                        val repo = ServiceLocator.provideCardRepository(this@MainActivity)
                        val id = backStackEntry.arguments?.getLong("id") ?: 0L
                        val vm: CardEditViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = CardEditViewModelFactory(repo, id))
                        CardEditScreen(vm = vm, onBack = { nav.popBackStack() }) { _ ->
                            nav.popBackStack()
                        }
                    }
                    composable(
                        route = "${Routes.CardDetail}/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.LongType }),
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(600, easing = EaseInOut)
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(600, easing = EaseInOut)
                            )
                        }
                    ) { backStackEntry ->
                        val repo = ServiceLocator.provideCardRepository(this@MainActivity)
                        val vm: CardDetailViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = CardDetailViewModelFactory(repo, applicationContext))
                        val id = backStackEntry.arguments?.getLong("id") ?: 0L
                        CardDetailScreen(
                            vm = vm, 
                            id = id, 
                            onGenerateQr = { cid ->
                                nav.navigate("${Routes.Qr}?id=$cid")
                            },
                            onEdit = { cid ->
                                nav.navigate("${Routes.CardEdit}/$cid")
                            },
                            onBack = { nav.popBackStack() }
                        )
                    }
                    composable(
                        route = "${Routes.Qr}?id={id}", 
                        arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = 0L }),
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(600, easing = EaseInOut)
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(600, easing = EaseInOut)
                            )
                        }
                    ) {
                        val repo = ServiceLocator.provideCardRepository(this@MainActivity)
                        val vm: QrViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = QrViewModelFactory(repo, applicationContext))
                        val id = it.arguments?.getLong("id") ?: 0L
                        if (id > 0) vm.dispatch(com.example.androidprogram.feature.qr.QrIntent.GenerateForCard(id))
                        QrScreen(vm, onBack = { nav.popBackStack() }, shareOnly = id > 0)
                    }
                    composable(
                        Routes.Favorites,
                        enterTransition = {
                            slideIntoContainer(
                                AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(600, easing = EaseInOut)
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(600, easing = EaseInOut)
                            )
                        }
                    ) {
                        val repo = ServiceLocator.provideCardRepository(this@MainActivity)
                        val vm: com.example.androidprogram.feature.favorites.FavoritesViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = com.example.androidprogram.feature.favorites.FavoritesViewModelFactory(repo))
                        com.example.androidprogram.feature.favorites.FavoritesScreen(
                            vm = vm,
                            onBack = { nav.popBackStack() },
                            onOpen = { id -> nav.navigate("${Routes.CardDetail}/$id") },
                            onEdit = { id -> nav.navigate("${Routes.CardEdit}/$id") },
                            onAdd = { nav.navigate(Routes.CardEdit) },
                            onOpenQR = { nav.navigate(Routes.Qr) }
                        )
                    }
                }
            }
        }
    }
}
