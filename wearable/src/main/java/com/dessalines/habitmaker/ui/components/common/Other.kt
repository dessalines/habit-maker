package com.dessalines.habitmaker.ui.components.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import com.dessalines.habitmaker.R
import com.dessalines.habitmaker.db.utils.HabitFrequency

fun HabitFrequency.toResId() =
    when (this) {
        HabitFrequency.Daily -> R.string.daily
        HabitFrequency.Weekly -> R.string.weekly
        HabitFrequency.Monthly -> R.string.monthly
        HabitFrequency.Yearly -> R.string.yearly
    }

@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
) = Text(
    text = title,
    style = MaterialTheme.typography.titleSmall,
    modifier = modifier,
)

@Composable
fun TransformingLazyColumnItemScope.ListHeaderHabits(
    title: String,
    transformationSpec: TransformationSpec
) {
    ListHeader(
        modifier =
            Modifier
                .fillMaxWidth()
                .transformedHeight(this, transformationSpec),
        transformation = SurfaceTransformation(transformationSpec),
    ) {
        Text(title)
    }
}
