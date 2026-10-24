package com.example.mob_dev_portfolio.ui.dashboard

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mob_dev_portfolio.databinding.ItemCategoryPillBinding
import java.util.Locale
import androidx.core.graphics.toColorInt

data class CategoryPillData(
    val categoryId: Long,
    val name: String,
    val colour: String,
    val amount: Double
)

class CategoryPillAdapter : ListAdapter<CategoryPillData, CategoryPillAdapter.PillViewHolder>(
    PillDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PillViewHolder {
        val binding = ItemCategoryPillBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PillViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PillViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PillViewHolder(
        private val binding: ItemCategoryPillBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pill: CategoryPillData) {
            val pillColour = try {
                pill.colour.toColorInt()
            } catch (e:Exception) {
                "#7A849A".toColorInt()
            }

            val dotDrawable = binding.viewPillDot.background as? GradientDrawable
            dotDrawable?.setColor(pillColour)

            binding.textPillLabel.text = String.format(
                Locale.UK, "%s £%.0f", pill.name, pill.amount
            )
            binding.textPillLabel.setTextColor(pillColour)
        }
    }

    private class PillDiffCallback : DiffUtil.ItemCallback<CategoryPillData>() {
        override fun areItemsTheSame(old: CategoryPillData, new: CategoryPillData): Boolean {
            return old.categoryId == new.categoryId
        }

        override fun areContentsTheSame(old: CategoryPillData, new: CategoryPillData): Boolean {
            return old == new
        }
    }
}