package com.cobaltware.webscraper.datahandling

sealed class Response {
    sealed class Success(val data: List<String?>) : Response()
    sealed class Failure(val failureMessage: String) : Response()
}
