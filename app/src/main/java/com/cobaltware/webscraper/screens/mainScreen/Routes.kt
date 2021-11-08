package com.cobaltware.webscraper.screens.mainScreen

sealed class Routes(val route: String) {
    object ListRoute : Routes(route = "list")
    object DropdownRoute : Routes(route = "dropdown")
}
