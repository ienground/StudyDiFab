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
import net.ienlab.study.database.DBHelper
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
                holder.tvContent.text = context.getString(R.string.msg_snooze)
            }

            else -> {
                holder.icon.setImageResource(R.drawable.ic_book)
                holder.tvTitle.text = context.getString(R.string.title_study)
                holder.tvContent.text = if (items[position].studyTime < 60) {
                    context.getString(R.string.msg_study_second, items[position].studyTime)
                } else {
                    context.getString(R.string.msg_study_minute_second, items[position].studyTime / 60, items[position].studyTime % 60)
                }
            }
        }
    }

    fun addItem(item: TimeData, dbHelper: DBHelper) {
        dbHelper.addItem(item)
        items.add(0, item)
//        items.sortWith(compareBy({ it.hourOfDay }, { it.minute }))
        notifyItemInserted(0)
    }

    inner class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon);
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvContent: TextView = itemView.findViewById(R.id.tv_content)
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
    }
}