package com.example.facturaelectronica

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.Database

class PrinReClienteActivity : AppCompatActivity() {
    private lateinit var spinnerDep: Spinner
    private lateinit var spinnerMun: Spinner
    private lateinit var nombre: EditText
    private lateinit var nit: EditText
    private lateinit var email: EditText
    private lateinit var direccion: EditText
    private lateinit var telefono: EditText
    private lateinit var departamento: Spinner
    private lateinit var municipio: Spinner
    private lateinit var agregarButton: Button
    private lateinit var database: Database
    private lateinit var regresar: ImageButton

    private val municipiosMap = mapOf(
        "Ahuachapán" to listOf("Ahuachapán", "Apaneca", "Atiquizaya", "Concepción de Ataco",
            "El Refugio", "Guaymango", "Jujutla", "San Francisco Menéndez", "San Lorenzo",
            "San Pedro Puxtla", "Tacuba", "Turín"),
        "Santa Ana" to listOf("Candelaria de la Frontera","Coatepeque","Chalchuapa","El Congo",
            "El Porvenir", "Masahuat","Metapán","San Antonio Pajonal","San Sebastián Salitrillo",
            "Santa Ana","Sta Rosa Guachipilin","Stgo de la Frontera","Texistepeque"),
        "Sonsonate" to listOf("Acajutla","Armenia","Caluco","Cusinahuat","Sta Ishuatan","Izalco",
            "Juayúa","Nahuizalco", "Nahuilingo","Salcoatitan","San Antonio del Monte","San Julián",
            "Sta C Masahuat","Santo Domingo Gúzman", "Sonsonate","Sonzacate"),
        "Chalatenango" to listOf("Agua Caliente","Arcatao","Azacualpa","Citalá","Comalapa",
            "Concepción Quezaltepeque","Chalatenango","Dulce Nombre de María","El Carrizal", "EL paraíso",
            "La Laguna","La Palma","La Reina","Las Vueltas", "Nombre de Jesus","Nueva Concepción",
            "Nueva Trinidad","Ojos de Agua","Potonico","San Antonio de La Cruz","San Ant Ranchos",
            "San Fernando","San Francisco Lempa","San Francisco Morazán","San Ingnacio","San I Labrador",
            "San J Cancasque", "San Jose Flores","San Luis Carmen","San Mig Mercedes","San Rafael",
            "Santa Rita", "Tejutla"),
        "La Libertad" to listOf("Antiguo Cuscatlán", "Ciudad Arce","Colón","Comasagua", "Chiltupan",
            "Huizúcar","Jayaque","Jicalapa","La Libertad","Nuevo Cuscatlán","Santa Tecla","Quezaltepeque",
            "Sacacoyo","San J Villanueva","San Juan Opico","San P Tacachico","Tamanique","Talnique",
            "Teotepeque","Tepecoyo","Zaragoza"),
        "San Salvador" to listOf("Aguilares","Apopa","Ayutuxtepeque","Cuscatancingo","El Paisnal",
            "Guazapa","Ilopango","Mejicanos","Nejapa","Panchimalco","Rosario de Mora","San Marcos",
            "San Martin","San Salvador","Stg Texacuangos","Santo Tomas","Soyapango","Tonacatepeque",
            "Ciudad Delgado"),
        "Cuscatlan" to listOf("Candelaria","Cojutepeque","El Carmen","El Rosario","Monte San Juan",
            "Orat Concepción","San B PeruliapA","San Cristóbal","San J Guayabal","San P Perulapán",
            "San Raf Cedros","San Ramón","Sta C Analquito","Sta C Michapa","Suchitoto","Tenancingo"),
        "La Paz" to listOf("Cuyultitán","El Rosario","Jesuralén","Merced La Ceiba","Olocuilta",
            "Paraíso Osorio", "San Ant Masahuat","San Emigdio","San Fco Chinamec","San J Nonualco",
            "San Juan Talpa","Sam Juan Tepezontes","San Juan Luis Talpa","San Miguel Tepezontes",
            "San pedro Masahuat","San Pedro Nonualco","San R Obrajuela","Sta Ma Ostuma","Stgo Nonualco",
            "Tapalhuaca","Zacatecoluca","San Luis La Herr","Cinquera", "Guacotecti"),
        "Cabañas" to listOf("Cinquera","Guacotecti","Ilobasco","Jutiapa","San Isidro","Sensuntepeque",
            "Tejutepeque","Victoria","Dolores"),
        "San Vicente" to listOf("Apastepeque","Guadalupe","San Cay Istepeq","Santa Clara",
            "Santo Domingo","SN EST Cartarina","San Ildefonso","San Lorenzo", "San Sebastián",
            "San Vicente","Tecoluca","Tepetitán","Verapaz"),
        "Usulatán" to listOf("Alegría","Berlín","California","Concep Batres","El Triunfo","Ereguayquín",
            "Estanzuelas","Jiquilisco","Jucuapa","Jucuarán","Mercedes Umaña","Nueva Granada",
            "Ozatlán","Puerto El Triunfo","San Agustín","SN Buenaventura","San Dionisio","Santa Elena",
            "San FCO Javier","Santa María","Santiago De María","Tecapán","Usulután"),
        "San Miguel" to listOf("Carolina","Ciudad Barrios","Comacarán","Chapeltique","Chinameca",
            "Chirilagua","El Transito","Lolotique","Moncagua","Nueva Guadalupe","Nuevo Edén San Juan",
            "Quelepa","San Antonio De Mosco","San Gerardo","San Jorge","San Luis Reina","San Miguel",
            "San Rafael Oriente","Sesori","Uluazapa"),
        "Morazán" to listOf("Arambala","Cacaopera","Corinto","Chilanga","Delic De Concep","El Divisadero","El Rosario",
            "Gualococti","Guatajiagua","Joateca","Jocoaitique","Jocoro","Lolotiquillo","Meanguera",
            "Osicala","Perquín","San Carlos","San Fernando","San Francisco Gotera","San Isidro",
            "San Simón","Sensembra","Sociedad","Torola","Yamabal","Yoloaiquín"),
        "La Unión" to listOf("Anamoros","Bolívar","Concep De OTE","Conchagua","El Carmen","El Sauce",
            "Intipucá","La Unión","Lislique","Meang Del Golfo","Nueva Esparta","Pasaquina","Polorós",
            "San Alejo","San Jose","Santa Rosa Lima","Yayantique","Yucuaiquín")
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_prin_re_cliente)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializa el Spinner de departamento
        spinnerDep = findViewById(R.id.departamento)
        val departamentos = arrayOf("Ahuachapán", "Santa Ana", "Sonsonate", "Chalatenango", "La Libertad",
            "San Salvador", "Cuscatlan", "La Paz", "Cabañas", "San Vicente", "Usulután", "San Miguel", "Morazán","La Unión")

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

        // Agrega un listener al Spinner de departamento
        spinnerDep.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedDept = parent.getItemAtPosition(position).toString()
                val municipios = municipiosMap[selectedDept] ?: listOf("Seleccione un municipio")
                val adapterMun = ArrayAdapter(this@PrinReClienteActivity, R.layout.spinner_personalizado, municipios)
                adapterMun.setDropDownViewResource(R.layout.spinner_dropdown_per)
                spinnerMun.adapter = adapterMun
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No hacer nada
            }
        }


        // Inicializar vistas
        nombre = findViewById(R.id.nombre)
        nit = findViewById(R.id.nit)
        email = findViewById(R.id.email)
        direccion = findViewById(R.id.direccion)
        departamento = findViewById(R.id.departamento)
        municipio = findViewById(R.id.municipio)
        telefono = findViewById(R.id.telefono)
        agregarButton = findViewById(R.id.btnAgregar)
        regresar = findViewById(R.id.atras)

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


        // Configurar evento de clic para el botón "Agregar"
        agregarButton.setOnClickListener {
            if (validarEntradas()) {
                guardarInformacion()
                // Limpiar los EditText
                nombre.text.clear()
                nit.text.clear()
                email.text.clear()
                direccion.text.clear()
                telefono.text.clear()

                // Iniciar otra actividad
                val intent = Intent(this, MenuActivity::class.java)
                startActivity(intent)
            }
        }

        regresar.setOnClickListener {
            // Iniciar otra actividad
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
        }

        // Inicializar la base de datos
        val app = application as MyApp
        database = app.database
    }
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MenuActivity ::class.java)
        startActivity(intent)
        finish()
    }

    private fun validarEntradas(): Boolean {
        val nombreText = nombre.text.toString()
        val nitText = nit.text.toString().replace("-", "")
        val emailText = email.text.toString()
        val direccionText = direccion.text.toString()
        val telefonoText = telefono.text.toString().replace("-", "")

        // Verifica que todos los campos estén llenos
        if (nombreText.isEmpty() || nitText.isEmpty() || emailText.isEmpty() ||  telefonoText.isEmpty()) {
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

        // Verifica que el teléfono sea un número válido de 8 dígitos
        if (!telefonoText.matches(Regex("\\d{8}"))) {
            Toast.makeText(this, "Teléfono debe ser un número válido de 8 dígitos", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun guardarInformacion() {
        val nombreText = nombre.text.toString()
        val nitText = nit.text.toString()
        val emailText = email.text.toString()
        val direccionText = direccion.text.toString()
        val departamentoText = departamento.selectedItem.toString()
        val municipioText = municipio.selectedItem.toString()
        val telefonoText = telefono.text.toString().replace("-", "")


        // Crear un documento mutable para guardar en la base de datos
        val document = MutableDocument()
            .setString("nombre", nombreText)
            .setString("nit", nitText)
            .setString("email", emailText)
            .setString("direccion", direccionText)
            .setString("departamento", departamentoText)
            .setString("municipio", municipioText)
            .setString("telefono", telefonoText)
            .setString("tipo", "cliente")

        try {
            // Guardar el documento en la base de datos
            database.save(document)
            Log.d("ReClienteActivity", "Datos guardados correctamente: \n $document")
            Toast.makeText(this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
        } catch (e: CouchbaseLiteException) {
            Log.e("ReClienteActivity", "Error al guardar los datos en la base de datos: ${e.message}", e)
            Toast.makeText(this, "Error al guardar los datos", Toast.LENGTH_SHORT).show()
        }
    }
}