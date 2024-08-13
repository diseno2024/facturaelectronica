package com.billsv.facturaelectronica

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
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
import android.text.InputFilter
import android.util.Base64.encodeToString
import androidx.annotation.RequiresApi

import com.billsv.signer.*;
import java.io.ByteArrayInputStream
import java.security.Key
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Base64

class InfoEmisorActivity : AppCompatActivity() {
    private lateinit var database: Database
    private lateinit var nombre: EditText
    private lateinit var nombreC: EditText
    private lateinit var DUI: EditText
    private lateinit var NIT: EditText
    private lateinit var NRC: EditText
    private lateinit var AcEco: EditText
    private lateinit var Direccion: EditText
    private lateinit var NumT: EditText
    private lateinit var Correo: EditText
    private lateinit var spinnerDep: Spinner
    private lateinit var spinnerMun: Spinner
    private val REQUEST_CODE_IMAGE_PICKER = 123
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
                val adapterMun = ArrayAdapter(this@InfoEmisorActivity, R.layout.spinner_personalizado, municipios)
                adapterMun.setDropDownViewResource(R.layout.spinner_dropdown_per)
                spinnerMun.adapter = adapterMun
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No hace nada
            }
        }
        ///////////------->
        if (verificar()) {
            val dataList = obtenerDatosGuardados()
            dataList.forEach { data ->
                val dato = data.split("\n")
                val spinnerDepd = dato[9]
                val spinnerMund = dato[10]

                val departamento = departamentosMap.entries.find { it.value == spinnerDepd }?.key
                if (departamento != null) {
                    val index = departamentos.indexOf(departamento)
                    if (index != -1) {
                        spinnerDep.setSelection(index)
                    } else {
                        Log.d("ReClienteActivity", "Departamento no encontrado en el Spinner: $departamento")
                    }
                } else {
                    Log.e("ReClienteActivity", "Código de departamento no válido: $spinnerDepd")
                }

                spinnerDep.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        val selectedDept = parent.getItemAtPosition(position).toString()
                        val municipios = municipiosMap[selectedDept]?.map { it.first } ?: listOf("Seleccione un municipio")
                        val adapterMun = ArrayAdapter(this@InfoEmisorActivity, R.layout.spinner_personalizado, municipios)
                        adapterMun.setDropDownViewResource(R.layout.spinner_dropdown_per)
                        spinnerMun.adapter = adapterMun

                        // Establecer la selección del municipio una vez que el Spinner de municipios está actualizado
                        val municipio = municipiosMap[selectedDept]?.find { it.second == spinnerMund }?.first
                        if (municipio != null) {
                            val index = municipios.indexOf(municipio)
                            if (index != -1) {
                                spinnerMun.setSelection(index)
                            } else {
                                Log.e("ReClienteActivity", "Municipio no encontrado en el Spinner: $municipio")
                            }
                        } else {
                            Log.e("ReClienteActivity", "Código de municipio no válido: $spinnerMund")
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // No hace nada
                    }
                }
            }
        }

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
        DUI = findViewById(R.id.DUI)
        NIT = findViewById(R.id.NIT)
        NRC = findViewById(R.id.NRC)
        AcEco = findViewById(R.id.AcEco)
        Direccion = findViewById(R.id.Direccion)
        spinnerDep = findViewById(R.id.departamento)
        spinnerMun = findViewById(R.id.municipio)
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
        DUI.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val mask = "########-#" // La máscara del NIT

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return

                isUpdating = true
                val formatted = formatDui(s.toString())
                DUI.setText(formatted)
                DUI.setSelection(formatted.length)
                isUpdating = false
            }

            private fun formatDui(dui: String): String {
                // Eliminar todos los caracteres no numéricos del NIT
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
        val clave = intent.getStringExtra("clave")
        if(clave=="faltacf" || clave=="faltaccf"){
            btnAtras.visibility = View.GONE
        }
        val btnGuardar: Button = findViewById(R.id.Guardar)
        btnGuardar.setOnClickListener {
            if (validarEntradas()){
                if(clave=="faltacf"){
                    guardarInformacion()
                    recreate()
                    val intent = Intent(this,EmitirCFActivity::class.java)
                    startActivity(intent)
                    finish()
                }else if(clave=="faltaccf"){
                    guardarInformacion()
                    recreate()
                    val intent = Intent(this,EmitirCCFActivity::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    guardarInformacion()
                    recreate()
                }
                /*val intent = Intent(this, MenuActivity::class.java)
                startActivity(intent)*/
            }
        }
        val btnEditar: Button = findViewById(R.id.Editar)
        btnEditar.setOnClickListener {
            habilitaredicion()
        }

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
        Correo.filters = arrayOf(lowerCaseFilter)
    }

    private fun habilitaredicion() {

        val nombre: EditText = findViewById(R.id.nombre)
        val nombrec: EditText = findViewById(R.id.nombreC)
        val dui: EditText = findViewById(R.id.DUI)
        val nit: EditText = findViewById(R.id.NIT)
        val nrc: EditText = findViewById(R.id.NRC)
        val AcEco: EditText = findViewById(R.id.AcEco)
        val spinnerDep: Spinner = findViewById(R.id.departamento)
        val spinnerMun: Spinner = findViewById(R.id.municipio)
        val direccion: EditText = findViewById(R.id.Direccion)
        val NumT: EditText = findViewById(R.id.NumT)
        val correo: EditText = findViewById(R.id.Correo)

        //pone el efectoclick
        nombre.isEnabled = true
        nombrec.isEnabled = true
        dui.isEnabled = true
        nit.isEnabled = true
        nrc.isEnabled = true
        AcEco.isEnabled = true
        spinnerDep.isEnabled = true
        spinnerMun.isEnabled = true
        direccion.isEnabled = true
        NumT.isEnabled = true
        correo.isEnabled = true

        val boton: Button = findViewById(R.id.Guardar)
        boton.visibility = View.VISIBLE
        val boton2: Button = findViewById(R.id.Editar)
        boton2.visibility = View.GONE

    }

    private fun verificar(): Boolean {
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
                return true
            }

        }
        return false
    }


    private fun mostrardata(dataList: List<String>) {
        //buscar los edittext
        val nombre: EditText = findViewById(R.id.nombre)
        val nombrec: EditText = findViewById(R.id.nombreC)
        val dui: EditText = findViewById(R.id.DUI)
        val nit: EditText = findViewById(R.id.NIT)
        val nrc: EditText = findViewById(R.id.NRC)
        val AcEco: EditText = findViewById(R.id.AcEco)
        val spinnerDep: Spinner = findViewById(R.id.departamento)
        val spinnerMun: Spinner = findViewById(R.id.municipio)
        val direccion: EditText = findViewById(R.id.Direccion)
        val NumT: EditText = findViewById(R.id.NumT)
        val correo: EditText = findViewById(R.id.Correo)
        dataList.forEach { data ->
            //sacar la data
            val datos = data.split("\n")
            val nombredato = datos[0]
            val nombrecdato = datos[1]
            val duidato = datos[8]
            val nitdato = datos[2]
            val nrcdato = datos[3]
            val AcEcodato = datos[4]
            val direcciondato = datos[5]
            val Numtdato = datos[6]
            val correodato = datos[7]

            //pasar la data a los edittext
            nombre.setText(nombredato)
            nombrec.setText(nombrecdato)
            dui.setText(duidato)
            nit.setText(nitdato)
            nrc.setText(nrcdato)
            AcEco.setText(AcEcodato)
            direccion.setText(direcciondato)
            NumT.setText(Numtdato)
            correo.setText(correodato)

            //quita el efectoclick
            nombre.isEnabled = false
            nombrec.isEnabled = false
            dui.isEnabled = false
            nit.isEnabled = false
            nrc.isEnabled = false
            AcEco.isEnabled = false
            spinnerDep.isEnabled = false
            spinnerMun.isEnabled = false
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
        val dui = DUI.text.toString()
        val nit = NIT.text.toString()
        val nrc = NRC.text.toString()
        val AcEco = AcEco.text.toString()
        val departamento = spinnerDep.selectedItem.toString()
        val municipio = spinnerMun.selectedItem.toString()
        val departamentoCodigo = departamentosMap[departamento]
        val municipioCodigo = municipiosMap[departamento]?.firstOrNull { it.first == municipio }?.second
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
                .setString("dui", dui)
                .setString("nit", nit)
                .setString("nrc", nrc)
                .setString("ActividadEco", AcEco)
                .setString("departamento", departamentoCodigo)
                .setString("municipio", municipioCodigo)
                .setString("direccion", direccion)
                .setString("telefono", telefono)
                .setString("correo", correo)
                .setString("deptext", departamento)
                .setString("muntext", municipio)
                .setString("tipo", "ConfEmisor")

            // Guardar el nuevo documento
            database.save(document)
            Log.d("ReClienteActivity", "Datos guardados correctamente: \n $document")
            generarClavesyCertificado();
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
            val dui = dict?.getString("dui")
            val nit = dict?.getString("nit")
            val nrc = dict?.getString("nrc")
            val AcEco = dict?.getString("ActividadEco")
            val departamento = dict?.getString("departamento")
            val municipio = dict?.getString("municipio")
            val direccion = dict?.getString("direccion")
            val telefono = dict?.getString("telefono")
            val correo = dict?.getString("correo")
            val deptext = dict?.getString("deptext")
            val muntext = dict?.getString("muntext")

            // Formatea los datos como una cadena y la agrega a la lista
            val dataString = "$nombre\n$nombrec\n$nit\n$nrc\n$AcEco\n$direccion\n$telefono\n$correo\n$dui\n$departamento\n$municipio\n$deptext\n$muntext"
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
        val duiText = DUI.text.toString().replace("-", "")
        val nitText = NIT.text.toString().replace("-", "")
        val nrcText = NRC.text.toString()
        val AcEco = AcEco.text.toString()
        val direccionText = Direccion.text.toString()
        val telefonoText = NumT.text.toString().replace("-", "")
        val emailText = Correo.text.toString()

        // Verifica que todos los campos estén llenos
        if (nombreText.isEmpty() || emailText.isEmpty() || telefonoText.isEmpty() || AcEco.isEmpty() || nombrecText.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return false
        } else if (nitText.isEmpty() && duiText.isEmpty()) {
            Toast.makeText(this, "Debe proporcionar NIT o DUI", Toast.LENGTH_SHORT).show()
            return false
        }

        // Verifica que el correo electrónico tenga un formato válido
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            Toast.makeText(this, "Correo electrónico no válido", Toast.LENGTH_SHORT).show()
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

    private fun generarClavesyCertificado() {
        val userInfo = obtenerInfoEmisor(database)
        Log.d("InfoEmisorActivity", "Entran a generarClavesyCertificado")
        userInfo?.let {
            try {
                val keyPair = generadorClaves.generarParClaves()
                Log.d("InfoEmisorActivity", "Llaves generadas")
                val certificate = generadorClaves.generarCertificado(keyPair, it)
                Log.d("InfoEmisorActivity", "Certificado generado")

                // Guardar claves y certificado en el Keystore
                val keystoreManager = guardarClavesEnKeystore()
                keystoreManager.guardarClavesEnKeystore(keyPair, certificate)
                Log.d("InfoEmisorActivity", "Llaves y certificado guardados en el Keystore")

                // Imprimir certificado en formato X.509
                Log.d("InfoEmisorActivity", "Imprimir certificado en formato X.509")
                val encodedCert = certificate.toString()
                Log.d("InfoEmisorActivity", "Certificado: \n$encodedCert")

                // Imprimir llave pública y privada en formato hexadecimal
                Log.d("InfoEmisorActivity", "Imprimir llave pública en formato hexadecimal")
                val encodedPublicKey = toHex(keyPair.public.encoded)
                Log.d("InfoEmisorActivity", "Llave pública: \n$encodedPublicKey")

                Log.d("InfoEmisorActivity", "Imprimir llave privada en formato hexadecimal")
                val encodedPrivateKey = toHex(keyPair.private.encoded)
                Log.d("InfoEmisorActivity", "Llave privada: \n$encodedPrivateKey")
            } catch (e: Exception) {
                Log.e("InfoEmisorActivity", "Error al generar claves y certificado: ${e.message}")
            }
        } ?: run {
            Log.e("InfoEmisorActivity", "No se encontró información del emisor")
        }
    }

    private fun toHex(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }
}