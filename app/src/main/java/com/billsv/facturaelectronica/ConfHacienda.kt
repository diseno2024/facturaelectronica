package com.billsv.facturaelectronica

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
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
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ConfHacienda : AppCompatActivity() {
    private lateinit var usuario: EditText
    private lateinit var contraseña: EditText
    private lateinit var checkBoxConsumidorFinal: CheckBox
    private lateinit var checkBoxCreditoFiscal: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_conf_hacienda)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Obtener instancia de la aplicación
        val app = application as MyApp
        // Ejemplo de cómo cambiar el valor de environment
        usuario = findViewById(R.id.usuario)
        contraseña = findViewById(R.id.contraseña)
        checkBoxConsumidorFinal = findViewById(R.id.checkBox_consumidor_final)
        checkBoxCreditoFiscal = findViewById(R.id.checkBox_credito_fiscal)

        verificar()

        val boton: Button = findViewById(R.id.button2)
        boton.setOnClickListener {
            guardarInformacion()

        }
        val boton2: Button = findViewById(R.id.button3)
        boton2.setOnClickListener {
            habilitaredicion()
        }
        val boton3: Button = findViewById(R.id.cert)
        boton3.setOnClickListener {
            val intent = Intent(this, CertificadoM::class.java)
            startActivity(intent)
            finish()
        }

        val toggleGroup = findViewById<MaterialButtonToggleGroup>(R.id.toggleButton)
        restoreSelectedButton()

        restoreCheckboxState()

        toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                saveSelectedButton(checkedId)
                when (checkedId) {
                    R.id.btnPrueba -> {
                        app.ambiente = "00"
                        checkBoxConsumidorFinal.visibility = View.GONE
                        checkBoxCreditoFiscal.visibility = View.GONE
                    }
                    R.id.btnProduccion -> {
                        app.ambiente = "01"
                        checkBoxConsumidorFinal.visibility = View.VISIBLE
                        checkBoxCreditoFiscal.visibility = View.VISIBLE
                    }
                }

                //Cargar Datos del Entorno
                cargarDatosDelEntorno()

                //Reiniciar el estado de edición dependiendo de los datos
                verificarEstadoEdicion()

                // Mostrar el nuevo estado para confirmar
                Toast.makeText(this, "Entorno: ${app.ambiente}", Toast.LENGTH_SHORT).show()
            }
        }
        cargarDatosDelEntorno()

        val atras = findViewById<ImageButton>(R.id.atras)
        atras.setOnClickListener {
            super.onBackPressed() // Llama al método onBackPressed() de la clase base
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
    override fun onBackPressed() {
        super.onBackPressed() // Llama al método onBackPressed() de la clase base
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
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

                checkBoxConsumidorFinal.isEnabled = false
                checkBoxCreditoFiscal.isEnabled = false
                return true
            }

        }
        checkBoxConsumidorFinal.isEnabled = true
        checkBoxCreditoFiscal.isEnabled = true
        return false
    }

    private fun mostrardata(dataList: List<String>) {
        val app = application as MyApp
        //buscar los edittext
        val usuario: EditText = findViewById(R.id.usuario)
        val contraseña: EditText = findViewById(R.id.contraseña)
        dataList.forEach { data ->
            //sacar la data
            val dato = data.split("\n")
            val usuariod = dato[0]
            val contraseñad = dato[1]

            if (dato.size > 3) {
                if (app.ambiente == "01") {
                    checkBoxConsumidorFinal.isChecked = dato[2].toBoolean()
                    checkBoxCreditoFiscal.isChecked = dato[3].toBoolean()
                    checkBoxConsumidorFinal.visibility = View.VISIBLE
                    checkBoxCreditoFiscal.visibility = View.VISIBLE
                }else{
                    checkBoxConsumidorFinal.isChecked = false
                    checkBoxCreditoFiscal.isChecked = false
                    checkBoxConsumidorFinal.visibility = View.GONE
                    checkBoxCreditoFiscal.visibility = View.GONE
                }
            }

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
        checkBoxConsumidorFinal.isEnabled = true
        checkBoxCreditoFiscal.isEnabled = true

        val boton: Button = findViewById(R.id.button2)
        boton.visibility = View.VISIBLE
        val boton2: Button = findViewById(R.id.button3)
        boton2.visibility = View.GONE

    }

    /*private fun generarUUIDv4(): String {
        val uuid = UUID.randomUUID()
        return uuid.toString().toUpperCase()
    }*/

    private fun cargarDatosDelEntorno(){
        val app = application as MyApp
        val datos = obtenerDatosGuardados()
        if (datos.isNotEmpty()){
            mostrardata(datos)
            verificarEstadoEdicion()
            if (app.ambiente == "01") {
                checkBoxConsumidorFinal.visibility = View.VISIBLE
                checkBoxCreditoFiscal.visibility = View.VISIBLE
            }else{
                checkBoxConsumidorFinal.visibility = View.GONE
                checkBoxCreditoFiscal.visibility = View.GONE
            }
        } else {
            limpiarCampos()
            checkBoxConsumidorFinal.isEnabled = true
            checkBoxCreditoFiscal.isEnabled = true
        }
    }

    private fun limpiarCampos() {
        val usuario: EditText = findViewById(R.id.usuario)
        val contraseña: EditText = findViewById(R.id.contraseña)

        //Limpiar los campos si no hay datos guardados
        usuario.setText("")
        contraseña.setText("")

        //Habilitar edición de los campos para ingresar nuevos datos
        usuario.isEnabled = true
        contraseña.isEnabled = true
    }

    private fun verificarEstadoEdicion() {
        val usuarioTexto = usuario.text.toString().trim()
        val contraseñaTexto = contraseña.text.toString().trim()

        val botonGuardar: Button = findViewById(R.id.button2)
        val botonEditar: Button = findViewById(R.id.button3)

        if (usuarioTexto.isNotEmpty() && contraseñaTexto.isNotEmpty()) {
            usuario.isEnabled = false
            contraseña.isEnabled = false
            botonGuardar.visibility = View.GONE
            botonEditar.visibility = View.VISIBLE
            checkBoxConsumidorFinal.isEnabled = false
            checkBoxCreditoFiscal.isEnabled = false
        } else {
            usuario.isEnabled = true
            contraseña.isEnabled = true
            botonGuardar.visibility = View.VISIBLE
            botonEditar.visibility = View.GONE
            checkBoxConsumidorFinal.isEnabled = true
            checkBoxCreditoFiscal.isEnabled = true
        }
    }

    private fun guardarInformacion() {
        val app = application as MyApp
        val database = app.database
        val usuario = usuario.text.toString()
        val contraseña = contraseña.text.toString()

        //Establecer un tipo de documeto diferente para cada entorno
        //val tipoAutenticacion = if (app.ambiente == "00") "AutenticacionPrueba" else "AutenticacionProduccion"

        // Buscar si ya existe un documento del tipo "ConfEmisor"
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("Autentificacion")))

        try {
            val resultSet = query.execute()
            val results = resultSet.allResults()

            var document : MutableDocument

            if (results.isNotEmpty()) {
                // Si ya existe un documento, obten su ID
                val docId = results[0].getString(0)
                val existingDoc = database.getDocument(docId!!)
                document = existingDoc?.toMutable() ?: MutableDocument()
            } else {
                // Crear un nuevo documento si no existe
                document = MutableDocument()
                document.setString("tipo","Autentificacion") //Un solo tipo general
            }

            //dependiendo del entorno, guardamos las credenciales en diferentes campos
            if (app.ambiente == "00") {//Entorno Prueba
                document.setString("usuarioPrueba",usuario)
                document.setString("contraseñaPrueba", contraseña)
            }else{//Entorno Produccion
                document.setString("usuarioProduccion", usuario)
                document.setString("contraseñaProduccion", contraseña)
                document.setBoolean("consumidorFinal", checkBoxConsumidorFinal.isChecked)
                document.setBoolean("creditoFiscal", checkBoxCreditoFiscal.isChecked)
            }

            // Guardar el nuevo documento
            database.save(document)
            Log.d("ReClienteActivity", "Datos guardados correctamente: \n $document")
            Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()

            verificarEstadoEdicion() //Actualiza la interfaz de usuario despues de guardar
        } catch (e: CouchbaseLiteException) {
            Log.e("ReClienteActivity", "Error al guardar los datos en la base de datos: ${e.message}", e)
            Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
        }
    }

    private fun obtenerDatosGuardados(): List<String> {
        // Obtén la instancia de la base de datos desde la aplicación
        val app = application as MyApp
        val database = app.database
        //val tipoAutenticacion = if (app.ambiente == "00") "AutenticacionPrueba" else "AutenticacionProduccion"

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
            val usuario = if (app.ambiente == "00") dict?.getString("usuarioPrueba") else dict?.getString("usuarioProduccion")
            val contraseña = if (app.ambiente == "00") dict?.getString("contraseñaPrueba") else dict?.getString("contraseñaProduccion")
            val consumidorFinal = dict?.getBoolean("consumidorFinal") ?: false
            val creditoFiscal = dict?.getBoolean("creditoFiscal") ?: false

            // Formatea los datos como una cadena y la agrega a la lista
            val dataString = "$usuario\n$contraseña\n$consumidorFinal\n$creditoFiscal"
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

            // También actualiza el ambiente en función del botón restaurado
            val app = application as MyApp
            when (selectedButtonId) {
                R.id.btnPrueba -> {
                    app.ambiente = "00"
                    checkBoxConsumidorFinal.visibility = View.GONE
                    checkBoxCreditoFiscal.visibility = View.GONE
                }
                R.id.btnProduccion -> {
                    app.ambiente = "01"
                    checkBoxConsumidorFinal.visibility = View.VISIBLE
                    checkBoxCreditoFiscal.visibility = View.VISIBLE
                }
            }

        }
    }

    private fun saveCheckboxState() {
        val sharedPreferences = getSharedPreferences("CheckboxStatePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("consumidorFinal", checkBoxConsumidorFinal.isChecked)
        editor.putBoolean("creditoFiscal", checkBoxCreditoFiscal.isChecked)
        editor.apply()
    }

    private fun restoreCheckboxState() {
        val sharedPreferences = getSharedPreferences("CheckboxStatePrefs", Context.MODE_PRIVATE)
        val consumidorFinalChecked = sharedPreferences.getBoolean("consumidorFinal", false)
        val creditoFiscalChecked = sharedPreferences.getBoolean("creditoFiscal", false)

        checkBoxConsumidorFinal.isChecked = consumidorFinalChecked
        checkBoxCreditoFiscal.isChecked = creditoFiscalChecked

        // Lógica para mostrar u ocultar los checkboxes basándose en el ambiente
        val app = application as MyApp
        if (app.ambiente == "01") {
            checkBoxConsumidorFinal.visibility = View.VISIBLE
            checkBoxCreditoFiscal.visibility = View.VISIBLE
        } else {
            checkBoxConsumidorFinal.visibility = View.GONE
            checkBoxCreditoFiscal.visibility = View.GONE
        }
    }


    override fun onPause() {
        super.onPause()
        saveCheckboxState()
    }



}