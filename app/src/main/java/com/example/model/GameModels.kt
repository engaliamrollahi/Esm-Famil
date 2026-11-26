package com.example.model

enum class Category(
    val id: String,
    val titleFa: String,
    val placeholderHint: String,
) {
    BOY_NAME("boy_name", "اسم پسر", "مثلاً: آرش"),
    GIRL_NAME("girl_name", "اسم دختر", "مثلاً: سارا"),
    LAST_NAME("last_name", "فامیل", "مثلاً: رضایی"),
    FOOD("food", "غذا", "مثلاً: قورمه‌سبزی"),
    FRUIT("fruit", "میوه", "مثلاً: سیب"),
    ANIMAL("animal", "حیوان", "مثلاً: شیر"),
    CITY("city", "شهر", "مثلاً: شیراز"),
    COUNTRY("country", "کشور", "مثلاً: ایران"),
    OBJECT("object", "اشیاء", "مثلاً: میز"),
    COLOR("color", "رنگ", "مثلاً: آبی"),
    CAR("car", "ماشین", "مثلاً: پراید"),
    FLOWER("flower", "گل", "مثلاً: رز"),
    JOB("job", "شغل", "مثلاً: پزشک"),
    BODY_PART("body_part", "اعضا بدن", "مثلاً: دست"),
    CLOTHING("clothing", "لباس", "مثلاً: پیراهن"),
    CELEBRITY("celebrity", "مشاهیر", "مثلاً: حافظ"),
    SPORT("sport", "ورزش", "مثلاً: فوتبال"),
    FOREIGN_MOVIE("foreign_movie", "فیلم خارجی", "مثلاً: تایتانیک"),
    IRANIAN_MOVIE("iranian_movie", "فیلم ایرانی", "مثلاً: جدایی نادر از سیمین"),
    ANIMATION("animation", "انیمیشن", "مثلاً: شیرشاه"),
}

val PERSIAN_ALPHABET: List<String> = listOf(
    "ا", "ب", "پ", "ت", "ث", "ج", "چ", "ح",
    "خ", "د", "ذ", "ر", "ز", "ژ", "س", "ش",
    "ص", "ض", "ط", "ظ", "ع", "غ", "ف", "ق",
    "ک", "گ", "ل", "م", "ن", "و", "ه", "ی"
)

val POPULAR_LETTERS: Set<String> = setOf(
    "ا", "ب", "پ", "ت", "ج", "د", "ر", "ز",
    "س", "ش", "ف", "ق", "ک", "گ", "ل", "م", "ن", "ه", "ی"
)

val TIMER_OPTIONS_SECONDS: List<Int> = listOf(60, 90, 120, 180, 240, 300, 0)

enum class BotDifficulty(val titleFa: String, val descriptionFa: String, val fillRate: Float) {
    EASY("آسان", "حریف حدود ۶۰٪ بخش‌ها را پر می‌کند", 0.62f),
    MEDIUM("متوسط", "حریف حدود ۸۰٪ بخش‌ها را پر می‌کند", 0.80f),
    HARD("حرفه‌ای", "حریف تقریباً تمام بخش‌ها را پر می‌کند", 0.95f),
}

enum class LetterPickMode(val titleFa: String) {
    RANDOM_FROM_SELECTED("تصادفی از حروف انتخابی"),
    MANUAL_CHOICE("انتخاب دستی حرف"),
}

enum class ThemeMode(val titleFa: String) {
    SYSTEM("مطابق سیستم"),
    LIGHT("روشن"),
    DARK("تاریک"),
}

data class GameSettings(
    val selectedLetters: Set<String> = PERSIAN_ALPHABET.toSet(),
    val timerSeconds: Int = 120,
    val roundsCount: Int = 3,
    val botDifficulty: BotDifficulty = BotDifficulty.MEDIUM,
    val letterPickMode: LetterPickMode = LetterPickMode.RANDOM_FROM_SELECTED,
    val manualLetter: String = "م",
    val soundEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

enum class PlayerAvatar(val id: String, val titleFa: String) {
    PEN_MASTER("pen_master", "استاد قلم"),
    CROWN("crown", "تاج‌دار"),
    SCHOLAR("scholar", "حکیم"),
    EXPLORER("explorer", "جهانگرد"),
    LION("lion", "شیر بیشه"),
    LIGHTNING("lightning", "تیزهوش"),
}

data class PlayerLevelInfo(
    val level: Int,
    val titleFa: String,
    val minWins: Int,
    val nextLevelWins: Int?,
    val nextLevelTitleFa: String?,
) {
    val isMaxLevel: Boolean get() = nextLevelWins == null

    fun progressFraction(totalWins: Int): Float {
        val target = nextLevelWins ?: return 1f
        val span = (target - minWins).coerceAtLeast(1)
        return ((totalWins - minWins).toFloat() / span.toFloat()).coerceIn(0f, 1f)
    }

    fun winsRemaining(totalWins: Int): Int {
        val target = nextLevelWins ?: return 0
        return (target - totalWins).coerceAtLeast(0)
    }
}

object LevelProgression {
    private data class LevelTier(val level: Int, val titleFa: String, val minWins: Int)

    private val tiers = listOf(
        LevelTier(1, "تازه‌وارد", 0),
        LevelTier(2, "شاگرد زرنگ", 2),
        LevelTier(3, "کلمه‌شناس", 4),
        LevelTier(4, "خوش‌قلم", 7),
        LevelTier(5, "استاد واژه‌ها", 11),
        LevelTier(6, "قهرمان اسم فامیل", 16),
        LevelTier(7, "اسطوره ادبیات", 23),
        LevelTier(8, "افسانه بی‌رقیب", 31),
    )

    fun fromWins(totalWins: Int): PlayerLevelInfo {
        val safeWins = totalWins.coerceAtLeast(0)
        val idx = tiers.indexOfLast { safeWins >= it.minWins }.coerceAtLeast(0)
        val current = tiers[idx]
        val next = tiers.getOrNull(idx + 1)
        return PlayerLevelInfo(
            level = current.level,
            titleFa = current.titleFa,
            minWins = current.minWins,
            nextLevelWins = next?.minWins,
            nextLevelTitleFa = next?.titleFa,
        )
    }

    fun allTiers(): List<PlayerLevelInfo> {
        return tiers.mapIndexed { index, tier ->
            val next = tiers.getOrNull(index + 1)
            PlayerLevelInfo(
                level = tier.level,
                titleFa = tier.titleFa,
                minWins = tier.minWins,
                nextLevelWins = next?.minWins,
                nextLevelTitleFa = next?.titleFa,
            )
        }
    }
}

enum class HonorBadge(
    val id: String,
    val titleFa: String,
    val requirementFa: String,
    val targetValue: Int,
) {
    FIRST_VICTORY("first_victory", "نشان نخستین پیروزی", "کسب ۱ برد در مسابقات", 1),
    RISING_STAR("rising_star", "ستاره نوظهور", "کسب ۳ برد در مسابقات", 3),
    FIVE_WINS_WARRIOR("five_wins", "نشان پنجگانه پیروزی", "کسب ۵ برد در مسابقات", 5),
    TEN_WINS_MASTER("ten_wins", "مدال استاد ده‌گانه", "کسب ۱۰ برد در مسابقات", 10),
    TWENTY_WINS_LEGEND("twenty_wins", "تاج اسطوره اسم فامیل", "کسب ۲۰ برد در مسابقات", 20),
    STREAK_CHAMPION("streak_champion", "قهرمان شکست‌ناپذیر", "۳ برد پیاپی بدون باخت", 3),
    HARD_MODE_CONQUEROR("hard_conqueror", "فاتح حریف حرفه‌ای", "۱ برد در سطح حریف حرفه‌ای", 1),
    CAREER_SCORER("career_scorer", "گنجینه امتیاز", "کسب ۱۰۰۰ امتیاز کلی", 1000);

    fun currentProgress(stats: AppStats): Int {
        return when (this) {
            FIRST_VICTORY, RISING_STAR, FIVE_WINS_WARRIOR, TEN_WINS_MASTER, TWENTY_WINS_LEGEND -> stats.totalWins
            STREAK_CHAMPION -> stats.bestWinStreak
            HARD_MODE_CONQUEROR -> stats.hardWins
            CAREER_SCORER -> stats.totalCareerScore
        }
    }

    fun isUnlocked(stats: AppStats): Boolean = currentProgress(stats) >= targetValue

    companion object {
        fun unlockedFor(stats: AppStats): List<HonorBadge> = entries.filter { it.isUnlocked(stats) }
    }
}

data class AppStats(
    val playerName: String = "بازیکن",
    val avatar: PlayerAvatar = PlayerAvatar.PEN_MASTER,
    val totalGamesPlayed: Int = 0,
    val totalWins: Int = 0,
    val totalCareerScore: Int = 0,
    val highScore: Int = 0,
    val currentWinStreak: Int = 0,
    val bestWinStreak: Int = 0,
    val hardWins: Int = 0,
    val perfectRounds: Int = 0,
) {
    val levelInfo: PlayerLevelInfo get() = LevelProgression.fromWins(totalWins)
    val unlockedBadges: List<HonorBadge> get() = HonorBadge.unlockedFor(this)
    val winRatePercent: Int
        get() = if (totalGamesPlayed <= 0) 0 else ((totalWins * 100f) / totalGamesPlayed).toInt().coerceIn(0, 100)
}

data class MatchProgressionDelta(
    val earnedCareerScore: Int,
    val winBonusPoints: Int,
    val oldLevel: PlayerLevelInfo,
    val newLevel: PlayerLevelInfo,
    val newlyUnlockedBadges: List<HonorBadge>,
) {
    val leveledUp: Boolean get() = newLevel.level > oldLevel.level
}

enum class GameMode(val titleFa: String, val subtitleFa: String) {
    AI("هوش مصنوعی", "رقابت با ربات هوشمند در ۳ سطح"),
    LOCAL_NETWORK("شبکه محلی", "همگام‌سازی با بلوتوث یا وای‌فای"),
    ONLINE("آنلاین", "مسابقه ابری و اینترنتی"),
}

enum class LocalTransportType(val titleFa: String, val subtitleFa: String) {
    WIFI("وای‌فای محلی", "اتصال روی یک شبکه وای‌فای یا هات‌اسپات"),
    BLUETOOTH("بلوتوث", "اتصال مستقیم بین دو گوشی نزدیک"),
}

enum class LocalRole {
    HOST,
    GUEST,
}

enum class LocalConnectionStatus(val labelFa: String) {
    IDLE("آماده اتصال"),
    HOSTING("در انتظار پیوستن بازیکن..."),
    SCANNING("در حال جست‌وجوی بازی‌های اطراف..."),
    CONNECTING("در حال برقراری ارتباط..."),
    CONNECTED("متصل و آماده بازی"),
    ERROR("خطا در اتصال"),
}

data class DiscoveredPeer(
    val id: String,
    val name: String,
    val address: String,
    val port: Int,
    val transport: LocalTransportType,
    val isPaired: Boolean = false,
)

data class LocalLobbyState(
    val transport: LocalTransportType = LocalTransportType.WIFI,
    val role: LocalRole? = null,
    val status: LocalConnectionStatus = LocalConnectionStatus.IDLE,
    val statusDetail: String = "",
    val roomName: String = "",
    val hostIpAddress: String = "",
    val hostPort: Int = 0,
    val discoveredPeers: List<DiscoveredPeer> = emptyList(),
    val opponentName: String? = null,
    val opponentAvatar: PlayerAvatar? = null,
    val waitingForOpponentAnswers: Boolean = false,
    val errorMessage: String? = null,
)

data class ActiveRoundState(
    val roundNumber: Int,
    val totalRounds: Int,
    val targetLetter: String,
    val totalDurationSeconds: Int,
    val remainingSeconds: Int,
    val answers: Map<Category, String> = Category.entries.associateWith { "" },
    val usedLetters: List<String> = emptyList(),
    val gameMode: GameMode = GameMode.AI,
    val opponentName: String = "حریف هوشمند",
    val isHost: Boolean = true,
)

data class CategoryEvaluation(
    val category: Category,
    val playerAnswer: String,
    val botAnswer: String,
    val isPlayerValid: Boolean,
    val isKnownInWordBank: Boolean,
    val isSharedAnswer: Boolean,
    val playerScore: Int,
    val botScore: Int,
    val startsWithCorrectLetter: Boolean,
)

data class RoundResult(
    val roundNumber: Int,
    val totalRounds: Int = 1,
    val targetLetter: String,
    val evaluations: List<CategoryEvaluation>,
    val playerRoundScore: Int,
    val botRoundScore: Int,
    val gameMode: GameMode = GameMode.AI,
    val opponentName: String = "حریف هوشمند",
    val isHost: Boolean = true,
)

data class MatchSummary(
    val rounds: List<RoundResult>,
    val playerTotalScore: Int,
    val botTotalScore: Int,
    val gameMode: GameMode = GameMode.AI,
    val opponentName: String = "حریف هوشمند",
    val progressionDelta: MatchProgressionDelta? = null,
)

object PersianTextHelper {
    private val diacriticsRegex = Regex("[\\u064B-\\u065F\\u0670\\u0640\\u200C\\u200D\\s\\p{Punct}]+")
    private val repeatedCharRegex = Regex("^(.)\\1+$")

    fun normalize(input: String): String {
        return input.trim()
            .replace('آ', 'ا')
            .replace('أ', 'ا')
            .replace('إ', 'ا')
            .replace('ٱ', 'ا')
            .replace('ك', 'ک')
            .replace('ي', 'ی')
            .replace('ى', 'ی')
            .replace('ئ', 'ی')
            .replace('ة', 'ه')
            .replace('ۀ', 'ه')
            .replace(diacriticsRegex, "")
            .lowercase()
    }

    fun firstNormalizedLetter(input: String): String? {
        val cleaned = normalize(input)
        return cleaned.firstOrNull()?.toString()
    }

    fun startsWithTargetLetter(input: String, targetLetter: String): Boolean {
        val first = firstNormalizedLetter(input) ?: return false
        val targetNorm = normalize(targetLetter)
        return first == targetNorm
    }

    fun isPlausibleWord(input: String, targetLetter: String): Boolean {
        val norm = normalize(input)
        if (norm.length < 2) return false
        if (!startsWithTargetLetter(input, targetLetter)) return false
        if (repeatedCharRegex.matches(norm)) return false
        return true
    }

    fun toPersianDigits(value: Int): String = toPersianDigits(value.toString())

    fun toPersianDigits(text: String): String {
        val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val sb = StringBuilder(text.length)
        for (ch in text) {
            if (ch in '0'..'9') {
                sb.append(persianDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatDurationLabel(seconds: Int): String {
        if (seconds <= 0) return "بدون زمان"
        val mins = seconds / 60
        val secs = seconds % 60
        return when {
            mins > 0 && secs == 0 -> "${toPersianDigits(mins)} دقیقه"
            mins > 0 -> "${toPersianDigits(mins)} دقیقه و ${toPersianDigits(secs)} ثانیه"
            else -> "${toPersianDigits(secs)} ثانیه"
        }
    }

    fun formatClock(seconds: Int): String {
        val safe = seconds.coerceAtLeast(0)
        val mins = safe / 60
        val secs = safe % 60
        val formatted = "%02d:%02d".format(mins, secs)
        return toPersianDigits(formatted)
    }
}
