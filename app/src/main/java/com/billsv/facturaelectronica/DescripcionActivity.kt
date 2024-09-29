package com.billsv.facturaelectronica

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.billsv.facturaelectronica.R
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.DataSource
import com.couchbase.lite.Expression
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult
import java.math.RoundingMode

class DescripcionActivity : AppCompatActivity() {
    private lateinit var Cantidad: EditText
    private lateinit var Producto: EditText
    private lateinit var Precio: EditText
    private lateinit var Tipo: Spinner
    private lateinit var Unidad: Spinner
    private lateinit var TipoV: Spinner
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_descripcion)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val clave = intent.getStringExtra("clave")
        val textotoll: TextView = findViewById(R.id.Detalles)
        if(clave == "ccf"){
            textotoll.text = "Articulos"
        }else{
            textotoll.text = "Detalles de la Factura"
        }
        Tipo = findViewById(R.id.TipoS)
// Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter.createFromResource(
            this,
            R.array.TipoS,
            R.layout.spinner_descripcion
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_per)
            // Apply the adapter to the spinner.
            Tipo.adapter = adapter
        }
        Unidad = findViewById(R.id.UnidadMedidaS)
// Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter.createFromResource(
            this,
            R.array.UnidadMedidaS,
            R.layout.spinner_descripcion
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_per)
            // Apply the adapter to the spinner.
            Unidad.adapter = adapter
        }
        TipoV = findViewById(R.id.Tipo_Venta)
// Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter.createFromResource(
            this,
            R.array.Tipo_Venta,
            R.layout.spinner_descripcion
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_per)
            // Apply the adapter to the spinner.
            TipoV.adapter = adapter
        }

        val Cancelar: Button = findViewById(R.id.Cancelar)
        Cancelar.setOnClickListener {
            val clave = intent.getStringExtra("clave")
            if(clave == "ccf"){
                val intent = Intent(this, EmitirCCFActivity::class.java)
                startActivity(intent)
                finish()
            }else {
                val intent = Intent(this, EmitirCFActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
        Cantidad = findViewById(R.id.Cantidad)
        Cantidad.setText("1")
        Producto = findViewById(R.id.Producto)
        Producto.requestFocus();
        Precio = findViewById(R.id.Precio)



        val Agregar: Button = findViewById(R.id.Agregar)
        Agregar.setOnClickListener {
            if(Validar()) {
                guardarItem()
                val Item = Dialog(this)
                Item.setContentView(R.layout.layout_item) // R.layout.layout_custom_dialog es tu diseño personalizado
                val width =
                    (resources.displayMetrics.widthPixels * 0.92).toInt() // 80% del ancho de la pantalla
                val height =
                    (resources.displayMetrics.heightPixels * 0.5).toInt() // 60% del alto de la pantalla
                Item.window?.setLayout(width, height)
                Item.setCanceledOnTouchOutside(false)
                val btnAgregar = Item.findViewById<Button>(R.id.AggItem)
                val btnRegresar = Item.findViewById<Button>(R.id.Regresar)
                btnAgregar.setOnClickListener {
                    Cantidad.text.clear()
                    Producto.text.clear()
                    Precio.text.clear()
                    //cierra el dialogo
                    Item.dismiss()
                    Tipo.setSelection(0)
                    TipoV.setSelection(0)
                    Unidad.setSelection(0)
                    Cantidad.setText("1")
                    Producto.requestFocus();
                }
                val clave = intent.getStringExtra("clave")
                btnRegresar.setOnClickListener {
                    if (clave == "ccf") {
                        val intent = Intent(this, EmitirCCFActivity::class.java)
                        startActivity(intent)
                        Item.dismiss()
                    } else {
                        val intent = Intent(this, EmitirCFActivity::class.java)
                        startActivity(intent)
                        Item.dismiss()
                    }
                }

                Item.show()
            }

        }
    }

    // Valida que el precio no tenga más de 4 decimales
    private fun tieneMaximoCuatroDecimales(valor: String): Boolean {
        val partes = valor.split(".")
        return if (partes.size == 2) partes[1].length <= 4 else true
    }

    private fun Validar(): Boolean {
        val cantidadStr = Cantidad.text.toString()
        val producto = Producto.text.toString()
        val precioStr = Precio.text.toString()

        if(cantidadStr.isNotEmpty() && cantidadStr.toInt() > 0 && producto.isNotEmpty() && precioStr.isNotEmpty() && precioStr.toDouble() > 0.00 && precioStr.toDouble() < 1000000.00 && tieneMaximoCuatroDecimales(precioStr)){
            return true
        }else{
            if(cantidadStr == ""){
                Toast.makeText(this, "Ingrese la cantidad", Toast.LENGTH_SHORT).show()
            }
            if(cantidadStr.toInt() < 0 || cantidadStr.toInt() == 0){
                Toast.makeText(this, "Cantidad no válida", Toast.LENGTH_SHORT).show()
            }
            if(producto == ""){
                Toast.makeText(this, "Ingrese el producto", Toast.LENGTH_SHORT).show()
            }
            if(precioStr == ""){
                Toast.makeText(this, "Ingrese el precio", Toast.LENGTH_SHORT).show()
            }
            if(precioStr == "0"){
                Toast.makeText(this, "Precio no válido", Toast.LENGTH_SHORT).show()
            }
            if (!tieneMaximoCuatroDecimales(precioStr)) {
                Toast.makeText(this, "El precio no puede tener más de 4 decimales", Toast.LENGTH_SHORT).show()
            }
        }
        return false
    }

    private fun guardarItem() {
        val app = application as MyApp
        val database = app.database
        val tipo = Tipo.selectedItem.toString()
        val cantidad = Cantidad.text.toString()
        val UnidadMe = Unidad.selectedItem.toString()
        val producto = Producto.text.toString()
        val TipoV = TipoV.selectedItem.toString()
        val Precio = Precio.text.toString()
        val clave = intent.getStringExtra("clave")
        var articulo = ""
        var ventaG="0.0"
        var ventaE="0.0"
        var ventaNS="0.0"
        var ivaItemT="0.0"
        if (clave == "ccf"){
            articulo = "Articuloccf"
        }else{
            articulo = "Articulocf"
        }
        when (TipoV) {
            "Gravado" -> {
                if (articulo=="Articulocf") {
                    val precioDouble: Double = Precio.toDouble()
                    val cantidadDouble: Double = cantidad.toDouble()
                    val ventaGT = precioDouble * cantidadDouble
                    ventaG = ventaGT.toString()

                    val ivaItem = (((precioDouble * cantidadDouble) / 1.13) * 0.13).toBigDecimal()
                        .setScale(6, RoundingMode.HALF_UP).toDouble()
                    ivaItemT = ivaItem.toString()
                }else{
                    val precioDouble: Double = Precio.toDouble()
                    val cantidadDouble: Double = cantidad.toDouble()
                    val ventaGT = precioDouble * cantidadDouble
                    ventaG = ventaGT.toString()
                }

            }
            "Exento" -> {
                val precioDouble: Double =  Precio.toDouble()
                val cantidadDouble: Double =cantidad.toDouble()
                val ventaET=precioDouble*cantidadDouble
                ventaE=ventaET.toString()
            }
            else -> {
                ventaNS=Precio
            }
        }
        //TIPO
        var codigo=""
        when (tipo) {
            "Bien" -> {
                codigo="1"

            }
            "Servicio" -> {
                codigo="2"
            }
            else -> {
                codigo="3"
            }
        }
        var codigoUnidad=""
        codigoUnidad = when (UnidadMe) {
            "Unidad" -> {
                "59"
            }

            "Docena" -> {
                "58"
            }

            "Ciento" -> {
                "57"
            }

            else -> {
                "99"
            }
        }
        // Condicional para mostrar los valores correspondientes
        codigoUnidad = when (codigoUnidad) {
            "59" -> "1"      // Mostrar 1 en lugar de 59
            "58" -> "12"     // Mostrar 12 en lugar de 58
            "57" -> "100"    // Mostrar 100 en lugar de 57
            "99" -> "Otros"  // Mostrar "Otros" en lugar de 99
            else -> codigoUnidad // Retorna el valor original si no coincide
        }

// Imprime el resultado
        println("Valor mostrado: $codigoUnidad")



        var count=0
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("Articulocf")).or(Expression.property("tipo").equalTo(Expression.string("Articuloccf"))))


        try {
            val result = query.execute()
            count +=result.allResults().size
            Log.d("ReClienteActivity", "Número de documentos de tipo 'ConfEmisor': $count")
        } catch (e: CouchbaseLiteException) {
            Log.e("ReClienteActivity", "Error al contar los documentos de tipo 'ConfEmisor': ${e.message}", e)
        }
        val numItem=(count+1).toString()
        // Crear un documento mutable para guardar en la base de datos
        if (tipo != "" && cantidad != "") {
            // Crear un documento mutable para guardar en la base de datos
            val document = MutableDocument()
                .setString("Tipod", codigo)
                .setString("Cantidad", cantidad)
                .setString("Unidad", codigoUnidad)
                .setString("Producto", producto)
                .setString("Tipo de Venta", TipoV)
                .setString("Precio", Precio)
                .setString("tipo", articulo)
                .setString("ventaG",ventaG)
                .setString("ventaE",ventaE)
                .setString("ventaNS",ventaNS)
                .setString("ivaItem",ivaItemT)
                .setString("codigoP",null)
                .setString("codigoT",null)
                .setString("tributo",null)
                .setString("psv","0.0")
                .setString("noGravado","0.0")
                .setString("montoDesc","0.0")
                .setString("numItem",numItem)



            try {
                // Guardar el documento en la base de datos
                database.save(document)
                Log.d("Descripcion", "Datos guardados correctamente: \n $document")
                Toast.makeText(this, "Artículo agregado", Toast.LENGTH_SHORT).show()
            } catch (e: CouchbaseLiteException) {
                Log.e(
                    "Descripcion",
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

    override fun onBackPressed() {
        super.onBackPressed() // Llama al método onBackPressed() de la clase base
        val clave = intent.getStringExtra("clave")
        if (clave == "ccf"){
            val intent = Intent(this, EmitirCCFActivity::class.java)
            startActivity(intent)
            finish()
        }else {
            val intent = Intent(this, EmitirCFActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}