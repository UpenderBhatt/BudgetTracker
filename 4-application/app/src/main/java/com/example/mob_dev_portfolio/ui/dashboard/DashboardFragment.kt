package com.example.mob_dev_portfolio.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mob_dev_portfolio.R
import com.example.mob_dev_portfolio.data.db.dao.CategorySpending
import com.example.mob_dev_portfolio.data.db.entity.Budget
import com.example.mob_dev_portfolio.data.db.entity.Category
import com.example.mob_dev_portfolio.databinding.FragmentDashboardBinding
import com.example.mob_dev_portfolio.ui.expenses.ExpenseAdapter
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()

    private lateinit var pillAdapter: CategoryPillAdapter
    private lateinit var recentExpenseAdapter: ExpenseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        showNoBudgetState()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        pillAdapter = CategoryPillAdapter()
        binding.recyclerCategoryPills.apply {
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false
            )
            adapter = pillAdapter
        }

        recentExpenseAdapter = ExpenseAdapter { expense ->

        }

        binding.recyclerRecentExpenses.apply{
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recentExpenseAdapter
        }
    }

    private fun observeViewModel() {
        var currentBudget: Budget? = null
        var currentSpent: Double? = null
        var currentCategories: List<Category> = emptyList()
        var currentCategorySpending: List<CategorySpending> = emptyList()

        viewModel.activeBudget.observe(viewLifecycleOwner) { budget ->
            currentBudget = budget

            if (budget == null) {
                showNoBudgetState()
            } else {
                showBudgetState(budget)
            }

            updateTip(currentSpent ?: 0.0, budget, currentCategorySpending, currentCategories)
        }

        viewModel.totalSpent.observe(viewLifecycleOwner) { spent ->
            currentSpent = spent
            val budget = currentBudget ?: return@observe
            updateBudgetCard(budget, spent ?: 0.0)
            updateTip(spent ?: 0.0, budget, currentCategorySpending, currentCategories)
        }

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            currentCategories = categories
            recentExpenseAdapter.setCategoryMap(categories)
        }

        viewModel.categorySpending.observe(viewLifecycleOwner) { spending ->
            currentCategorySpending = spending
            updateCategoryPills(spending, currentCategories)
            updateTip(
                currentSpent ?: 0.0,
                currentBudget,
                spending,
                currentCategories
            )
        }

        viewModel.recentExpenses.observe(viewLifecycleOwner) { expenses ->
            recentExpenseAdapter.submitList(expenses)

            if (expenses.isNullOrEmpty()) {
                binding.labelRecentExpenses.visibility = View.GONE
                binding.recyclerRecentExpenses.visibility = View.GONE
            } else {
                binding.labelRecentExpenses.visibility = View.VISIBLE
                binding.recyclerRecentExpenses.visibility = View.VISIBLE
            }
        }
    }

    private fun showNoBudgetState() {
        binding.layoutNoBudget.visibility = View.VISIBLE
        binding.cardBudget.visibility = View.GONE
        binding.recyclerCategoryPills.visibility = View.GONE
        binding.textTip.visibility = View.GONE
    }

    private fun showBudgetState(budget: Budget) {
        binding.layoutNoBudget.visibility = View.GONE
        binding.cardBudget.visibility = View.VISIBLE

        binding.textBudgetAmount.text = String.format(
            Locale.UK, "£%.2f", budget.amount
        )

        val periodDisplay = budget.periodType.lowercase()
            .replaceFirstChar { it.uppercase() }
        binding.textPeriodLabel.text = "$periodDisplay budget"

        val daysLeft = viewModel.getDaysRemianing(budget)
        binding.textDaysLeft.text = getString(R.string.days_left_label, daysLeft)
    }

    private fun updateBudgetCard(budget: Budget, spent: Double) {
        val remaining = budget.amount - spent
        val percentage = viewModel.getSpendingPercentatge(spent, budget)

        binding.textRemaining.text = String.format(Locale.UK, "£%.2f", maxOf(0.0, remaining))

        val remainingColour = when {
            remaining <= 0 -> resources.getColor(R.color.danger, null)
            percentage >= 90 -> resources.getColor(R.color.rent_category, null)
            else -> resources.getColor(R.color.green_accent, null)
        }
        binding.textRemaining.setTextColor(remainingColour)

        if (remaining <= 0) {
           binding.labelRemaining.text = getString(R.string.over_budget_label)
           binding.labelRemaining.setTextColor(resources.getColor(R.color.danger, null))
        } else {
            binding.labelRemaining.text = getString(R.string.remaining_label)
            binding.labelRemaining.setTextColor(resources.getColor(R.color.text_muted, null))
        }

        binding.textSpent.text = String.format(Locale.UK, "£%.2f spent", spent)

        updateProgressBar(percentage)

        val progressDrawable = when {
            percentage >= 90 -> R.drawable.bg_progress_danger
            percentage >= 70 -> R.drawable.bg_progress_warning
            else -> R.drawable.bg_progress_green
        }
        binding.progressFill.setBackgroundResource(progressDrawable)
    }

    private fun updateProgressBar(percentage: Float) {
        val constraintLayout = binding.cardBudget
        val constraintSet = ConstraintSet()
        constraintSet.clone(constraintLayout)

        constraintSet.constrainPercentWidth(
            R.id.progressFill,
            (percentage / 100f).coerceIn(0f, 1f)
        )

        constraintSet.applyTo(constraintLayout)
    }

    private fun updateCategoryPills(
        spending: List<CategorySpending>,
        categories: List<Category>
    ) {
        if (spending.isEmpty()) {
            binding.recyclerCategoryPills.visibility = View.GONE
            return
        }

        val categoryMap = categories.associateBy { it.id }
        val pills = spending.mapNotNull {
            cs ->
            val category = cs.categoryId?.let { categoryMap[it] }
            if (category != null) {
                CategoryPillData(
                    categoryId = category.id,
                    name = category.name,
                    colour = category.colour,
                    amount = cs.total
                )
            } else {
                null
            }
        }.sortedByDescending { it.amount }

        pillAdapter.submitList(pills)
        binding.recyclerCategoryPills.visibility = View.VISIBLE
    }

    private fun updateTip(
        spent: Double,
        budget: Budget?,
        categorySpending: List<CategorySpending>,
        categories: List<Category>
    ) {
        if (budget == null) {
            binding.textTip.visibility = View.GONE
            return
        }
        val tip = viewModel.generateTip(spent, budget, categorySpending, categories)
        if (tip != null) {
            binding.textTip.text = tip
            binding.textTip.visibility = View.VISIBLE
        } else {
            binding.textTip.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}