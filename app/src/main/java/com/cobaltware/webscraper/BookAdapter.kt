package com.cobaltware.webscraper

import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item.view.*
open class BookAdapter(var bookList : List<Book>) : RecyclerView.Adapter<BookAdapter.ItemHolder>(){
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
    open fun addClickHandler (col_id : Int){}
    open fun openClickHandler(col_id : Int){}

    fun changeItems(newList : List<Book>){ bookList = newList }

    class ItemHolder(itemView : View) : RecyclerView.ViewHolder(itemView)
    {
        // Important variables for functions
        val titleView : TextView = itemView.bookTitle
        val moreButton : Button = itemView.moreButton
        val clickableArea : View = itemView.clickableArea
    }

}

// Beefy custom Listener https://stackoverflow.com/questions/29424944/recyclerview-itemclicklistener-in-kotlin
/* Deprecated
class RecyclerItemClickListener(context: Context, recyclerView: RecyclerView, private val mListener: OnItemClickListener?) : RecyclerView.OnItemTouchListener {

    private val mGestureDetector: GestureDetector

    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)

        fun onItemLongClick(view: View?, position: Int)
    }

    init {

        mGestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                val childView = recyclerView.findChildViewUnder(e.x, e.y)

                if (childView != null && mListener != null) {
                    mListener.onItemLongClick(childView, recyclerView.getChildAdapterPosition(childView))
                }
            }
        })
    }

    override fun onInterceptTouchEvent(view: RecyclerView, e: MotionEvent): Boolean {
        val childView = view.findChildViewUnder(e.x, e.y)

        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            mListener.onItemClick(childView, view.getChildAdapterPosition(childView))
        }

        return false
    }

    override fun onTouchEvent(view: RecyclerView, motionEvent: MotionEvent) {}

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}}

 */