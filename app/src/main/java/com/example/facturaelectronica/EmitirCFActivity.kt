package com.example.facturaelectronica


import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.facturaelectronica.R


class EmitirCFActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_emitir_cf)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val spinner: Spinner = findViewById(R.id.ConOp_spinner)
// Create an ArrayAdapter using the string array and a default spinner layout.
        ArrayAdapter.createFromResource(
            this,
            R.array.CondOp_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner.
            spinner.adapter = adapter
        }
        val btnAtras: ImageButton = findViewById(R.id.atras)
        btnAtras.setOnClickListener {
            // Crear un intent para ir a MenuActivity
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()  // Finalizar la actividad actual si se desea
        }
        val vista_previa: Button = findViewById(R.id.VistaPrevia)
        vista_previa.setOnClickListener {
            val intent = Intent(this, PDF_CFActivity::class.java)
            startActivity(intent)
            finish()
        }
        val generar: Button = findViewById(R.id.Generar)
        generar.setOnClickListener {
            val dialogoGenerar = Dialog(this)
            dialogoGenerar.setContentView(R.layout.layout_generar) // R.layout.layout_custom_dialog es tu diseño personalizado
            val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% del ancho de la pantalla
            val height = (resources.displayMetrics.heightPixels * 0.45).toInt() // 60% del alto de la pantalla
            dialogoGenerar.window?.setLayout(width, height)
            dialogoGenerar.setCanceledOnTouchOutside(false)
            val btnYes = dialogoGenerar.findViewById<Button>(R.id.btnYes)
            val btnNo = dialogoGenerar.findViewById<Button>(R.id.btnNo)
            btnYes.setOnClickListener {
                dialogoGenerar.dismiss()
                //nuevo dialogo para clave
                val Clave = Dialog(this)
                Clave.setContentView(R.layout.layout_clave) // R.layout.layout_custom_dialog es tu diseño personalizado
                val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% del ancho de la pantalla
                val height = (resources.displayMetrics.heightPixels * 0.3).toInt() // 60% del alto de la pantalla
                Clave.window?.setLayout(width, height)
                Clave.setCanceledOnTouchOutside(false)
                val btnAceptar = Clave.findViewById<Button>(R.id.acept)
                val btnCancelar = Clave.findViewById<Button>(R.id.cancel)
                btnAceptar.setOnClickListener {
                    //Pagina para crear factura
                    Clave.dismiss()
                }

                btnCancelar.setOnClickListener {
                    Clave.dismiss()
                }

                Clave.show()
            }

            btnNo.setOnClickListener {
                // Acción al hacer clic en el botón "Cancelar"
                dialogoGenerar.dismiss()
            }

            dialogoGenerar.show()
        }


    }
    fun showDescription(view: View?) {
        val intent = Intent(this, DescripcionActivity::class.java)
        startActivity(intent)
        finish()

    }
    fun showDataClient(view: View) {

        val dialogoCliente = Dialog(this)
        dialogoCliente.setContentView(R.layout.layout_dialogo_cliente) // R.layout.layout_custom_dialog es tu diseño personalizado
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 80% del ancho de la pantalla
        val height = (resources.displayMetrics.heightPixels * 0.5).toInt() // 60% del alto de la pantalla
        dialogoCliente.window?.setLayout(width, height)
        dialogoCliente.setCanceledOnTouchOutside(false)
        val btnImportar = dialogoCliente.findViewById<Button>(R.id.btnImportar)
        val btnAgregar = dialogoCliente.findViewById<Button>(R.id.btnAgregar)
        val btnExit = dialogoCliente.findViewById<ImageButton>(R.id.exit)
        btnImportar.setOnClickListener {

        }

        btnAgregar.setOnClickListener {
            //Pagina para agregar datos de clientes
            val intent = Intent(this, ReClienteActivity::class.java)
            startActivity(intent)
            finish()
        }
        btnExit.setOnClickListener {
            // Acción al hacer clic en el botón "Cancelar"
            dialogoCliente.dismiss()
        }

        dialogoCliente.show()
    }
}