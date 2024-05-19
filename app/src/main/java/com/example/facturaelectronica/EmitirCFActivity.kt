package com.example.facturaelectronica


import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


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
        val btnAtras: ImageButton = findViewById(R.id.atras)
        btnAtras.setOnClickListener {
            // Crear un intent para ir a MenuActivity
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()  // Finalizar la actividad actual si se desea
        }
        val siguiente: Button = findViewById(R.id.Siguiente)
        siguiente.setOnClickListener {
            val intent = Intent(this, PDF_CFActivity::class.java)
            startActivity(intent)
            finish()
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
        btnImportar.setOnClickListener {
            //Pagina para agregar datos de clientes
            val intent = Intent(this, ImportarClientes::class.java)
            startActivity(intent)
            finish()
        }
        btnExit.setOnClickListener {
            // Acción al hacer clic en el botón "Cancelar"
            dialogoCliente.dismiss()
        }

        dialogoCliente.show()
    }
    override fun onBackPressed() {
        super.onBackPressed() // Llama al método onBackPressed() de la clase base
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }
}