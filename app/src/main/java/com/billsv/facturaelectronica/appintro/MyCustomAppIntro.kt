package com.billsv.facturaelectronica.appintro

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.billsv.facturaelectronica.MenuActivity
import com.billsv.facturaelectronica.R
import com.github.appintro.AppIntro
import com.github.appintro.AppIntroFragment
import com.github.appintro.AppIntroPageTransformerType

class MyCustomAppIntro : AppIntro() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setProgressIndicator()
        // Change Indicator Color
        setIndicatorColor(
            selectedIndicatorColor = getColor(R.color.selected_indicator_color),
            unselectedIndicatorColor = getColor(R.color.unselected_indicator_color)
        )
        setImmersiveMode()
        // Add slides
        addSlide(Bienvenida.newInstance())
        addSlide(Configurar.newInstance())
        addSlide(PIN.newInstance())
        addSlide(InfoEmisor1.newInstance())
        addSlide(InfoEmisor2.newInstance())
        //logo
        addSlide(Autentificacion.newInstance())
        addSlide(Certificado.newInstance())
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        // Show a loading indicator to give feedback to the user
        showLoadingIndicator()
        // Delay the skip to ensure permissions are handled
        currentFragment?.view?.postDelayed({
            hideLoadingIndicator()
            finish()
        }, 500) // Adjust the delay as needed
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showLoadingIndicator() {
        // Show some kind of loading indicator or message to the user
    }

    private fun hideLoadingIndicator() {
        // Hide the loading indicator
    }
}
