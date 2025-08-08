package es.hegocre.scorecounter.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import es.hegocre.scorecounter.R
import es.hegocre.scorecounter.ScoreViewModel
import es.hegocre.scorecounter.model.Score
import es.hegocre.scorecounter.ui.theme.ScoreCounterTheme
import androidx.core.content.edit
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun ScoreList(
    scoreViewModel: ScoreViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

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
    var showSetStartingScoreDialog by remember { mutableStateOf(false) }

    ScoreCounterTheme {
        val configuration = LocalConfiguration.current

        Scaffold(
            floatingActionButton = {
                val interactionSource = remember { MutableInteractionSource() }

                val viewConfiguration = LocalViewConfiguration.current

                LaunchedEffect(interactionSource) {
                    var isLongClick = false

                    interactionSource.interactions.collectLatest { interaction ->
                        when (interaction) {
                            is PressInteraction.Press -> {
                                isLongClick = false
                                delay(viewConfiguration.longPressTimeoutMillis)
                                isLongClick = true
                                showSetStartingScoreDialog = true
                            }

                            is PressInteraction.Release -> {
                                if (isLongClick.not()) {
                                    scoreViewModel.add(scoreViewModel.startingScore)
                                }

                            }

                        }
                    }
                }

                FloatingActionButton(onClick = { }, interactionSource = interactionSource) {
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
                                HorizontalDivider(
                                    thickness = 4.dp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            ScoreView(
                                index = scoreIndex,
                                score = scores[scoreIndex],
                                onScoreAdd = { scoreViewModel.inc(scoreIndex) },
                                onScoreSub = { scoreViewModel.dec(scoreIndex) },
                                onScoreReset = { scoreViewModel.reset(scoreIndex) },
                                onScoreDelete = { scoreViewModel.del(scoreIndex) },
                                onPlayerNameChange = { playerName ->
                                    scoreViewModel.setPlayerName(scoreIndex, playerName)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(maxOf(screenHeight / scoresNum, 200.dp))
                            )
                        }
                    }

                } else {
                    val screenWidth = configuration.screenWidthDp.dp

                    LazyRow(modifier = Modifier.fillMaxSize()) {
                        items(count = scoresNum) { scoreIndex ->
                            if (scoreIndex != 0) {
                                VerticalDivider(
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(4.dp)
                                )
                            }
                            ScoreView(
                                index = scoreIndex,
                                score = scores[scoreIndex],
                                onScoreAdd = { scoreViewModel.inc(scoreIndex) },
                                onScoreSub = { scoreViewModel.dec(scoreIndex) },
                                onScoreReset = { scoreViewModel.reset(scoreIndex) },
                                onScoreDelete = { scoreViewModel.del(scoreIndex) },
                                onPlayerNameChange = { playerName ->
                                    scoreViewModel.setPlayerName(scoreIndex, playerName)
                                },
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(maxOf(screenWidth / scoresNum, 200.dp))
                            )
                        }
                    }
                }
            }
        }

        if (showTutorialDialog) {
            context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
                .edit { putBoolean("isFirstLaunch", false) }

            AlertDialog(
                onDismissRequest = { showTutorialDialog = false },
                confirmButton = {
                    TextButton(onClick = { showTutorialDialog = false }) {
                        Text(text = stringResource(id = android.R.string.ok))
                    }
                },
                title = { Text(text = stringResource(id = R.string.dialog_tutorial_title)) },
                text = { Text(text = stringResource(id = R.string.dialog_tutorial_message)) }
            )
        }

        if (showSetStartingScoreDialog) {
            InputStartingScoreDialog(
                currentStartingScore = scoreViewModel.startingScore,
                onSetStartingScore = {
                    coroutineScope.launch {
                        scoreViewModel.setStartingScore(it.toIntOrNull() ?: 0)
                    }
                },
                onDismissRequest = {
                    showSetStartingScoreDialog = false
                }
            )
        }
    }
}

@Composable
fun ScoreView(
    index: Int,
    score: Score,
    onScoreAdd: (Offset) -> Unit,
    onScoreSub: (Offset) -> Unit,
    onScoreReset: (Offset) -> Unit,
    onScoreDelete: (Offset) -> Unit,
    onPlayerNameChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPlayerNameDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = "${score.score}",
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { showPlayerNameDialog = true })
                    },
                text = score.playerName.ifBlank { "${stringResource(id = R.string.player)} ${index + 1}" },
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = if (score.playerName.isBlank()) 0.4f else 1f)
            )

            Spacer(Modifier.weight(1f))

            Icon(
                modifier = Modifier
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = onScoreDelete)
                    },
                imageVector = Icons.Default.Close,
                contentDescription = "Delete score",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }

    if (showPlayerNameDialog) {
        InputPlayerNameDialog(
            onSetPlayerName = {
                onPlayerNameChange(it)
                showPlayerNameDialog = false
            },
            onDismissRequest = {
                showPlayerNameDialog = false
            }
        )
    }
}

@Composable
fun InputPlayerNameDialog(
    onSetPlayerName: (String) -> Unit,
    onDismissRequest: (() -> Unit)? = null
) {
    val requester = FocusRequester()

    val (playerName, setPlayerName) = remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = { onDismissRequest?.invoke() },
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            contentColor = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
        ) {
            Column(modifier = Modifier.padding(all = 24.dp)) {
                Text(
                    text = stringResource(id = R.string.player_name),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    modifier = Modifier
                        .padding(bottom = 16.dp, top = 8.dp)
                        .focusRequester(requester),
                    value = playerName,
                    onValueChange = setPlayerName,
                    singleLine = true,
                    maxLines = 1,
                    label = { Text(text = stringResource(id = R.string.player_name)) },
                )


                TextButton(
                    onClick = {
                        onSetPlayerName(playerName)
                    },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(horizontal = 0.dp)
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        requester.requestFocus()
    }
}

@Composable
fun InputStartingScoreDialog(
    currentStartingScore: Int,
    onSetStartingScore: (String) -> Unit,
    onDismissRequest: (() -> Unit)? = null
) {
    val requester = FocusRequester()

    val (startingScore, setStartingScore) = remember { mutableStateOf(currentStartingScore.toString()) }

    Dialog(
        onDismissRequest = { onDismissRequest?.invoke() },
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            contentColor = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
        ) {
            Column(modifier = Modifier.padding(all = 24.dp)) {
                Text(
                    text = stringResource(id = R.string.starting_score),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    modifier = Modifier
                        .padding(bottom = 16.dp, top = 8.dp)
                        .focusRequester(requester),
                    value = startingScore,
                    onValueChange = { if (it.isBlank() || it.toIntOrNull() != null) setStartingScore(it) },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    maxLines = 1,
                    label = { Text(text = stringResource(id = R.string.starting_score)) },
                    placeholder = { Text(text = currentStartingScore.toString()) }
                )


                TextButton(
                    onClick = {
                        if (startingScore.isBlank()) {
                            onSetStartingScore(currentStartingScore.toString())
                        } else if (startingScore.toIntOrNull() != null) {
                            onSetStartingScore(startingScore)
                        }
                        onDismissRequest?.invoke()
                    },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(horizontal = 0.dp)
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        requester.requestFocus()
    }
}