package com.billsv.facturaelectronica

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.DataSource
import com.couchbase.lite.Expression
import com.couchbase.lite.Meta
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult
import java.text.Normalizer

class ImportarClientes : AppCompatActivity() {
    private var letra:String?=null
    private lateinit var btnBuscar: ImageButton
    private lateinit var btnCerrarBusqueda: ImageButton
    private lateinit var btnBusqueda: ImageButton
    private lateinit var searchBar: EditText
    private lateinit var linearLayout: LinearLayout
    private lateinit var dataList: List<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_importar_clientes)
        linearLayout = findViewById<LinearLayout>(R.id.containerLayout)
        // Inicializar la barra de búsqueda y el botón
        btnBuscar = findViewById(R.id.btn_buscar)
        btnCerrarBusqueda = findViewById(R.id.btn_cerrar)
        btnBusqueda = findViewById(R.id.btn_buscador)
        searchBar = findViewById<EditText>(R.id.search_bar)
        dataList = obtenerDatosGuardados()
        // Obtener la letra pasada en el Intent
        val letra = intent.getStringExtra("letra")

        // Mostrar los datos en la vista
        dataList.forEach { data ->
            if (letra != null) {
                agregarItemALinearLayout(data, letra)
            }
        }

        // Encontrar el botón en el diseño
        val button = findViewById<ImageButton>(R.id.btnAgregar)

        // Mostrar el botón solo si la letra es 's'
        if (letra == "s") {
            button.visibility = View.VISIBLE
        } else {
            button.visibility = View.GONE
        }
        button.setOnClickListener{
            val intent=Intent(this, PrinReClienteActivity::class.java)
            startActivity(intent)
        }

        val botonAtras = findViewById<ImageButton>(R.id.atras)
        botonAtras.setOnClickListener {
            val intent = when (letra) {
                "s" -> {
                    Intent(this, MenuActivity::class.java)
                }
                "r" -> {
                    Intent(this, EmitirCCFActivity::class.java)
                }
                else -> {
                    Intent(this, EmitirCFActivity::class.java)
                }
            }
            startActivity(intent)
            finish()
        }

        // Establecer un OnClickListener para el botón de búsqueda
        btnBuscar.setOnClickListener {
            searchBar.visibility = View.VISIBLE
            btnBusqueda.visibility = View.VISIBLE
            btnBuscar.visibility = View.GONE
            btnCerrarBusqueda.visibility = View.VISIBLE
        }
        // Establecer un OnClickListener para el botón de cerrar búsqueda
        btnCerrarBusqueda.setOnClickListener {
            searchBar.visibility = View.GONE
            btnBusqueda.visibility = View.GONE
            btnCerrarBusqueda.visibility = View.GONE
            btnBuscar.visibility = View.VISIBLE
            searchBar.text.clear()

            // Actualizar el dataList con los datos actuales
            linearLayout.removeAllViews()
            dataList = obtenerDatosGuardados()
            dataList.forEach { data ->
                if (letra != null) {
                    agregarItemALinearLayout(data, letra)
                }
            }
        }

        // Establecer un OnClickListener para el botón de búsqueda
        btnBusqueda.setOnClickListener {
            val query = searchBar.text.toString()
            if (letra != null) {
                realizarBusqueda(query, letra)
            }
        }
        // Establecer ejecución de la búsqueda al presionar Enter
        searchBar.setOnEditorActionListener { _, _, _ ->
            val query = searchBar.text.toString()
            if (letra != null) {
                realizarBusqueda(query, letra)
            }
            true
        }
        // Establecer ejecución de la búsqueda al presionar Enter
        searchBar.setOnEditorActionListener { _, actionId, event ->
            Log.d("Prin_Re_Cliente", "Se presionó una tecla: $actionId")
            val query = searchBar.text.toString()
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH, EditorInfo.IME_ACTION_DONE -> {
                    if (letra != null) {
                        realizarBusqueda(query, letra)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun realizarBusqueda(query: String, letra: String) {
        // Limpiar resultados anteriores
        linearLayout.removeAllViews()

        // Normalizar la entrada del usuario eliminando tildes y espacios en exceso
        val cleanedQuery = removeAccents(query.trim()).split(Regex("\\s+")) // Divide la query en palabras separadas por espacios

        val filteredList = dataList.filter { data ->
            val datos = data.split("\n")
            val nombre = removeAccents(datos[0].replace("-", "").trim())  // Asumiendo que el nombre completo está en la posición 0
            val dui = datos[8].replace("-", "").replace(" ", "").trim()  // Asumiendo que el DUI está en la posición 8
            val nit = datos[11].replace("-", "").replace(" ", "").trim() // Asumiendo que el NIT está en la posición 11
            val nrc = datos[9].replace("-", "").replace(" ", "").trim()  // Asumiendo que el NRC está en la posición 9

            // Separar el nombre completo en palabras (nombres y apellidos)
            val nombrePalabras = nombre.split(Regex("\\s+"))

            // Comparar la entrada con los nombres normalizados
            val nombreCoincide = cleanedQuery.all { queryWord ->
                nombrePalabras.any { it.contains(queryWord, ignoreCase = true) }
            }

            // Verificar si el nombre completo coincide, o si el DUI, NIT o NRC coinciden (sin tildes, ya que son números)
            nombreCoincide ||
                    dui.contains(query, ignoreCase = true) ||
                    nit.contains(query, ignoreCase = true) ||
                    nrc.contains(query, ignoreCase = true)
        }

        // Mostrar los datos filtrados
        filteredList.forEach { data ->
            agregarItemALinearLayout(data, letra)
        }
    }

    // Función para remover acentos de una cadena.
    fun removeAccents(text: String): String {
        return Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    }

    private fun agregarItemALinearLayout(data: String, letra: String) {
        val datos = data.split("\n")

        if (datos[7] == "Contribuyente") {
            val itemLayout2 = layoutInflater.inflate(R.layout.layout_list_item_contribuyentes, null)

            val borrar2 = itemLayout2.findViewById<ImageButton>(R.id.btnBorrarData)
            val Editar2 = itemLayout2.findViewById<ImageButton>(R.id.btnEditarData)
            val Ver2 = itemLayout2.findViewById<ImageButton>(R.id.btnVerData)
            val textViewNombre2 = itemLayout2.findViewById<TextView>(R.id.textViewNombreComercial)
            val textViewNRC = itemLayout2.findViewById<TextView>(R.id.textViewNrc)
            val textViewTelefono = itemLayout2.findViewById<TextView>(R.id.textViewTelefono)

            textViewNombre2.text = datos[0]
            textViewNRC.text = datos[9]
            textViewTelefono.text = datos[13]

            // Establece un onClickListener para cada tarjeta
            itemLayout2.setOnClickListener {
                if (letra == "r" || letra == "c") {
                    Pasardata(data)
                } else if (letra == "s") {
                    mostrardialogo(data)
                }
            }

            // Control de visibilidad de botones
            controlarVisibilidadBotones(letra, borrar2, Editar2, Ver2)

            borrar2.setOnClickListener {
                Borrardatos(data, itemLayout2, linearLayout)
            }
            Editar2.setOnClickListener {
                editardatos(data)
            }
            Ver2.setOnClickListener {
                mostrardialogo(data)
            }

            linearLayout.addView(itemLayout2)

        } else {
            val itemLayout = layoutInflater.inflate(R.layout.layout_list_item_cliente, null)

            val borrar = itemLayout.findViewById<ImageButton>(R.id.btnBorrarData)
            val Editar = itemLayout.findViewById<ImageButton>(R.id.btnEditarData)
            val Ver = itemLayout.findViewById<ImageButton>(R.id.btnVerData)
            val textViewNombre = itemLayout.findViewById<TextView>(R.id.textViewNombre)
            val textViewTelefono = itemLayout.findViewById<TextView>(R.id.textViewTelefono)
            val textViewNit = itemLayout.findViewById<TextView>(R.id.textViewNit)

            textViewNombre.text = datos[0]
            textViewTelefono.text = datos[13]
            textViewNit.text = datos[12]

            // Establece un onClickListener para cada tarjeta
            itemLayout.setOnClickListener {
                if (letra == "c") {
                    Pasardata(data)
                } else if (letra == "s") {
                    mostrardialogo(data)
                }
            }

            // Control de visibilidad de botones
            controlarVisibilidadBotones(letra, borrar, Editar, Ver)

            borrar.setOnClickListener {
                Borrardatos(data, itemLayout, linearLayout)
            }
            Editar.setOnClickListener {
                editardatos(data)
            }
            Ver.setOnClickListener {
                mostrardialogo(data)
            }

            if (letra != "r") {
                linearLayout.addView(itemLayout)
            }
        }
    }

    // Función para controlar la visibilidad de los botones
    private fun controlarVisibilidadBotones(letra: String, borrar: ImageButton, editar: ImageButton, ver: ImageButton) {
        if (letra == "r" || letra == "c") {
            borrar.visibility = View.GONE
            editar.visibility = View.GONE
            ver.visibility = View.VISIBLE
        } else if (letra == "s") {
            borrar.visibility = View.VISIBLE
            editar.visibility = View.VISIBLE
            ver.visibility = View.GONE
        }
    }

    private fun editardatos(data: String) {
        val intent = Intent(this, PrinReClienteActivity::class.java)
        intent.putExtra("datos",data)
        startActivity(intent)
    }

    private fun mostrardialogo(data: String) {
        val dialogoCliente = Dialog(this)
        dialogoCliente.setContentView(R.layout.layout_mostrar_data) // R.layout.layout_custom_dialog es tu diseño personalizado
        dialogoCliente.window?.setBackgroundDrawableResource(R.drawable.cuadro_dialogo)
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% del ancho de la pantalla
        val height = (resources.displayMetrics.heightPixels * 0.7).toInt() // 60% del alto de la pantalla
        val nombre = dialogoCliente.findViewById<TextView>(R.id.nombre)
        val dui = dialogoCliente.findViewById<TextView>(R.id.dui)
        val departamento = dialogoCliente.findViewById<TextView>(R.id.departamento)
        val municipio = dialogoCliente.findViewById<TextView>(R.id.municipio)
        val direccion = dialogoCliente.findViewById<TextView>(R.id.direccion)
        val correo = dialogoCliente.findViewById<TextView>(R.id.correo)
        val telefono = dialogoCliente.findViewById<TextView>(R.id.telefono)
        val nit = dialogoCliente.findViewById<TextView>(R.id.nit)
        val nrc = dialogoCliente.findViewById<TextView>(R.id.nrc)
        val AcEco = dialogoCliente.findViewById<TextView>(R.id.AcEco)
        val RemoverA = dialogoCliente.findViewById<LinearLayout>(R.id.vistaAc)
        val RemoverNRC = dialogoCliente.findViewById<LinearLayout>(R.id.vistaNRC)
        dialogoCliente.window?.setLayout(width, height)
        dialogoCliente.setCanceledOnTouchOutside(false)
        val btnExit = dialogoCliente.findViewById<ImageButton>(R.id.exit)
        btnExit.setOnClickListener {
            // Acción al hacer clic en el botón "Cancelar"
            dialogoCliente.dismiss()
        }
        val datos = data.split("\n")
        nombre.text = datos[0]
        dui.text = datos[12]
        departamento.text = datos[14]
        municipio.text = datos[15]
        direccion.text = datos[3]
        correo.text = datos[2]
        telefono.text = datos[13]
        nit.text = datos[11]
        nrc.text = datos[9]
        AcEco.text = datos[10]
        if(datos[7]!="Contribuyente"){
            RemoverA.visibility = View.GONE
            RemoverNRC.visibility = View.GONE
        }
        dialogoCliente.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val letra=intent.getStringExtra("letra")
        val intent = when (letra) {
            "s" -> {
                Intent(this, MenuActivity::class.java)
            }
            "r" -> {
                Intent(this, EmitirCCFActivity::class.java)
            }
            else -> {
                Intent(this, EmitirCFActivity::class.java)
            }
        }
        startActivity(intent)
        finish()
    }

    private fun obtenerDatosGuardados(): List<String> {
        // Obtén la instancia de la base de datos desde la aplicación
        val app = application as MyApp
        val database = app.database

        // Crea una consulta para seleccionar todos los documentos con tipo = "cliente"
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("cliente")))

        // Ejecuta la consulta
        val result = query.execute()

        // Lista para almacenar los datos obtenidos
        val dataList = mutableListOf<String>()

        // Itera sobre todos los resultados de la consulta
        result.allResults().forEach { result ->
            // Obtiene el diccionario del documento del resultado actual
            val dict = result.getDictionary(database.name)

            // Extrae los valores de los campos del documento
            val nombre = dict?.getString("nombre")
            val nit = dict?.getString("nit")
            val email = dict?.getString("email")
            val direccion = dict?.getString("direccion")
            val departamento = dict?.getString("departamento")
            val municipio = dict?.getString("municipio")
            val telefono = dict?.getString("telefono")
            val tipo = dict?.getString("tipoCliente")
            val dui = dict?.getString("dui")
            val nrc = dict?.getString("nrc")
            val AcEco = dict?.getString("actividadEconomica")
            val nitM= dict?.getString("nitM")
            val duiM=dict?.getString("duiM")
            val telM=dict?.getString("telefonoM")
            val depaText= dict?.getString("departamentoT")
            val muniText=dict?.getString("municipioT")

            // Formatea los datos como una cadena y la agrega a la lista
            val dataString = "$nombre\n$nit\n$email\n$direccion\n$departamento\n$municipio\n$telefono\n$tipo\n$dui\n$nrc\n$AcEco\n$nitM\n$duiM\n$telM\n$depaText\n$muniText"
            dataList.add(dataString)
        }

        // Devuelve la lista de datos
        return dataList
    }

    private fun Pasardata(data: String) {
        val letra = intent.getStringExtra("letra")
        if(letra=="r"){
            val intent = Intent(this, EmitirCCFActivity::class.java)
            // Pasa los datos de la carta seleccionada a la siguiente actividad
            intent.putExtra("Cliente", data)
            intent.putExtra("letrai", "P")
            startActivity(intent)
            finish()
        }else if(letra=="c"){
            val intent = Intent(this, EmitirCFActivity::class.java)
            // Pasa los datos de la carta seleccionada a la siguiente actividad
            intent.putExtra("Cliente", data)
            intent.putExtra("letrai", "P")
            startActivity(intent)
            finish()
        }
    }

    private fun Borrardatos(data: String, itemLayout: View, linearLayout: LinearLayout) {
        val app = application as MyApp
        val database = app.database
        val datos = data.split("\n")
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("dui").equalTo(Expression.string(datos[8])))

        try {
            val resultSet = query.execute()
            val results = resultSet.allResults()

            if (results.isNotEmpty()) {
                // Itera sobre los resultados y elimina cada documento
                for (result in results) {
                    val docId = result.getString(0) // Obtenemos el ID del documento en el índice 0
                    docId?.let {
                        val document = database.getDocument(it)
                        document?.let {
                            database.delete(it)
                        }
                    }
                }
                // Elimina la tarjeta de la vista
                linearLayout.removeView(itemLayout)


                Log.d("Prin_Re_Cliente", "Se eliminó el cliente")
                showToast("Cliente eliminado")
            } else {
                Log.d("Prin_Re_Cliente", "No existe el cliente")
                showToast("No se encontró el cliente para eliminar")
            }
        } catch (e: CouchbaseLiteException) {
            Log.e("Prin_Re_Cliente", "Error al eliminar al cliente: ${e.message}", e)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}