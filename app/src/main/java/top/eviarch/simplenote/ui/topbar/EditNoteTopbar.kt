package top.eviarch.simplenote.ui.topbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import top.eviarch.simplenote.R
import top.eviarch.simplenote.core.SimpleNoteApplication
import top.eviarch.simplenote.data.NoteEntity
import top.eviarch.simplenote.extra.ToastUtil
import top.eviarch.simplenote.ui.screen.DeleteWaringAlertDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteTopBar(
    visible: Boolean,
    note: NoteEntity,
    isReadOnly: Boolean,
    onSaveNote: (NoteEntity) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit,
    onBack: () -> Unit,
    enableReadOnly: () -> Unit
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
        var showDialog by remember { mutableStateOf(false) }
        val interactionSource = remember { MutableInteractionSource() }
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ),
            title = {
                Text(
                    modifier = Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        ToastUtil.showToast(SimpleNoteApplication.Context.getString(R.string.try_to_write_something))
                    },
                    text = SimpleNoteApplication.Context.getString(R.string.edit_note),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Cursive
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = {
                        onSaveNote(note)
                        onBack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                var lock by remember { mutableStateOf(note.lock) }
                IconButton(
                    onClick = {
                        lock = !lock
                        onSaveNote(note.copy(lock = lock))
                    }
                ) {
                    Icon(
                        imageVector = if (lock) {
                            ImageVector.vectorResource(id = R.drawable.baseline_lock_person_24)
                        } else {
                            ImageVector.vectorResource(id = R.drawable.baseline_lock_open_24)
                        },
                        contentDescription = "Lock"
                    )
                }
                IconButton(
                    onClick = {
                        onSaveNote(note)
                        enableReadOnly()
                    }
                ) {
                    Icon(
                        imageVector = if (isReadOnly) {
                            ImageVector.vectorResource(id = R.drawable.baseline_eye_24)
                        } else {
                            Icons.Filled.Create
                        },
                        contentDescription = "Read Only"
                    )
                }
                IconButton(
                    onClick = { showDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete"
                    )
                }
            }
        )
        DeleteWaringAlertDialog(
            showDialog = showDialog,
            onConfirm = {
                showDialog = false
                onDeleteNote(note)
                onBack()
            },
            onDismiss = { showDialog = false }
        )
    }
}