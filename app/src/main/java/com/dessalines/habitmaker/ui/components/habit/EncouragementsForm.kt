package com.dessalines.habitmaker.ui.components.habit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.dessalines.habitmaker.R
import com.dessalines.habitmaker.db.Encouragement
import com.dessalines.habitmaker.db.sampleEncouragements
import com.dessalines.habitmaker.ui.components.common.SMALL_PADDING
import com.dessalines.habitmaker.utils.USER_GUIDE_URL_ENCOURAGEMENTS
import com.dessalines.habitmaker.utils.openLink
import okhttp3.internal.toImmutableList

@Composable
fun EncouragementsForm(
    initialEncouragements: List<Encouragement>,
    onChange: (List<Encouragement>) -> Unit,
) {
    val ctx = LocalContext.current

    var encouragements by rememberSaveable {
        mutableStateOf(initialEncouragements)
    }

    Column {
        encouragements.forEachIndexed { index, encouragement ->
            EncouragementForm(
                encouragement = encouragement,
                onChange = {
                    val tmp = encouragements.toMutableList()
                    tmp[index] = it
                    encouragements = tmp.toImmutableList()
                    onChange(encouragements)
                },
                onDelete = {
                    val tmp = encouragements.toMutableList()
                    tmp.removeAt(index)
                    encouragements = tmp.toImmutableList()
                    onChange(encouragements)
                },
            )
        }
        if (encouragements.isEmpty()) {
            TextButton(
                onClick = { openLink(USER_GUIDE_URL_ENCOURAGEMENTS, ctx) },
            ) {
                Text(stringResource(R.string.what_are_encouragements))
            }
        }
        OutlinedButton(
            modifier = Modifier.padding(horizontal = SMALL_PADDING),
            onClick = {
                val tmp = encouragements.toMutableList()
                tmp.add(
                    Encouragement(
                        id = 0,
                        habitId = 0,
                        content = "",
                    ),
                )
                encouragements = tmp.toImmutableList()
                onChange(encouragements)
            },
        ) {
            Text(stringResource(R.string.add_encouragement))
        }
    }
}

@Composable
@Preview
fun EncouragementsFormPreview() {
    EncouragementsForm(
        initialEncouragements = sampleEncouragements,
        onChange = {},
    )
}
