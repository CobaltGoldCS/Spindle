package com.cobaltware.webscraper.general


import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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

@ExperimentalAnimationApi
@Composable
fun HidingFAB(visibility: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    AnimatedVisibility(
        visible = visibility,
        enter = slideInHorizontally(),
        exit = slideOutHorizontally()
    ) {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            content = { Icon(imageVector = Icons.Filled.Add, null) },
            backgroundColor = MaterialTheme.colors.secondary,
            contentColor = MaterialTheme.colors.onSecondary,
            elevation = FloatingActionButtonDefaults.elevation(
                10.dp,
                0.dp
            )
        )
    }
}

object WebscraperThemeColors {
    private val darkSecond = Color(0xFFFB3640)
    private val platinum = Color(0xFFE2E2E2)

    @SuppressLint("ConflictingOnColor")
    val dark = darkColors(
        primary = Color(0xFF87C0BD),
        primaryVariant = Color(0xFF4B8F8C),
        onPrimary = platinum,
        background = Color(0xff1e2127),
        onBackground = platinum,
        surface = Color(0xff1e2127),
        secondary = darkSecond,
        secondaryVariant = Color(0xFFFFE66D),
        onSecondary = platinum,
        error = darkSecond,
        onError = platinum
    )

    private val dimGray = Color(0xFF222222)
    private val secondVar = Color(0xFFE94957)
    val light = lightColors(
        primary = Color(0xFF79ADDC),
        primaryVariant = Color(0xFFCD700A),
        onPrimary = dimGray,
        background = Color.White,
        surface = platinum,
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

