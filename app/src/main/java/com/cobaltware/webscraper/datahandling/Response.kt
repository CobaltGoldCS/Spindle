package com.cobaltware.webscraper.datahandling

sealed class Response {
    class Success(val data: List<String?>) : Response()
    class Failure(val failureMessage: String) : Response()
}
