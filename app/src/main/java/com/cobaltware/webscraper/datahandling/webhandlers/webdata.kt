package com.cobaltware.webscraper.datahandling.webhandlers

import com.cobaltware.webscraper.datahandling.Response
import org.jsoup.Connection
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import java.io.IOException

/**Function used to get relevant data from [url]
 * @param url The url to get the information from
 * @param contentPath The path of the text content of the chapter / page
 * @param prevPath The path that contains the url to the previous chapter
 * @param nextPath The path that contains the url to the next chapter
 * @return A Call to a specific syntax analyzer [analyzeSyntax]
 * or an empty list if it fails
 * @see analyzeSyntax*/
fun webdata(
    url: String, contentPath: String,
    prevPath: String, nextPath: String
): Response<List<String?>> {
    return try {
        val connection: Connection.Response = Jsoup.connect(url)
            .userAgent(" Mozilla/5.0 (Android 7.0; Mobile; rv:62.0)")
            .referrer("http://www.google.com")
            .execute()
        analyzeSyntax(connection, url, contentPath, prevPath, nextPath)
    } catch (e: HttpStatusException) {
        Response.Failure("Connection failed; Http error Code: ${e.statusCode}")
    } catch (e: IOException) {
        Response.Failure("Connection failed; Error message: ${e.message}")
    }
}

fun analyzeSyntax(
    connection: Connection.Response, url: String, contentPath: String,
    prevPath: String, nextPath: String
): Response<List<String?>> {
    val document = connection.parse()
    if (isCssPath(document, contentPath)) {
        return Response.Success(csspathReader(document, contentPath, prevPath, nextPath))
    }
    if (contentPath.startsWith("/"))
        return xPathReader(url, document.html(), contentPath, prevPath, nextPath)

    return Response.Failure("This domain / url is not properly supported in the configs")
}


