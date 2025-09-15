package com.example.camerax.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.camerax.R
import com.example.camerax.databinding.LayoutFilterBinding
import com.otaliastudios.cameraview.filter.Filters

class FilterAdapter(
    private val items: Array<Filters>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<FilterAdapter.FilterViewHolder>() {

    private var selectedPosition = 0 // mặc định chọn item đầu tiên

    inner class FilterViewHolder(val binding: LayoutFilterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Filters, position: Int) {
            binding.tvName.text = item.name

            // đổi màu text dựa vào trạng thái selected
            val colorRes = if (position == selectedPosition) {
                R.color.black
            } else {
                R.color.white
            }
            binding.tvName.setTextColor(
                ContextCompat.getColor(binding.root.context, colorRes)
            )

            binding.root.setOnClickListener {
                val oldPos = selectedPosition
                selectedPosition = position
                notifyItemChanged(oldPos)    // cập nhật lại màu cho item cũ
                notifyItemChanged(position)  // cập nhật lại màu cho item mới

                onClick(position) // trả ra index filter đã chọn
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val binding = LayoutFilterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FilterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size
}
