package net.ienlab.study.adapter

import android.content.Context
import android.graphics.Typeface
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

class MainDataAdapter(var items: ArrayList<TimeData>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var context: Context
    lateinit var timeFormat: SimpleDateFormat
    lateinit var dateFormat: SimpleDateFormat

    lateinit var gmSansBold: Typeface
    lateinit var gmSansMedium: Typeface

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context

        return when (viewType) {
            TimeData.VIEWTYPE_DATE -> {
                val view = LayoutInflater.from(context).inflate(R.layout.adapter_main_date, parent, false)
                DateItemViewHolder(view)
            }
            TimeData.VIEWTYPE_NOTI -> {
                val view = LayoutInflater.from(context).inflate(R.layout.adapter_main_data, parent, false)
                ItemViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(context).inflate(R.layout.adapter_main_data, parent, false)
                ItemViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        timeFormat = SimpleDateFormat("a h:mm", Locale.getDefault())
        dateFormat = SimpleDateFormat(context.getString(R.string.date_format), Locale.getDefault())

        gmSansMedium = Typeface.createFromAsset(context.assets, "fonts/gmsans_medium.otf")
        gmSansBold = Typeface.createFromAsset(context.assets, "fonts/gmsans_bold.otf")

        when (holder) {
            is DateItemViewHolder -> {
                holder.tvDate.typeface = gmSansBold
                holder.tvDate.text = dateFormat.format(Calendar.getInstance().apply { timeInMillis = items[position].dateTime }.time)
            }
            is ItemViewHolder -> {
                holder.tvTitle.typeface = gmSansBold
                holder.tvContent.typeface = gmSansMedium
                holder.tvTime.typeface = gmSansMedium

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
        }
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].viewType
    }

    fun addItem(item: TimeData, dbHelper: DBHelper) {
        if (item.viewType == TimeData.VIEWTYPE_NOTI) {
            dbHelper.addItem(item)
        }
        items.add(1, item)
        notifyItemInserted(1)
    }

    inner class ItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        val tvContent: TextView = itemView.findViewById(R.id.tv_content)
        val tvTime: TextView = itemView.findViewById(R.id.tv_time)
    }

    inner class DateItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tv_date)
    }
}