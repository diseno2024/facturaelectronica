package com.billsv.facturaelectronica

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.billsv.facturaelectronica.appintro.InfoEmisor1
import java.io.File


class FacturaAdaptercf(
    private val context: Context,
    private var facturaList: MutableList<Factura>,
    private val onItemClickListener: (Factura) -> Unit // Callback para manejar el clic
) : RecyclerView.Adapter<FacturaAdaptercf.FacturaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacturaViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.listar_facturascf, parent, false)
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

        // Configurar el botón Gmail para enviar los documentos adjuntos
        holder.btnEnviarPorGmail.setOnClickListener {

            //Verificar que hay una dirección a la cual enviar el correo
            if (factura.correo.isEmpty()) {
                Toast.makeText(context, "No hay dirección de correo asociada a esta factura.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                // Obtener las rutas de los archivos adjuntos
                val pdfFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/${factura.codigoG}.pdf"
                val jsonFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/${factura.codigoG}.json"

                // Crear objetos File para los archivos
                val pdfFile = File(pdfFilePath)
                val jsonFile = File(jsonFilePath)

                // Crear objetos Uri para los archivos
                val pdfUri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", pdfFile)
                val jsonUri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", jsonFile)

                // Crear un Intent para enviar el correo electrónico
                val emailIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "text/html" // Cambiar el tipo de contenido a text/html
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(factura.correo))
                    putExtra(Intent.EXTRA_SUBJECT, "DOCUMENTO TRIBUTARIO ELECTRÓNICO")
                    putExtra(Intent.EXTRA_TEXT, Html.fromHtml("Estimado(a) cliente:<br><br>" +
                            "Adjuntamos tu Documento Tributario Electrónico - DTE - COMPROBANTE DE CRÉDITO FISCAL.<br><br>" +
                            "Código de Generación:<br>" +
                            "<span style=\"font-weight:bold\">${factura.codigoG}</span><br>" +
                            "Código de Control:<br>" +
                            "<span style=\"font-weight:bold\">${factura.numeroControl}</span><br>" +
                            "Sello de Recepción:<br>" +
                            "<span style=\"font-weight:bold\">${factura.sello}</span><br><br>" +
                            "Esta es una notificación automática. Por favor, no responda a este correo.", Html.FROM_HTML_MODE_COMPACT))
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayListOf(pdfUri, jsonUri))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                // Iniciar la actividad de correo electrónico
                try {
                    context.startActivity(Intent.createChooser(emailIntent, "Enviar email..."))
                } catch (e: ActivityNotFoundException) {
                    // Manejar el caso en que no hay aplicaciones de correo instaladas
                    Toast.makeText(context, "No hay aplicaciones de correo instaladas.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Configurar el botón WhatsApp para enviar los documentos adjuntos
        holder.btnEnviarPorWhatsapp.setOnClickListener {
            Toast.makeText(context, "Enviar vía WhatsApp CCF", Toast.LENGTH_SHORT).show()
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
        val btnEnviarPorGmail: ImageButton = itemView.findViewById(R.id.btnEnviarPorGmail)          // El botón para enviar documentos por Gmail
        val btnEnviarPorWhatsapp: ImageButton = itemView.findViewById(R.id.btnEnviarPorWhatsApp)    // El botón para enviar documentos por WhatsApp
    }
}
