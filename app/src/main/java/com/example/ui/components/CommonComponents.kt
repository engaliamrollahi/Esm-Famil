package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Face
import androidx.compose.material.icons.rounded.Face3
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.LocalFlorist
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Theaters
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Category
import com.example.model.Category.*
import com.example.model.HonorBadge
import com.example.model.PersianTextHelper
import com.example.model.PlayerAvatar
import com.example.ui.theme.PersianTeal
import com.example.ui.theme.PersianTealLight
import com.example.ui.theme.SaffronGold
import com.example.ui.theme.SaffronGoldLight
import com.example.ui.theme.ScoreFiveAmber
import com.example.ui.theme.ScoreFiveAmberBg
import com.example.ui.theme.ScoreTenGreen
import com.example.ui.theme.ScoreTenGreenBg
import com.example.ui.theme.ScoreZeroRed
import com.example.ui.theme.ScoreZeroRedBg

fun categoryIcon(category: Category): ImageVector {
    return when (category) {
        BOY_NAME -> Icons.Rounded.Face
        GIRL_NAME -> Icons.Rounded.Face3
        LAST_NAME -> Icons.Rounded.FamilyRestroom
        FOOD -> Icons.Rounded.Restaurant
        FRUIT -> Icons.Rounded.Eco
        ANIMAL -> Icons.Rounded.Pets
        CITY -> Icons.Rounded.LocationCity
        COUNTRY -> Icons.Rounded.Public
        OBJECT -> Icons.Rounded.Inventory2
        COLOR -> Icons.Rounded.Palette
        CAR -> Icons.Rounded.DirectionsCar
        FLOWER -> Icons.Rounded.LocalFlorist
        JOB -> Icons.Rounded.Work
        BODY_PART -> Icons.Rounded.AccessibilityNew
        CLOTHING -> Icons.Rounded.Checkroom
        CELEBRITY -> Icons.Rounded.AutoAwesome
        SPORT -> Icons.AutoMirrored.Rounded.DirectionsRun
        FOREIGN_MOVIE -> Icons.Rounded.Movie
        IRANIAN_MOVIE -> Icons.Rounded.Theaters
        ANIMATION -> Icons.Rounded.Animation
    }
}

@Composable
fun SoundQuickToggleButton(
    soundEnabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onToggle,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag("sound_quick_toggle"),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Icon(
                imageVector = if (soundEnabled) Icons.AutoMirrored.Rounded.VolumeUp else Icons.AutoMirrored.Rounded.VolumeOff,
                contentDescription = if (soundEnabled) "صدا روشن" else "صدا خاموش",
                tint = if (soundEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = if (soundEnabled) "صدا روشن" else "صدا خاموش",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun LetterTileBadge(
    letter: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(PersianTeal, PersianTealLight)
                )
            ),
    ) {
        Text(
            text = letter,
            color = Color.White,
            fontSize = (size.value * 0.46f).sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun ScorePill(
    score: Int,
    modifier: Modifier = Modifier,
) {
    val (bg, fg) = when {
        score >= 10 -> ScoreTenGreenBg to ScoreTenGreen
        score >= 5 -> ScoreFiveAmberBg to ScoreFiveAmber
        else -> ScoreZeroRedBg to ScoreZeroRed
    }
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = bg,
        modifier = modifier,
    ) {
        Text(
            text = "${PersianTextHelper.toPersianDigits(score)} امتیاز",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

fun avatarIcon(avatar: PlayerAvatar): ImageVector {
    return when (avatar) {
        PlayerAvatar.PEN_MASTER -> Icons.Rounded.Edit
        PlayerAvatar.CROWN -> Icons.Rounded.WorkspacePremium
        PlayerAvatar.SCHOLAR -> Icons.Rounded.School
        PlayerAvatar.EXPLORER -> Icons.Rounded.Explore
        PlayerAvatar.LION -> Icons.Rounded.Shield
        PlayerAvatar.LIGHTNING -> Icons.Rounded.Bolt
    }
}

fun badgeIcon(badge: HonorBadge): ImageVector {
    return when (badge) {
        HonorBadge.FIRST_VICTORY -> Icons.Rounded.EmojiEvents
        HonorBadge.RISING_STAR -> Icons.Rounded.Star
        HonorBadge.FIVE_WINS_WARRIOR -> Icons.Rounded.MilitaryTech
        HonorBadge.TEN_WINS_MASTER -> Icons.Rounded.WorkspacePremium
        HonorBadge.TWENTY_WINS_LEGEND -> Icons.Rounded.AutoAwesome
        HonorBadge.STREAK_CHAMPION -> Icons.Rounded.LocalFireDepartment
        HonorBadge.HARD_MODE_CONQUEROR -> Icons.Rounded.Shield
        HonorBadge.CAREER_SCORER -> Icons.Rounded.Badge
    }
}

@Composable
fun PlayerAvatarBadge(
    avatar: PlayerAvatar,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(SaffronGold, SaffronGoldLight)
                )
            ),
    ) {
        Icon(
            imageVector = avatarIcon(avatar),
            contentDescription = avatar.titleFa,
            tint = Color.White,
            modifier = Modifier.size(size * 0.54f),
        )
    }
}
