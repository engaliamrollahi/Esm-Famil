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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.NavigateNext
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Category
import com.example.model.MatchSummary
import com.example.model.PersianTextHelper
import com.example.model.RoundResult
import com.example.ui.components.LetterTileBadge
import com.example.ui.components.ScorePill
import com.example.ui.components.badgeIcon
import com.example.ui.components.categoryIcon
import com.example.ui.theme.PersianTeal
import com.example.ui.theme.SaffronGold
import com.example.ui.theme.ScoreTenGreen
import com.example.ui.theme.ScoreTenGreenBg

@Composable
fun RoundResultScreen(
    roundResult: RoundResult,
    totalRounds: Int,
    matchSummary: MatchSummary?,
    onToggleValidity: (Category) -> Unit,
    onNextRound: () -> Unit,
    onPlayAgain: () -> Unit,
    onReturnHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler {
        onReturnHome()
    }

    val isLastRound = roundResult.roundNumber >= totalRounds || matchSummary != null

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        LetterTileBadge(letter = roundResult.targetLetter, size = 48.dp)
                        Column {
                            Text(
                                text = "نتایج دور ${PersianTextHelper.toPersianDigits(roundResult.roundNumber)} (حرف «${roundResult.targetLetter}»)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "برای تغییر داوری کلمات، دکمه داور هر بخش را بزنید",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
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
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    if (!isLastRound) {
                        Button(
                            onClick = onNextRound,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PersianTeal,
                                contentColor = Color.White,
                            ),
                            contentPadding = PaddingValues(vertical = 14.dp),
                            modifier = Modifier
                                .widthIn(max = 720.dp)
                                .fillMaxWidth()
                                .testTag("next_round_button"),
                        ) {
                            Text(
                                text = "شروع دور بعدی (${PersianTextHelper.toPersianDigits(roundResult.roundNumber + 1)} از ${PersianTextHelper.toPersianDigits(totalRounds)})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Rounded.NavigateNext, contentDescription = null)
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .widthIn(max = 720.dp)
                                .fillMaxWidth(),
                        ) {
                            OutlinedButton(
                                onClick = onReturnHome,
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(vertical = 14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("result_home_button"),
                            ) {
                                Icon(Icons.Rounded.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("صفحه اصلی", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onPlayAgain,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PersianTeal,
                                    contentColor = Color.White,
                                ),
                                contentPadding = PaddingValues(vertical = 14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("play_again_button"),
                            ) {
                                Icon(Icons.Rounded.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("بازی دوباره", fontWeight = FontWeight.Bold)
                            }
                        }
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
                // Final Match Summary Banner if match is complete
                if (matchSummary != null) {
                    item {
                        val playerWon = matchSummary.playerTotalScore > matchSummary.botTotalScore
                        val isTie = matchSummary.playerTotalScore == matchSummary.botTotalScore
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (playerWon || isTie) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.secondaryContainer
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("match_summary_card"),
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.EmojiEvents,
                                    contentDescription = null,
                                    tint = SaffronGold,
                                    modifier = Modifier.size(44.dp),
                                )
                                Text(
                                    text = when {
                                        playerWon -> "تبریک! شما برنده مسابقه شدید"
                                        isTie -> "مسابقه با نتیجه مساوی به پایان رسید!"
                                        else -> "حریف هوشمند این دست را برد!"
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "امتیاز کل شما: ${PersianTextHelper.toPersianDigits(matchSummary.playerTotalScore)}  |  حریف: ${PersianTextHelper.toPersianDigits(matchSummary.botTotalScore)}",
                                    style = MaterialTheme.typography.titleMedium,
                                )

                                matchSummary.progressionDelta?.let { delta ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                    ) {
                                        Text(
                                            text = if (delta.winBonusPoints > 0) {
                                                "+${PersianTextHelper.toPersianDigits(delta.earnedCareerScore)} امتیاز کلی (شامل ${PersianTextHelper.toPersianDigits(delta.winBonusPoints)} پاداش برد)"
                                            } else {
                                                "+${PersianTextHelper.toPersianDigits(delta.earnedCareerScore)} امتیاز کلی ثبت شد"
                                            },
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = PersianTeal,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        )
                                    }

                                    if (delta.leveledUp) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = ScoreTenGreenBg,
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.AutoAwesome,
                                                    contentDescription = null,
                                                    tint = ScoreTenGreen,
                                                    modifier = Modifier.size(18.dp),
                                                )
                                                Text(
                                                    text = "ارتقاء سطح! سطح ${PersianTextHelper.toPersianDigits(delta.newLevel.level)}: ${delta.newLevel.titleFa}",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ScoreTenGreen,
                                                )
                                            }
                                        }
                                    }

                                    if (delta.newlyUnlockedBadges.isNotEmpty()) {
                                        delta.newlyUnlockedBadges.forEach { badge ->
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = SaffronGold.copy(alpha = 0.2f),
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                ) {
                                                    Icon(
                                                        imageVector = badgeIcon(badge),
                                                        contentDescription = null,
                                                        tint = SaffronGold,
                                                        modifier = Modifier.size(18.dp),
                                                    )
                                                    Text(
                                                        text = "نشان افتخار جدید: ${badge.titleFa}",
                                                        style = MaterialTheme.typography.labelLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Round Score Comparison Card
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "امتیاز شما در این دور",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = PersianTextHelper.toPersianDigits(roundResult.playerRoundScore),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = PersianTeal,
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "امتیاز حریف هوشمند",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    text = PersianTextHelper.toPersianDigits(roundResult.botRoundScore),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                            }
                        }
                    }
                }

                // All 20 Category Evaluations
                items(
                    items = roundResult.evaluations,
                    key = { it.category.id },
                ) { eval ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
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
                                    Icon(
                                        imageVector = categoryIcon(eval.category),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Text(
                                        text = eval.category.titleFa,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                ScorePill(score = eval.playerScore)
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "پاسخ شما:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = eval.playerAnswer.ifBlank { "— (خالی)" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "پاسخ حریف (${PersianTextHelper.toPersianDigits(eval.botScore)} امتیاز):",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = eval.botAnswer.ifBlank { "— (خالی)" },
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }

                            // Status & Referee Override
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val statusText = when {
                                    eval.playerAnswer.isBlank() -> "پاسخ داده نشده"
                                    !eval.startsWithCorrectLetter -> "با حرف «${roundResult.targetLetter}» شروع نشده"
                                    !eval.isPlayerValid -> "رد شده توسط داور"
                                    eval.isSharedAnswer -> "پاسخ مشترک با حریف"
                                    eval.isKnownInWordBank -> "تاییدشده در بانک کلمات"
                                    else -> "پذیرفته‌شده (کلمه آزاد)"
                                }
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (eval.isPlayerValid) ScoreTenGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                )

                                if (eval.playerAnswer.isNotBlank() && eval.startsWithCorrectLetter) {
                                    TextButton(
                                        onClick = { onToggleValidity(eval.category) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Gavel,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (eval.isPlayerValid) "رد داور" else "تایید داور",
                                            style = MaterialTheme.typography.labelMedium,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(12.dp)) }
            }
        }
    }
}
