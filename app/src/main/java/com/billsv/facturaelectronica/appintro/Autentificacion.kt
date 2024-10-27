package com.billsv.facturaelectronica.appintro

import android.app.Dialog
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
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
import com.google.android.material.card.MaterialCardView


class Autentificacion : Fragment() {

    private lateinit var database: Database
    private lateinit var userp: EditText
    private lateinit var contrap: EditText
    private lateinit var userpr: EditText
    private lateinit var contrapr: EditText
    private lateinit var facturaCheckP: MaterialCardView
    private lateinit var facturaCheckPro: MaterialCardView
    private lateinit var creditoCheckP: MaterialCardView
    private lateinit var creditoCheckPro: MaterialCardView
    private var Semostropru: Boolean = false
    private var Semostropro: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inicializa la base de datos desde la aplicación
        database = (requireActivity().application as MyApp).personalDB

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.activity_autentificacion, container, false)

        facturaCheckP = view.findViewById(R.id.FacturacheckP)
        val textViewFactura = view.findViewById<TextView>(R.id.Facturatext)
        val iconoCardf = view.findViewById<ImageView>(R.id.iconoCardf)

        facturaCheckPro = view.findViewById(R.id.FacturacheckPro)
        val textViewFacturapro = view.findViewById<TextView>(R.id.Facturatext2)
        val iconoCardfpro = view.findViewById<ImageView>(R.id.iconoCardf2)

        creditoCheckP = view.findViewById(R.id.CreditocheckP)
        val textViewcredito = view.findViewById<TextView>(R.id.Creditotext)
        val iconoCardC = view.findViewById<ImageView>(R.id.iconoCardc)

        creditoCheckPro = view.findViewById(R.id.CreditocheckPro)
        val textViewcreditopro = view.findViewById<TextView>(R.id.Creditotext2)
        val iconoCardCpro = view.findViewById<ImageView>(R.id.iconoCardc2)

        facturaCheckP.setOnClickListener {
            Toast.makeText(requireContext(), "Factura Prueba", Toast.LENGTH_SHORT).show()
            if(facturaCheckPro.strokeColor == Color.parseColor("#4A8C74")){
                textViewFacturapro.setTextColor(Color.parseColor("#9E9E9E"))
                iconoCardfpro.setColorFilter(Color.parseColor("#9E9E9E"), PorterDuff.Mode.SRC_IN)
                facturaCheckPro.strokeColor = Color.parseColor("#9E9E9E")
                textViewFactura.setTextColor(Color.parseColor("#4A8C74"))
                iconoCardf.setColorFilter(Color.parseColor("#4A8C74"), PorterDuff.Mode.SRC_IN)
                facturaCheckP.strokeColor = Color.parseColor("#4A8C74")
            }else{
                textViewFactura.setTextColor(Color.parseColor("#4A8C74"))
                iconoCardf.setColorFilter(Color.parseColor("#4A8C74"), PorterDuff.Mode.SRC_IN)
                facturaCheckP.strokeColor = Color.parseColor("#4A8C74")
            }
            if(creditoCheckP.strokeColor == Color.parseColor("#9E9E9E") && !Semostropru){
                dialogopru()
                Semostropru = true
            }
        }

        creditoCheckP.setOnClickListener {
            Toast.makeText(requireContext(), "Credito Prueba", Toast.LENGTH_SHORT).show()
            if(creditoCheckPro.strokeColor == Color.parseColor("#4A8C74")){
                textViewcreditopro.setTextColor(Color.parseColor("#9E9E9E"))
                iconoCardCpro.setColorFilter(Color.parseColor("#9E9E9E"), PorterDuff.Mode.SRC_IN)
                creditoCheckPro.strokeColor = Color.parseColor("#9E9E9E")
                textViewcredito.setTextColor(Color.parseColor("#4A8C74"))
                iconoCardC.setColorFilter(Color.parseColor("#4A8C74"), PorterDuff.Mode.SRC_IN)
                creditoCheckP.strokeColor = Color.parseColor("#4A8C74")
            }else{
                textViewcredito.setTextColor(Color.parseColor("#4A8C74"))
                iconoCardC.setColorFilter(Color.parseColor("#4A8C74"), PorterDuff.Mode.SRC_IN)
                creditoCheckP.strokeColor = Color.parseColor("#4A8C74")
            }
            if(facturaCheckP.strokeColor == Color.parseColor("#9E9E9E") && !Semostropru){
                dialogopru()
                Semostropru = true
            }
        }

        facturaCheckPro.setOnClickListener {
            Toast.makeText(requireContext(), "Factura Produccion", Toast.LENGTH_SHORT).show()
            if(facturaCheckP.strokeColor == Color.parseColor("#4A8C74")){
                textViewFactura.setTextColor(Color.parseColor("#9E9E9E"))
                iconoCardf.setColorFilter(Color.parseColor("#9E9E9E"), PorterDuff.Mode.SRC_IN)
                facturaCheckP.strokeColor = Color.parseColor("#9E9E9E")
                textViewFacturapro.setTextColor(Color.parseColor("#4A8C74"))
                iconoCardfpro.setColorFilter(Color.parseColor("#4A8C74"), PorterDuff.Mode.SRC_IN)
                facturaCheckPro.strokeColor = Color.parseColor("#4A8C74")
            }else {
                textViewFacturapro.setTextColor(Color.parseColor("#4A8C74"))
                iconoCardfpro.setColorFilter(Color.parseColor("#4A8C74"), PorterDuff.Mode.SRC_IN)
                facturaCheckPro.strokeColor = Color.parseColor("#4A8C74")
            }
            if(creditoCheckPro.strokeColor == Color.parseColor("#9E9E9E") && !Semostropro){
                dialogopro()
                Semostropro = true
            }
        }

        creditoCheckPro.setOnClickListener {
            Toast.makeText(requireContext(), "Credito Produccion", Toast.LENGTH_SHORT).show()
            if(creditoCheckP.strokeColor == Color.parseColor("#4A8C74")){
                textViewcredito.setTextColor(Color.parseColor("#9E9E9E"))
                iconoCardC.setColorFilter(Color.parseColor("#9E9E9E"), PorterDuff.Mode.SRC_IN)
                creditoCheckP.strokeColor = Color.parseColor("#9E9E9E")
                textViewcreditopro.setTextColor(Color.parseColor("#4A8C74"))
                iconoCardCpro.setColorFilter(Color.parseColor("#4A8C74"), PorterDuff.Mode.SRC_IN)
                creditoCheckPro.strokeColor = Color.parseColor("#4A8C74")
            }else{
                textViewcreditopro.setTextColor(Color.parseColor("#4A8C74"))
                iconoCardCpro.setColorFilter(Color.parseColor("#4A8C74"), PorterDuff.Mode.SRC_IN)
                creditoCheckPro.strokeColor = Color.parseColor("#4A8C74")
            }
            if(facturaCheckPro.strokeColor == Color.parseColor("#9E9E9E") && !Semostropro){
                dialogopro()
                Semostropro = true
            }
        }
        return view
    }
    fun validarCHECKS():Boolean{
        if(facturaCheckP.strokeColor == Color.parseColor("#4A8C74") || facturaCheckPro.strokeColor == Color.parseColor("#4A8C74") && creditoCheckP.strokeColor == Color.parseColor("#4A8C74") || creditoCheckPro.strokeColor == Color.parseColor("#4A8C74")){
            return true
        }else if(facturaCheckP.strokeColor != Color.parseColor("#4A8C74") && facturaCheckPro.strokeColor != Color.parseColor("#4A8C74")){
            Toast.makeText(requireContext(), "Seleccione un ambiente para Factura", Toast.LENGTH_SHORT).show()
        }else if(creditoCheckP.strokeColor != Color.parseColor("#4A8C74") && creditoCheckPro.strokeColor != Color.parseColor("#4A8C74")){
            Toast.makeText(requireContext(), "Seleccione un ambiente para Crédito Fiscal", Toast.LENGTH_SHORT).show()
        }
        Toast.makeText(requireContext(), "Seleccione un ambiente para ambos DTE'S", Toast.LENGTH_SHORT).show()
        return false
    }
    private fun dialogopru() {
        val prueba = Dialog(requireContext())
        prueba.setContentView(R.layout.dialogo_credenciales_prueba) // R.layout.layout_custom_dialog es tu diseño personalizado
        val width =
            (resources.displayMetrics.widthPixels * 0.8).toInt() // 80% del ancho de la pantalla
        val height =
            (resources.displayMetrics.heightPixels * 0.4).toInt() // 60% del alto de la pantalla
        prueba.window?.setLayout(width, height)
        prueba.setCanceledOnTouchOutside(false)
        val btnAgregar = prueba.findViewById<Button>(R.id.guardar)
        userp = prueba.findViewById(R.id.userp)
        contrap = prueba.findViewById(R.id.contrap)
        btnAgregar.setOnClickListener {
            if(validarCredencialesAutentificacionpru()){
                guardarInformacionAutentificacionpru()
                prueba.dismiss()
            }
        }
        prueba.show()
    }
    private fun dialogopro() {
        val produccion = Dialog(requireContext())
        produccion.setContentView(R.layout.dialogo_credenciales_produccion) // R.layout.layout_custom_dialog es tu diseño personalizado
        val width =
            (resources.displayMetrics.widthPixels * 0.8).toInt() // 80% del ancho de la pantalla
        val height =
            (resources.displayMetrics.heightPixels * 0.4).toInt() // 60% del alto de la pantalla
        produccion.window?.setLayout(width, height)
        produccion.setCanceledOnTouchOutside(false)
        val btnAgregar = produccion.findViewById<Button>(R.id.guardar)
        userpr = produccion.findViewById(R.id.userpr)
        contrapr = produccion.findViewById(R.id.contrapr)
        btnAgregar.setOnClickListener {
            if(validarCredencialesAutentificacionpro()){
                guardarInformacionAutentificacionpro()
                produccion.dismiss()
            }
        }
        produccion.show()
    }
    fun guardarInformacionAutentificacionpro() {
        val userprText = userpr.text.toString()
        val contraprText = contrapr.text.toString()

        // Buscar si ya existe un documento del tipo "Autentificacion"
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("Autentificacionpro")))

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
                .setString("usuario", userprText)
                .setString("contraseña", contraprText)
                .setString("tipo", "Autentificacionpro")

            // Guardar el nuevo documento
            database.save(document)
            Log.e("Authetificacion", "Datos guardados correctamente: \n $document")

            // Mostrar Toast usando el contexto de la actividad
            Toast.makeText(requireContext(), "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
        } catch (e: CouchbaseLiteException) {
            Log.e("ReClienteActivity", "Error al guardar los datos en la base de datos: ${e.message}", e)
            Toast.makeText(requireContext(), "Error al guardar los datos", Toast.LENGTH_SHORT).show()
        }
    }
    fun guardarInformacionAutentificacionpru() {
        val userpText = userp.text.toString()
        val contrapText = contrap.text.toString()

        // Buscar si ya existe un documento del tipo "Autentificacion"
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("Autentificacionpru")))

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
                .setString("usuario", userpText)
                .setString("contraseña", contrapText)
                .setString("tipo", "Autentificacionpru")

            // Guardar el nuevo documento
            database.save(document)
            Log.e("Authetificacion", "Datos guardados correctamente: \n $document")

            // Mostrar Toast usando el contexto de la actividad
            Toast.makeText(requireContext(), "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
        } catch (e: CouchbaseLiteException) {
            Log.e("ReClienteActivity", "Error al guardar los datos en la base de datos: ${e.message}", e)
            Toast.makeText(requireContext(), "Error al guardar los datos", Toast.LENGTH_SHORT).show()
        }
    }

    // Variables para verificar si el mensaje ya se mostró
    private var MensajeError1: Boolean = false
    private var MensajeError2: Boolean = false
    private var MensajeError3: Boolean = false
    private var MensajeError4: Boolean = false
    fun validarCredencialesAutentificacionpro(): Boolean{
        val userProduccion=userpr.text.toString()
        val contraProduccion=contrapr.text.toString()

        // Validar que todos los campos estén llenos
        if (userProduccion.isNotEmpty() && contraProduccion.isNotEmpty()) {
            // Si todos los campos están correctos, entonces que avance
            return true
        } else { // De lo contrario verificar qué es lo que el usuario aún no ha completado
            // Verificar si el usuario ingresó el usuario de producción
            if (userProduccion.isEmpty()) {
                // Verifica si el mensaje ya se mostró
                if (!MensajeError3) {
                    Toast.makeText(requireContext(), "Ingrese el usuario de producción", Toast.LENGTH_SHORT).show()
                    MensajeError3 = true // El mensaje ya se mostró, no se volverá a mostrar
                }
                return false
            }

            // Verificar si el usuario ingresó la contraseña de producción
            if (contraProduccion.isEmpty()) {
                // Verifica si el mensaje ya se mostró
                if (!MensajeError4) {
                    Toast.makeText(requireContext(), "Ingrese la contraseña de producción", Toast.LENGTH_SHORT).show()
                    MensajeError4 = true // El mensaje ya se mostró, no se volverá a mostrar
                }
                return false
            }

        }
        return false

    }
    fun validarCredencialesAutentificacionpru(): Boolean{
        val userPrueba=userp.text.toString()
        val contraPrueba=contrap.text.toString()

        // Validar que todos los campos estén llenos
        if (userPrueba.isNotEmpty() && contraPrueba.isNotEmpty()) {
            // Si todos los campos están correctos, entonces que avance
            return true
        } else { // De lo contrario verificar qué es lo que el usuario aún no ha completado

            // Verificar si el usuario ingresó el usuario de prueba
            if (userPrueba.isEmpty()) {
                // Verifica si el mensaje ya se mostró
                if (!MensajeError1) {
                    Toast.makeText(requireContext(), "Ingrese el usuario de prueba", Toast.LENGTH_SHORT).show()
                    MensajeError1 = true // El mensaje ya se mostró, no se volverá a mostrar
                }
                return false
            }
            // Verificar si el usuario ingresó la contraseña de pruebas
            if (contraPrueba.isEmpty()) {
                // Verifica si el mensaje ya se mostró
                if (!MensajeError2) {
                    Toast.makeText(requireContext(), "Ingrese la contraseña de prueba", Toast.LENGTH_SHORT).show()
                    MensajeError2 = true // El mensaje ya se mostró, no se volverá a mostrar
                }
                return false
            }
        }
        return false

    }

    fun guardarAmbiente() {
        // Buscar si ya existe un documento del tipo "Autentificacion"
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("Ambiente")))

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
                .setBoolean("consumidorFinal", facturaCheckPro.strokeColor.equals(Color.parseColor("#4A8C74")))
                .setBoolean("creditoFiscal", creditoCheckPro.strokeColor.equals(Color.parseColor("#4A8C74")))
            if (facturaCheckPro.strokeColor.equals(Color.parseColor("#4A8C74"))){
                document.setString("ambienteCF","01")
            }else{
                document.setString("ambienteCF","00")
            }
            if (creditoCheckPro.strokeColor.equals(Color.parseColor("#4A8C74"))){
                document.setString("ambienteCCF","01")
            }else{
                document.setString("ambienteCCF","00")
            }
                .setString("tipo", "Ambiente")

            // Guardar el nuevo documento
            database.save(document)
            Log.e("Authetificacion", "Datos guardados correctamente: \n $document")

            // Mostrar Toast usando el contexto de la actividad
            Toast.makeText(requireContext(), "Datos guardados correctamente", Toast.LENGTH_SHORT).show()
        } catch (e: CouchbaseLiteException) {
            Log.e("ReClienteActivity", "Error al guardar los datos en la base de datos: ${e.message}", e)
            Toast.makeText(requireContext(), "Error al guardar los datos", Toast.LENGTH_SHORT).show()
        }

    }

    companion object {
        // Puedes pasar argumentos aquí si lo necesitas
        fun newInstance(): Autentificacion {
            return Autentificacion()
        }
    }
    //CHECKS

}
