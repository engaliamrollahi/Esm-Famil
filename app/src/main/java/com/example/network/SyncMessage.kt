package com.example.network

import com.example.model.Category
import org.json.JSONObject

sealed interface SyncMessage {
    val type: String

    data class Hello(
        val playerName: String,
        val avatarId: String,
    ) : SyncMessage {
        override val type: String = TYPE
        companion object {
            const val TYPE = "HELLO"
        }
    }

    data class RoundStart(
        val roundNumber: Int,
        val totalRounds: Int,
        val targetLetter: String,
        val timerSeconds: Int,
        val isNewMatch: Boolean,
    ) : SyncMessage {
        override val type: String = TYPE
        companion object {
            const val TYPE = "ROUND_START"
        }
    }

    data class StopPressed(
        val roundNumber: Int,
    ) : SyncMessage {
        override val type: String = TYPE
        companion object {
            const val TYPE = "STOP_PRESSED"
        }
    }

    data class RoundAnswers(
        val roundNumber: Int,
        val answers: Map<Category, String>,
    ) : SyncMessage {
        override val type: String = TYPE
        companion object {
            const val TYPE = "ROUND_ANSWERS"
        }
    }

    data class LeaveMatch(
        val reason: String = "left",
    ) : SyncMessage {
        override val type: String = TYPE
        companion object {
            const val TYPE = "LEAVE_MATCH"
        }
    }

    fun encode(): String {
        val obj = JSONObject()
        obj.put("type", type)
        when (this) {
            is Hello -> {
                obj.put("playerName", playerName)
                obj.put("avatarId", avatarId)
            }
            is RoundStart -> {
                obj.put("roundNumber", roundNumber)
                obj.put("totalRounds", totalRounds)
                obj.put("targetLetter", targetLetter)
                obj.put("timerSeconds", timerSeconds)
                obj.put("isNewMatch", isNewMatch)
            }
            is StopPressed -> {
                obj.put("roundNumber", roundNumber)
            }
            is RoundAnswers -> {
                obj.put("roundNumber", roundNumber)
                val answersJson = JSONObject()
                answers.forEach { (category, value) ->
                    answersJson.put(category.id, value)
                }
                obj.put("answers", answersJson)
            }
            is LeaveMatch -> {
                obj.put("reason", reason)
            }
        }
        return obj.toString()
    }

    companion object {
        fun decode(raw: String): SyncMessage? {
            return try {
                val obj = JSONObject(raw.trim())
                when (obj.optString("type")) {
                    Hello.TYPE -> Hello(
                        playerName = obj.optString("playerName", "حریف").ifBlank { "حریف" },
                        avatarId = obj.optString("avatarId", "PEN_MASTER"),
                    )
                    RoundStart.TYPE -> RoundStart(
                        roundNumber = obj.optInt("roundNumber", 1),
                        totalRounds = obj.optInt("totalRounds", 1),
                        targetLetter = obj.optString("targetLetter", "م"),
                        timerSeconds = obj.optInt("timerSeconds", 120),
                        isNewMatch = obj.optBoolean("isNewMatch", false),
                    )
                    StopPressed.TYPE -> StopPressed(
                        roundNumber = obj.optInt("roundNumber", 1),
                    )
                    RoundAnswers.TYPE -> {
                        val roundNum = obj.optInt("roundNumber", 1)
                        val answersObj = obj.optJSONObject("answers") ?: JSONObject()
                        val map = Category.entries.associateWith { category ->
                            answersObj.optString(category.id, "")
                        }
                        RoundAnswers(roundNumber = roundNum, answers = map)
                    }
                    LeaveMatch.TYPE -> LeaveMatch(
                        reason = obj.optString("reason", "left"),
                    )
                    else -> null
                }
            } catch (_: Exception) {
                null
            }
        }
    }
}
