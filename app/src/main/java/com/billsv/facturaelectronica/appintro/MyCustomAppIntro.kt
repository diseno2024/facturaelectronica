package com.billsv.facturaelectronica.appintro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.billsv.facturaelectronica.MenuActivity
import com.billsv.facturaelectronica.R
import com.github.appintro.AppIntro

class MyCustomAppIntro : AppIntro() {
    private val REQUEST_CODE_PERMISSIONS = 1001
    private val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 101

    // Lista de permisos adaptada según la versión de Android
    private val permissionList: List<String> = if (Build.VERSION.SDK_INT >= 33) {
        listOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_IMAGES
        )
    } else {
        listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setProgressIndicator()
        setIndicatorColor(
            selectedIndicatorColor = getColor(R.color.selected_indicator_color),
            unselectedIndicatorColor = getColor(R.color.unselected_indicator_color)
        )
        setImmersiveMode()
        setDoneText("comencemos")
        // Configurar los colores de los botones
        setSeparatorColor(getColor(R.color.transparent))
        setNextArrowColor(getColor(R.color.selected_indicator_color))
        setColorSkipButton(getColor(R.color.selected_indicator_color))
        setColorDoneText(getColor(R.color.selected_indicator_color))

        // Añadir las diapositivas
        addSlide(Bienvenida.newInstance())
        addSlide(Configurar.newInstance())
        addSlide(PIN.newInstance())
        addSlide(InfoEmisor1.newInstance())
        addSlide(InfoEmisor2.newInstance())
        addSlide(Logo.newInstance()) // Aquí es donde se mostrará el botón "Saltar"
        addSlide(Autentificacion.newInstance())
        addSlide(Certificado.newInstance())
        addSlide(DonePage.newInstance())

    }

    override fun onNextPressed(currentFragment: Fragment?) {
        super.onNextPressed(currentFragment)
        if (currentFragment is Autentificacion){
            currentFragment.guardarInformacion()
        }
        if (currentFragment is Certificado){
            currentFragment.guardarCertificado()
            currentFragment.guardarClavePrivada()
        }
    }
    override fun onDonePressed(currentFragment: Fragment?) {
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        requestPermissions()
        finish()
    }



    override fun onSlideChanged(oldFragment: Fragment?, newFragment: Fragment?) {
        super.onSlideChanged(oldFragment, newFragment)

        // Mostrar el botón "Saltar" solo en la diapositiva del logo
        if (newFragment is Logo) {
            isSkipButtonEnabled = true // Habilitar el botón "Saltar"
        } else{
            isSkipButtonEnabled = false
            // Deshabilitar el botón Saltar en los demás fragmentos
        }
        if (oldFragment is InfoEmisor1) {
            oldFragment.guardarInformacion()
        }

        if (oldFragment is InfoEmisor2) {
            oldFragment.actualizarInformacion()
        }
        if (oldFragment is Autentificacion){
            oldFragment.guardarInformacion()
        }
        if (oldFragment is Certificado){
            oldFragment.guardarClavePrivada()
            oldFragment.guardarCertificado()
        }
    }
    override fun onCanRequestNextPage(): Boolean {
         val currentFragment = supportFragmentManager.fragments.lastOrNull()

         // Validar los campos solo si el usuario está en InfoEmisor1
         if (currentFragment is InfoEmisor2) {
             // Buscar el fragmento PIN
             val pinFragment = supportFragmentManager.fragments.find { it is InfoEmisor1 } as? InfoEmisor1

             // Si el fragmento PIN existe, ejecutar la función de PIN
             pinFragment?.let {
                 if (it.validarEntradas()) { // Llama a la función de PIN aquí
                 } else {
                     return false // Detener si PIN falla
                 }
             }
         }
         if (currentFragment is Logo) {
             // Buscar el fragmento PIN
             val pinFragment = supportFragmentManager.fragments.find { it is InfoEmisor2 } as? InfoEmisor2

             // Si el fragmento PIN existe, ejecutar la función de PIN
             pinFragment?.let {
                 if (it.validarEntradas()) { // Llama a la función de PIN aquí
                 } else {
                     return false // Detener si PIN falla
                 }
             }
         }
        if (currentFragment is Certificado){
            val authFragment= supportFragmentManager.fragments.find { it is Autentificacion } as? Autentificacion


            authFragment?.let{
                if (it.validarCredenciales()){


                }else{
                    return false
                }
            }
        }


        if (currentFragment is DonePage){
            val certiFragment = supportFragmentManager.fragments.find { it is Certificado } as? Certificado
            certiFragment?.let {
                if (it.validarCertificados() ){


                }else{
                    return false
                }
            }
        }


        return super.onCanRequestNextPage() // Permitir avanzar en otros casos
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        // Implementación personalizada al presionar el botón "Saltar"
    }

    private fun showErrorMessage(message: String) {
        // Mostrar un mensaje de error, puede ser un Toast o un diálogo
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        for (permission in permissionList) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                WRITE_EXTERNAL_STORAGE_REQUEST_CODE
            )
        } else {

        }
    }


}
