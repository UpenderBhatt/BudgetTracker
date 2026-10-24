package com.example.mob_dev_portfolio.ui.analytics

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mob_dev_portfolio.databinding.ItemLegendBinding
import androidx.core.graphics.toColorInt

data class LegendItem(
    val name: String,
    val colour: String,
    val percentage: Int
)

class LegendAdapter : RecyclerView.Adapter<LegendAdapter.LegendViewHolder>() {

    private var items: List<LegendItem> = emptyList()

    fun submitList(newItems: List<LegendItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LegendViewHolder {
        val binding = ItemLegendBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return LegendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LegendViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class LegendViewHolder(
        private val binding: ItemLegendBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LegendItem) {
            val colour = try {
                item.colour.toColorInt()
            } catch (e: Exception) {
                "#7A849A".toColorInt()
            }

            val dotDrawable = binding.viewLegendDot.background as? GradientDrawable
            dotDrawable?.setColor(colour)

            binding.textLegendLabel.text = "${item.name} ${item.percentage}%"
        }
    }
}