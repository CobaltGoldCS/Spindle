package com.cobaltware.webscraper.datahandling.webhandlers

import com.cobaltware.webscraper.datahandling.Response
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.StringReader
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathExpressionException
import javax.xml.xpath.XPathFactory

/** A scraper using xpaths
 * @param url The current url of the page you want to scrape
 * @param text The html content of the page
 * @param contentPath The xpath that will select the content of your book
 * @param prevPath The xpath that selects the url to the previous chapter
 * (NOTE: select the url DIRECTLY using something like /@href)
 * @param nextPath The xpath that selects the url to the next chapter
 * (NOTE: select the url DIRECTLY using something like /@href)
 * @author CobaltGoldCS
 * **/
fun xPathReader(
    url: String,
    text: String,
    contentPath: String,
    prevPath: String,
    nextPath: String,
): Response<List<String?>> {
    val doc: Document = DocumentBuilderFactory.newInstance()
        .newDocumentBuilder().parse(InputSource(StringReader(text)))
    val factory = XPathFactory.newInstance()

    val content = getValuesFromXPath(factory, contentPath, doc)
    val prevUrl = getUrlFromXPath(url, factory, prevPath, doc)
    val nextUrl = getUrlFromXPath(url, factory, nextPath, doc)

    if (content.isNullOrEmpty())
        return Response.Failure("There was no body text detected in the contentPath")
    val title = factory.newXPath().compile("//title").evaluate(doc, XPathConstants.STRING) as String

    return Response.Success(listOf(title, content, prevUrl, nextUrl, url))
}

fun getValuesFromXPath(factory: XPathFactory, path: String, doc: Document): String? {
    return try {
        val contentExpression: XPathExpression =
            factory.newXPath().compile(path)
        contentExpression.evaluate(doc, XPathConstants.STRING) as String
    } catch (e: XPathExpressionException) {
        null
    }
}

fun getUrlFromXPath(
    url: String,
    factory: XPathFactory,
    path: String,
    doc: Document,
): String? {
    val element = getValuesFromXPath(factory, path, doc)
    if (element?.startsWith("http") == false)
        return null
    return try {
        URL(URL(url), element).toString()
    } catch (e: java.net.MalformedURLException) {
        null
    }
}