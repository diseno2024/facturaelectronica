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
    private var facturaList: MutableList<Factura>,
    private val onItemClickListener: (Factura) -> Unit // Callback para manejar el clic
) : RecyclerView.Adapter<FacturaAdapter.FacturaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacturaViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.listar_facturas, parent, false)
        return FacturaViewHolder(view)
    }

    override fun onBindViewHolder(holder: FacturaViewHolder, position: Int) {
        val factura = facturaList[position]
        holder.textViewNombre.text = factura.nombre
        holder.textViewTelefono.text = factura.numeroControl
        val fecha = factura.fecha.split(" ")[0]
        holder.textViewDui.text = "Fecha: ${fecha}"
        // Hacer clickeable toda la vista del item
        holder.itemView.setOnClickListener {
            onItemClickListener(factura) // Llamar al callback cuando se haga clic en el item
        }
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
