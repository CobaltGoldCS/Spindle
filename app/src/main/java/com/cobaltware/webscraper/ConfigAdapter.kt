package com.cobaltware.webscraper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cobaltware.webscraper.databinding.ItemConfigListBinding
import com.cobaltware.webscraper.datahandling.Config

open class ConfigAdapter(private var configList: List<Config>) :
    RecyclerView.Adapter<ConfigAdapter.ItemHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val itemView = ItemConfigListBinding.inflate(LayoutInflater.from(parent.context))
        return ItemHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val currentItem = configList[position]
        holder.titleView.text = currentItem.domain
        holder.clickableArea.setOnClickListener {
            clickHandler(currentItem.row_id)
        }
    }

    class ItemHolder(itemView: ItemConfigListBinding) : RecyclerView.ViewHolder(itemView.root) {
        // Important variables for functions
        val titleView: TextView = itemView.configTitle
        val clickableArea: View = itemView.clickableArea
    }

    fun changeItems(newList: List<Config>?) {
        if (newList != null){
            val diffresult = DiffUtil.calculateDiff(ConfigCallback(newList, configList))
            diffresult.dispatchUpdatesTo(this)
            configList = newList
        }
    }

    open fun clickHandler(row_id: Int) {}
    override fun getItemCount(): Int = configList.size
}

// For calculating difference between new and old in recyclerview
class ConfigCallback(private val newConfigs: List<Config>, private val oldConfigs: List<Config>) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldConfigs.size
    override fun getNewListSize(): Int = newConfigs.size

    override fun areItemsTheSame(oldItemPos: Int, newItemPos: Int): Boolean =
        oldConfigs[oldItemPos].row_id == newConfigs[newItemPos].row_id

    override fun areContentsTheSame(oldItemPos: Int, newItemPos: Int): Boolean =
        oldConfigs[oldItemPos] == newConfigs[newItemPos]

}