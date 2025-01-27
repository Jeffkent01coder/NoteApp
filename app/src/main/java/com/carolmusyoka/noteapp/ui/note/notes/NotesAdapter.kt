package com.carolmusyoka.noteapp.ui.note.notes

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.carolmusyoka.noteapp.R
import com.carolmusyoka.noteapp.data.room.model.Notes
import kotlinx.android.synthetic.main.item_post_notes.view.*
import java.util.*


class NotesAdapter : RecyclerView.Adapter<NotesAdapter.NotesVH>() {

    inner class NotesVH(itemView: View) : RecyclerView.ViewHolder(itemView)


    private val differCallback = object : DiffUtil.ItemCallback<Notes>() {
        override fun areItemsTheSame(oldItem: Notes, newItem: Notes): Boolean {
            return oldItem.id == newItem.id
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: Notes, newItem: Notes): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesVH {

        return NotesVH(
                LayoutInflater.from(parent.context).inflate(
                        R.layout.item_post_notes,
                        parent,
                        false
                )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: NotesVH, position: Int) {
        val item = differ.currentList[position]



              holder.itemView.apply {
                  val androidColors = resources.getIntArray(R.array.androidcolors)
                  val randomAndroidColor = androidColors[Random().nextInt(androidColors.size)]
                  view_color.setBackgroundColor(randomAndroidColor)
            item_notes_title.text = item.title
            item_notes_desc.text = item.description
            date.text = item.date
            // on item click
            setOnClickListener {
                onItemClickListener?.let { it(item) }
            }
        }

    }

    // on item click listener
    private var onItemClickListener: ((Notes) -> Unit)? = null
    fun setOnItemClickListener(listener: (Notes) -> Unit) {
        onItemClickListener = listener
    }
}