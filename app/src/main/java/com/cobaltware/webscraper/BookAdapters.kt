package com.cobaltware.webscraper

import android.content.Context
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item.view.*
import java.util.*


open class BookAdapter(var bookList: List<Book>) : RecyclerView.Adapter<BookAdapter.ItemHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder
    {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item,
                parent, false)
        return ItemHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int)
    {
        val currentItem = bookList[position]
        holder.titleView.text = currentItem.title
        // Hacky way to make the on click listeners of list items customizable from outside
        holder.moreButton.setOnClickListener{
            val id : Int = currentItem.col_id
            addClickHandler(id)
        }
        holder.clickableArea.setOnClickListener {
            val id : Int = currentItem.col_id
            openClickHandler(id)
        }
    }
    override fun getItemCount() = bookList.size

    fun getItemsList() : ArrayList<Book> = ArrayList(bookList)
    // This allows for modification of the click behavior of the buttons
    open fun addClickHandler(col_id: Int){}
    open fun openClickHandler(col_id: Int){}

    fun changeItems(newList: List<Book>){
        val difresult = DiffUtil.calculateDiff(BookCallback(newList, bookList))
        difresult.dispatchUpdatesTo(this)
        bookList = newList
    }

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        // Important variables for functions
        val titleView :  TextView = itemView.bookTitle
        val moreButton : Button  = itemView.moreButton
        val clickableArea : View = itemView.clickableArea
    }

}
// For calculating difference between new and old in recyclerview
class BookCallback(newBooks: List<Book>, oldBooks: List<Book>) : DiffUtil.Callback()
{
    var oldBooks: List<Book>
    var newBooks: List<Book>
    override fun getOldListSize(): Int {
        return oldBooks.size
    }

    override fun getNewListSize(): Int {
        return newBooks.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldBooks[oldItemPosition].col_id == newBooks[newItemPosition].col_id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldBooks[oldItemPosition] == newBooks[newItemPosition]
    }

    init {
        this.newBooks = newBooks
        this.oldBooks = oldBooks
    }
}
