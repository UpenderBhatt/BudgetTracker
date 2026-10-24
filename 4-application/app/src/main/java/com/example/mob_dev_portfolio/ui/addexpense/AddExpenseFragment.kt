package com.example.mob_dev_portfolio.ui.addexpense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mob_dev_portfolio.R
import com.example.mob_dev_portfolio.databinding.FragmentAddExpenseBinding
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class AddExpenseFragment : Fragment() {

    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddExpenseViewModel by viewModels()

    private lateinit var categoryAdapter: CategoryAdapter

    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.UK)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategoryRecyclerView()
        setupDatePicker()
        setupRecurringToggle()
        setupSaveButton()
        observeViewModel()
    }

    private fun setupCategoryRecyclerView() {
        categoryAdapter = CategoryAdapter { category ->
            viewModel.selectCategory(category)
        }

        binding.recyclerCategories.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = categoryAdapter
        }
    }

    private fun setupDatePicker() {
        binding.textSelectedDate.text = dateFormatter.format(Date())

        binding.textSelectedDate.setOnClickListener{
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.date_label))
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.timeInMillis = selection
                val date = calendar.time

                viewModel.selectDate(date)
                binding.textSelectedDate.text = dateFormatter.format(date)
            }

            datePicker.show(parentFragmentManager, "expenseDatePicker")
        }
    }

    private fun setupRecurringToggle() {
        binding.switchRecurring.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setRecurring(isChecked)
        }

        viewModel.isRecurring.observe(viewLifecycleOwner) { isRecurring ->
            binding.frequencyGroup.visibility = if (isRecurring) View.VISIBLE else View.GONE
        }

        binding.btnFreqDaily.setOnClickListener{
            viewModel.setRecurringFrequency("DAILY")
        }

        binding.btnFreqWeekly.setOnClickListener{
            viewModel.setRecurringFrequency("WEEKLY")
        }

        binding.btnFreqMonthly.setOnClickListener{
            viewModel.setRecurringFrequency("MONTHLY")
        }

        viewModel.recurringFrequency.observe(viewLifecycleOwner) { frequency ->

            binding.btnFreqDaily.setBackgroundResource(
                if (frequency == "DAILY") R.drawable.bg_toggle_selected
                else R.drawable.bg_toggle_unselected
            )
            binding.btnFreqDaily.setTextColor(
                resources.getColor(
                    if (frequency == "DAILY") R.color.background else R.color.text_muted, null
                )
            )

            binding.btnFreqWeekly.setBackgroundResource(
                if(frequency == "WEEKLY") R.drawable.bg_toggle_selected else R.drawable.bg_toggle_unselected
            )
            binding.btnFreqWeekly.setTextColor(
                resources.getColor(
                    if (frequency == "WEEKLY") R.color.background else R.color.text_muted, null
                )
            )

            binding.btnFreqMonthly.setBackgroundResource(
                if(frequency == "MONTHLY") R.drawable.bg_toggle_selected else R.drawable.bg_toggle_unselected
            )
            binding.btnFreqMonthly.setTextColor(
                resources.getColor(
                    if (frequency == "MONTHLY") R.color.background else R.color.text_muted, null
                )
            )
        }
    }

    private fun setupSaveButton() {
        binding.btnSaveExpense.setOnClickListener {
            val amount = binding.editAmount.text.toString().trim()
            val description = binding.editDescription.text.toString().trim()
            viewModel.saveExpense(amount, description)
        }
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.submitList(categories)
        }

        viewModel.selectedCategory.observe(viewLifecycleOwner) { category ->
            categoryAdapter.setSelectedCategory(category?.id)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.textError.text = error
                binding.textError.visibility = View.VISIBLE
            } else {
                binding.textError.visibility = View.GONE
            }
        }


        viewModel.expenseSaved.observe(viewLifecycleOwner) { saved ->
            if (saved) {
                Toast.makeText(requireContext(), R.string.expense_saved, Toast.LENGTH_SHORT).show()

                binding.editAmount.text?.clear()
                binding.editDescription.text?.clear()
                binding.textSelectedDate.text = dateFormatter.format(Date())
                binding.switchRecurring.isChecked = false

                viewModel.resetForm()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}