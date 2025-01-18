package com.dessalines.habitmaker.ui.components.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.dessalines.habitmaker.R
import com.dessalines.habitmaker.ui.components.common.SimpleTopAppBar
import com.dessalines.habitmaker.utils.DONATE_URL
import com.dessalines.habitmaker.utils.GITHUB_URL
import com.dessalines.habitmaker.utils.LEMMY_URL
import com.dessalines.habitmaker.utils.MASTODON_URL
import com.dessalines.habitmaker.utils.MATRIX_CHAT_URL
import com.dessalines.habitmaker.utils.openLink
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.PreferenceCategory
import me.zhanghai.compose.preference.ProvidePreferenceTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    val ctx = LocalContext.current

    val version = ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "1"
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            SimpleTopAppBar(
                text = stringResource(R.string.about),
                onBackClick = { navController.navigateUp() },
            )
        },
        content = { padding ->
            Column(
                modifier =
                    Modifier
                        .padding(padding)
                        .verticalScroll(scrollState),
            ) {
                ProvidePreferenceTheme {
                    Preference(
                        title = { Text(stringResource(R.string.whats_new)) },
                        summary = { Text(stringResource(R.string.version, version)) },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.NewReleases,
                                contentDescription = stringResource(R.string.releases),
                            )
                        },
                        onClick = {
                            openLink("$GITHUB_URL/blob/main/RELEASES.md", ctx)
                        },
                    )
                    SettingsDivider()
                    PreferenceCategory(
                        title = { Text(stringResource(R.string.support)) },
                    )
                    Preference(
                        title = { Text(stringResource(R.string.issue_tracker)) },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.BugReport,
                                contentDescription = stringResource(R.string.issue_tracker),
                            )
                        },
                        onClick = {
                            openLink("$GITHUB_URL/issues", ctx)
                        },
                    )
                    Preference(
                        title = { Text(stringResource(R.string.developer_matrix_chatroom)) },
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Chat,
                                contentDescription = stringResource(R.string.developer_matrix_chatroom),
                            )
                        },
                        onClick = {
                            openLink(MATRIX_CHAT_URL, ctx)
                        },
                    )

                    Preference(
                        title = { Text(stringResource(R.string.donate_to_habit_maker)) },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.AttachMoney,
                                contentDescription = stringResource(R.string.donate_to_habit_maker),
                            )
                        },
                        onClick = {
                            openLink(DONATE_URL, ctx)
                        },
                    )
                    SettingsDivider()
                    PreferenceCategory(
                        title = { Text(stringResource(R.string.social)) },
                    )
                    Preference(
                        title = { Text(stringResource(R.string.join_c_habitmaker)) },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                modifier = Modifier.size(32.dp),
                                contentDescription = stringResource(R.string.join_c_habitmaker),
                            )
                        },
                        onClick = {
                            openLink(LEMMY_URL, ctx)
                        },
                    )
                    Preference(
                        title = { Text(stringResource(R.string.follow_me_mastodon)) },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.TravelExplore,
                                contentDescription = stringResource(R.string.follow_me_mastodon),
                            )
                        },
                        onClick = {
                            openLink(MASTODON_URL, ctx)
                        },
                    )
                    SettingsDivider()
                    PreferenceCategory(
                        title = { Text(stringResource(R.string.open_source)) },
                    )
                    Preference(
                        title = { Text(stringResource(R.string.source_code)) },
                        summary = {
                            Text(stringResource(R.string.source_code_subtitle))
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Code,
                                contentDescription = stringResource(R.string.source_code),
                            )
                        },
                        onClick = {
                            openLink(GITHUB_URL, ctx)
                        },
                    )
                }
            }
        },
    )
}

@Composable
fun SettingsDivider() {
    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
}

@Preview
@Composable
fun AboutPreview() {
    AboutScreen(navController = rememberNavController())
}
