package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Category
import com.example.model.PersianTextHelper
import com.example.ui.theme.ScoreFiveAmber
import com.example.ui.theme.ScoreFiveAmberBg
import com.example.ui.theme.ScoreTenGreen
import com.example.ui.theme.ScoreTenGreenBg
import com.example.ui.theme.ScoreZeroRed
import com.example.ui.theme.ScoreZeroRedBg

@Composable
fun GuideScreen(
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("guide_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(46.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Column {
                    Text(
                        text = "راهنمای مرحله‌به‌مرحله بازی",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "روش بازی اسم فامیل و نحوه دقیق محاسبه امتیازات",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Step-by-step How to Play
            Text(
                text = "مراحل انجام بازی",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            GuideStepCard(
                stepNumber = 1,
                title = "تنظیم حروف الفبا، زمان و صدا",
                description = "در صفحه اصلی قبل از شروع بازی، می‌توانید حروف مجاز الفبا را انتخاب کنید (همه ۳۲ حرف، حروف پرکاربرد یا انتخاب یک حرف مشخص)، زمان پاسخگویی هر دور را از ۶۰ ثانیه تا بدون زمان تنظیم کنید و سطح حریف هوشمند و صدای بازی را روشن یا خاموش نمایید.",
                icon = Icons.Rounded.Settings,
            )

            GuideStepCard(
                stepNumber = 2,
                title = "شروع دور و نمایش حرف هدف",
                description = "با زدن دکمه «شروع بازی اسم فامیل»، یک حرف از حروف انتخابی شما تعیین می‌شود و تایمر به حرکت درمی‌آید. در این صفحه تمام ۲۰ بخش بازی به ترتیب نمایش داده می‌شوند.",
                icon = Icons.Rounded.PlayCircle,
            )

            GuideStepCard(
                stepNumber = 3,
                title = "پر کردن هر ۲۰ بخش با حرف مشخص‌شده",
                description = "باید برای تمام ۲۰ بخش (${Category.entries.joinToString("، ") { it.titleFa }}) کلمه‌ای بنویسید که با حرف آن دور شروع شود. با دکمه «بعدی» صفحه‌کلید می‌توانید سریع بین بخش‌ها جابجا شوید.",
                icon = Icons.Rounded.EditNote,
            )

            GuideStepCard(
                stepNumber = 4,
                title = "اعلام «استپ!» یا پایان زمان",
                description = "اگر زودتر از پایان وقت تمام بخش‌ها را پر کردید یا خواستید دور را پایان دهید، دکمه «استپ! تمام کردم» را بزنید. اگر زمان به صفر برسد، پاسخ‌های شما به صورت خودکار ثبت و داوری می‌شوند.",
                icon = Icons.Rounded.StopCircle,
            )

            // Scoring Rules Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 6.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Calculate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    text = "نحوه محاسبه امتیازات (مرحله‌به‌مرحله)",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            ScoreRuleCard(
                points = "۱۰ امتیاز",
                title = "پاسخ صحیح و غیرمشترک",
                explanation = "اگر پاسخ شما با حرف آن دور شروع شود، معتبر باشد و حریف هوشمند کلمه‌ای متفاوت از شما نوشته باشد (یا حریف آن بخش را خالی گذاشته باشد)، ۱۰ امتیاز کامل می‌گیرید.",
                example = "مثال برای حرف «م»: شما «مرجان» نوشته‌اید و حریف «مریم» نوشته است ← هر دو ۱۰ امتیاز.",
                bgColor = ScoreTenGreenBg,
                accentColor = ScoreTenGreen,
                icon = Icons.Rounded.CheckCircle,
            )

            ScoreRuleCard(
                points = "۵ امتیاز",
                title = "پاسخ صحیح و مشترک",
                explanation = "اگر پاسخ شما و حریف هوشمند در یک بخش کاملاً یکسان باشد (پس از یکسان‌سازی نیم‌فاصله و حروف)، هر دو نفر نصف امتیاز یعنی ۵ امتیاز دریافت می‌کنید.",
                example = "مثال برای حرف «س»: شما «سیب» نوشته‌اید و حریف هم «سیب» نوشته است ← ۵ امتیاز.",
                bgColor = ScoreFiveAmberBg,
                accentColor = ScoreFiveAmber,
                icon = Icons.Rounded.People,
            )

            ScoreRuleCard(
                points = "۰ امتیاز",
                title = "پاسخ خالی، با حرف اشتباه یا نامعتبر",
                explanation = "اگر بخشی را خالی بگذارید، کلمه شما با حرف آن دور شروع نشود، یا کلمه نامعتبر/تکراری بی‌معنی باشد، آن بخش ۰ امتیاز می‌گیرد.",
                example = "مثال برای حرف «ب»: اگر بنویسید «انار» یا خانه را خالی بگذارید ← ۰ امتیاز.",
                bgColor = ScoreZeroRedBg,
                accentColor = ScoreZeroRed,
                icon = Icons.Rounded.Close,
            )

            // Extra Tips & Referee Feature
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Gavel,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        Text(
                            text = "نکات مهم و داوری عادلانه",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                    Text(
                        text = "• حروف «آ» و «ا» در ابتدای کلمات یکسان در نظر گرفته می‌شوند (مثلاً «آرش» و «امیر» هر دو برای حرف الف صحیح هستند).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Text(
                        text = "• در صفحه نتایج پایان دور، می‌توانید با دکمه «تایید داور / رد داور» روی هر بخش، نظر نهایی داوری را تغییر دهید و امتیاز کل بلافاصله به‌روزرسانی می‌شود.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "بخش «تقلب» برای مرور و یادگیری کلمات همیشه در صفحه اصلی در دسترس است (در حین بازی مخفی می‌ماند).",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun GuideStepCard(
    stepNumber: Int,
    title: String,
    description: String,
    icon: ImageVector,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(34.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = PersianTextHelper.toPersianDigits(stepNumber),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ScoreRuleCard(
    points: String,
    title: String,
    explanation: String,
    example: String,
    bgColor: Color,
    accentColor: Color,
    icon: ImageVector,
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
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
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = accentColor,
                ) {
                    Text(
                        text = points,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF1F2937),
            )
            Text(
                text = example,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = accentColor,
            )
        }
    }
}
