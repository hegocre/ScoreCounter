package es.hegocre.scorecounter.ui.components

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import es.hegocre.scorecounter.ScoreViewModel
import es.hegocre.scorecounter.ui.theme.ScoreCounterTheme

@Composable
fun ScoreList(
    scoreViewModel: ScoreViewModel
) {
    val context = LocalContext.current

    val scores = scoreViewModel.scores
    val scoresNum by remember {
        derivedStateOf { scores.size }
    }

    var showTutorialDialog by rememberSaveable {
        mutableStateOf(
            context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
                .getBoolean("isFirstLaunch", true)
        )
    }

    ScoreCounterTheme {
        val configuration = LocalConfiguration.current

        Scaffold(
            floatingActionButton = {
                FloatingActionButton(onClick = { scoreViewModel.add() }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Score"
                    )
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    val screenHeight = configuration.screenHeightDp.dp

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(count = scoresNum) { scoreIndex ->
                            if (scoreIndex != 0) {
                                Divider(
                                    thickness = 4.dp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            ScoreView(
                                score = scores[scoreIndex],
                                onScoreAdd = { scoreViewModel.inc(scoreIndex) },
                                onScoreSub = { scoreViewModel.dec(scoreIndex) },
                                onScoreReset = { scoreViewModel.reset(scoreIndex) },
                                onScoreDelete = { scoreViewModel.del(scoreIndex) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(screenHeight / scoresNum)
                            )
                        }
                    }

                } else {
                    val screenWidth = configuration.screenWidthDp.dp

                    LazyRow(modifier = Modifier.fillMaxSize()) {
                        items(count = scoresNum) { scoreIndex ->
                            if (scoreIndex != 0) {
                                Divider(
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(4.dp)
                                )
                            }
                            ScoreView(
                                score = scores[scoreIndex],
                                onScoreAdd = { scoreViewModel.inc(scoreIndex) },
                                onScoreSub = { scoreViewModel.dec(scoreIndex) },
                                onScoreReset = { scoreViewModel.reset(scoreIndex) },
                                onScoreDelete = { scoreViewModel.del(scoreIndex) },
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(screenWidth / scoresNum)
                            )
                        }
                    }
                }
            }
        }



        if (showTutorialDialog) {
            context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
                .edit().putBoolean("isFirstLaunch", false).apply()

            AlertDialog(
                onDismissRequest = { showTutorialDialog = false },
                confirmButton = {
                    TextButton(onClick = { showTutorialDialog = false }) {
                        Text(text = stringResource(id = android.R.string.ok))
                    }
                },
                title = { Text(text = stringResource(id = es.hegocre.scorecounter.R.string.dialog_tutorial_title)) },
                text = { Text(text = stringResource(id = es.hegocre.scorecounter.R.string.dialog_tutorial_message)) }
            )
        }
    }
}

@Composable
fun ScoreView(
    score: Int,
    onScoreAdd: (Offset) -> Unit,
    onScoreSub: (Offset) -> Unit,
    onScoreReset: (Offset) -> Unit,
    onScoreDelete: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = "$score",
            fontSize = 70.sp,
            color = MaterialTheme.colorScheme.onBackground
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
        Icon(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = onScoreDelete)
                }
                .padding(16.dp),
            imageVector = Icons.Default.Close,
            contentDescription = "Delete score",
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
}