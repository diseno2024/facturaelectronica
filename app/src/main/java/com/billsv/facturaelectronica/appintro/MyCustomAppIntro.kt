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
        setDoneText("COMENCEMOS")
        setSkipText("SALTAR")

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

//        if (currentFragment is Autentificacion) {
//            currentFragment.guardarInformacionAutentificacion()
//        }
//
//        if (currentFragment is Certificado) {
//            currentFragment.guardarCertificado()
//            currentFragment.guardarClavePrivada()
//        }

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
            // Habilitar el botón "Saltar"
            isSkipButtonEnabled = true
        } else{
            // Deshabilitar el botón Saltar en los demás fragmentos
            isSkipButtonEnabled = false
        }

        // Cuando estoy en la actividad de InfoEmisor1
        if (oldFragment is InfoEmisor1) {
            oldFragment.guardarInformacionInfoEmisor1() // Que me guarde la información que acabo de ingresar
        }

        // Cuando estoy en la actividad de InfoEmisor2
        if (oldFragment is InfoEmisor2) {
            oldFragment.actualizarInformacionInfoEmisor2() // Que me guarde la información que acabo de ingresar
        }

        // Cuando estoy en la actividad Autentificacion
        if (oldFragment is Autentificacion) {
            oldFragment.guardarInformacionAutentificacion() // Que me guarde la información que acabo de ingresar
        }

        // Cuando estoy en la actividad Certificado
        if (oldFragment is Certificado) {
            oldFragment.guardarCertificado() // Que me guarde el Certificado
            oldFragment.guardarClavePrivada() // Que me guarde la Clave Privada
        }

    }

    override fun onCanRequestNextPage(): Boolean {
        val currentFragment = supportFragmentManager.fragments.lastOrNull()
        // LO DEMÁS DEL CODE

        if (currentFragment is InfoEmisor1) {
            val pinFragment = supportFragmentManager.fragments.find { it is PIN } as? PIN
            pinFragment?.let {
                if (it.PinCorrecto()) {
                    // Si las validacinoes se cumplen, entonces se permite pasar a la siguiente diapositiva
                }else{
                    // Si el PIN no está correcto, entonces no avanza a la siguiente diapositiva
                    return false
                }
            }
        }

        if (currentFragment is InfoEmisor2) {
            val pinFragment = supportFragmentManager.fragments.find { it is InfoEmisor1 } as? InfoEmisor1
            pinFragment?.let {
                if (it.validarEntradasInfoEmisor1()) {
                    // Si las validacinoes se cumplen, entonces se permite pasar a la siguiente diapositiva
                }else{
                    // Si todos los datos no están completos, entonces no avanza a la siguiente diapositiva
                    return false
                }
            }
        }

        if (currentFragment is Logo) {
            val pinFragment = supportFragmentManager.fragments.find { it is InfoEmisor2 } as? InfoEmisor2
            pinFragment?.let {
                if (it.validarEntradasInfoEmisor2()) {
                    // Si las validacinoes se cumplen, entonces se permite pasar a la siguiente diapositiva
                }else{
                    // Si todos los datos no están completos, entonces no avanza a la siguiente diapositiva
                    return false
                }
            }
        }

        if (currentFragment is Certificado) {
            val authFragment = supportFragmentManager.fragments.find { it is Autentificacion } as? Autentificacion
            authFragment?.let {
                if (it.validarCredencialesAutentificacion()) {
                    // Si las validacinoes se cumplen, entonces se permite pasar a la siguiente diapositiva
                }else{
                    // Si todos los datos no están completos, entonces no avanza a la siguiente diapositiva
                    return false
                }
            }
        }

        if (currentFragment is DonePage) {
            val certFragment = supportFragmentManager.fragments.find { it is Certificado } as? Certificado
            certFragment?.let {
                if (it.validarCertificados()) {
                    // Si las validacinoes se cumplen, entonces se permite pasar a la siguiente diapositiva
                }else {
                    // Si todos los datos no están completos, entonces no avanza a la siguiente diapositiva
                    return false
                }
            }
        }


        return super.onCanRequestNextPage() // Permitir avanzar en otros casos
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        // Implementación personalizada al presionar el botón "Saltar"
        if (currentFragment is Logo) {
            // Navegar a la siguiente diapositiva
            goToNextSlide()
        }
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
