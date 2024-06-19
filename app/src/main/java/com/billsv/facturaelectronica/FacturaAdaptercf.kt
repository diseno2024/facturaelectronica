package com.billsv.facturaelectronica

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class FacturaAdaptercf(
    private val context: Context,
    private var facturaList: MutableList<Factura>
) : RecyclerView.Adapter<FacturaAdaptercf.FacturaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacturaViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.listar_facturascf, parent, false)
        return FacturaViewHolder(view)
    }

    override fun onBindViewHolder(holder: FacturaViewHolder, position: Int) {
        val factura = facturaList[position]
        holder.textViewNombre.text = factura.nombre
        holder.textViewTelefono.text = factura.telefono
        holder.textViewDui.text = "DUI: ${factura.dui}"
    }

    override fun getItemCount(): Int {
        return facturaList.size
    }

    fun setFacturas(nuevasFacturas: List<Factura>) {
        facturaList.clear()
        facturaList.addAll(nuevasFacturas)
        notifyDataSetChanged()
    }

    class FacturaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewNombre: TextView = itemView.findViewById(R.id.textViewNombre)
        val textViewTelefono: TextView = itemView.findViewById(R.id.textViewTelefono)
        val textViewDui: TextView = itemView.findViewById(R.id.textViewDui)
    }
}

