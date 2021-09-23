package com.cobaltware.webscraper.viewcontrollers


import android.annotation.SuppressLint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.LiveData

@Composable
fun <T> LiveRecycler(
    data: LiveData<List<T>>,
    state: LazyListState = rememberLazyListState(),
    content: LazyListScope.(List<T>) -> Unit,
) {
    val columnData by data.observeAsState()
    columnData?.let { list ->
        BaseRecycler(list, content, state)
    }
}

@Composable
private fun <T> BaseRecycler(
    data: List<T>,
    content: LazyListScope.(List<T>) -> Unit,
    state: LazyListState
) {
    LazyColumn(
        state = state,
        content = {
            content.invoke(this, data)
        }
    )
}

@Composable
fun <T> LiveDropdown(items: LiveData<List<T>>, content: @Composable (List<T>) -> Unit) {
    val data by items.observeAsState()
    data?.let {
        ThemedDropdown(items = it, content = content)
    }
}


@Composable
fun <T> ThemedDropdown(items: List<T>, content: @Composable (List<T>) -> Unit) {
    content.invoke(items)
}

private object WebscraperThemeColors {
    private val darkBackground = Color(0xff1e2127)
    private val darkSecond = Color(0xFFFF6B6B)

    @SuppressLint("ConflictingOnColor")
    val dark = darkColors(
        primary = Color(0xFF5DB7DE),
        primaryVariant = Color(0xFF1F7498),
        onPrimary = Color.White,
        background = darkBackground,
        onBackground = Color.White,
        surface = darkBackground,
        secondary = darkSecond,
        secondaryVariant = Color(0xFFFFE66D),
        onSecondary = Color.White,
        error = darkSecond,
        onError = Color.White
    )

    private val dimGray = Color(0xFF222222)
    private val secondVar = Color(0xFFE94957)
    val light = lightColors(
        primary = Color(0xFF79ADDC),
        primaryVariant = Color(0xFFCD700A),
        onPrimary = dimGray,
        background = Color.White,
        surface = Color.White,
        secondary = Color(0xFFEE901A),
        secondaryVariant = secondVar,
        onSecondary = Color.White,
        error = secondVar,
        onError = Color.White
    )
}

@Composable
fun WebscraperTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = if (isSystemInDarkTheme()) WebscraperThemeColors.dark else WebscraperThemeColors.light,
        content = content
    )
}

