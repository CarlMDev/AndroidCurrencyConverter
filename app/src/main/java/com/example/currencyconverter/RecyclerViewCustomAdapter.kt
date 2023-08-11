package com.example.currencyconverter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewCustomAdapter (private val mList : List<ItemsViewModel>)
    :RecyclerView.Adapter<RecyclerViewCustomAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //inflating the card view layout, it'll hold the list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_design, parent, false)
        return ViewHolder(view)
    }

    //binds list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemsViewModel = mList[position]

        holder.baseCurrencyCardLayout.text = itemsViewModel.baseCurrency
        holder.baseCurrencyAmountCardLayout.text = String.format("%.2f", itemsViewModel.baseCurrencyAmount)
        holder.targetCurrencyCardLayout.text = itemsViewModel.targetCurrency
        holder.targetCurrencyAmountCardLayout.text = String.format("%.2f", itemsViewModel.targetCurrencyAmount)
        holder.dateCardLayout.text = itemsViewModel.recordDate
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    // defines the views that will contain the data
    class ViewHolder(ItemView : View) : RecyclerView.ViewHolder(ItemView) {
        val baseCurrencyCardLayout : TextView = itemView.findViewById(R.id.tvBaseCurrencyCardLayout)
        val baseCurrencyAmountCardLayout : TextView  = itemView.findViewById(R.id.tvBaseCurrencyAmountCardLayout)
        val targetCurrencyCardLayout : TextView = itemView.findViewById(R.id.tvTargetCurrencyCardLaout)
        val targetCurrencyAmountCardLayout : TextView = itemView.findViewById(R.id.tvTargetCurrencyAmountCardLayout)
        val dateCardLayout : TextView = itemView.findViewById(R.id.tvDateCardLayout)
    }

}