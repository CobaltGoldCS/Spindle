package com.cobaltware.webscraper

import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cobaltware.webscraper.datahandling.Book
import kotlinx.android.synthetic.main.item_reader_list.view.*


open class BookAdapter(var bookList: List<Book>) : RecyclerView.Adapter<BookAdapter.ItemHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder
    {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_reader_list,
                parent, false)
        return ItemHolder(itemView)
    }
    override fun getItemCount() = bookList.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int)
    {
        val currentItem = bookList[position]
        holder.titleView.text = currentItem.title
        // Hacky way to make the on click listeners of list items customizable from outside
        holder.moreButton.setOnClickListener{
            val id : Int = currentItem.col_id
            modifyClickHandler(id)
        }
        holder.clickableArea.setOnClickListener {
            val id : Int = currentItem.col_id
            openClickHandler(id)
        }
    }
    // This allows for modification of the click behavior of the buttons
    open fun modifyClickHandler (col_id: Int){}
    open fun openClickHandler(col_id: Int){}

    fun changeItems(newList: List<Book>){
        val diffresult = DiffUtil.calculateDiff(BookCallback(newList, bookList))
        diffresult.dispatchUpdatesTo(this)
        bookList = newList
    }

    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        // Important variables for functions
        val titleView :  TextView = itemView.bookTitle
        val moreButton : Button   = itemView.moreButton
        val clickableArea : View  = itemView.clickableArea
    }

}
// For calculating difference between new and old in recyclerview
class BookCallback(private val newBooks: List<Book>, private val oldBooks: List<Book>) : DiffUtil.Callback()
{

    override fun getOldListSize(): Int = oldBooks.size
    override fun getNewListSize(): Int = newBooks.size

    override fun areItemsTheSame(oldItemPos: Int, newItemPos: Int): Boolean
       = oldBooks[oldItemPos].col_id == newBooks[newItemPos].col_id

    override fun areContentsTheSame(oldItemPos: Int, newItemPos: Int): Boolean
       = oldBooks[oldItemPos] == newBooks[newItemPos]

}
