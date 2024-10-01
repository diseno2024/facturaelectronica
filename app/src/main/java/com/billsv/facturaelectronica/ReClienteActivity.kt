package com.billsv.facturaelectronica

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.DataSource
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.Database
import com.couchbase.lite.Expression
import com.couchbase.lite.Meta
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult
import android.text.InputFilter



class ReClienteActivity : AppCompatActivity() {
    private lateinit var spinnerDep: Spinner
    private lateinit var spinnerMun: Spinner
    private lateinit var nombre: EditText
    private lateinit var email: EditText
    private lateinit var direccion: EditText
    private lateinit var telefono: EditText
    private lateinit var dui: EditText
    private lateinit var nit: EditText
    private lateinit var agregarButton: Button
    private lateinit var cancelarButton: Button
    private lateinit var database: Database

    private val departamentosMap = mapOf(
        "Ahuachapán" to "01",
        "Santa Ana" to "02",
        "Sonsonate" to "03",
        "Chalatenango" to "04",
        "La Libertad" to "05",
        "San Salvador" to "06",
        "Cuscatlán" to "07",
        "La Paz" to "08",
        "Cabañas" to "09",
        "San Vicente" to "10",
        "Usulután" to "11",
        "San Miguel" to "12",
        "Morazán" to "13",
        "La Unión" to "14"
    )

    private val municipiosMap = mapOf(
        "Ahuachapán" to listOf(
            "Ahuachapán" to "01",
            "Apaneca" to "02",
            "Atiquizaya" to "03",
            "Concepción de Ataco" to "04",
            "El Refugio" to "05",
            "Guaymango" to "06",
            "Jujutla" to "07",
            "San Francisco Menéndez" to "08",
            "San Lorenzo" to "09",
            "San Pedro Puxtla" to "10",
            "Tacuba" to "11",
            "Turín" to "12"
        ),
        "Santa Ana" to listOf(
            "Candelaria de la Frontera" to "01",
            "Coatepeque" to "02",
            "Chalchuapa" to "03",
            "El Congo" to "04",
            "El Porvenir" to "05",
            "Masahuat" to "06",
            "Metapán" to "07",
            "San Antonio Pajonal" to "08",
            "San Sebastián Salitrillo" to "09",
            "Santa Ana" to "10",
            "Santa Rosa Guachipilin" to "11",
            "Santiago de la Frontera" to "12",
            "Texistepeque" to "13"
        ),
        "Sonsonate" to listOf(
            "Acajutla" to "01",
            "Armenia" to "02",
            "Caluco" to "03",
            "Cuisnahuat" to "04",
            "Santa Isabel Ishuatán" to "05",
            "Izalco" to "06",
            "Juayúa" to "07",
            "Nahuizalco" to "08",
            "Nahuilingo" to "09",
            "Salcoatitán" to "10",
            "San Antonio del Monte" to "11",
            "San Julián" to "12",
            "Santa Catarina Masahuat" to "13",
            "Santo Domingo de Guzmán" to "14",
            "Sonsonate" to "15",
            "Sonzacate" to "16"
        ),
        "Chalatenango" to listOf(
            "Agua Caliente" to "01",
            "Arcatao" to "02",
            "Azacualpa" to "03",
            "Citalá" to "04",
            "Comalapa" to "05",
            "Concepción Quezaltepeque" to "06",
            "Chalatenango" to "07",
            "Dulce Nombre de María" to "08",
            "El Carrizal" to "09",
            "El Paraíso" to "10",
            "La Laguna" to "11",
            "La Palma" to "12",
            "La Reina" to "13",
            "Las Vueltas" to "14",
            "Nombre de Jesús" to "15",
            "Nueva Concepción" to "16",
            "Nueva Trinidad" to "17",
            "Ojos de Agua" to "18",
            "Potonico" to "19",
            "San Antonio de La Cruz" to "20",
            "San Antonio Los Ranchos" to "21",
            "San Fernando" to "22",
            "San Francisco Lempa" to "23",
            "San Francisco Morazán" to "24",
            "San Ignacio" to "25",
            "San Isidro Labrador" to "26",
            "San José Cancasque" to "27",
            "San José Las Flores" to "28",
            "San Luis del Carmen" to "29",
            "San Miguel de Mercedes" to "30",
            "San Rafael" to "31",
            "Santa Rita" to "32",
            "Tejutla" to "33"
        ),
        "La Libertad" to listOf(
            "Antiguo Cuscatlán" to "01",
            "Ciudad Arce" to "02",
            "Colón" to "03",
            "Comasagua" to "04",
            "Chiltiupán" to "05",
            "Huizúcar" to "06",
            "Jayaque" to "07",
            "Jicalapa" to "08",
            "La Libertad" to "09",
            "Nuevo Cuscatlán" to "10",
            "Santa Tecla" to "11",
            "Quezaltepeque" to "12",
            "Sacacoyo" to "13",
            "San José Villanueva" to "14",
            "San Juan Opico" to "15",
            "San Pablo Tacachico" to "16",
            "Tamanique" to "17",
            "Talnique" to "18",
            "Teotepeque" to "19",
            "Tepecoyo" to "20",
            "Zaragoza" to "21"
        ),
        "San Salvador" to listOf(
            "Aguilares" to "01",
            "Apopa" to "02",
            "Ayutuxtepeque" to "03",
            "Cuscatancingo" to "04",
            "El Paisnal" to "05",
            "Guazapa" to "06",
            "Ilopango" to "07",
            "Mejicanos" to "08",
            "Nejapa" to "09",
            "Panchimalco" to "10",
            "Rosario de Mora" to "11",
            "San Marcos" to "12",
            "San Martin" to "13",
            "San Salvador" to "14",
            "Santiago Texacuangos" to "15",
            "Santo Tomas" to "16",
            "Soyapango" to "17",
            "Tonacatepeque" to "18",
            "Ciudad Delgado" to "19"
        ),
        "Cuscatlán" to listOf(
            "Candelaria" to "01",
            "Cojutepeque" to "02",
            "El Carmen" to "03",
            "El Rosario" to "04",
            "Monte San Juan" to "05",
            "Oratorio de Concepción" to "06",
            "San Bartolomé Perulapía" to "07",
            "San Cristóbal" to "08",
            "San José Guayabal" to "09",
            "San Pedro Perulapán" to "10",
            "San Rafael Cedros" to "11",
            "San Ramón" to "12",
            "Santa Cruz Analquito" to "13",
            "Santa Cruz Michapa" to "14",
            "Suchitoto" to "15",
            "Tenancingo" to "16"
        ),
        "La Paz" to listOf(
            "Cuyultitán" to "01",
            "El Rosario" to "02",
            "Jerusalén" to "03",
            "Mercedes La Ceiba" to "04",
            "Olocuilta" to "05",
            "Paraíso de Osorio" to "06",
            "San Antonio Masahuat" to "07",
            "San Emigdio" to "08",
            "San Francisco Chinameca" to "09",
            "San Juan Nonualco" to "10",
            "San Juan Talpa" to "11",
            "San Juan Tepezontes" to "12",
            "San Luis Talpa" to "13",
            "San Miguel Tepezontes" to "14",
            "San Pedro Masahuat" to "15",
            "San Pedro Nonualco" to "16",
            "San Rafael Obrajuelo" to "17",
            "Santa María Ostuma" to "18",
            "Santiago Nonualco" to "19",
            "Tapalhuaca" to "20",
            "Zacatecoluca" to "21",
            "San Luis La Herradura" to "22",
            "Cinquera" to "23",
            "Guacotecti" to "24"
        ),
        "Cabañas" to listOf(
            "Cinquera" to "01",
            "Guacotecti" to "02",
            "Ilobasco" to "03",
            "Jutiapa" to "04",
            "San Isidro" to "05",
            "Sensuntepeque" to "06",
            "Tejutepeque" to "07",
            "Victoria" to "08",
            "Dolores" to "09"
        ),
        "San Vicente" to listOf(
            "Apastepeque" to "01",
            "Guadalupe" to "02",
            "San Cayetano Istepeque" to "03",
            "Santa Clara" to "04",
            "Santo Domingo" to "05",
            "San Esteban Catarina" to "06",
            "San Ildefonso" to "07",
            "San Lorenzo" to "08",
            "San Sebastián" to "09",
            "San Vicente" to "10",
            "Tecoluca" to "11",
            "Tepetitán" to "12",
            "Verapaz" to "13"
        ),
        "Usulután" to listOf(
            "Alegría" to "01",
            "Berlín" to "02",
            "California" to "03",
            "Concepción Batres" to "04",
            "El Triunfo" to "05",
            "Ereguayquín" to "06",
            "Estanzuelas" to "07",
            "Jiquilisco" to "08",
            "Jucuapa" to "09",
            "Jucuarán" to "10",
            "Mercedes Umaña" to "11",
            "Nueva Granada" to "12",
            "Ozatlán" to "13",
            "Puerto El Triunfo" to "14",
            "San Agustín" to "15",
            "San Buenaventura" to "16",
            "San Dionisio" to "17",
            "Santa Elena" to "18",
            "San Francisco Javier" to "19",
            "Santa María" to "20",
            "Santiago de María" to "21",
            "Tecapán" to "22",
            "Usulután" to "23"
        ),
        "San Miguel" to listOf(
            "Carolina" to "01",
            "Ciudad Barrios" to "02",
            "Comacarán" to "03",
            "Chapeltique" to "04",
            "Chinameca" to "05",
            "Chirilagua" to "06",
            "El Tránsito" to "07",
            "Lolotique" to "08",
            "Moncagua" to "09",
            "Nueva Guadalupe" to "10",
            "Nuevo Edén de San Juan" to "11",
            "Quelepa" to "12",
            "San Antonio del Mosco" to "13",
            "San Gerardo" to "14",
            "San Jorge" to "15",
            "San Luis de la Reina" to "16",
            "San Miguel" to "17",
            "San Rafael Oriente" to "18",
            "Sesori" to "19",
            "Uluazapa" to "20"
        ),
        "Morazán" to listOf(
            "Arambala" to "01",
            "Cacaopera" to "02",
            "Corinto" to "03",
            "Chilanga" to "04",
            "Delicias de Concepción" to "05",
            "El Divisadero" to "06",
            "El Rosario" to "07",
            "Gualococti" to "08",
            "Guatajiagua" to "09",
            "Joateca" to "10",
            "Jocoaitique" to "11",
            "Jocoro" to "12",
            "Lolotiquillo" to "13",
            "Meanguera" to "14",
            "Osicala" to "15",
            "Perquín" to "16",
            "San Carlos" to "17",
            "San Fernando" to "18",
            "San Francisco Gotera" to "19",
            "San Isidro" to "20",
            "San Simón" to "21",
            "Sensembra" to "22",
            "Sociedad" to "23",
            "Torola" to "24",
            "Yamabal" to "25",
            "Yoloaiquín" to "26"
        ),
        "La Unión" to listOf(
            "Anamoros" to "01",
            "Bolívar" to "02",
            "Concepción de Oriente" to "03",
            "Conchagua" to "04",
            "El Carmen" to "05",
            "El Sauce" to "06",
            "Intipucá" to "07",
            "La Unión" to "08",
            "Lislique" to "09",
            "Meanguera del Golfo" to "10",
            "Nueva Esparta" to "11",
            "Pasaquina" to "12",
            "Polorós" to "13",
            "San Alejo" to "14",
            "San José" to "15",
            "Santa Rosa de Lima" to "16",
            "Yayantique" to "17",
            "Yucuaiquín" to "18"
        )
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_re_cliente)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializa el Spinner de departamento
        spinnerDep = findViewById(R.id.departamento)
        val departamentos = departamentosMap.keys.toTypedArray()

        // Configura el adaptador para el Spinner de departamento
        val adapterDep = ArrayAdapter(this, R.layout.spinner_personalizado, departamentos)
        adapterDep.setDropDownViewResource(R.layout.spinner_dropdown_per)
        spinnerDep.adapter = adapterDep

        // Inicializa el Spinner de municipio
        spinnerMun = findViewById(R.id.municipio)
        val initialMunicipios = arrayOf("Seleccione un municipio")

        // Configura el adaptador para el Spinner de municipio
        val adapterMun = ArrayAdapter(this, R.layout.spinner_personalizado, initialMunicipios)
        adapterMun.setDropDownViewResource(R.layout.spinner_dropdown_per)
        spinnerMun.adapter = adapterMun

        // listener para el Spinner de departamento
        spinnerDep.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedDept = parent.getItemAtPosition(position).toString()
                val municipios = municipiosMap[selectedDept]?.map { it.first } ?: listOf("Seleccione un municipio")
                val adapterMun = ArrayAdapter(this@ReClienteActivity, R.layout.spinner_personalizado, municipios)
                adapterMun.setDropDownViewResource(R.layout.spinner_dropdown_per)
                spinnerMun.adapter = adapterMun
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No hace nada
            }
        }

        // Inicializar vistas
        nombre = findViewById(R.id.nombre)
        email = findViewById(R.id.correo)
        spinnerDep = findViewById(R.id.departamento)
        spinnerMun = findViewById(R.id.municipio)
        direccion = findViewById(R.id.complemento)
        telefono = findViewById(R.id.telefono)
        agregarButton = findViewById(R.id.btnAgregar)
        cancelarButton = findViewById(R.id.btnCancelar)
        dui = findViewById(R.id.dui)
        nit = findViewById(R.id.nit)


        // Agregar TextWatcher para el campo de teléfono
        telefono.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val mask = "####-####"

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return

                isUpdating = true
                val formatted = formatPhoneNumber(s.toString())
                telefono.setText(formatted)
                telefono.setSelection(formatted.length)
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
        //mascara del dui
        dui.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val mask = "########-#" // La máscara del DUI

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return

                isUpdating = true
                val formatted = formatDui(s.toString())
                dui.setText(formatted)
                dui.setSelection(formatted.length)
                isUpdating = false
            }

            private fun formatDui(dui: String): String {
                // Eliminar todos los caracteres no numéricos del DUI
                val digits = dui.replace(Regex("\\D"), "")
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
        nit.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val mask = "####-######-###-#" // La máscara del NIT

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return

                isUpdating = true
                val formatted = formatNIT(s.toString())
                nit.setText(formatted)
                nit.setSelection(formatted.length)
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


        val Check: CheckBox = findViewById(R.id.checkGuardar)
        //val isCheck = false
        Check.isChecked
        /*Check.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // El CheckBox está marcado
                // Realiza las acciones que desees cuando se marque
                // Por ejemplo, muestra un mensaje o actualiza algún dato
                // Puedes usar "isChecked" para saber si está marcado o no
            } else {
                // El CheckBox está desmarcado
                // Realiza las acciones que desees cuando se desmarque
            }
        }*/
        //datos a pasar
        // Configurar evento de clic para el botón "Agregar"
        agregarButton.setOnClickListener {
            if (validarEntradas() && Check.isChecked) {
                guardarInformacion()
                val duivalue= dui.text.toString().replace("-","")
                // Iniciar otra actividad
                val intent = Intent(this, EmitirCFActivity::class.java)
                // Crear un Intent y agregar los datos
                intent.putExtra("dui", duivalue)
                intent.putExtra("letra","P")
                startActivity(intent)
                // Limpiar los EditText
                nombre.text.clear()
                email.text.clear()
                direccion.text.clear()
                telefono.text.clear()
                dui.text.clear()
                nit.text.clear()
                finish()
            }else if(validarEntradas()){
                guardarInfoTemporal()
                val duivalue= dui.text.toString().replace("-","")
                // Iniciar otra actividad
                val intent = Intent(this, EmitirCFActivity::class.java)
                // Crear un Intent y agregar los datos
                intent.putExtra("dui", duivalue)
                intent.putExtra("letra","T")
                startActivity(intent)
                // Limpiar los EditText
                nombre.text.clear()
                email.text.clear()
                direccion.text.clear()
                telefono.text.clear()
                dui.text.clear()
                nit.text.clear()
                finish()
            }
        }

        cancelarButton.setOnClickListener {
            // Iniciar otra actividad
            val intent = Intent(this, EmitirCFActivity::class.java)
            startActivity(intent)
        }

        // Inicializar la base de datos
        val app = application as MyApp
        database = app.database

        // Filtro personalizado para convertir todas las letras a minúsculas
        val lowerCaseFilter = InputFilter { source, start, end, dest, dstart, dend ->
            val result = StringBuilder()
            for (i in start until end) {
                val char = source[i]
                if (char.isUpperCase()) {
                    result.append(char.lowercaseChar())
                } else {
                    result.append(char)
                }
            }
            result.toString()
        }

        // Aplicar el filtro al campo de correo electrónico
        email.filters = arrayOf(lowerCaseFilter)

    }

    private fun validarEntradas(): Boolean {
        val app = application as MyApp///////
        val database = app.database///////
        val nombreText = nombre.text.toString()
        val emailText = email.text.toString()
        val direccionText = direccion.text.toString()
        val telefonoText = telefono.text.toString().replace("-", "")
        val duiText=dui.text.toString().replace("-","")
        val nitText=nit.text.toString().replace("-","")

        //////
        // Verifica que todos los campos estén llenos
        if (nombreText.isEmpty() || emailText.isEmpty() ||  telefonoText.isEmpty()) {
            Toast.makeText(this, "Llene todos los campos necesarios", Toast.LENGTH_SHORT).show()
            return false
        }else if (nitText.isEmpty() && duiText.isEmpty()) {
            Toast.makeText(this, "Debe proporcionar NIT o DUI", Toast.LENGTH_SHORT).show()
            return false
        }
        // Verifica que el DUI sea un número válido de 9 dígitos solo si no está vacío
        if (duiText.isNotEmpty() && !duiText.matches(Regex("\\d{9}"))) {
            Toast.makeText(this, "DUI debe ser un número válido de 9 dígitos", Toast.LENGTH_SHORT).show()
            return false
        }
        // Verifica que el NIT sea un número válido
        if (nitText.isNotEmpty() && !nitText.matches(Regex("\\d{14}"))) {
            Toast.makeText(this, "NIT debe ser un número válido", Toast.LENGTH_SHORT).show()
            return false
        }
        // Verifica que el correo electrónico tenga un formato válido
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            Toast.makeText(this, "Correo electrónico no válido", Toast.LENGTH_SHORT).show()
            return false
        }
        // Verifica que el teléfono sea un número válido de 8 dígitos
        if (!telefonoText.matches(Regex("\\d{8}"))) {
            Toast.makeText(this, "Teléfono debe ser un número válido de 8 dígitos", Toast.LENGTH_SHORT).show()
            return false
        }
        if(nitText.isNotEmpty() && duiText.isEmpty()){
            val query2 = QueryBuilder.select(SelectResult.expression(Meta.id))
                .from(DataSource.database(database))
                .where(Expression.property("nit").equalTo(Expression.string(nitText)))

            try {
                val resultSet = query2.execute()
                val results = resultSet.allResults()

                if (results.isNotEmpty()) {
                    Log.d("Re_Cliente", "Datos actualizados correctamente")
                    showToast("Ya existe un cliente con ese NIT")
                    return false
                } else {
                    Log.d("Re_Cliente", "PASS")
                }
            } catch (e: CouchbaseLiteException) {
                Log.e("Re_Cliente", "Error al actualizar el documento: ${e.message}", e)
                showToast("Error al buscar el NIT")
            }
        }else if(duiText.isNotEmpty() && nitText.isEmpty()){
            val query = QueryBuilder.select(SelectResult.expression(Meta.id))
                .from(DataSource.database(database))
                .where(Expression.property("dui").equalTo(Expression.string(duiText)))

            try {
                val resultSet = query.execute()
                val results = resultSet.allResults()

                if (results.isNotEmpty()) {
                    Log.d("Prin_Re_Cliente", "Datos actualizados correctamente")
                    showToast("Ya existe un cliente con ese dui")
                    return false
                } else {
                    Log.d("Prin_Re_Cliente", "PASS")
                }
            } catch (e: CouchbaseLiteException) {
                Log.e("Prin_Re_Cliente", "Error al actualizar el documento: ${e.message}", e)
                showToast("Error al buscar el dui")
            }
        }else if(duiText.isNotEmpty() && nitText.isNotEmpty()){
            val query = QueryBuilder.select(SelectResult.expression(Meta.id))
                .from(DataSource.database(database))
                .where(Expression.property("dui").equalTo(Expression.string(duiText)))

            try {
                val resultSet = query.execute()
                val results = resultSet.allResults()

                if (results.isNotEmpty()) {
                    Log.d("Prin_Re_Cliente", "Datos actualizados correctamente")
                    showToast("Ya existe un cliente con ese dui")
                    return false
                } else {
                    Log.d("Prin_Re_Cliente", "PASS")
                }
            } catch (e: CouchbaseLiteException) {
                Log.e("Prin_Re_Cliente", "Error al actualizar el documento: ${e.message}", e)
                showToast("Error al buscar el dui")
            }
            val query2 = QueryBuilder.select(SelectResult.expression(Meta.id))
                .from(DataSource.database(database))
                .where(Expression.property("nit").equalTo(Expression.string(nitText)))

            try {
                val resultSet = query2.execute()
                val results = resultSet.allResults()

                if (results.isNotEmpty()) {
                    Log.d("Re_Cliente", "Datos actualizados correctamente")
                    showToast("Ya existe un cliente con ese NIT")
                    return false
                } else {
                    Log.d("Re_Cliente", "PASS")
                }
            } catch (e: CouchbaseLiteException) {
                Log.e("Re_Cliente", "Error al actualizar el documento: ${e.message}", e)
                showToast("Error al buscar el NIT")
            }
        }
        return true
    }

    private fun guardarInformacion() {
        val nombreText = nombre.text.toString()
        val emailText = email.text.toString()
        val nitText = nit.text.toString().replace("-", "")
        val direccionText = direccion.text.toString()
        val departamentoText = spinnerDep.selectedItem.toString()
        val municipioText = spinnerMun.selectedItem.toString()
        val telefonoText = telefono.text.toString().replace("-", "")
        val departamentoCodigo = departamentosMap[departamentoText]
        val municipioCodigo = municipiosMap[departamentoText]?.firstOrNull { it.first == municipioText }?.second
        val duiText=dui.text.toString().replace("-", "")
        val telefonoMostrar = telefono.text.toString()
        val duiMostrar=dui.text.toString()
        val nitMostrar = nit.text.toString()
        // Crear un documento mutable para guardar en la base de datos
        if (departamentoCodigo != null && municipioCodigo != null) {
            // Crear un documento mutable para guardar en la base de datos
            val document = MutableDocument()
                .setString("nombre", nombreText)
                .setString("email", emailText)
                .setString("direccion", direccionText)
                .setString("departamento", departamentoCodigo)
                .setString("municipio", municipioCodigo)
                .setString("telefono", telefonoText)
                .setString("dui",duiText)
                .setString("tipo", "cliente")
                .setString("tipoCliente","Consumidor Final")
                .setString("telefonoM",telefonoMostrar)
                .setString("duiM",duiMostrar)
                .setString("municipioT",municipioText)
                .setString("departamentoT",departamentoText)
                .setString("nit",nitText)
                .setString("nitM",nitMostrar)


            try {
                // Guardar el documento en la base de datos
                database.save(document)
                Log.d("ReClienteActivity", "Datos guardados correctamente: \n $document")
                Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
            } catch (e: CouchbaseLiteException) {
                Log.e(
                    "ReClienteActivity",
                    "Error al guardar los datos en la base de datos: ${e.message}",
                    e
                )
                Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(
                this,
                "Error: Código de departamento o municipio no válido",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    private fun guardarInfoTemporal() {
        val nombreText = nombre.text.toString()
        val emailText = email.text.toString()
        val nitText = nit.text.toString().replace("-", "")
        val direccionText = direccion.text.toString()
        val departamentoText = spinnerDep.selectedItem.toString()
        val municipioText = spinnerMun.selectedItem.toString()
        val telefonoText = telefono.text.toString().replace("-", "")
        val departamentoCodigo = departamentosMap[departamentoText]
        val municipioCodigo = municipiosMap[departamentoText]?.firstOrNull { it.first == municipioText }?.second
        val duiText=dui.text.toString().replace("-", "")
        val telefonoMostrar = telefono.text.toString()
        val duiMostrar=dui.text.toString()
        val nitMostrar = nit.text.toString()
        // Crear un documento mutable para guardar en la base de datos
        if (departamentoCodigo != null && municipioCodigo != null) {
            // Crear un documento mutable para guardar en la base de datos
            val document = MutableDocument()
                .setString("nombre", nombreText)
                .setString("email", emailText)
                .setString("direccion", direccionText)
                .setString("departamento", departamentoCodigo)
                .setString("municipio", municipioCodigo)
                .setString("telefono", telefonoText)
                .setString("dui",duiText)
                .setString("tipo", "clientetemporal")
                .setString("tipoCliente","Consumidor Final")
                .setString("telefonoM",telefonoMostrar)
                .setString("duiM",duiMostrar)
                .setString("municipioT",municipioText)
                .setString("departamentoT",departamentoText)
                .setString("nit",nitText)
                .setString("nitM",nitMostrar)


            try {
                // Guardar el documento en la base de datos
                database.save(document)
                Log.d("ReClienteActivity", "Datos guardados correctamente: \n $document")
                Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
            } catch (e: CouchbaseLiteException) {
                Log.e(
                    "ReClienteActivity",
                    "Error al guardar los datos en la base de datos: ${e.message}",
                    e
                )
                Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(
                this,
                "Error: Código de departamento o municipio no válido",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    override fun onBackPressed() {
        super.onBackPressed() // Llama al método onBackPressed() de la clase base
        val intent = Intent(this, EmitirCFActivity::class.java)

        startActivity(intent)
        finish()
    }
}
