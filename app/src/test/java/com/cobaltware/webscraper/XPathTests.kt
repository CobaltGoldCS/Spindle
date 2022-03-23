package com.cobaltware.webscraper


import com.cobaltware.webscraper.datahandling.webhandlers.getUrlFromXPath
import com.cobaltware.webscraper.datahandling.webhandlers.xPathReader
import org.junit.Assert
import org.junit.Test
import javax.xml.xpath.XPathFactory

class XPathTests {
    @Test
    fun xPathReaderABTest() {
        val (title, main_text_content, previous_url, next_url, current_url) = xPathReader(
            "https://test.com",
            testDocument().html(),
            "//p[@class=\"test\"]",
            "//a/@href",
            "//a/@href").confirmSuccess().data
        Assert.assertEquals("Test Title", title)
        Assert.assertEquals("Test text", main_text_content)
        // There is one a tag that both should get present in testDocument()
        Assert.assertArrayEquals(arrayOf<String?>("https://test.com", "https://test.com"),
            arrayOf(previous_url, next_url))
        Assert.assertEquals("https://test.com", current_url)
    }

    @Test
    fun nullInvalidUrlsProperly() {
        // Malformed xpath
        val test1 = getUrlFromXPath(
            "https://test.com",
            XPathFactory.newInstance(),
            "///p",
            w3cCompliantDocument())
        // Xpath that doesn't contain href (or point properly)
        val test2 = getUrlFromXPath(
            "https://test.com",
            XPathFactory.newInstance(),
            "//p",
            w3cCompliantDocument())

        Assert.assertEquals(null, test1)
        Assert.assertEquals(null, test2)
    }
}