package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.model.ActiveRoundState
import com.example.model.Category
import com.example.model.PersianTextHelper
import com.example.ui.components.LetterTileBadge
import com.example.ui.components.categoryIcon
import com.example.ui.theme.PersianTeal
import com.example.ui.theme.ScoreTenGreen
import com.example.ui.theme.ScoreZeroRed
import com.example.ui.theme.TerracottaRose

@Composable
fun GamePlayScreen(
    roundState: ActiveRoundState,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit,
    onAnswerChange: (Category, String) -> Unit,
    onSubmitRound: () -> Unit,
    onExitGame: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showExitConfirm by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    BackHandler {
        showExitConfirm = true
    }

    val filledCount = roundState.answers.count { it.value.isNotBlank() }
    val totalCount = Category.entries.size
    val isUrgentTime = roundState.totalDurationSeconds > 0 && roundState.remainingSeconds <= 15

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            IconButton(
                                onClick = { showExitConfirm = true },
                                modifier = Modifier.testTag("game_exit_button"),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "خروج از بازی",
                                )
                            }
                            LetterTileBadge(letter = roundState.targetLetter, size = 48.dp)
                            Column {
                                Text(
                                    text = "حرف «${roundState.targetLetter}»",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "دور ${PersianTextHelper.toPersianDigits(roundState.roundNumber)} از ${PersianTextHelper.toPersianDigits(roundState.totalRounds)} • ${PersianTextHelper.toPersianDigits(filledCount)} از ${PersianTextHelper.toPersianDigits(totalCount)} بخش",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            IconButton(onClick = onToggleSound) {
                                Icon(
                                    imageVector = if (soundEnabled) Icons.AutoMirrored.Rounded.VolumeUp else Icons.AutoMirrored.Rounded.VolumeOff,
                                    contentDescription = "تغییر وضعیت صدا",
                                )
                            }

                            // Timer Pill
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isUrgentTime) {
                                    TerracottaRose
                                } else {
                                    MaterialTheme.colorScheme.primaryContainer
                                },
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Schedule,
                                        contentDescription = null,
                                        tint = if (isUrgentTime) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        text = if (roundState.totalDurationSeconds > 0) {
                                            PersianTextHelper.formatClock(roundState.remainingSeconds)
                                        } else {
                                            "نامحدود"
                                        },
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUrgentTime) Color.White else MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = { filledCount.toFloat() / totalCount.toFloat() },
                        color = PersianTeal,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Button(
                        onClick = onSubmitRound,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TerracottaRose,
                            contentColor = Color.White,
                        ),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        modifier = Modifier
                            .widthIn(max = 720.dp)
                            .fillMaxWidth()
                            .testTag("stop_round_button"),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Flag,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "استپ! تمام کردم (${PersianTextHelper.toPersianDigits(filledCount)}/${PersianTextHelper.toPersianDigits(totalCount)})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxSize(),
            ) {
                itemsIndexed(
                    items = Category.entries,
                    key = { _, category -> category.id },
                ) { index, category ->
                    val currentAnswer = roundState.answers[category].orEmpty()
                    val isNonEmpty = currentAnswer.isNotBlank()
                    val startsCorrect = isNonEmpty && PersianTextHelper.startsWithTargetLetter(
                        currentAnswer,
                        roundState.targetLetter,
                    )

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(
                            1.dp,
                            when {
                                isNonEmpty && startsCorrect -> ScoreTenGreen.copy(alpha = 0.5f)
                                isNonEmpty && !startsCorrect -> ScoreZeroRed.copy(alpha = 0.6f)
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                            },
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(30.dp),
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = categoryIcon(category),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${PersianTextHelper.toPersianDigits(index + 1)}. ${category.titleFa}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }

                                if (isNonEmpty) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Icon(
                                            imageVector = if (startsCorrect) Icons.Rounded.CheckCircle else Icons.Rounded.WarningAmber,
                                            contentDescription = null,
                                            tint = if (startsCorrect) ScoreTenGreen else ScoreZeroRed,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Text(
                                            text = if (startsCorrect) "شروع صحیح" else "با «${roundState.targetLetter}» بنویسید",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (startsCorrect) ScoreTenGreen else ScoreZeroRed,
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = currentAnswer,
                                onValueChange = { onAnswerChange(category, it) },
                                placeholder = {
                                    Text(
                                        text = "${category.titleFa} با حرف «${roundState.targetLetter}»...",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                },
                                singleLine = true,
                                trailingIcon = {
                                    if (isNonEmpty) {
                                        IconButton(onClick = { onAnswerChange(category, "") }) {
                                            Icon(
                                                imageVector = Icons.Rounded.Clear,
                                                contentDescription = "پاک کردن",
                                            )
                                        }
                                    }
                                },
                                keyboardOptions = KeyboardOptions(
                                    imeAction = if (index == Category.entries.lastIndex) ImeAction.Done else ImeAction.Next,
                                ),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                    onDone = { focusManager.clearFocus() },
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_${category.id}"),
                            )
                        }
                    }
                }
            }
        }
    }

    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            title = { Text("خروج از بازی") },
            text = { Text("آیا می‌خواهید از این دور خارج شوید؟ پیشرفت فعلی ذخیره نخواهد شد.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitConfirm = false
                        onExitGame()
                    }
                ) {
                    Text("خروج به خانه", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirm = false }) {
                    Text("ادامه بازی")
                }
            },
        )
    }
}
