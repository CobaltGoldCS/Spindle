package com.cobaltware.webscraper.viewcontrollers

import com.cobaltware.webscraper.fragments.FragmentRead
import java.util.concurrent.CompletableFuture


/** Handler for handling page changing logistics
 * @param readFragment The readFragment, which this uses the methods from
 */
class ChapterChangeHandler(private val readFragment: FragmentRead) {
    private lateinit var futureUrl: CompletableFuture<List<String?>>

    /** Preloads the url asynchronously
     *  @param url The url to preload*/
    fun prepPageChange(url: String) {
        futureUrl = CompletableFuture.supplyAsync { readFragment.getUrlInfo(url) }
    }

    /** Handles updating the UI of the fragment from the preloaded [futureUrl] defined in [prepPageChange]*/
    fun changePage() {
        readFragment.vibrate(100)
        val data = futureUrl.get()
        if (data.isNullOrEmpty()) {   // Error Handling in event of url break
            readFragment.quit("Unknown Error has occurred, may be linking to bad url")
            return
        }
        val (title, content, prevUrl, nextUrl, current) = data
        readFragment.requireActivity().runOnUiThread {
            readFragment.updateUi(title!!, content!!, prevUrl, nextUrl, current)
        }
    }

}