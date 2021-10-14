package com.cobaltware.webscraper.datahandling

open class Response {
    data class Success(val data: List<String?>) : Response()
    data class Failure(val failureMessage: String) : Response()
}
