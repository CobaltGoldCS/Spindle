package com.cobaltware.webscraper.datahandling

sealed class Response<S> {
    open class Success<S>(val data: S) : Response<S>()
    class Failure<F>(val failureMessage: String) : Response<F>()

    /**Always check for a successful response before calling this please
     * @return The response as a success object**/
    fun confirmSuccess(): Success<S> = this as Success<S>
}
