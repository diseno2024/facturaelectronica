package com.billsv.facturaelectronica.appintro

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.billsv.facturaelectronica.MenuActivity
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

class InfoEmisor1 : Fragment() {
    private var currentTextWatcher: TextWatcher? = null
    private lateinit var database: Database
    private lateinit var nombre: EditText
    private lateinit var DUI_NIT: EditText
    private lateinit var NumT: EditText
    private lateinit var Correo: EditText
    private lateinit var spinnerDep: Spinner
    private lateinit var spinnerMun: Spinner
    private lateinit var spinnerDN: Spinner
    private val tipoDoc = mapOf(
        "DUI" to "13",
        "NIT" to "36"
    )
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
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_info_emisor1, container, false)
        val app = requireActivity().application as MyApp
        database = app.database
        nombre = view.findViewById(R.id.nombre)
        DUI_NIT = view.findViewById(R.id.duionit)
        //Spiner Tipos
        spinnerDN = view.findViewById(R.id.spinnerdn)
        val tipos = tipoDoc.keys.toTypedArray()
        val adaptertipo = ArrayAdapter(requireContext(), R.layout.spinner_personalizado, tipos)
        adaptertipo.setDropDownViewResource(R.layout.spinner_dropdown_per)
        spinnerDN.adapter = adaptertipo

        NumT = view.findViewById(R.id.tel)

        //Spiner departamento
        spinnerDep = view.findViewById(R.id.spinnerd)
        val departamentos = departamentosMap.keys.toTypedArray()
        val adapterDep = ArrayAdapter(requireContext(), R.layout.spinner_personalizado, departamentos)
        adapterDep.setDropDownViewResource(R.layout.spinner_dropdown_per)
        spinnerDep.adapter = adapterDep

        //Spiner municipio
        spinnerMun = view.findViewById(R.id.spinnerm)
        val initialMunicipios = arrayOf("Seleccione un municipio")
        val adapterMun = ArrayAdapter(requireContext(), R.layout.spinner_personalizado, initialMunicipios)
        adapterMun.setDropDownViewResource(R.layout.spinner_dropdown_per)
        spinnerMun.adapter = adapterMun


        // listener para el Spinner de departamento
        spinnerDep.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedDept = parent.getItemAtPosition(position).toString()
                val municipios = municipiosMap[selectedDept]?.map { it.first } ?: listOf("Seleccione un municipio")
                val adapterMun2 = ArrayAdapter(requireContext(), R.layout.spinner_personalizado, municipios)
                adapterMun2.setDropDownViewResource(R.layout.spinner_dropdown_per)
                spinnerMun.adapter = adapterMun2
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No hace nada
            }
        }
        NumT.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private val mask = "####-####" // La máscara del Teléfono

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
        // Variable global para almacenar el TextWatcher actual
        spinnerDN.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()

                // Elimina el TextWatcher anterior si existe
                currentTextWatcher?.let {
                    DUI_NIT.removeTextChangedListener(it)
                }

                // Crea un nuevo TextWatcher basado en la selección
                currentTextWatcher = if (selectedItem == "DUI") {
                    object : TextWatcher {
                        private var isUpdating = false
                        private val mask = "########-#" // La máscara del DUI

                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                        override fun afterTextChanged(s: Editable?) {
                            if (isUpdating) return

                            isUpdating = true
                            val formatted = formatDui(s.toString())
                            DUI_NIT.setText(formatted)
                            DUI_NIT.setSelection(formatted.length)
                            isUpdating = false
                        }

                        private fun formatDui(dui: String): String {
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
                    }
                } else {
                    object : TextWatcher {
                        private var isUpdating = false
                        private val mask = "####-######-###-#" // La máscara del NIT

                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                        override fun afterTextChanged(s: Editable?) {
                            if (isUpdating) return

                            isUpdating = true
                            val formatted = formatNIT(s.toString())
                            DUI_NIT.setText(formatted)
                            DUI_NIT.setSelection(formatted.length)
                            isUpdating = false
                        }

                        private fun formatNIT(nit: String): String {
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
                    }
                }

                // Agrega el nuevo TextWatcher
                DUI_NIT.addTextChangedListener(currentTextWatcher)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No hacer nada si no hay selección
            }
        }

        Correo = view.findViewById(R.id.correo)
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
        return view
    }
    fun guardarInformacionInfoEmisor1() {
        val nombre = nombre.text.toString()
        val documentoidentidad = DUI_NIT.text.toString()
        var dui = ""
        var nit = ""
        val tipo = spinnerDN.selectedItem.toString()
        val departamento = spinnerDep.selectedItem.toString()
        val municipio = spinnerMun.selectedItem.toString()
        val departamentoCodigo = departamentosMap[departamento]
        val municipioCodigo = municipiosMap[departamento]?.firstOrNull { it.first == municipio }?.second
        val telefono = NumT.text.toString().replace("-", "")
        val correo = Correo.text.toString()

        if (tipo == "DUI"){
            dui = documentoidentidad
            nit = ""
        }else{
            nit = documentoidentidad
            dui = ""
        }
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
                .setString("nombreC", "")
                .setString("dui", dui)
                .setString("nit", nit)
                .setString("nrc", "")
                .setString("ActividadEco", "")
                .setString("departamento", departamentoCodigo)
                .setString("municipio", municipioCodigo)
                .setString("direccion", "")
                .setString("telefono", telefono)
                .setString("correo", correo)
                .setString("deptext", departamento)
                .setString("muntext", municipio)
                .setString("tipo", "ConfEmisor")

            // Guardar el nuevo documento
            database.save(document)
            Log.d("ReClienteActivity", "Datos guardados correctamente: \n $document")
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
    private var MensajeError6: Boolean = false
    private var MensajeError7: Boolean = false
    private var MensajeError8: Boolean = false
    private var MensajeError9: Boolean = false

    fun validarEntradasInfoEmisor1(): Boolean {
        val tipo = spinnerDN.selectedItem.toString()
        val nombreText = nombre.text.toString()
        val duiText = DUI_NIT.text.toString().replace("-", "")
        val nitText = DUI_NIT.text.toString().replace("-", "")
        val telefonoText = NumT.text.toString().replace("-", "")
        val emailText = Correo.text.toString()

        // Validación para verificar que los campos no estén vacíos y sean válidos
        if (nombreText.isNotEmpty() && ((duiText.isNotEmpty() && duiText.matches(Regex("\\d{9}"))) || (nitText.isNotEmpty() && nitText.matches(Regex("\\d{14}")))) && (telefonoText.isNotEmpty() && telefonoText.matches(Regex("\\d{8}"))) && (emailText.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches())) {
            return true
        } else { // De lo contrario verificar qué es lo que el usuario no ingresó o ingresó mal

            // Si el usuario no ingresó el nombre
            if (nombreText.isEmpty()) {
                // Verifica si el mensaje ya se mostró
                if (!MensajeError1) {
                    Toast.makeText(requireContext(), "Ingrese el Nombre", Toast.LENGTH_SHORT).show()
                    MensajeError1 = true // El mensaje ya se mostró, no se volverá a mostrar
                }
                return false
            }

            // Hacer la validación si seleccionó DUI o NIT
            if (tipo == "DUI") { // Si seleccionó el DUI
                // Si el usuario no ingresó el DUI
                if (duiText.isEmpty()) {
                    if (!MensajeError2) {
                        Toast.makeText(requireContext(), "Ingrese el DUI", Toast.LENGTH_SHORT).show()
                        MensajeError2 = true // El mensaje ya se mostró, no se volverá a mostrar
                    }
                    return false
                } else if (!duiText.matches(Regex("\\d{9}"))) { // Si el DUI no es válido
                    if (!MensajeError3) {
                        Toast.makeText(requireContext(), "Ingrese un DUI válido", Toast.LENGTH_SHORT).show()
                        MensajeError3 = true // El mensaje ya se mostró, no se volverá a mostrar
                    }
                    return false
                }
            } else { // Si seleccionó el NIT
                // Si el usuario no ingresó el NIT
                if (nitText.isEmpty()) {
                    if (!MensajeError4) {
                        Toast.makeText(requireContext(), "Ingrese el NIT", Toast.LENGTH_SHORT).show()
                        MensajeError4 = true // El mensaje ya se mostró, no se volverá a mostrar
                    }
                    return false
                } else if (!duiText.matches(Regex("\\d{14}"))) { // Si el NIT no es válido
                    if (!MensajeError5) {
                        Toast.makeText(requireContext(), "Ingrese un NIT válido", Toast.LENGTH_SHORT).show()
                        MensajeError5 = true // El mensaje ya se mostró, no se volverá a mostrar
                    }
                    return false
                }
            }

            // Si el usuario no ingresó el teléfono
            if (telefonoText.isEmpty()) {
                if (!MensajeError6) {
                    Toast.makeText(requireContext(), "Ingrese el Teléfono", Toast.LENGTH_SHORT).show()
                    MensajeError6 = true // El mensaje ya se mostró, no se volverá a mostrar
                }
                return false
            } else if (!telefonoText.matches(Regex("\\d{8}"))) { // Si el teléfono no es válido
                if (!MensajeError7) {
                    Toast.makeText(requireContext(), "Ingrese un Teléfono válido", Toast.LENGTH_SHORT).show()
                    MensajeError7 = true // El mensaje ya se mostró, no se volverá a mostrar
                }
                return false
            }

            // Si el usuario no ingresó el correo electrónico
            if (emailText.isEmpty()) {
                if (!MensajeError8) {
                    Toast.makeText(requireContext(), "Ingrese el Correo Electrónico", Toast.LENGTH_SHORT).show()
                    MensajeError8 = true // El mensaje ya se mostró, no se volverá a mostrar
                }
                return false
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) { // Si el correo electrónico no es válido
                if (!MensajeError9) {
                    Toast.makeText(requireContext(), "Ingrese un Correo Electrónico válido", Toast.LENGTH_SHORT).show()
                    MensajeError9 = true // El mensaje ya se mostró, no se volverá a mostrar
                }
                return false
            }

        }

        return false
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        fun newInstance(): InfoEmisor1 {
            return InfoEmisor1()
        }
    }
}
