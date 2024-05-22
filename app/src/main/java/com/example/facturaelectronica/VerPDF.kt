package com.example.facturaelectronica

import android.os.Bundle
import android.os.Environment
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.facturaelectronica.databinding.ActivityVerPdfBinding
import com.example.facturaelectronica.databinding.ActivityVerPdfCcfBinding
import java.io.File

class VerPDF : AppCompatActivity() {

    // private lateinit var binding: ActivityVerPdfBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ver_pdf)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        /*
        binding = ActivityVerPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // El acceso lo hace através del directorio de la descargas
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        // Con ese nombre se le va a guardar el PDF
        val outputFilePath = File(downloadsDir, "Comprobante de Crédito Fiscal.pdf")

        binding.vistaPdf.fromFile(outputFilePath)
        binding.vistaPdf.isZoomEnabled = true

        binding.vistaPdf.show()

        */
    }
}