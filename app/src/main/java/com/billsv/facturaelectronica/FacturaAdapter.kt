package com.billsv.facturaelectronica

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class FacturaAdapter(
    private val context: Context,
    private val facturaList: List<Factura>
) : RecyclerView.Adapter<FacturaAdapter.FacturaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacturaViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.listar_facturas, parent, false)
        return FacturaViewHolder(view)
    }

    override fun onBindViewHolder(holder: FacturaViewHolder, position: Int) {
        val factura = facturaList[position]
        holder.textViewNombre.text = "${factura.nombre}"
        holder.textViewTelefono.text = "Teléfono: ${factura.telefono}"
        holder.textViewDui.text = "DUI: ${factura.dui}"
        // Puedes configurar otras propiedades aquí si es necesario
    }

    override fun getItemCount(): Int {
        return facturaList.size
    }

    class FacturaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewNombre: TextView = itemView.findViewById(R.id.textViewNombre)
        val textViewTelefono: TextView = itemView.findViewById(R.id.textViewTelefono)
        val textViewDui: TextView = itemView.findViewById(R.id.textViewDui)
        val iconoCard: ImageView = itemView.findViewById(R.id.iconoCard)
        val cardView: CardView = itemView.findViewById(R.id.card_factura)
    }
}

