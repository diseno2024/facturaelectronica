package com.billsv.facturaelectronica

import android.app.Dialog
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.os.Environment
import android.widget.Button
import android.widget.Toast
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File
import java.io.FileWriter
import java.lang.Exception

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
                json()
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
    private fun json(){

        val documento = Documento(
            identificacion = Identificacion(
                version = 1,
                ambiente = "Producción",
                tipoDte = "Factura",
                numeroControl = "12345",
                codigoGeneracion = "FF54E9DB-79C3-42CE-B432-EC522C97EFB9",
                tipoModelo = 1,
                tipoOperacion = 1,
                tipoContingencia = null,
                motivoContin = null,
                fecEmi = "2023-06-01",
                horEmi = "12:00:00",
                tipoMoneda = "USD"
            ),
            documentoRelacionado = null,
            emisor = Emisor(
                nit = "06140030211001",
                nrc = "123456-7",
                nombre = "Empresa XYZ",
                codActividad = "6201",
                descActividad = "Desarrollo de Software",
                nombreComercial = "XYZ Tech",
                tipoEstablecimiento = "Oficina",
                direccion = Direccion(
                    departamento = "San Salvador",
                    municipio = "San Salvador",
                    complemento = "Colonia Escalón"
                ),
                telefono = "2500-0000",
                correo = "contacto@xyztech.com",
                codEstableMH = null,
                codEstable = "1",
                codPuntoVentaMH = null,
                codPuntoVenta = "1"
            ),
            receptor = Receptor(
                tipoDocumento = "DUI",
                numDocumento = "12345678-9",
                nrc = null,
                nombre = "Cliente ABC",
                codActividad = null,
                descActividad = null,
                telefono = "2100-0000",
                direccion = Direccion(
                    departamento = "La Libertad",
                    municipio = "Santa Tecla",
                    complemento = "Residencial Las Piletas"
                ),
                correo = "cliente.abc@gmail.com"
            ),
            otrosDocumentos = null,
            ventaTercero = null,
            cuerpoDocumento = listOf(
                CuerpoDocumento(
                    ivaItem = 0.0,
                    psv = 0.0,
                    noGravado = 0.0,
                    numItem = 1,
                    tipoItem = 1,
                    numeroDocumento = null,
                    cantidad = 2.0,
                    codigo = "P001",
                    codTributo = null,
                    uniMedida = 1,
                    descripcion = "Producto A",
                    precioUni = 10.0,
                    montoDescu = 0.0,
                    ventaNoSuj = 0.0,
                    ventaExenta = 0.0,
                    ventaGravada = 20.0,
                    tributos = null
                )
            ),
            resumen = Resumen(
                totalIva = 2.6,
                porcentajeDescuento = 0.0,
                ivaRete1 = 0.0,
                reteRenta = 0.0,
                totalNoGravado = 0.0,
                totalPagar = 22.6,
                saldoFavor = 0.0,
                condicionOperacion = 1,
                pagos = listOf(
                    Pago(
                        codigo = "01",
                        montoPago = 22.6,
                        referencia = "Pago en efectivo",
                        plazo = "Inmediato",
                        periodo = 0
                    )
                ),
                numPagoElectronico = "0001",
                totalNoSuj = 0.0,
                totalExenta = 0.0,
                totalGravada = 20.0,
                subTotalVentas = 20.0,
                descuNoSuj = 0.0,
                descuExenta = 0.0,
                descuGravada = 0.0,
                totalDescu = 0.0,
                tributos = null,
                subTotal = 20.0,
                montoTotalOperacion = 22.6,
                totalLetras = "VEINTIDÓS CON 60/100"
            ),
            extension = Extension(
                placaVehiculo = null,
                docuEntrega = null,
                nombEntrega = null,
                docuRecibe = null,
                nombRecibe = null,
                observaciones = null
            ),
            apendice = null,
            selloRecibido = "Sello de recibido",
            firmaElectronica = "Firma electrónica"
        )

        // Crear una instancia de ObjectMapper
        val mapper = ObjectMapper().registerModule(KotlinModule())
        mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)

        // Convertir la instancia de Documento a JSON
        val json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(documento)

        saveJsonToExternalStorage(json)
    }
    private fun saveJsonToExternalStorage(jsonData: String) {
        val fileName = "documento.json"
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(directory, fileName)

        try {
            FileWriter(file).use {
                it.write(jsonData)
            }
            Toast.makeText(this, "Archivo JSON guardado en: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al guardar el archivo JSON", Toast.LENGTH_SHORT).show()
        }
    }
}