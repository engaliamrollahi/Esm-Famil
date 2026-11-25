package com.example

import com.example.data.WordBank
import com.example.model.Category
import com.example.model.PERSIAN_ALPHABET
import com.example.model.PersianTextHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WordBankValidationTest {

    @Test
    fun `every letter and every category has 1 to 10 valid entries`() {
        assertEquals(32, PERSIAN_ALPHABET.size)
        assertEquals(20, Category.entries.size)

        val failures = mutableListOf<String>()

        for (letter in PERSIAN_ALPHABET) {
            for (category in Category.entries) {
                val entries = WordBank.getEntries(letter, category)
                if (entries.isEmpty() || entries.size > 10) {
                    failures.add("$letter -> ${category.titleFa}: count=${entries.size}")
                }
                for (entry in entries) {
                    if (!PersianTextHelper.startsWithTargetLetter(entry, letter)) {
                        failures.add("$letter -> ${category.titleFa}: invalid start word '$entry'")
                    }
                }
            }
        }

        assertTrue("WordBank issues:\n${failures.joinToString("\n")}", failures.isEmpty())
    }
}
