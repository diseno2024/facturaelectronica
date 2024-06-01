package com.billsv.facturaelectronica

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.DataSource
import com.couchbase.lite.Database
import com.couchbase.lite.Expression
import com.couchbase.lite.Meta
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult
import com.google.android.material.card.MaterialCardView

class InfoEmisorActivity : AppCompatActivity() {
    private lateinit var database: Database
    private lateinit var nombre: EditText
    private lateinit var nombreC: EditText
    private lateinit var NIT: EditText
    private lateinit var NRC: EditText
    private lateinit var AcEco: EditText
    private lateinit var Direccion: EditText
    private lateinit var NumT: EditText
    private lateinit var Correo: EditText
    /*private lateinit var departamento: Spinner
    private lateinit var municipio: Spinner*/
    private val REQUEST_CODE_IMAGE_PICKER = 123
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_info_emisor)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val app = application as MyApp
        database = app.database
        verificar()
        contarDocumentosConfEmisor()
        val btnAtras: ImageButton = findViewById(R.id.atras)
        btnAtras.setOnClickListener {
            // Crear un intent para ir a MenuActivity
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()  // Finalizar la actividad actual si se desea
        }
        //quita la imagen seleccionada
        val btnBorrarImagen: ImageButton = findViewById(R.id.btnBorrarImagen)
        btnBorrarImagen.setOnClickListener {
            borrarImagen()
        }
        nombre = findViewById(R.id.nombre)
        nombreC = findViewById(R.id.nombreC)
        NIT = findViewById(R.id.NIT)
        NRC = findViewById(R.id.NRC)
        AcEco = findViewById(R.id.AcEco)
        Direccion = findViewById(R.id.Direccion)
        /*departamento = findViewById(R.id.departamento)
        municipio = findViewById(R.id.municipio)*/
        NumT = findViewById(R.id.NumT)
        Correo = findViewById(R.id.Correo)

        NumT.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val mask = "####-####"

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return

                isUpdating = true
                val formatted = formatPhoneNumber(s.toString())
                NumT.setText(formatted)
                NumT.setSelection(formatted.length)
                isUpdating = false
            }

            private fun formatPhoneNumber(phone: String): String {
                val digits = phone.replace(Regex("\\D"), "")
                val formatted = StringBuilder()

                var i = 0
                for (m in mask.toCharArray()) {
                    if (m != '#') {
                        formatted.append(m)
                        continue
                    }
                    if (i >= digits.length) break
                    formatted.append(digits[i])
                    i++
                }
                return formatted.toString()
            }

        })
        NIT.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val mask = "####-######-###-#" // La máscara del NIT

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return

                isUpdating = true
                val formatted = formatNIT(s.toString())
                NIT.setText(formatted)
                NIT.setSelection(formatted.length)
                isUpdating = false
            }

            private fun formatNIT(nit: String): String {
                // Eliminar todos los caracteres no numéricos del NIT
                val digits = nit.replace(Regex("\\D"), "")
                val formatted = StringBuilder()

                var i = 0
                for (m in mask.toCharArray()) {
                    if (m != '#') {
                        formatted.append(m)
                        continue
                    }
                    if (i >= digits.length) break
                    formatted.append(digits[i])
                    i++
                }
                return formatted.toString()
            }
        })
        NRC.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val mask = "#######"

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return

                isUpdating = true
                val formatted = formatPhoneNumber(s.toString())
                NRC.setText(formatted)
                NRC.setSelection(formatted.length)
                isUpdating = false
            }

            private fun formatPhoneNumber(phone: String): String {
                val digits = phone.replace(Regex("\\D"), "")
                val formatted = StringBuilder()

                var i = 0
                for (m in mask.toCharArray()) {
                    if (m != '#') {
                        formatted.append(m)
                        continue
                    }
                    if (i >= digits.length) break
                    formatted.append(digits[i])
                    i++
                }
                return formatted.toString()
            }

        })
        val btnGuardar: Button = findViewById(R.id.Guardar)
        btnGuardar.setOnClickListener {
            if (validarEntradas()){
                guardarInformacion()
                recreate()
                /*val intent = Intent(this, MenuActivity::class.java)
                startActivity(intent)*/
            }
        }
        val btnEditar: Button = findViewById(R.id.Editar)
        btnEditar.setOnClickListener {
            habilitaredicion()
        }
    }

    private fun habilitaredicion() {

        val nombre: EditText = findViewById(R.id.nombre)
        val nombrec: EditText = findViewById(R.id.nombreC)
        val nit: EditText = findViewById(R.id.NIT)
        val nrc: EditText = findViewById(R.id.NRC)
        val AcEco: EditText = findViewById(R.id.AcEco)
        val direccion: EditText = findViewById(R.id.Direccion)
        val NumT: EditText = findViewById(R.id.NumT)
        val correo: EditText = findViewById(R.id.Correo)

        //habilitar
        nombre.isFocusable = true
        nombre.isFocusableInTouchMode = true

        nombrec.isFocusable = true
        nombrec.isFocusableInTouchMode = true

        nit.isFocusable = true
        nit.isFocusableInTouchMode = true

        nrc.isFocusable = true
        nrc.isFocusableInTouchMode = true

        AcEco.isFocusable = true
        AcEco.isFocusableInTouchMode = true

        direccion.isFocusable = true
        direccion.isFocusableInTouchMode = true

        NumT.isFocusable = true
        NumT.isFocusableInTouchMode = true

        correo.isFocusable = true
        correo.isFocusableInTouchMode = true

        //pone el efectoclick
        nombre.isEnabled = true
        nombrec.isEnabled = true
        nit.isEnabled = true
        nrc.isEnabled = true
        AcEco.isEnabled = true
        direccion.isEnabled = true
        NumT.isEnabled = true
        correo.isEnabled = true

        val boton: Button = findViewById(R.id.Guardar)
        boton.visibility = View.VISIBLE
        val boton2: Button = findViewById(R.id.Editar)
        boton2.visibility = View.GONE

    }

    private fun verificar() {
        val dataList = obtenerDatosGuardados()
        dataList.forEach { data ->
            //sacar la data
            val datos = data.split("\n")
            val nombredato = datos[0]
            if(nombredato!=""){
                mostrardata(dataList)
                val boton: Button = findViewById(R.id.Guardar)
                boton.visibility = View.GONE
                val boton2: Button = findViewById(R.id.Editar)
                boton2.visibility = View.VISIBLE
            }

        }
        val uri = obtenerUriGuardada()?.toUri()
        if(uri!=null){
            val Imagen: ImageView = findViewById(R.id.Logo)
            Imagen.setBackgroundColor(Color.TRANSPARENT)
            Imagen.setImageURI(uri)
            // Mostrar el botón de borrar
            val btnBorrarImagen: ImageButton = findViewById(R.id.btnBorrarImagen)
            btnBorrarImagen.visibility = View.VISIBLE
            val Card: MaterialCardView = findViewById(R.id.Imagen)
            Card.setClickable(false)
        }else{
            val Imagen: ImageView = findViewById(R.id.Logo)
            val drawable = ContextCompat.getDrawable(this, R.drawable.ic_add_photo)
            Imagen.setImageDrawable(drawable)
        }
    }


    private fun mostrardata(dataList: List<String>) {
        //buscar los edittext
        val nombre: EditText = findViewById(R.id.nombre)
        val nombrec: EditText = findViewById(R.id.nombreC)
        val nit: EditText = findViewById(R.id.NIT)
        val nrc: EditText = findViewById(R.id.NRC)
        val AcEco: EditText = findViewById(R.id.AcEco)
        val direccion: EditText = findViewById(R.id.Direccion)
        val NumT: EditText = findViewById(R.id.NumT)
        val correo: EditText = findViewById(R.id.Correo)
        dataList.forEach { data ->
            //sacar la data
            val datos = data.split("\n")
            val nombredato = datos[0]
            val nombrecdato = datos[1]
            val nitdato = datos[2]
            val nrcdato = datos[3]
            val AcEcodato = datos[4]
            val direcciondato = datos[5]
            val Numtdato = datos[6]
            val correodato = datos[7]

            //pasar la data a los edittext
            nombre.setText(nombredato)
            nombrec.setText(nombrecdato)
            nit.setText(nitdato)
            nrc.setText(nrcdato)
            AcEco.setText(AcEcodato)
            direccion.setText(direcciondato)
            NumT.setText(Numtdato)
            correo.setText(correodato)

            //deshabilitar
            nombre.isFocusable = false
            nombre.isFocusableInTouchMode = false

            nombrec.isFocusable = false
            nombrec.isFocusableInTouchMode = false

            nit.isFocusable = false
            nit.isFocusableInTouchMode = false

            nrc.isFocusable = false
            nrc.isFocusableInTouchMode = false

            AcEco.isFocusable = false
            AcEco.isFocusableInTouchMode = false

            direccion.isFocusable = false
            direccion.isFocusableInTouchMode = false

            NumT.isFocusable = false
            NumT.isFocusableInTouchMode = false

            correo.isFocusable = false
            correo.isFocusableInTouchMode = false

            //quita el efectoclick
            nombre.isEnabled = false
            nombrec.isEnabled = false
            nit.isEnabled = false
            nrc.isEnabled = false
            AcEco.isEnabled = false
            direccion.isEnabled = false
            NumT.isEnabled = false
            correo.isEnabled = false
        }
    }

    private fun mostrarImagen(){
        val uri = obtenerUriGuardada()?.toUri()
        if(uri!=null){
            val Imagen: ImageView = findViewById(R.id.Logo)
            Imagen.setBackgroundColor(Color.TRANSPARENT)
            Imagen.setImageURI(uri)
            // Mostrar el botón de borrar
            val btnBorrarImagen: ImageButton = findViewById(R.id.btnBorrarImagen)
            btnBorrarImagen.visibility = View.VISIBLE
            val Card: MaterialCardView = findViewById(R.id.Imagen)
            Card.setClickable(false)
            showToast("Imagen cargada")
        }else{
            val Imagen: ImageView = findViewById(R.id.Logo)
            val drawable = ContextCompat.getDrawable(this, R.drawable.ic_add_photo)
            Imagen.setImageDrawable(drawable)
        }
    }
    private fun obtenerUriGuardada(): String? {

        val query = QueryBuilder.select(SelectResult.property("URI"))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("Imagen")))

        return try {
            val resultSet = query.execute()
            val result = resultSet.next()

            result?.getString("URI")
        } catch (e: CouchbaseLiteException) {
            Log.e("Prin_Re_Cliente", "Error al obtener la URI de la base de datos: ${e.message}", e)
            null
        }
    }
    fun showGallery(view: View?) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICKER)

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE_PICKER && resultCode == RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                val contentResolver = contentResolver
                val mimeType = contentResolver.getType(uri)

                if (mimeType != null) {
                    if (mimeType == "image/jpeg" || mimeType == "image/png") {
                        // La imagen es válida (JPEG o PNG)
                        guardarURI(uri)
                    } else {
                        // La imagen no es válida (otro formato)
                        showToast("Selecciona una imagen en formato JPEG o PNG")
                    }
                } else {
                    // No se pudo determinar el tipo MIME
                    showToast("Error al obtener el tipo de la imagen")
                }
            } else {
                showToast("Error al obtener la URI de la imagen seleccionada")
            }
        }
    }
    private fun guardarURI(uri: Uri) {
        val uriString = uri.toString()
        // Consulta para verificar si la URI ya existe
        val query = QueryBuilder.select(SelectResult.property("URI"))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("Imagen")))

        try {
            val resultSet = query.execute()
            if (resultSet.allResults().isEmpty()) {
                // Crear un documento mutable para guardar en la base de datos
                val document = MutableDocument()
                    .setString("URI", uriString)
                    .setString("tipo", "Imagen")

                // Guardar el documento en la base de datos
                database.save(document)
                Log.d("Prin_Re_Cliente", "Datos guardados correctamente: \n $document")
                Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("Prin_Re_Cliente", "La URI ya existe en la base de datos: $uriString")
                Toast.makeText(this, "La URI ya existe en la base de datos", Toast.LENGTH_SHORT).show()
            }
        } catch (e: CouchbaseLiteException) {
            Log.e("Prin_Re_Cliente", "Error al consultar o guardar los datos en la base de datos: ${e.message}", e)
            Toast.makeText(this, "Error al consultar o guardar los datos", Toast.LENGTH_SHORT).show()
        }
        mostrarImagen()
    }
    private fun borrarImagen(){
        // Eliminar la imagen seleccionada (puedes reiniciar la variable 'logo' a null)
        val Imagen: ImageView = findViewById(R.id.Logo)
        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_add_photo)
        Imagen.setBackgroundColor(hexToColorInt("#EFEEEE"))
        Imagen.setImageDrawable(drawable)
        // Ocultar el botón de borrar
        val btnBorrarImagen: ImageButton = findViewById(R.id.btnBorrarImagen)
        btnBorrarImagen.visibility = View.GONE
        val Card: MaterialCardView = findViewById(R.id.Imagen)
        Card.setClickable(true)
        // Realiza una consulta para obtener todos los documentos que contienen URI
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("Imagen")))

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
                Log.d("Prin_Re_Cliente", "La URI ha sido eliminada de la base de datos")
                showToast("Imagen Eliminada")
            } else {
                Log.d("Prin_Re_Cliente", "No hay URI en la base de datos para borrar")
                showToast("No hay URI en la base de datos para borrar")
            }
        } catch (e: CouchbaseLiteException) {
            Log.e("Prin_Re_Cliente", "Error al eliminar las URI de la base de datos: ${e.message}", e)
        }
    }
    fun hexToColorInt(hex: String): Int {
        return try {
            Color.parseColor(hex)
        } catch (e: IllegalArgumentException) {
            // Manejar errores si el formato del color hexadecimal es incorrecto
            Color.BLACK // Valor predeterminado en caso de error
        }
    }

    // Método para mostrar un mensaje en un Toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun guardarInformacion() {
        val nombre = nombre.text.toString()
        val nombreC = nombreC.text.toString()
        val nit = NIT.text.toString()
        val nrc = NRC.text.toString()
        val AcEco = AcEco.text.toString()
        val direccion = Direccion.text.toString()
        val telefono = NumT.text.toString().replace("-", "")
        val correo = Correo.text.toString()

        // Buscar si ya existe un documento del tipo "ConfEmisor"
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("ConfEmisor")))

        try {
            val resultSet = query.execute()
            val results = resultSet.allResults()

            if (results.isNotEmpty()) {
                // Iterar sobre los resultados y eliminar cada documento
                for (result in results) {
                    val docId = result.getString(0) // Obtenemos el ID del documento en el índice 0
                    docId?.let {
                        val document = database.getDocument(it)
                        document?.let {
                            database.delete(it)
                        }
                    }
                }
                Log.d("ReClienteActivity", "Documento existente borrado")
            }

            // Crear un nuevo documento
            val document = MutableDocument()
                .setString("nombre", nombre)
                .setString("nombreC", nombreC)
                .setString("nit", nit)
                .setString("nrc", nrc)
                .setString("ActividadEco", AcEco)
                .setString("direccion", direccion)
                .setString("telefono", telefono)
                .setString("correo", correo)
                .setString("tipo", "ConfEmisor")

            // Guardar el nuevo documento
            database.save(document)
            Log.d("ReClienteActivity", "Datos guardados correctamente: \n $document")
            Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
        } catch (e: CouchbaseLiteException) {
            Log.e("ReClienteActivity", "Error al guardar los datos en la base de datos: ${e.message}", e)
            Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
        }
    }
    private fun obtenerDatosGuardados(): List<String> {
        // Obtén la instancia de la base de datos desde la aplicación
        val app = application as MyApp
        val database = app.database

        // Crea una consulta para seleccionar todos los documentos con tipo = "cliente"
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("ConfEmisor")))

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
            val nombrec = dict?.getString("nombreC")
            val nit = dict?.getString("nit")
            val nrc = dict?.getString("nrc")
            val AcEco = dict?.getString("ActividadEco")
            val direccion = dict?.getString("direccion")
            val telefono = dict?.getString("telefono")
            val correo = dict?.getString("correo")

            // Formatea los datos como una cadena y la agrega a la lista
            val dataString = "$nombre\n$nombrec\n$nit\n$nrc\n$AcEco\n$direccion\n$telefono\n$correo"
            dataList.add(dataString)
        }

        // Devuelve la lista de datos
        return dataList
    }
    private fun contarDocumentosConfEmisor() {
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("ConfEmisor")))

        try {
            val result = query.execute()
            val count = result.allResults().size
            Log.d("ReClienteActivity", "Número de documentos de tipo 'ConfEmisor': $count")
        } catch (e: CouchbaseLiteException) {
            Log.e("ReClienteActivity", "Error al contar los documentos de tipo 'ConfEmisor': ${e.message}", e)
        }
    }
    private fun validarEntradas(): Boolean {
        val nombreText = nombre.text.toString()
        val nombrecText = nombreC.text.toString()
        val nitText = NIT.text.toString().replace("-", "")
        val nrcText = NRC.text.toString()
        val AcEco = AcEco.text.toString()
        val direccionText = Direccion.text.toString()
        val telefonoText = NumT.text.toString().replace("-", "")
        val emailText = Correo.text.toString()

        // Verifica que todos los campos estén llenos
        if (nombreText.isEmpty() || nitText.isEmpty() || emailText.isEmpty() ||  telefonoText.isEmpty() || AcEco.isEmpty() || nombrecText.isEmpty()){
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return false
        }

        // Verifica que el correo electrónico tenga un formato válido
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            Toast.makeText(this, "Correo electrónico no válido", Toast.LENGTH_SHORT).show()
            return false
        }

        // Verifica que el NIT sea un número válido
        if (!nitText.matches(Regex("\\d{14}"))) {
            Toast.makeText(this, "NIT debe ser un número válido", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!nrcText.matches(Regex("\\d{7}"))) {
            Toast.makeText(this, "NRC debe ser un número válido", Toast.LENGTH_SHORT).show()
            return false
        }

        // Verifica que el teléfono sea un número válido de 8 dígitos
        if (!telefonoText.matches(Regex("\\d{8}"))) {
            Toast.makeText(this, "Teléfono debe ser un número válido de 8 dígitos", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

}