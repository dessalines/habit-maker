package com.dessalines.habitmaker.ui.components.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.asLiveData
import com.dessalines.habitmaker.R
import com.dessalines.habitmaker.db.AppSettingsViewModel
import com.dessalines.habitmaker.utils.getVersionCode
import dev.jeziellago.compose.markdowntext.MarkdownText

val DONATION_MARKDOWN =
    """
    ### Support Habit-Maker
    [Habit-Maker](https://github.com/dessalines/habit-maker) is free, open-source software, meaning no spying, keylogging, or advertising, ever.

    No one likes recurring donations, but they've proven to be the only way open-source software like Habit-Maker can stay alive. If you find yourself using Habit-Maker every day, please consider donating:
    - [Support on Liberapay](https://liberapay.com/dessalines).
    - [Support on Patreon](https://www.patreon.com/dessalines).
    ---
    
    """.trimIndent()

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ShowChangelog(appSettingsViewModel: AppSettingsViewModel) {
    val ctx = LocalContext.current
    val lastVersionCodeViewed =
        appSettingsViewModel.appSettings
            .asLiveData()
            .observeAsState()
            .value
            ?.lastVersionCodeViewed

    // Make sure its initialized
    lastVersionCodeViewed?.also { lastViewed ->
        val currentVersionCode = ctx.getVersionCode()
        val viewed = lastViewed == currentVersionCode

        var whatsChangedDialogOpen by remember { mutableStateOf(!viewed) }

        if (whatsChangedDialogOpen) {
            val scrollState = rememberScrollState()
            val markdown by appSettingsViewModel.changelog.collectAsState()
            LaunchedEffect(appSettingsViewModel) {
                appSettingsViewModel.updateChangelog(ctx)
            }

            AlertDialog(
                text = {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState),
                    ) {
                        val markdownText = DONATION_MARKDOWN + markdown
                        MarkdownText(
                            markdown = markdownText,
                            linkColor = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            whatsChangedDialogOpen = false
                            appSettingsViewModel.updateLastVersionCodeViewed(currentVersionCode)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.done))
                    }
                },
                onDismissRequest = {
                    whatsChangedDialogOpen = false
                    appSettingsViewModel.updateLastVersionCodeViewed(currentVersionCode)
                },
                modifier = Modifier.semantics { testTagsAsResourceId = true },
            )
        }
    }
}

@Composable
fun ToolTip(text: String) {
    ElevatedCard {
        Text(
            text = text,
            modifier = Modifier.padding(SMALL_PADDING),
        )
    }
}

@Composable
fun AreYouSureDialog(
    show: MutableState<Boolean>,
    title: String,
    onYes: () -> Unit,
) {
    if (show.value) {
        AlertDialog(
            title = { Text(title) },
            text = { Text(stringResource(R.string.are_you_sure)) },
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = stringResource(R.string.are_you_sure),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onYes()
                        show.value = false
                    },
                ) {
                    Text(
                        stringResource(R.string.yes),
                    )
                }
            },
            onDismissRequest = {
                show.value = false
            },
            dismissButton = {
                TextButton(
                    onClick = { show.value = false },
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Preview
@Composable
fun PreviewAreYouSureDialog() {
    val show = remember { mutableStateOf(true) }
    AreYouSureDialog(
        show = show,
        title = "Test title",
        onYes = {},
    )
}
