package com.example.mob_dev_portfolio.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.mob_dev_portfolio.R
import com.example.mob_dev_portfolio.data.db.entity.Budget
import com.example.mob_dev_portfolio.databinding.FragmentSettingsBinding
import com.example.mob_dev_portfolio.ui.budget.BudgetViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetViewModel by viewModels()

    private val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.UK)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPeriodSelector()
        setupDatePicker()
        setupToggles()
        setupSaveButton()
        observeViewModel()
    }

    private fun setupPeriodSelector() {
        binding.btnWeekly.setOnClickListener {
            viewModel.selectPeriodType(Budget.PERIOD_WEEKLY)
        }

        binding.btnMonthly.setOnClickListener {
            viewModel.selectPeriodType(Budget.PERIOD_MONTHLY)
        }

        viewModel.selectedPeriodType.observe(viewLifecycleOwner) { periodType ->
            val isWeekly = periodType == Budget.PERIOD_WEEKLY

            binding.btnWeekly.setBackgroundResource(
                if(isWeekly) R.drawable.bg_toggle_selected else R.drawable.bg_toggle_unselected
            )
            binding.btnWeekly.setTextColor(
                resources.getColor(
                    if(isWeekly) R.color.background else R.color.text_muted,
                    null
                )
            )

            binding.btnMonthly.setBackgroundResource(
                if(!isWeekly) R.drawable.bg_toggle_selected else R.drawable.bg_toggle_unselected
            )
            binding.btnMonthly.setTextColor(
                resources.getColor(
                    if(!isWeekly) R.color.background else R.color.text_muted,
                    null
                )
            )
        }
    }


    private fun setupDatePicker() {
        binding.textSelectedDate.text = dateFormatter.format(Date())

        binding.textSelectedDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.start_date_label))
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val calendar = java.util.Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.timeInMillis = selection
                val date = calendar.time

                viewModel.selectStartDate(date)
                binding.textSelectedDate.text = dateFormatter.format(date)
            }

            datePicker.show(parentFragmentManager, "datePicker")
        }
    }


    private fun setupToggles() {
        viewModel.alertsEnabled.observe(viewLifecycleOwner) { enabled ->
            if (binding.switchAlerts.isChecked != enabled) {
                binding.switchAlerts.isChecked = enabled
            }
        }

        viewModel.autoRenewEnabled.observe(viewLifecycleOwner) { enabled ->
            if (binding.switchAutoRenew.isChecked != enabled) {
                binding.switchAutoRenew.isChecked = enabled
            }
        }

        binding.switchAlerts.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateAlertsEnabled(isChecked)
        }

        binding.switchAutoRenew.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateAutoRenew(isChecked)
        }
    }

    private fun setupSaveButton() {
        binding.btnSaveBudget.setOnClickListener {
            val amountText = binding.editAmount.text.toString().trim()
            viewModel.saveBudget(amountText)
        }
    }

    private fun observeViewModel() {
        viewModel.amountError.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.textAmountError.text = error
                binding.textAmountError.visibility = View.VISIBLE
            } else {
                binding.textAmountError.visibility = View.GONE
            }
        }

        viewModel.budgetSaved.observe(viewLifecycleOwner) { saved ->
            if(saved) {
                Toast.makeText(requireContext(), R.string.budget_saved, Toast.LENGTH_SHORT).show()
                binding.editAmount.text?.clear()
            }
        }

        viewModel.activeBudget.observe(viewLifecycleOwner) { budget ->
            if (budget != null) {
                binding.cardCurrentBudget.visibility = View.VISIBLE
                binding.textCurrentAmount.text = getString(
                    R.string.budget_amount_display,
                    String.format(Locale.UK, "%.2f", budget.amount)
                )
                binding.textCurrentPeriod.text = getString(
                    R.string.active_budget_period_label,
                    budget.periodType.lowercase()
                        .replaceFirstChar { it.uppercase() }
                )
                binding.textCurrentDates.text = getString(
                    R.string.budget_dates_label,
                    dateFormatter.format(budget.startDate),
                    dateFormatter.format(budget.endDate)
                )
            } else {
                binding.cardCurrentBudget.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}