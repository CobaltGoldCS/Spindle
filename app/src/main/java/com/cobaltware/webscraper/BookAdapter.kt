package com.cobaltware.webscraper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.cobaltware.webscraper.databinding.ItemReaderListBinding
import com.cobaltware.webscraper.datahandling.Book
import com.cobaltware.webscraper.dialogs.ModifyBookDialog
import com.cobaltware.webscraper.viewcontrollers.MainViewController


open class BookAdapter(
    private val controller: MainViewController
) : RecyclerView.Adapter<BookAdapter.ItemHolder>() {

    var bookList = mutableListOf<Book>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val itemView = ItemReaderListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(itemView)
    }

    override fun getItemCount() = bookList.size

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val currentItem = bookList[position]
        holder.titleView.text = currentItem.title
        // Hacky way to make the on click listeners of list items customizable from outside
        holder.moreButton.setOnClickListener { modifyClickHandler(currentItem) }
        holder.clickableArea.setOnClickListener { openClickHandler(currentItem) }
    }

    // This allows for modification of the click behavior of the buttons
    open fun modifyClickHandler(book: Book) {
        ModifyBookDialog(book)
    }

    open fun openClickHandler(book: Book) {
        controller.initReadFragment(book)
    }

    fun changeItems(newList: List<Book>) {
        val diffResult = DiffUtil.calculateDiff(BookCallback(newList, bookList))
        diffResult.dispatchUpdatesTo(this)
        bookList = newList as MutableList<Book>
    }

    class ItemHolder(itemView: ItemReaderListBinding) : RecyclerView.ViewHolder(itemView.root) {
        // Important variables for functions

        val titleView: TextView = itemView.bookTitle
        val moreButton: Button = itemView.moreButton
        val clickableArea: View = itemView.clickableArea
    }


}

// For calculating difference between new and old in recyclerview
class BookCallback(private val newBooks: List<Book>, private val oldBooks: List<Book>) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldBooks.size
    override fun getNewListSize(): Int = newBooks.size

    override fun areItemsTheSame(oldItemPos: Int, newItemPos: Int): Boolean =
        oldBooks[oldItemPos].row_id == newBooks[newItemPos].row_id

    override fun areContentsTheSame(oldItemPos: Int, newItemPos: Int): Boolean =
        oldBooks[oldItemPos] == newBooks[newItemPos]

}
