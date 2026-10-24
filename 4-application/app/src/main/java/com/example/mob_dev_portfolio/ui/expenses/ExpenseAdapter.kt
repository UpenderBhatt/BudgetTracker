package com.example.mob_dev_portfolio.ui.expenses

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mob_dev_portfolio.data.db.entity.Category
import com.example.mob_dev_portfolio.data.db.entity.Expense
import com.example.mob_dev_portfolio.databinding.ItemExpenseBinding
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.core.graphics.toColorInt

class ExpenseAdapter (
    private val onExpenseClick: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(ExpenseDiffCallback()) {

    private var categoryMap: Map<Long, Category> = emptyMap()

    private val dateFormatter = SimpleDateFormat("dd MMM", Locale.UK)

    fun setCategoryMap(categories: List<Category>) {
        categoryMap = categories.associateBy { it.id }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExpenseViewHolder(
        private val binding: ItemExpenseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: Expense) {
            val category = expense.categoryId?.let { categoryMap[it] }
            val displayName = expense.description.ifBlank {
                category?.name ?: "Expense"
            }
            binding.textDescription.text = displayName

            val categoryName = category?.name ?: "Uncategorised"
            val dateText = dateFormatter.format(expense.date)
            binding.textCategoryDate.text = "$categoryName - $dateText"

            val dotColour = try {
                (category?.colour ?: "#7A849A").toColorInt()
            } catch (e: Exception) {
                "#7A849A".toColorInt()
            }
            val dotDrawable = binding.viewCategoryDot.background as? GradientDrawable
            dotDrawable?.setColor(dotColour)

            binding.textAmount.text = String.format(Locale.UK, "-£%.2f", expense.amount)

            binding.tagRecurring.visibility = if (expense.isRecurring) {
                View.VISIBLE
            } else {
                View.GONE
            }

            binding.root.setOnClickListener {
                onExpenseClick(expense)
            }
        }
    }

    private class ExpenseDiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem == newItem
        }
    }

}