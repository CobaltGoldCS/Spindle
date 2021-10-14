package com.cobaltware.webscraper.screens.readScreen

import com.cobaltware.webscraper.datahandling.Response
import kotlinx.coroutines.*
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.CoroutineContext


/** Handler for handling page changing logistics
 * @param readFragment The readFragment, which this uses the methods from
 */
class ChapterChangeHandler(private val readFragment: FragmentRead) {
    private lateinit var loadedData: CompletableFuture<Response>
    private val scope = CoroutineScope(Dispatchers.IO)

    /** Preloads the url asynchronously
     *  @param url The url to preload*/
    fun prepPageChange(url: String) {
        loadedData = CompletableFuture.supplyAsync { readFragment.getUrlInfo(url) }
    }

    /** Handles updating the UI of the fragment from the preloaded [loadedData] defined in [prepPageChange]*/
    fun changePage() {
        readFragment.vibrate(100)
        val data = loadedData.get()
        if (data is Response.Failure) {   // Error Handling in event of url break
            readFragment.quit(data.failureMessage)
            return
        }
        val (title, content, prevUrl, nextUrl, current) = (data as Response.Success).data
        runBlocking { readFragment.updateUi(title!!, content!!, prevUrl, nextUrl, current) }
    }

}