package com.billsv.facturaelectronica

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.DataSource
import com.couchbase.lite.Expression
import com.couchbase.lite.Meta
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult
import com.google.android.material.button.MaterialButtonToggleGroup
import android.content.Context
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class ConfHacienda : AppCompatActivity() {
    private lateinit var usuario: EditText
    private lateinit var contraseña: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_conf_hacienda)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        verificar()
        val credenciales = obtenerDatosGuardados()
        var usuarioapi = ""
        var contraseñaapi = ""
        credenciales.forEach { data ->
            val datosapi = data.split("\n")
            usuarioapi = datosapi[0]
            contraseñaapi = datosapi[1]
        }
        usuario = findViewById(R.id.usuario)
        contraseña = findViewById(R.id.contraseña)

        //https://apitest.dtes.mh.gob.sv/seguridad/auth
        //https://api.dtes.mh.gob.sv/seguridad/authw
        val boton: Button = findViewById(R.id.button2)
        boton.setOnClickListener {
            guardarInformacion()
            recreate()
        }
        val boton2: Button = findViewById(R.id.button3)
        boton2.setOnClickListener {
            habilitaredicion()
        }
        val toggleGroup = findViewById<MaterialButtonToggleGroup>(R.id.toggleButton)
        restoreSelectedButton()

        toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                saveSelectedButton(checkedId)
            }
        }
    }

    private fun verificar():Boolean {
        val datos = obtenerDatosGuardados()
        datos.forEach { data ->
            //sacar la data
            val dato = data.split("\n")
            val usuario = dato[0]
            if(usuario!=""){
                mostrardata(datos)
                val boton: Button = findViewById(R.id.button2)
                boton.visibility = View.GONE
                val boton2: Button = findViewById(R.id.button3)
                boton2.visibility = View.VISIBLE
                return true
            }

        }
        return false
    }

    private fun mostrardata(dataList: List<String>) {
        //buscar los edittext
        val usuario: EditText = findViewById(R.id.usuario)
        val contraseña: EditText = findViewById(R.id.contraseña)
        dataList.forEach { data ->
            //sacar la data
            val dato = data.split("\n")
            val usuariod = dato[0]
            val contraseñad = dato[1]

            //pasar la data a los edittext
            usuario.setText(usuariod)
            contraseña.setText(contraseñad)


            //quita el efectoclick
            usuario.isEnabled = false
            contraseña.isEnabled = false

        }
    }
    private fun habilitaredicion() {

        val usuario: EditText = findViewById(R.id.usuario)
        val contraseña: EditText = findViewById(R.id.contraseña)

        //pone el efectoclick
        usuario.isEnabled = true
        contraseña.isEnabled = true

        val boton: Button = findViewById(R.id.button2)
        boton.visibility = View.VISIBLE
        val boton2: Button = findViewById(R.id.button3)
        boton2.visibility = View.GONE

    }
    private fun guardarInformacion() {
        val app = application as MyApp
        val database = app.database
        val usuario = usuario.text.toString()
        val contraseña = contraseña.text.toString()

        // Buscar si ya existe un documento del tipo "ConfEmisor"
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("Autentificacion")))

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
                .setString("usuario", usuario)
                .setString("contraseña", contraseña)
                .setString("tipo", "Autentificacion")

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
            .where(Expression.property("tipo").equalTo(Expression.string("Autentificacion")))

        // Ejecuta la consulta
        val result = query.execute()

        // Lista para almacenar los datos obtenidos
        val dataList = mutableListOf<String>()

        // Itera sobre todos los resultados de la consulta
        result.allResults().forEach { result ->
            // Obtiene el diccionario del documento del resultado actual
            val dict = result.getDictionary(database.name)

            // Extrae los valores de los campos del documento
            val usuario = dict?.getString("usuario")
            val contraseña = dict?.getString("contraseña")

            // Formatea los datos como una cadena y la agrega a la lista
            val dataString = "$usuario\n$contraseña"
            dataList.add(dataString)
        }

        // Devuelve la lista de datos
        return dataList
    }
    private fun saveSelectedButton(buttonId: Int) {
        val sharedPreferences = getSharedPreferences("ButtonStatePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("selectedButton", buttonId)
        editor.apply()
    }
    private fun restoreSelectedButton() {
        val sharedPreferences = getSharedPreferences("ButtonStatePrefs", Context.MODE_PRIVATE)
        val selectedButtonId = sharedPreferences.getInt("selectedButton", View.NO_ID)
        if (selectedButtonId != View.NO_ID) {
            findViewById<MaterialButton>(selectedButtonId)?.isChecked = true
        }
    }




}