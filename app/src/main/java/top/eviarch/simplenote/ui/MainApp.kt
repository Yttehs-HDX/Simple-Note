package top.eviarch.simplenote.ui

import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntSize
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.google.accompanist.navigation.animation.AnimatedNavHost
import top.eviarch.simplenote.AppDestination
import top.eviarch.simplenote.MainViewModel
import top.eviarch.simplenote.R
import top.eviarch.simplenote.SettingsViewModel
import top.eviarch.simplenote.StorageManagerValue
import top.eviarch.simplenote.core.SimpleNoteApplication
import top.eviarch.simplenote.data.NoteEntity
import top.eviarch.simplenote.extra.ToastUtil
import top.eviarch.simplenote.extra.navigateBack
import top.eviarch.simplenote.extra.navigateSingleTopTo
import top.eviarch.simplenote.ui.topbar.EditNoteTopBar
import top.eviarch.simplenote.ui.topbar.NoteColumnTopBar
import top.eviarch.simplenote.ui.topbar.SettingsTopBar
import top.eviarch.simplenote.ui.topbar.WebViewContainerTopBar

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    context: Context,
    navController: NavHostController,
    mainViewModel: MainViewModel,
    settingsViewModel: SettingsViewModel
) {
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val homeRoute = AppDestination.NotesColumnDestination.route
        val targetDestination by mainViewModel.targetDestination.collectAsState()

        val allNotes by mainViewModel.noteListFlow.collectAsState(initial = emptyList())
        val targetNote by mainViewModel.targetNote.collectAsState()

        var searchState by rememberSaveable { mutableStateOf(false) }
        var matchedString by rememberSaveable { mutableStateOf("") }
        var searchedNotes by rememberSaveable { mutableStateOf<List<NoteEntity>>(emptyList()) }

        val noteColumnTopBarState = rememberTopAppBarState()
        val noteColumnTopBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(noteColumnTopBarState)

        var isReadOnly by remember { mutableStateOf(false) }

        val url by mainViewModel.url.collectAsState()
        Scaffold(
            Modifier.fillMaxSize(),
            floatingActionButton = {
                val configuration = LocalConfiguration.current
                val isLandscape = remember(configuration) {
                    configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                }
                val verticalPosition by settingsViewModel.verticalPosition.collectAsState()
                val horizontalPosition by settingsViewModel.horizontalPosition.collectAsState()
                AddNoteFloatingButton(
                    visible = targetDestination == AppDestination.NotesColumnDestination.route
                            && !searchState,
                    landscape = isLandscape,
                    verticalStartPosition = verticalPosition,
                    horizontalStartPosition = horizontalPosition,
                    onClick = {
                        mainViewModel.updateNote(targetNote)
                        val route = AppDestination.EditNoteDestination.route
                        mainViewModel.updateDestination(route)
                        navController.navigateSingleTopTo(route)
                    },
                    onStop = { position ->
                        if (isLandscape) {
                            settingsViewModel.saveHorizontalPositionData(position)
                        } else {
                            settingsViewModel.saveVerticalPositionData(position)
                        }
                    }
                )
            },
            topBar = {
                NoteColumnTopBar(
                    scrollBehavior = noteColumnTopBarScrollBehavior,
                    visible = targetDestination == AppDestination.NotesColumnDestination.route,
                    searchState = searchState,
                    onSearchStart = {
                        searchState = true
                        searchedNotes = allNotes
                    },
                    onSearchStop = {
                        matchedString = ""
                        searchedNotes = emptyList()
                        searchState = false
                    },
                    onSearch = { input ->
                        matchedString = input
                        searchedNotes = allNotes.filter { eachNote ->
                            input in eachNote.title || input in eachNote.content
                        }
                    },
                    onClickSettingsButton = {
                        val route = AppDestination.SettingsDestination.route
                        mainViewModel.updateDestination(route)
                        navController.navigateSingleTopTo(route)
                    },
                    onBack = {
                        matchedString = ""
                        searchedNotes = emptyList()
                        searchState = false
                    }
                )
                EditNoteTopBar(
                    visible = targetDestination == AppDestination.EditNoteDestination.route,
                    note = targetNote,
                    isReadOnly = isReadOnly,
                    onSaveNote = { note ->
                        mainViewModel.updateNote(note)
                    },
                    onDeleteNote = { note ->
                        mainViewModel.deleteNote(note)
                    },
                    onBack = {
                        mainViewModel.clearTargetNote()
                        mainViewModel.updateDestination(homeRoute)
                        navController.navigateBack()
                    },
                    enableReadOnly = {
                        isReadOnly = !isReadOnly
                    }
                )
                SettingsTopBar(
                    visible = targetDestination == AppDestination.SettingsDestination.route,
                    onClickResetButton = {
                        settingsViewModel.resetSettings()
                    },
                    onBack = {
                        mainViewModel.updateDestination(homeRoute)
                        navController.navigateBack()
                    }
                )
                WebViewContainerTopBar(
                    visible = targetDestination == AppDestination.WebViewDestination.route,
                    url = url,
                    onBack = {
                        mainViewModel.updateDestination(AppDestination.SettingsDestination.route)
                        navController.navigateBack()
                    }
                )
            }
        ) { paddingValues ->
            @Suppress("DEPRECATION")
            AnimatedNavHost(
                modifier = Modifier.padding(paddingValues),
                navController = navController,
                startDestination = AppDestination.NotesColumnDestination.route,
            ) {
                composable(route = AppDestination.NotesColumnDestination.route) {
                    AppDestination.NotesColumnDestination.Content(
                        scrollBehavior = noteColumnTopBarScrollBehavior,
                        viewModel = settingsViewModel,
                        noteList = if (searchState) searchedNotes else allNotes,
                        searchState = searchState,
                        matchedString = matchedString,
                        onClick = { note ->
                            if (note.lock) {
                               ToastUtil.showToast(SimpleNoteApplication.Context.getString(R.string.note_unlock_hint),Toast.LENGTH_LONG)
                            } else {
                                val route = AppDestination.EditNoteDestination.route
                                mainViewModel.updateDestination(route)
                                mainViewModel.updateNote(note)
                                navController.navigateSingleTopTo(route)
                                searchState = false
                            }
                        },
                        onButtonClick = {note ->
                            if (note.lock) {
                                val executor = ContextCompat.getMainExecutor(context)
                                val biometricPrompt = BiometricPrompt(
                                    context as FragmentActivity, executor,
                                    object : BiometricPrompt.AuthenticationCallback() {
                                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                            super.onAuthenticationSucceeded(result)
                                            val route = AppDestination.EditNoteDestination.route
                                            mainViewModel.updateDestination(route)
                                            mainViewModel.updateNote(note)
                                            navController.navigateSingleTopTo(route)
                                            searchState = false
                                        }
                                        override fun onAuthenticationFailed() {
                                            super.onAuthenticationFailed()
                                            ToastUtil.showToast(SimpleNoteApplication.Context.getString(R.string.authentication_failure), Toast.LENGTH_LONG)
                                        }
                                    })
                                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                    .setTitle(SimpleNoteApplication.Context.getString(R.string.unlock_title))
                                    .setSubtitle(SimpleNoteApplication.Context.getString(R.string.unlock_subtitle))
                                    .setNegativeButtonText(SimpleNoteApplication.Context.getString(R.string.cancel))
                                    .build()
                                biometricPrompt.authenticate(promptInfo)
                            } else {
                                val route = AppDestination.EditNoteDestination.route
                                mainViewModel.updateDestination(route)
                                mainViewModel.updateNote(note)
                                navController.navigateSingleTopTo(route)
                                searchState = false
                            }
                        },
                        onDeleteNote = { note ->
                            mainViewModel.deleteNote(note)
                            searchedNotes = searchedNotes.filter { it != note }
                        }
                    )
                }
                composable(
                    route = AppDestination.EditNoteDestination.route,
                    enterTransition = {
                        expandIn(
                            animationSpec = tween(500),
                            expandFrom = Alignment.Center
                        ) { IntSize.Zero }
                    },
                    exitTransition = {
                        shrinkOut(
                            animationSpec = tween(500),
                            shrinkTowards = Alignment.Center
                        ) { IntSize.Zero }
                    }
                ) {
                    AppDestination.EditNoteDestination.Content(
                        note = targetNote,
                        isReadOnly = isReadOnly,
                        onDone = { note ->
                            mainViewModel.updateNote(note)
                        },
                        onBack = {
                            mainViewModel.clearTargetNote()
                            mainViewModel.updateDestination(homeRoute)
                            navController.navigateBack()
                        }
                    )
                }
                composable(route = AppDestination.SettingsDestination.route) {
                    AppDestination.SettingsDestination.Content(
                        mainViewModel = mainViewModel,
                        settingsViewModel = settingsViewModel,
                        jumpUrl = { url ->
                            mainViewModel.updateUrl(url)
                            val route = AppDestination.WebViewDestination.route
                            mainViewModel.updateDestination(route)
                            navController.navigateSingleTopTo(route)
                        },
                        onBack = {
                            mainViewModel.updateDestination(homeRoute)
                            navController.navigateBack()
                        }
                    )
                }
                composable(route = AppDestination.WebViewDestination.route) {
                    AppDestination.WebViewDestination.Content(
                        url = url,
                        onBack = {
                            mainViewModel.updateDestination(AppDestination.SettingsDestination.route)
                            navController.navigateBack()
                        }
                    )
                }
            }
        }
        val autoDelete by mainViewModel.showAutoDeleteDialog.collectAsState()
        val deleteDate by settingsViewModel.autoDeleteDate.collectAsState()
        val deletingNotes = if (deleteDate == StorageManagerValue.Never) emptyList() else {
            val timeLimit = System.currentTimeMillis() - deleteDate.toTimeMillis()
            allNotes.filter { it.modifiedDate < timeLimit }
        }
        AutoDeleteNoteAlert(
            showDialog = autoDelete && deletingNotes.isNotEmpty(),
            deletingNotes = deletingNotes,
            onConfirm = {
                deletingNotes.forEach {
                    mainViewModel.deleteNote(it)
                }
            },
            onDismiss = {
                mainViewModel.updateAutoDeleteDialogVisibility(false)
            }
        )
    }
}