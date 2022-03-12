package com.cobaltware.webscraper

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cobaltware.webscraper.datahandling.webhandlers.customSyntaxAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CSSPathTests {
    @Test
    fun syntaxAnalysisTest() {
        val text = customSyntaxAnalyzer(testDocument(), "\$text .text") as String
        assertEquals("Test text", text)
    }
}