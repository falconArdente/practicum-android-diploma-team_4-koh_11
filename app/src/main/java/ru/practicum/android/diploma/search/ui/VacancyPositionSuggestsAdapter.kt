package ru.practicum.android.diploma.search.ui

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import ru.practicum.android.diploma.R
import ru.practicum.android.diploma.utils.toPx

class VacancyPositionSuggestsAdapter(
    private val context: Activity, private val hostTextView: AutoCompleteTextView
) : BaseAdapter(), Filterable {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var suggestionsList: MutableList<String> = mutableListOf()
    fun add(item: String) {
        suggestionsList.add(item)
        adjustDropDownSize()
        this.notifyDataSetChanged()
    }

    private fun adjustDropDownSize() {
        hostTextView.dropDownHeight = context.toPx(36).toInt()*suggestionsList.size
    }

    fun applyDataSet(items: Collection<String>) {
        suggestionsList.clear()
        suggestionsList.addAll(items)
        adjustDropDownSize()
        this.notifyDataSetChanged()
    }


    override fun getCount(): Int {
        return suggestionsList.count()
    }

    override fun getItem(position: Int): Any {
        return suggestionsList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        if (convertView != null) {
            (convertView as TextView).text = suggestionsList[position]
            return convertView
        }
        val newView = inflater.inflate(R.layout.suggestion_view_holder, null)
        (newView as TextView).text = suggestionsList[position]
        return newView
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint != null) {
                    filterResults.values = suggestionsList.toList()
                    filterResults.count = suggestionsList.size
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                if (results != null && results.count > 0) {
                    suggestionsList.clear()
                    suggestionsList.addAll(results.values as List<String>)
                } else {
                    notifyDataSetInvalidated()
                }
            }
        }
    }
}
