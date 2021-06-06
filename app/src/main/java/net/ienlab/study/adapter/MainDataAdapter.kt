package net.ienlab.study.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import net.ienlab.study.R
import net.ienlab.study.data.TimeData
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainDataAdapter(var items: ArrayList<TimeData>): RecyclerView.Adapter<MainDataAdapter.ItemViewHolder>() {

    lateinit var context: Context
    lateinit var timeFormat: SimpleDateFormat

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        context = parent.context

        val view = LayoutInflater.from(context).inflate(R.layout.adapter_main_data, parent, false)
        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        timeFormat = SimpleDateFormat("a h:mm", Locale.getDefault())
        holder.tvTime.text = timeFormat.format((Calendar.getInstance().apply { timeInMillis = items[position].dateTime }).time)

        when (items[position].type) {
            TimeData.TYPE_SNOOZE -> {
                holder.icon.setImageResource(R.drawable.ic_snooze)
                holder.tvTitle.text = context.getString(R.string.title_snooze)
            }

            else -> {
                holder.icon.setImageResource(R.drawable.ic_book)
                holder.tvTitle.text = context.getString(R.string.title_study)
            }
        }
    }

    inner class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon);
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvContent: TextView = itemView.findViewById(R.id.tv_content)
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
    }
}