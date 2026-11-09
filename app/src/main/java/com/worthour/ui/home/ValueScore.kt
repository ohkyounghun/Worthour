package com.worthour.ui.home

import androidx.compose.ui.graphics.Color
import com.worthour.ui.theme.ScoreGoodBg
import com.worthour.ui.theme.ScoreGoodText
import com.worthour.ui.theme.ScoreLowBg
import com.worthour.ui.theme.ScoreLowText
import com.worthour.ui.theme.ScoreMeasuringBg
import com.worthour.ui.theme.ScoreMeasuringText
import com.worthour.ui.theme.ScoreOkBg
import com.worthour.ui.theme.ScoreOkText

enum class ValueScore(
    val label: String,
    val textColor: Color,
    val backgroundColor: Color
) {
    GOOD("Good", ScoreGoodText, ScoreGoodBg),
    OK("OK", ScoreOkText, ScoreOkBg),
    LOW("Low", ScoreLowText, ScoreLowBg),
    MEASURING("Measuring", ScoreMeasuringText, ScoreMeasuringBg)
}
