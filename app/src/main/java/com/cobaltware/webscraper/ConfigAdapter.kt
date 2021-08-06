package com.cobaltware.webscraper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cobaltware.webscraper.datahandling.Config
import kotlinx.android.synthetic.main.item_config_list.view.*
import kotlinx.android.synthetic.main.item_reader_list.view.clickableArea

open class ConfigAdapter(var configList: List<Config>) :
    RecyclerView.Adapter<ConfigAdapter.ItemHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_config_list,
            parent, false
        )
        return ItemHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val currentItem = configList[position]
        holder.titleView.text = currentItem.domain
        holder.clickableArea.setOnClickListener {
            clickHandler(currentItem.col_id)
        }
    }

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Important variables for functions
        val titleView: TextView = itemView.configTitle
        val clickableArea: View = itemView.clickableArea
    }

    fun changeItems(newList: List<Config>) {
        val diffresult = DiffUtil.calculateDiff(ConfigCallback(newList, configList))
        diffresult.dispatchUpdatesTo(this)
        configList = newList
    }

    open fun clickHandler(col_id: Int) {}
    override fun getItemCount(): Int = configList.size
}

// For calculating difference between new and old in recyclerview
class ConfigCallback(private val newConfigs: List<Config>, private val oldConfigs: List<Config>) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldConfigs.size
    override fun getNewListSize(): Int = newConfigs.size

    override fun areItemsTheSame(oldItemPos: Int, newItemPos: Int): Boolean =
        oldConfigs[oldItemPos].col_id == newConfigs[newItemPos].col_id

    override fun areContentsTheSame(oldItemPos: Int, newItemPos: Int): Boolean =
        oldConfigs[oldItemPos] == newConfigs[newItemPos]

}