package com.example.mob_dev_portfolio.ui.expenses

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mob_dev_portfolio.R
import com.example.mob_dev_portfolio.databinding.FragmentExpensesBinding
import com.google.android.material.snackbar.Snackbar
import java.util.Locale
import androidx.core.graphics.toColorInt

class ExpensesFragment : Fragment() {

    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExpensesViewModel by viewModels()

    private lateinit var expenseAdapter: ExpenseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeToDelete()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        expenseAdapter = ExpenseAdapter { expense->

        }

        binding.recyclerExpenses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = expenseAdapter
        }
    }

    private fun setupSwipeToDelete() {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if(position != RecyclerView.NO_POSITION) {
                    val expense = expenseAdapter.currentList[position]
                    viewModel.deleteExpense(expense)
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
                ) {
                val itemView = viewHolder.itemView

                val paint = Paint()
                paint.color = "#F05B6E".toColorInt()
                val background = RectF(
                    itemView.right.toFloat() + dX,
                    itemView.top.toFloat(),
                    itemView.right.toFloat(),
                    itemView.bottom.toFloat()
                )
                c.drawRoundRect(background, 16f, 16f, paint)


                val textPaint = Paint()
                textPaint.color  = Color.WHITE
                textPaint.textSize = 36f
                textPaint.textAlign = Paint.Align.CENTER
                val textX = itemView.right.toFloat() - 120f
                val textY = itemView.top.toFloat() +
                        (itemView.height / 2f) +
                        (textPaint.textSize / 3f)
                if (dX < -200f) {
                    c.drawText("Delete", textX, textY, textPaint)
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.recyclerExpenses)
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            expenseAdapter.setCategoryMap(categories)
        }

        viewModel.expenses.observe(viewLifecycleOwner) { expenses ->
            expenseAdapter.submitList(expenses)

            if (expenses.isNullOrEmpty()) {
                binding.recyclerExpenses.visibility = View.GONE
                binding.layoutEmpty.root.visibility = View.VISIBLE
            } else {
                binding.recyclerExpenses.visibility = View.VISIBLE
                binding.layoutEmpty.root.visibility = View.GONE
            }
        }

        viewModel.totalSpent.observe(viewLifecycleOwner) { total ->
            val displayTotal = total ?: 0.0
            binding.textTotalSpent.text = String.format(
                Locale.UK, "£%.2f", displayTotal
            )
        }

        viewModel.expenseDeleted.observe(viewLifecycleOwner) { expense ->
            if (expense != null) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.expense_deleted),
                    Snackbar.LENGTH_LONG
                ).setAction(getString(R.string.undo_action)) {
                    viewModel.undoDelete(expense)
                }.setBackgroundTint(resources.getColor(R.color.card, null))
                    .setTextColor(resources.getColor(R.color.text_primary, null))
                    .setActionTextColor(resources.getColor(R.color.green_accent, null))
                    .show()

                viewModel.onDeleteHandled()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}