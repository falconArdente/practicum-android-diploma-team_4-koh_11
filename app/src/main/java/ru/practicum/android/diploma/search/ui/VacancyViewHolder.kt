package ru.practicum.android.diploma.search.ui

import android.util.TypedValue
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.databinding.JobListItemBinding
import ru.practicum.android.diploma.search.domain.model.Vacancy
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

private const val CORNER = 12f

class VacancyViewHolder(private val binding: JobListItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(vacancy: Vacancy) {
        binding.jobTitle.text = jobTitleText(vacancy)
        binding.jobEmployer.text = vacancy.employer?.name ?: "ЗАМЕЩАЮЩИЙ ТЕКСТ?"
        binding.jobSalary.text = jobSalaryText(vacancy)
        Glide.with(itemView)
            .load(vacancy.employer?.logoUrls?.size90)
            .placeholder(R.drawable.placeholder_logo)
            .centerCrop()
            .transform(RoundedCorners(dpToPx(itemView, CORNER)))
            .into(binding.jobImage)

    }

    private fun jobTitleText(vacancy: Vacancy): String {
        var allText = "${vacancy.name}, ${vacancy.area}"
        if (vacancy.area == null) {
            allText = vacancy.name
        }
        return allText
    }

    private fun jobSalaryText(vacancy: Vacancy): String {
        var text = when {
            vacancy.salary == null -> binding.root.resources.getString(R.string.no_salary_msg)
            vacancy.salary.from == null && vacancy.salary.to != null -> {
                binding.root.resources.getString(
                    R.string.salary_to,
                    formatSalaryAmount(vacancy.salary.to),
                    convertCurrencyToSymbol(vacancy)
                )
            }

            vacancy.salary.to == null -> {
                binding.root.resources.getString(
                    R.string.salary_from,
                    formatSalaryAmount(vacancy.salary.from),
                    convertCurrencyToSymbol(vacancy)
                )
            }

            else -> {
                binding.root.resources.getString(
                    R.string.salary_range,
                    formatSalaryAmount(vacancy.salary.from),
                    formatSalaryAmount(vacancy.salary.to),
                    convertCurrencyToSymbol(vacancy)
                )
            }
        }
        return text
    }

    private fun formatSalaryAmount(salaryAmount: Int?): String {
        val delimiterSymbol = DecimalFormatSymbols().apply { groupingSeparator = ' ' }
        val numberFormat = DecimalFormat("###,###,###,###,###", delimiterSymbol)
        return numberFormat.format(salaryAmount).toString()
    }

    private fun convertCurrencyToSymbol(vacancy: Vacancy): String {
        var currencyCode = vacancy.salary?.currency ?: "RUR"
        if (currencyCode == "RUR") currencyCode = "RUB"
        val currency = java.util.Currency.getInstance(currencyCode)
        return currency.symbol
    }

    private fun dpToPx(view: View, dp: Float): Int {
        val displayMetrics = view.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics)
            .toInt()
    }
}
