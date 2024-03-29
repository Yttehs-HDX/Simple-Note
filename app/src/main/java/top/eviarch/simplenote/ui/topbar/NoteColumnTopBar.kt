package top.eviarch.simplenote.ui.topbar

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.eviarch.simplenote.R
import top.eviarch.simplenote.core.SimpleNoteApplication
import top.eviarch.simplenote.extra.ToastUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteColumnTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    visible: Boolean,
    searchState: Boolean,
    onSearchStart: () -> Unit,
    onSearchStop: () -> Unit,
    onSearch: (String) -> Unit,
    onClickSettingsButton: () -> Unit,
    onBack: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                easing = LinearEasing
            )
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = 300,
                easing = LinearEasing
            )
        )
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        TopAppBar(
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ),
            title = {
                AnimatedVisibility(
                    visible = !searchState,
                    enter = fadeIn(
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = LinearEasing
                        )
                    ),
                    exit = fadeOut(
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = LinearEasing
                        )
                    )
                ) {
                    Text(
                        modifier = Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            ToastUtil.showToast(SimpleNoteApplication.Context.getString(R.string.smile_face))
                        },
                        text = SimpleNoteApplication.Context.getString(R.string.app_name),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Cursive
                    )
                }
            },
            actions = {
                SearchBox(
                    searchState = searchState,
                    onSearchStart = onSearchStart,
                    onSearchStop = onSearchStop,
                    onSearch = { input ->
                        onSearch(input)
                    },
                    onBack = onBack
                )
                IconButton(
                    onClick = onClickSettingsButton
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
            }
        )
    }
}

@Composable
fun SearchBox(
    searchState: Boolean,
    onSearchStart: () -> Unit,
    onSearchStop: () -> Unit,
    onSearch: (String) -> Unit,
    onBack: () -> Unit
) {
    Row {
        AnimatedVisibility(
            visible = searchState,
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearEasing
                )
            )
        ) {
            val focusRequester = FocusRequester()
            val keyboardController = LocalSoftwareKeyboardController.current
            var inputText by rememberSaveable { mutableStateOf("") }
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .focusRequester(focusRequester),
                value = inputText,
                onValueChange = { tempString ->
                    inputText = tempString
                    onSearch(tempString)
                },
                maxLines = 1,
                placeholder = {
                    Row {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = SimpleNoteApplication.Context.getString(R.string.searching),
                            color = MaterialTheme.colorScheme.surfaceTint,
                            fontWeight = FontWeight.Light
                        )
                    }
                },
                trailingIcon = {
                    IconButton(
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.surfaceTint
                        ),
                        onClick = {
                            inputText = ""
                            onSearchStop()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                        )
                    }
                }
            )
            DisposableEffect(Unit) {
                if (searchState) {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
                onDispose { }
            }
        }
        if (!searchState) {
            IconButton(onClick = onSearchStart) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            }
        }
    }
    if (searchState) {
        BackHandler(onBack = onBack)
    }
}