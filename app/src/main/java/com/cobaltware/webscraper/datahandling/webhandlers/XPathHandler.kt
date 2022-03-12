package com.cobaltware.webscraper.datahandling.webhandlers

import com.cobaltware.webscraper.datahandling.Response
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.StringReader
import java.net.URL
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathExpressionException
import javax.xml.xpath.XPathFactory

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
    for (value in listOf(content, prevUrl, nextUrl))
        if (value == "INVALID") return Response.Failure("Invalid x path")

    val title = factory.newXPath().compile("//title").evaluate(doc, XPathConstants.STRING) as String

    return Response.Success(listOf(title, content, prevUrl, nextUrl, url))
}

fun getValuesFromXPath(factory: XPathFactory, path: String, doc: Document): String? {
    val contentExpression: XPathExpression =
        factory.newXPath().compile(path)
    return try {
        contentExpression.evaluate(doc, XPathConstants.STRING) as String?
    } catch (e: XPathExpressionException) {
        "INVALID"
    }
}

fun getUrlFromXPath(url: String, factory: XPathFactory, path: String, doc: Document): String {
    var element: String? = getValuesFromXPath(factory, path, doc)
    if (!element.isNullOrBlank()) {
        val urlMatcher =
            Pattern.compile("((http|https|ftp)://([\\w-\\d]+\\.)+[\\w-\\d]+)?(/[\\w~,;\\-\\./?%&+#=]*)" + ")")
                .matcher(element)
        val substring = urlMatcher.group()
        element = if (!substring.isNullOrEmpty()) substring else "INVALID"
        if (element == "INVALID") return element
    }

    val baseURL = URL(url)
    return URL(baseURL, element).toString()
}