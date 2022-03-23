package com.cobaltware.webscraper

import com.cobaltware.webscraper.datahandling.webhandlers.csspathReader
import com.cobaltware.webscraper.datahandling.webhandlers.customSyntaxAnalyzer
import com.cobaltware.webscraper.datahandling.webhandlers.processElementIntoUrl
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class CSSPathTests {
    @Test
    fun syntaxAnalysisTest() {
        val text = customSyntaxAnalyzer(testDocument(), "\$text .test").orElse(null)
        assertEquals("Test text", text)
    }

    @Test
    fun readerABTest() {
        val (title, main_text_content, previous_url, next_url, current_url) = csspathReader(
            testDocument(),
            ".test",
            "a",
            "a").confirmSuccess().data

        assertEquals("Test Title", title)
        assertEquals("Test text", main_text_content)
        // There is one a tag that both should get present in testDocument()
        assertArrayEquals(arrayOf<String?>("https://test.com", "https://test.com"),
            arrayOf(previous_url, next_url))
        assertEquals("", current_url)
    }

    @Test
    fun urlScraperTest() {
        // http://test2.com is just a filler url that doesn't have any significance in this test
        val testValue =
            processElementIntoUrl("http://test2.com", testDocument()
                .select("a")
                .first())
        assertEquals("https://test.com", testValue)
    }
}