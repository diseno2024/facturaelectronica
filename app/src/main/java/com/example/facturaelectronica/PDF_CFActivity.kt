package com.example.facturaelectronica

import android.app.Dialog
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button

class PDF_CFActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pdf_cfactivity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
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
    override fun onBackPressed() {
        super.onBackPressed() // Llama al método onBackPressed() de la clase base
        val intent = Intent(this, EmitirCFActivity::class.java)
        startActivity(intent)
        finish()
    }
}