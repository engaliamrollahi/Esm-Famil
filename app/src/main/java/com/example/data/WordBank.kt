package com.example.data

import com.example.model.Category
import com.example.model.PERSIAN_ALPHABET
import com.example.model.PersianTextHelper

object WordBank {
    private val allEntries: Map<String, Map<Category, List<String>>> by lazy {
        buildMap {
            putAll(wordBankPart1)
            putAll(wordBankPart2)
            putAll(wordBankPart3)
            putAll(wordBankPart4)
        }
    }

    private val normalizedLookup: Map<Pair<String, Category>, Set<String>> by lazy {
        buildMap {
            for (letter in PERSIAN_ALPHABET) {
                val letterMap = allEntries[letter].orEmpty()
                for (category in Category.entries) {
                    val words = letterMap[category].orEmpty()
                    put(
                        letter to category,
                        words.map { PersianTextHelper.normalize(it) }.toSet()
                    )
                }
            }
        }
    }

    fun getEntries(letter: String, category: Category): List<String> {
        return allEntries[letter]?.get(category).orEmpty()
    }

    fun getAllForLetter(letter: String): Map<Category, List<String>> {
        return allEntries[letter].orEmpty()
    }

    fun isKnownWord(letter: String, category: Category, input: String): Boolean {
        val norm = PersianTextHelper.normalize(input)
        if (norm.isEmpty()) return false
        val knownSet = normalizedLookup[letter to category].orEmpty()
        if (norm in knownSet) return true
        // Also allow matching base word without parenthetical notes if any exist
        return knownSet.any { known ->
            known == norm || (norm.length >= 3 && known.startsWith(norm) && known.length - norm.length <= 4)
        }
    }
}
