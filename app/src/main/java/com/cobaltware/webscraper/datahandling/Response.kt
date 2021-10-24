package com.cobaltware.webscraper.datahandling

sealed class Response {
    class Success<T>(val data: T) : Response()
    class Failure(val failureMessage: String) : Response()
}
