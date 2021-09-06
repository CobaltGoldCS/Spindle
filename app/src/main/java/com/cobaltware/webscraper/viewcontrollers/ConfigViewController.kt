package com.cobaltware.webscraper.viewcontrollers

import com.cobaltware.webscraper.R
import android.content.Context
import android.util.TypedValue
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import com.cobaltware.webscraper.datahandling.Config


class ConfigViewController(private val context: Context) {

    private fun getColor(res: Int): Color {
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(res, typedValue, true)
        return Color(typedValue.data)
    }

    @Composable
    fun ConfigRecycler(data: LiveData<List<Config>>, clickHandler: (Config) -> Unit) {
        val columnData by data.observeAsState()
        val state = rememberLazyListState()
        columnData?.let { list ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(0.dp, 3.dp),
                state = state
            ) {
                items(items = list, itemContent = { item ->
                    Text(
                        text = item.domain,
                        modifier = Modifier
                            .clickable(onClick = { clickHandler.invoke(item) })
                            .fillMaxWidth()
                            .padding(5.dp, 2.dp)
                            .border(
                                BorderStroke(
                                    2.dp,
                                    getColor(R.attr.colorOnPrimary),
                                ),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(10.dp, 5.dp),
                        getColor(R.attr.colorOnPrimary),
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                    )
                })
            }
        }
    }

}