package com.cobaltware.webscraper.datahandling.webhandlers

import com.cobaltware.webscraper.datahandling.Response
import org.jsoup.Connection
import org.jsoup.Jsoup

/**Function used to get relevant data from [url]
 * @param url The url to get the information from
 * @param contentPath The path of the text content of the chapter / page
 * @param prevPath The path that contains the url to the previous chapter
 * @param nextPath The path that contains the url to the next chapter
 * @return A Call to a [csspathreader] instance using the document and the paths passed in,
 * or an empty list if it fails
 * @see csspathReader*/
fun webdata(
    url: String, contentPath: String,
    prevPath: String, nextPath: String
): Response {
    val connection: Connection.Response = Jsoup.connect(url)
        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/86.0")
        .execute()

    // Fail if nothing is found at location
    if (connection.statusCode() != 200)
        return Response.Failure("Connection failed; Error ${connection.statusCode()}")

    // Put any syntax analyzers down here
    val document = connection.parse()
    if (isCssPath(document, contentPath)) {
        return Response.Success(csspathReader(document, contentPath, prevPath, nextPath))
    }
    return Response.Failure("This domain / url is not properly supported in the configs; or it may not exist anymore")
}


