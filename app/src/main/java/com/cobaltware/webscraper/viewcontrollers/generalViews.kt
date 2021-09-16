package com.cobaltware.webscraper.viewcontrollers


import android.content.Context
import android.util.TypedValue
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import com.cobaltware.webscraper.R

@Composable
fun <T> LiveRecycler(
    data: LiveData<List<T>>,
    content: LazyListScope.(List<T>) -> Unit
) {
    val columnData by data.observeAsState()
    columnData?.let { list ->
        BaseRecycler(list, content)
    }
}

@Composable
private fun <T> BaseRecycler(
    data: List<T>,
    content: LazyListScope.(List<T>) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(.99f),
        content = {
            content.invoke(this, data)
        }
    )
}

fun getColor(res: Int, context: Context): Color {
    val typedValue = TypedValue()
    val theme = context.theme
    theme.resolveAttribute(res, typedValue, true)
    return Color(typedValue.data)
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