package com.example.mob_dev_portfolio.ui.analytics

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.mob_dev_portfolio.R
import com.example.mob_dev_portfolio.databinding.ItemComparisonBinding

data class ComparisonItem(
    val categoryName: String,
    val colour: String,
    val currentAmount: Double,
    val previousAmount: Double,
    val maxAmount: Double
)

class ComparisonAdapter : RecyclerView.Adapter<ComparisonAdapter.ComparisonViewHolder>() {

    private var items: List<ComparisonItem> = emptyList()

    fun submitList(newItems: List<ComparisonItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComparisonViewHolder {
        val binding = ItemComparisonBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ComparisonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ComparisonViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ComparisonViewHolder(
        private val binding: ItemComparisonBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ComparisonItem) {
            binding.textCategoryName.text = item.categoryName

            val barColour = try {
                item.colour.toColorInt()
            } catch (e: Exception) {
                ("#7A849A").toColorInt()
            }

            binding.compBarFill.background.setTint(barColour)

            val percentage = if (item.maxAmount > 0) {
                (item.currentAmount / item.maxAmount).toFloat().coerceIn(0f, 1f)
            } else {
                0f
            }

            binding.compBarTrack.post {
                val trackWidth = binding.compBarTrack.width
                val barWidth = (trackWidth * percentage).toInt()

                if (barWidth <= 0) {
                    binding.compBarFill.visibility = android.view.View.INVISIBLE
                } else {
                    binding.compBarFill.visibility = android.view.View.VISIBLE
                    val params = binding.compBarFill.layoutParams
                    params.width = barWidth
                    binding.compBarFill.layoutParams = params
                }
            }

            val change = if (item.previousAmount > 0) {
                ((item.currentAmount - item.previousAmount) / item.previousAmount * 100).toInt()
            } else {
                if (item.currentAmount > 0) 100 else 0
            }

            val context = binding.root.context

            when {
                change > 0 -> {
                    binding.textChange.text = "+${change}%"
                    binding.textChange.setTextColor(context.resources.getColor(R.color.danger, null))
                }
                change < 0 -> {
                    binding.textChange.text = "${change}%"
                    binding.textChange.setTextColor(context.resources.getColor(R.color.green_accent, null))
                }
                else -> {
                    binding.textChange.text = "0%"
                    binding.textChange.setTextColor(context.resources.getColor(R.color.text_muted, null))
                }
            }
        }
    }

}