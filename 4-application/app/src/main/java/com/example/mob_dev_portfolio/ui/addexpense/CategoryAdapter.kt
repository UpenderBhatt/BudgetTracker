package com.example.mob_dev_portfolio.ui.addexpense

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mob_dev_portfolio.R
import com.example.mob_dev_portfolio.data.db.entity.Category
import com.example.mob_dev_portfolio.databinding.ItemCategoryChipBinding

class CategoryAdapter(
    private val onCategorySelected: (Category) -> Unit
    ) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {


        private var selectedCategoryId: Long? = null

        fun setSelectedCategory(categoryId: Long?) {
            val previousId = selectedCategoryId
            selectedCategoryId = categoryId

            currentList.forEachIndexed { index, category ->
                if (category.id == previousId || category.id == categoryId) {
                    notifyItemChanged(index)
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryChipBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(
        private val binding: ItemCategoryChipBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.chipCategory.text = category.name

            val isSelected = category.id == selectedCategoryId

            if(isSelected) {
                binding.chipCategory.setBackgroundResource(R.drawable.bg_chip_selected)
                binding.chipCategory.setTextColor(Color.parseColor(category.colour))
            } else {
                binding.chipCategory.setBackgroundResource(R.drawable.bg_toggle_unselected)
                binding.chipCategory.setTextColor(Color.parseColor(category.colour))
            }

            binding.chipCategory.setOnClickListener {
                onCategorySelected(category)
            }
        }
    }

    private class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem:Category): Boolean {
            return oldItem == newItem
        }
    }
}









