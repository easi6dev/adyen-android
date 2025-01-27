/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/12/2020.
 */

package com.adyen.checkout.mbway.country

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import com.adyen.checkout.mbway.databinding.CountryViewBinding

// We need context to inflate the views and localizedContext to fetch the strings
// Do not remove localizedContext! It's not used at the moment, however it is kept to prevent usage of context
// in case of the need to read strings.
class CountryAdapter(private val context: Context, private val localizedContext: Context) : BaseAdapter(), Filterable {

    private val countries: MutableList<CountryModel> = mutableListOf()
    private val countryFilter: CountryFilter = CountryFilter(countries)

    fun setItems(countries: List<CountryModel>) {
        this.countries.clear()
        this.countries.addAll(countries)
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: CountryViewHolder
        val binding: CountryViewBinding
        if (convertView == null) {
            binding = CountryViewBinding.inflate(LayoutInflater.from(context), parent, false)
            view = binding.root
            viewHolder = CountryViewHolder(binding)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as CountryViewHolder
        }
        viewHolder.bindItem(getItem(position))
        return view
    }

    override fun getCount() = countries.size

    override fun getItem(position: Int): CountryModel = countries[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getFilter(): Filter {
        return countryFilter
    }
}

class CountryFilter(private val countries: List<CountryModel>) : Filter() {
    override fun performFiltering(constraint: CharSequence?): FilterResults {
        return FilterResults().apply {
            values = countries
            count = countries.size
        }
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        // do nothing
    }

    override fun convertResultToString(resultValue: Any?): CharSequence {
        val country = resultValue as? CountryModel
        return country?.toShortString() ?: ""
    }
}
