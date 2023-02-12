package es.hegocre.scorecounter

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scoreViewModel by viewModels<ScoreViewModel>()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val darkTheme = isSystemInDarkTheme()
            val colorScheme = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    val context = LocalContext.current
                    if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(
                        context
                    )
                }
                darkTheme -> darkColorScheme()
                else -> lightColorScheme()
            }
            MaterialTheme(colorScheme = colorScheme) {
                Surface(color = if (darkTheme) Color.Black else MaterialTheme.colorScheme.surface) {
                    val context = LocalContext.current

                    var showTutorialDialog by rememberSaveable {
                        mutableStateOf(
                            context.getSharedPreferences("preferences", Context.MODE_PRIVATE)
                                .getBoolean("isFirstLaunch", true)
                        )
                    }

                    Row {
                        ScoreView(
                            score = scoreViewModel.score1,
                            onScoreAdd = { scoreViewModel.score1++ },
                            onScoreSub = { scoreViewModel.score1-- },
                            onScoreReset = { scoreViewModel.score1 = 0 },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        Spacer(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(4.dp)
                                .background(MaterialTheme.colorScheme.onSurface)
                        )
                        ScoreView(
                            score = scoreViewModel.score2,
                            onScoreAdd = { scoreViewModel.score2++ },
                            onScoreSub = { scoreViewModel.score2-- },
                            onScoreReset = { scoreViewModel.score2 = 0 },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
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
                            title = { Text(text = stringResource(id = R.string.dialog_tutorial_title)) },
                            text = { Text(text = stringResource(id = R.string.dialog_tutorial_message)) }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
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
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = "$score",
            fontSize = 70.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = onScoreAdd, onLongPress = onScoreReset)
                }
            )
            Box(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = onScoreSub, onLongPress = onScoreReset)
                }
            )
        }
    }
}