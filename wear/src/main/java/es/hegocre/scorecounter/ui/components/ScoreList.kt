package es.hegocre.scorecounter.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.LocalContentColor
import androidx.wear.compose.material.Text
import es.hegocre.scorecounter.ScoreViewModel

@Composable
fun ScoreList(
    scoreViewModel: ScoreViewModel,
    modifier: Modifier = Modifier
) {
    val scores = scoreViewModel.scores
    val color = LocalContentColor.current

    Row(modifier = modifier.fillMaxSize()) {
        ScoreView(
            score = scores[0],
            onScoreAdd = { scoreViewModel.inc(0) },
            onScoreSub = { scoreViewModel.dec(0) },
            onScoreReset = { scoreViewModel.reset(0) },
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
        )

        Spacer(
            modifier = Modifier
                .fillMaxHeight()
                .width(4.dp)
                .drawBehind { drawRect(color = color) },
        )

        ScoreView(
            score = scores[1],
            onScoreAdd = { scoreViewModel.inc(1) },
            onScoreSub = { scoreViewModel.dec(1) },
            onScoreReset = { scoreViewModel.reset(1) },
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
        )
    }
}

@Composable
fun ScoreView(
    score: Int,
    onScoreAdd: (Offset) -> Unit,
    onScoreSub: (Offset) -> Unit,
    onScoreReset: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val color = LocalContentColor.current

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = "$score",
            fontSize = 50.sp,
            color = color
        )
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = onScoreAdd, onLongPress = onScoreReset)
                }
            )
            Box(modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = onScoreSub, onLongPress = onScoreReset)
                }
            )
        }
    }
}