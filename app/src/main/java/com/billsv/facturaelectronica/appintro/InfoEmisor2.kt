package com.billsv.facturaelectronica.appintro

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.billsv.facturaelectronica.MyApp
import com.billsv.facturaelectronica.R
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.DataSource
import com.couchbase.lite.Database
import com.couchbase.lite.Expression
import com.couchbase.lite.Meta
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult

class InfoEmisor2 : Fragment() {
    private lateinit var database: Database
    private lateinit var nombreC: EditText
    private lateinit var NRC: EditText
    private lateinit var AcEco: EditText
    private lateinit var Direccion: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_info_emisor2, container, false)
        val app = requireActivity().application as MyApp
        database = app.database
        nombreC = view.findViewById(R.id.nombreC)
        NRC = view.findViewById(R.id.nrc)
        AcEco = view.findViewById(R.id.AcEco)
        Direccion = view.findViewById(R.id.Direccion)
        NRC.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            //private val mask = "#######"
            private val mask4Digits = "###-#"
            private val mask7Digits = "######-#"

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return

                isUpdating = true

                val digitsOnly = s.toString().replace(Regex("\\D"), "") // Remover cualquier carácter no numérico

                // Determinar qué máscara usar según la cantidad de dígitos ingresados
                val mask = when (digitsOnly.length) {
                    in 1..4 -> mask4Digits
                    in 5..7 -> mask7Digits
                    else -> mask7Digits // Si se excede, aplica la máscara de 7 dígitos
                }

                val formatted = formatNRC(digitsOnly, mask)
                NRC.setText(formatted)
                NRC.setSelection(formatted.length)
                isUpdating = false
            }

            private fun formatNRC(nrc: String, mask: String): String {
                val formatted = StringBuilder()
                var i = 0

                // Aplicar la máscara a los dígitos ingresados
                for (m in mask.toCharArray()) {
                    if (m != '#') {
                        formatted.append(m)
                        continue
                    }
                    if (i >= nrc.length) break
                    formatted.append(nrc[i])
                    i++
                }
                return formatted.toString()
            }
        })
        return view
    }

    fun actualizarInformacionInfoEmisor2() {
        val nombreC = nombreC.text.toString()
        val nrc = NRC.text.toString()
        val AcEco = AcEco.text.toString()
        val direccion = Direccion.text.toString()
        // Buscar si ya existe un documento del tipo "ConfEmisor"
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("ConfEmisor")))

        try {
            val resultSet = query.execute()
            val results = resultSet.allResults()

            if (results.isNotEmpty()) {
                // Si existe un documento, actualizarlo
                val docId = results.first().getString(0) // Obtener el ID del primer documento encontrado
                docId?.let {
                    val document = database.getDocument(it)?.toMutable() // Convertir el documento a mutable
                    document?.let {
                        document.setString("nrc", nrc)
                        document.setString("ActividadEco", AcEco)
                        document.setString("nombreC", nombreC)
                        document.setString("direccion", direccion)

                        // Guardar el documento actualizado
                        database.save(document)
                        Log.d("ReClienteActivity", "Documento actualizado correctamente")
                    }
                }
            }
            showToast("Datos guardados correctamente")
        } catch (e: CouchbaseLiteException) {
            Log.e("ReClienteActivity", "Error al guardar los datos en la base de datos: ${e.message}", e)
            showToast("Error al guardar")
        }
    }

    // Variables para verificar si el mensaje ya se mostró
    private var MensajeError1: Boolean = false
    private var MensajeError2: Boolean = false
    private var MensajeError3: Boolean = false
    private var MensajeError4: Boolean = false
    private var MensajeError5: Boolean = false

    fun validarEntradasInfoEmisor2(): Boolean {
        val nombrecText = nombreC.text.toString()
        val nrcText = NRC.text.toString().replace(Regex("\\D"), "") // Remover cualquier carácter no numérico
        val acEcoText = AcEco.text.toString()
        val direccionText = Direccion.text.toString()

        // Verificar que todos los campos estén completos
        if (acEcoText.isNotEmpty() && nombrecText.isNotEmpty() && direccionText.isNotEmpty() && (nrcText.matches(Regex("\\d{7}")) || nrcText.matches(Regex("\\d{4}")))) {
            return true
        }else{ // De lo contrario verificar qué es lo que el usuario no ingresó o ingresó mal

            // Verificar si el usuario ingresó el nombre
            if (nombrecText.isEmpty()) {
                // Verifica si el mensaje ya se mostró
                if (!MensajeError1) {
                    Toast.makeText(requireContext(), "Ingrese el Nombre Comercial", Toast.LENGTH_SHORT).show()
                    MensajeError1 = true // El mensaje ya se mostró, no se volverá a mostrar
                }
                return false
            }

            // Verificar si el ususario ingresó el NRC
            if (nrcText.isEmpty()) {
                // Verifica si el mensaje ya se mostró
                if (!MensajeError2) {
                    Toast.makeText(requireContext(), "Ingrese el NRC", Toast.LENGTH_SHORT).show()
                    MensajeError2 = true // El mensaje ya se mostró, no se volverá a mostrar
                }
                return false
            } else if (!nrcText.matches(Regex("\\d{7}")) && !nrcText.matches(Regex("\\d{4}"))) {
                // Verifica si el mensaje ya se mostró
                if (!MensajeError3) {
                    Toast.makeText(requireContext(), "Ingrese un NRC válido", Toast.LENGTH_SHORT).show()
                    MensajeError3 = true // El mensaje ya se mostró, no se volverá a mostrar
                }
                return false
            }

            // Verificar si el usuario ingresó la actividad económica
            if (acEcoText.isEmpty()) {
                // Verifica si el mensaje ya se mostró
                if (!MensajeError4) {
                    Toast.makeText(requireContext(), "Ingrese la Actividad Económica", Toast.LENGTH_SHORT).show()
                    MensajeError4 = true // El mensaje ya se mostró, no se volverá a mostrar
                }
                return false
            }

            // Verficar si el usuario ingresó la dirección
            if (direccionText.isEmpty()) {
                // Verifica si el mensaje ya se mostró
                if (!MensajeError5) {
                    Toast.makeText(requireContext(), "Ingrese la Dirección", Toast.LENGTH_SHORT).show()
                    MensajeError5 = true // El mensaje ya se mostró, no se volverá a mostrar
                }
                return false
            }

            return false
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        // Puedes pasar argumentos aquí si lo necesitas
        fun newInstance(): InfoEmisor2  {
            return InfoEmisor2 ()
        }
    }
}
