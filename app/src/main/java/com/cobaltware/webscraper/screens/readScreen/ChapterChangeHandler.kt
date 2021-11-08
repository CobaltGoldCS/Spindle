package com.cobaltware.webscraper.screens.readScreen

import com.cobaltware.webscraper.datahandling.Response
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CompletableFuture


/** Handler for handling page changing logistics
 * @param readFragment The readFragment, which this uses the methods from
 */
class ChapterChangeHandler(private val readFragment: FragmentRead) {
    private lateinit var loadedData: CompletableFuture<Response<List<String?>>>
    private var url: String = ""

    /** Preloads the url asynchronously
     *  @param url The url to preload*/
    fun prepPageChange(url: String) {
        this.url = url
        var tries = 3
        // Retries the operation if it fails twice
        do {
            loadedData = CompletableFuture.supplyAsync { readFragment.getUrlInfo(url) }
            tries--
        } while (tries > 0 && completedAndFailed())
    }

    /** Handles updating the UI of the fragment from the preloaded [loadedData] defined in [prepPageChange]*/
    fun changePage() {
        readFragment.vibrate(100)
        when (val data = loadedData.get()) {
            is Response.Success<*> -> {
                val (title, content, prevUrl, nextUrl, current) = data.confirmSuccess().data
                runBlocking { readFragment.updateUi(title!!, content!!, prevUrl, nextUrl, current) }
            }
            is Response.Failure -> {
                readFragment.quit(data.failureMessage)
            }
        }
    }

    /**Utility function that returns true if the get data operation completed and failed**/
    private fun completedAndFailed() = loadedData.isDone && loadedData.get() is Response.Failure

}