package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AppStats
import com.example.model.BotDifficulty
import com.example.model.DiscoveredPeer
import com.example.model.GameMode
import com.example.model.GameSettings
import com.example.model.LetterPickMode
import com.example.model.LocalConnectionStatus
import com.example.model.LocalLobbyState
import com.example.model.LocalRole
import com.example.model.LocalTransportType
import com.example.model.PERSIAN_ALPHABET
import com.example.model.PersianTextHelper
import com.example.model.TIMER_OPTIONS_SECONDS
import com.example.ui.components.LocalNetworkLobbyCard
import com.example.ui.components.PlayerAvatarBadge
import com.example.ui.components.SoundQuickToggleButton
import com.example.ui.theme.PersianTeal
import com.example.ui.theme.PersianTealDark
import com.example.ui.theme.SaffronGold

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeSetupScreen(
    settings: GameSettings,
    stats: AppStats,
    selectedGameMode: GameMode,
    localLobbyState: LocalLobbyState,
    isBluetoothSupported: Boolean,
    isBluetoothEnabled: Boolean,
    missingBluetoothHostPermissions: () -> List<String>,
    missingBluetoothScanPermissions: () -> List<String>,
    onSelectGameMode: (GameMode) -> Unit,
    onSelectLocalTransport: (LocalTransportType) -> Unit,
    onStartLocalHosting: () -> Unit,
    onStartLocalDiscovery: () -> Unit,
    onConnectLocalPeer: (DiscoveredPeer) -> Unit,
    onConnectDirectWifi: (String, Int) -> Unit,
    onDisconnectLocalLobby: () -> Unit,
    onToggleSound: () -> Unit,
    onSetSoundEnabled: (Boolean) -> Unit,
    onToggleLetter: (String) -> Unit,
    onSelectAllLetters: () -> Unit,
    onSelectPopularLetters: () -> Unit,
    onSetLetterPickMode: (LetterPickMode) -> Unit,
    onSetManualLetter: (String) -> Unit,
    onSetTimerSeconds: (Int) -> Unit,
    onSetRoundsCount: (Int) -> Unit,
    onSetBotDifficulty: (BotDifficulty) -> Unit,
    onStartGame: () -> Unit,
    onOpenCheatSheet: () -> Unit,
    onOpenGuide: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Top Header with Sound Quick Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "دفترچه اسم فامیل",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "۲۰ بخش کامل • ۳۲ حرف الفبای فارسی",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                SoundQuickToggleButton(
                    soundEnabled = settings.soundEnabled,
                    onToggle = onToggleSound,
                )
            }

            // Hero Banner Card
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(176.dp),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner),
                        contentDescription = "تصویر دفترچه بازی اسم فامیل",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        PersianTealDark.copy(alpha = 0.88f),
                                    )
                                )
                            ),
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "آماده رقابتی جذاب هستید؟",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                        )
                        Text(
                            text = "حروف الفبا و زمان را تنظیم کنید و همه ۲۰ بخش را با سرعت پر کنید!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                        )
                    }
                }
            }

            // Player Profile & Level Summary Banner
            val levelInfo = stats.levelInfo
            Card(
                onClick = onOpenProfile,
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_profile_card"),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            PlayerAvatarBadge(avatar = stats.avatar, size = 46.dp)
                            Column {
                                Text(
                                    text = stats.playerName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Star,
                                        contentDescription = null,
                                        tint = SaffronGold,
                                        modifier = Modifier.size(15.dp),
                                    )
                                    Text(
                                        text = "سطح ${PersianTextHelper.toPersianDigits(levelInfo.level)}: ${levelInfo.titleFa}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MilitaryTech,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = "${PersianTextHelper.toPersianDigits(stats.unlockedBadges.size)} نشان",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                )
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = { levelInfo.progressFraction(stats.totalWins) },
                        color = SaffronGold,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                    )
                }
            }

            // Stats Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                StatChipCard(
                    title = "امتیاز کلی",
                    value = PersianTextHelper.toPersianDigits(stats.totalCareerScore),
                    modifier = Modifier.weight(1f),
                )
                StatChipCard(
                    title = "بردها",
                    value = PersianTextHelper.toPersianDigits(stats.totalWins),
                    modifier = Modifier.weight(1f),
                )
                StatChipCard(
                    title = "بهترین امتیاز",
                    value = PersianTextHelper.toPersianDigits(stats.highScore),
                    modifier = Modifier.weight(1f),
                )
            }

            // Quick Access on Main Screen: Cheat Sheet & Guide
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                QuickActionTile(
                    title = "بخش تقلب",
                    subtitle = "۱ تا ۱۰ مثال برای هر حرف",
                    icon = Icons.Rounded.AutoStories,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    onClick = onOpenCheatSheet,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_cheat_sheet_button"),
                )
                QuickActionTile(
                    title = "راهنمای بازی",
                    subtitle = "آموزش و محاسبه امتیازات",
                    icon = Icons.AutoMirrored.Rounded.MenuBook,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    onClick = onOpenGuide,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_guide_button"),
                )
            }

            // 3 Game Modes Selector
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "انتخاب حالت بازی",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GameMode.entries.forEach { mode ->
                        val selected = selectedGameMode == mode
                        val icon = when (mode) {
                            GameMode.AI -> Icons.Rounded.SmartToy
                            GameMode.LOCAL_NETWORK -> Icons.Rounded.Hub
                            GameMode.ONLINE -> Icons.Rounded.Public
                        }
                        Surface(
                            onClick = { onSelectGameMode(mode) },
                            shape = RoundedCornerShape(16.dp),
                            color = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            border = BorderStroke(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                },
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("game_mode_${mode.name.lowercase()}"),
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = mode.titleFa,
                                    tint = if (selected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(26.dp),
                                )
                                Text(
                                    text = mode.titleFa,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                )
                                Text(
                                    text = when (mode) {
                                        GameMode.AI -> "تک‌نفره"
                                        GameMode.LOCAL_NETWORK -> "بلوتوث / وای‌فای"
                                        GameMode.ONLINE -> "اینترنتی"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            if (selectedGameMode == GameMode.LOCAL_NETWORK) {
                LocalNetworkLobbyCard(
                    lobbyState = localLobbyState,
                    isBluetoothSupported = isBluetoothSupported,
                    isBluetoothEnabled = isBluetoothEnabled,
                    missingBluetoothHostPermissions = missingBluetoothHostPermissions,
                    missingBluetoothScanPermissions = missingBluetoothScanPermissions,
                    onSelectTransport = onSelectLocalTransport,
                    onStartHosting = onStartLocalHosting,
                    onStartDiscovery = onStartLocalDiscovery,
                    onConnectPeer = onConnectLocalPeer,
                    onConnectDirectWifi = onConnectDirectWifi,
                    onDisconnect = onDisconnectLocalLobby,
                )
            }

            if (selectedGameMode == GameMode.ONLINE) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("online_mode_card"),
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CloudOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(28.dp),
                            )
                            Column {
                                Text(
                                    text = "بازی آنلاین ابری غیرفعال است",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "برای ساخت اتاق اینترنتی و همگام‌سازی ابری، فعال‌سازی سرویس ابری (Firebase) لازم است که در این نشست تأیید نشد.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Button(
                                onClick = { onSelectGameMode(GameMode.LOCAL_NETWORK) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PersianTeal),
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("شبکه محلی (بلوتوث/وای‌فای)", style = MaterialTheme.typography.labelMedium)
                            }
                            OutlinedButton(
                                onClick = { onSelectGameMode(GameMode.AI) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("بازی با هوش مصنوعی", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            val showRoundSetupCard = selectedGameMode == GameMode.AI ||
                (selectedGameMode == GameMode.LOCAL_NETWORK && localLobbyState.role != LocalRole.GUEST)

            if (showRoundSetupCard) {
                // Pre-Game Setup Card
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = if (selectedGameMode == GameMode.LOCAL_NETWORK) {
                                "تنظیمات بازی میزبان (حروف و زمان)"
                            } else {
                                "تنظیمات پیش از شروع بازی"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )

                        // Sound Setting inline
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Icon(
                                        imageVector = if (settings.soundEnabled) Icons.AutoMirrored.Rounded.VolumeUp else Icons.AutoMirrored.Rounded.VolumeOff,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                    Column {
                                        Text(
                                            text = "صدای بازی",
                                            style = MaterialTheme.typography.titleSmall,
                                        )
                                        Text(
                                            text = if (settings.soundEnabled) "افکت‌های صوتی و هشدار زمان فعال است" else "بازی در حالت بی‌صدا اجرا می‌شود",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                Switch(
                                    checked = settings.soundEnabled,
                                    onCheckedChange = onSetSoundEnabled,
                                    modifier = Modifier.testTag("pregame_sound_switch"),
                                )
                            }
                        }

                        // Timer Selection
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(20.dp),
                                )
                                Text(
                                    text = "زمان برای پاسخگویی",
                                    style = MaterialTheme.typography.titleSmall,
                                )
                            }
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                TIMER_OPTIONS_SECONDS.forEach { seconds ->
                                    val selected = settings.timerSeconds == seconds
                                    FilterChip(
                                        selected = selected,
                                        onClick = { onSetTimerSeconds(seconds) },
                                        label = {
                                            Text(
                                                text = PersianTextHelper.formatDurationLabel(seconds),
                                                style = MaterialTheme.typography.labelMedium,
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        ),
                                        modifier = Modifier.testTag("timer_chip_$seconds"),
                                    )
                                }
                            }
                        }

                        // Bot Difficulty (only in AI Mode)
                        if (selectedGameMode == GameMode.AI) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Psychology,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Text(
                                        text = "سطح حریف هوشمند",
                                        style = MaterialTheme.typography.titleSmall,
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    BotDifficulty.entries.forEach { diff ->
                                        val selected = settings.botDifficulty == diff
                                        FilterChip(
                                            selected = selected,
                                            onClick = { onSetBotDifficulty(diff) },
                                            label = {
                                                Text(
                                                    text = diff.titleFa,
                                                    style = MaterialTheme.typography.labelMedium,
                                                )
                                            },
                                            modifier = Modifier.weight(1f),
                                        )
                                    }
                                }
                                Text(
                                    text = settings.botDifficulty.descriptionFa,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                    // Letter Pick Mode
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "نحوه انتخاب حرف الفبا",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            LetterPickMode.entries.forEach { mode ->
                                val selected = settings.letterPickMode == mode
                                FilterChip(
                                    selected = selected,
                                    onClick = { onSetLetterPickMode(mode) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = if (mode == LetterPickMode.RANDOM_FROM_SELECTED) {
                                                Icons.Rounded.Casino
                                            } else {
                                                Icons.Rounded.TouchApp
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = mode.titleFa,
                                            style = MaterialTheme.typography.labelSmall,
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }

                        if (settings.letterPickMode == LetterPickMode.RANDOM_FROM_SELECTED) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "تعداد دور مسابقه:",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(1, 3, 5).forEach { count ->
                                        FilterChip(
                                            selected = settings.roundsCount == count,
                                            onClick = { onSetRoundsCount(count) },
                                            label = {
                                                Text("${PersianTextHelper.toPersianDigits(count)} دور")
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Alphabet Selection Grid
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val titleText = if (settings.letterPickMode == LetterPickMode.RANDOM_FROM_SELECTED) {
                                "حروف مجاز الفبا (${PersianTextHelper.toPersianDigits(settings.selectedLetters.size)} حرف)"
                            } else {
                                "انتخاب حرف شروع: «${settings.manualLetter}»"
                            }
                            Text(
                                text = titleText,
                                style = MaterialTheme.typography.titleSmall,
                            )
                            if (settings.letterPickMode == LetterPickMode.RANDOM_FROM_SELECTED) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    TextButton(onClick = onSelectAllLetters) {
                                        Text("همه", style = MaterialTheme.typography.labelMedium)
                                    }
                                    TextButton(onClick = onSelectPopularLetters) {
                                        Text("پرکاربرد", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }

                        // 8-column responsive Persian Alphabet Grid
                        val rows = PERSIAN_ALPHABET.chunked(8)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            rows.forEach { rowLetters ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    rowLetters.forEach { letter ->
                                        val isSelected = if (settings.letterPickMode == LetterPickMode.RANDOM_FROM_SELECTED) {
                                            letter in settings.selectedLetters
                                        } else {
                                            letter == settings.manualLetter
                                        }
                                        AlphabetCell(
                                            letter = letter,
                                            selected = isSelected,
                                            onClick = {
                                                if (settings.letterPickMode == LetterPickMode.RANDOM_FROM_SELECTED) {
                                                    onToggleLetter(letter)
                                                } else {
                                                    onSetManualLetter(letter)
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("letter_cell_$letter"),
                                        )
                                    }
                                }
                            }
                        }
                        }
                    }
                }
            }

            // Primary Start Game CTA (AI mode or Host connected in Local Network mode)
            val canStartAi = selectedGameMode == GameMode.AI
            val canStartLocalHost = selectedGameMode == GameMode.LOCAL_NETWORK &&
                localLobbyState.status == LocalConnectionStatus.CONNECTED &&
                localLobbyState.role == LocalRole.HOST

            if (canStartAi || canStartLocalHost) {
                Button(
                    onClick = onStartGame,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PersianTeal,
                        contentColor = Color.White,
                    ),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("start_game_button"),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedGameMode == GameMode.LOCAL_NETWORK) {
                            "شروع بازی دونفره (${localLobbyState.transport.titleFa})"
                        } else {
                            "شروع بازی با هوش مصنوعی"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun StatChipCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun QuickActionTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        modifier = modifier.minimumInteractiveComponentSize(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(28.dp),
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = contentColor,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.85f),
                )
            }
        }
    }
}

@Composable
private fun AlphabetCell(
    letter: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
        ),
        modifier = modifier.height(44.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = letter,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}
