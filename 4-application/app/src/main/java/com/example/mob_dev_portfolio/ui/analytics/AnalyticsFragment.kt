package com.example.mob_dev_portfolio.ui.analytics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mob_dev_portfolio.R
import com.example.mob_dev_portfolio.data.db.dao.CategorySpending
import com.example.mob_dev_portfolio.data.db.entity.Budget
import com.example.mob_dev_portfolio.data.db.entity.Category
import com.example.mob_dev_portfolio.data.db.entity.Expense
import com.example.mob_dev_portfolio.databinding.FragmentAnalyticsBinding
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.Locale

class AnalyticsFragment : Fragment() {

    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AnalyticsViewModel by viewModels()

    private lateinit var legendAdapter: LegendAdapter
    private lateinit var comparisonAdapter: ComparisonAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupChartDefaults()
        observeViewModel()
    }

    private fun setupRecyclerViews() {
        legendAdapter = LegendAdapter()
        binding.recyclerLegend.apply {
            layoutManager = LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false
            )
            adapter = legendAdapter
        }

        comparisonAdapter = ComparisonAdapter()
        binding.recyclerComparison.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = comparisonAdapter
        }
    }

    private fun setupChartDefaults() {
        val cardColour = resources.getColor(R.color.card, null)
        val mutedColour = resources.getColor(R.color.text_muted, null)
        val surfaceColour = resources.getColor(R.color.surface, null)
        val textColour = resources.getColor(R.color.text_primary, null)

        binding.pieChart.apply {
            setUsePercentValues(false)
            description.isEnabled = false
            legend.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 55f
            transparentCircleRadius = 60f
            setHoleColor(cardColour)
            setTransparentCircleColor(cardColour)
            setDrawEntryLabels(false)
            setTouchEnabled(true)
            isRotationEnabled = true
            setDrawCenterText(true)
            setCenterTextColor(textColour)
            setCenterTextSize(16f)
        }

        binding.barChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(true)
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setFitBars(true)

            xAxis.apply {
                position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                textColor = mutedColour
                textSize = 10f
                granularity = 1f
            }

            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = surfaceColour
                textColor = mutedColour
                textSize = 10f
                axisMinimum = 0f
            }

            axisRight.isEnabled = false
        }
    }

    private fun observeViewModel() {
        var currentCategories: List<Category> = emptyList()
        var currentSpending: List<CategorySpending> = emptyList()
        var currentBudget: Budget? = null
        var currentTotalSpent: Double? = null

        fun refreshUI() {
            val budget = currentBudget
            val spending = currentSpending
            val categories = currentCategories
            val totalSpent = currentTotalSpent

            if (budget == null || spending.isEmpty()) {
                showEmptyState()
                return
            }

            hideEmptyState()

            if (totalSpent != null && totalSpent > 0) {
                updatePieChart(spending, categories, totalSpent)
                viewModel.loadComparisonData(spending, categories)

                val insight = viewModel.generateInsight( totalSpent, budget, spending, categories)
                if (insight != null) {
                    binding.textInsightTip.text = insight
                    binding.textInsightTip.visibility = View.VISIBLE
                } else {
                    binding.textInsightTip.visibility = View.GONE
                }
            } else {
                showEmptyState()
            }
        }

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            currentCategories = categories
            refreshUI()
        }

        viewModel.activeBudget.observe(viewLifecycleOwner) { budget ->
            currentBudget = budget
            refreshUI()
        }

        viewModel.totalSpent.observe(viewLifecycleOwner) { spent ->
            currentTotalSpent = spent
            refreshUI()
        }

        viewModel.categorySpending.observe(viewLifecycleOwner) { spending ->
            currentSpending = spending
            refreshUI()
        }

        viewModel.expenses.observe(viewLifecycleOwner) { expenses ->
            if (expenses.isNotEmpty()) {
                updateBarChart(expenses)
                binding.cardDailySpending.visibility = View.VISIBLE
            } else {
                binding.cardDailySpending.visibility = View.GONE
            }
        }

        viewModel.comparisonData.observe(viewLifecycleOwner) { items ->
            if (items.isNotEmpty()) {
                comparisonAdapter.submitList(items)
                binding.cardComparison.visibility = View.VISIBLE
            } else {
                binding.cardComparison.visibility = View.GONE
            }
        }

        viewModel.hasPreviousPeriod.observe(viewLifecycleOwner) { hasPrevious ->
            if (!hasPrevious) {
                binding.cardComparison.visibility = View.GONE
            }
        }
    }

    private fun updatePieChart(
        spending: List<CategorySpending>,
        categories: List<Category>,
        totalSpent: Double
    ) {
        val categoryMap = categories.associateBy { it.id }
        val entries = mutableListOf<PieEntry>()
        val colours = mutableListOf<Int>()
        val legendItems = mutableListOf<LegendItem>()

        spending.sortedByDescending { it.total }.forEach { cs ->
            val category = cs.categoryId?.let { categoryMap[it] }
            val name = category?.name ?: "Other"
            val colour = try {
                (category?.colour ?: "#7A849A").toColorInt()
            } catch (e: Exception) {
                "7A849A".toColorInt()
            }

            entries.add(PieEntry(cs.total.toFloat(), name))
            colours.add(colour)

            val percentage = if (totalSpent > 0) {
                (cs.total / totalSpent * 100).toInt()
            } else {
                0
            }
            legendItems.add(LegendItem(name, category?.colour ?: "#7A849A", percentage))
        }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colours
            setDrawValues(false)
            sliceSpace = 2f
        }

        binding.pieChart.apply {
            data = PieData(dataSet)
            centerText = String.format(Locale.UK, "£%.0f\nspent", totalSpent)
            invalidate()
        }

        binding.cardPieChart.visibility = View.VISIBLE
        legendAdapter.submitList(legendItems)
    }

    private fun updateBarChart(expenses: List<Expense>) {
        val dayFormat = SimpleDateFormat("EEE", Locale.UK)
        val dayDateFormat = SimpleDateFormat("yyyyMMdd", Locale.UK)

        val dailyTotals = expenses
            .groupBy { dayDateFormat.format(it.date) }
            .mapValues { (_, exps) -> exps.sumOf { it.amount} }
            .toSortedMap()
            .toList()

        val recentDays = dailyTotals.takeLast(7)

        if (recentDays.isEmpty()) return

        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()

        recentDays.forEachIndexed { index, pair ->
            val dateKey = pair.first
            val total = pair.second

            entries.add(BarEntry(index.toFloat(), total.toFloat()))

            try {
                val date = dayDateFormat.parse(dateKey)
                labels.add(if (date != null) dayFormat.format(date) else "")
            } catch (e: Exception) {
                labels.add("")
            }
        }

        val dataSet = BarDataSet(entries, "").apply {
            color = resources.getColor(R.color.blue_accent, null)
            setDrawValues(false)
        }

        binding.barChart.apply {
            data = BarData(dataSet).apply {
                barWidth = 0.5f
            }
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.labelCount = labels.size
            invalidate()
        }
    }

    private fun showEmptyState() {
        binding.layoutEmpty.visibility = View.VISIBLE
        binding.cardPieChart.visibility = View.GONE
        binding.cardDailySpending.visibility = View.GONE
        binding.cardComparison.visibility = View.GONE
        binding.textInsightTip.visibility = View.GONE
    }

    private fun hideEmptyState() {
        binding.layoutEmpty.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}