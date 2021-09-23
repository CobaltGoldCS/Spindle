package com.cobaltware.webscraper.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.cobaltware.webscraper.ReaderApplication.Companion.DB
import com.cobaltware.webscraper.databinding.FragmentConfigBinding
import com.cobaltware.webscraper.datahandling.Config
import com.cobaltware.webscraper.dialogs.ConfigDialog
import com.cobaltware.webscraper.viewcontrollers.LiveRecycler
import com.cobaltware.webscraper.viewcontrollers.WebscraperTheme


/**
 * A simple [Fragment] subclass.
 * Used for setting up configurations
 */
class FragmentConfig : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val viewer = FragmentConfigBinding.inflate(inflater)
        viewer.addActionButton.setOnClickListener { addOrChangeConfigDialog(null) }
        return ComposeView(requireContext()).apply {
            setContent {
                val recyclerState = rememberLazyListState()
                WebscraperTheme {
                    Scaffold(
                        floatingActionButtonPosition = FabPosition.Center,
                        content = {
                            LiveRecycler(data = DB.readAllConfigs) { list: List<Config> ->
                                items(items = list) { item -> ConfigItem(item = item) }
                            }
                        },

                        floatingActionButton = {
                            if (recyclerState.firstVisibleItemIndex <= 0) {
                                FloatingActionButton(
                                    onClick = { addOrChangeConfigDialog(null) },
                                    content = { Icon(imageVector = Icons.Filled.Add, null) },
                                    backgroundColor = MaterialTheme.colors.primary,
                                    contentColor = MaterialTheme.colors.onSecondary,
                                    elevation = FloatingActionButtonDefaults.elevation(
                                        10.dp,
                                        0.dp
                                    )
                                )
                            }

                        }

                    )
                    // End of Webscraper theme
                }
                // End of set content
            }
        }
    }

    /** Creates a [ConfigDialog] using the given [config] and displays it
     * @param config The config to put in the [ConfigDialog]*/
    private fun addOrChangeConfigDialog(config: Config?) {
        val dialog = ConfigDialog(config)
        dialog.show(requireActivity().supportFragmentManager, "Add New Config")
    }

    @Composable
    fun ConfigItem(item: Config) {
        Text(
            text = item.domain,
            modifier = Modifier
                .clickable(onClick = {
                    val neededConfig =
                        DB.readItemFromConfigs(item.row_id)
                    addOrChangeConfigDialog(neededConfig)
                })
                .fillMaxWidth()
                .padding(5.dp, 2.dp)
                .border(
                    BorderStroke(
                        2.dp,
                        MaterialTheme.colors.onPrimary,
                    ),
                    RoundedCornerShape(10.dp)
                )
                .padding(10.dp, 5.dp),
            MaterialTheme.colors.onPrimary,
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
        )
    }
}