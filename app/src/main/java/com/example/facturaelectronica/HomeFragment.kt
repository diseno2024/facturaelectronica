package com.example.facturaelectronica

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Maneja la navegación a través de BottomNavigationView
        val bottomNavigationView = view.findViewById<BottomNavigationView>(R.id.boton_navegacion)
        val floatingActionButton = view.findViewById<FloatingActionButton>(R.id.floatingActionButton)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.boton_factura -> {
                    floatingActionButton.backgroundTintList = ColorStateList.valueOf(hexToColorInt("#80BFA8"))
                    (activity as MenuActivity).navigateToFragment(FacturaFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.boton_creditoFiscal -> {
                    floatingActionButton.backgroundTintList = ColorStateList.valueOf(hexToColorInt("#892026"))
                    (activity as MenuActivity).navigateToFragment(CFiscalFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                // Agrega más casos según sea necesario para más fragmentos
                else -> return@setOnNavigationItemSelectedListener false
            }
        }
    }
    fun hexToColorInt(hex: String): Int {
        return try {
            Color.parseColor(hex)
        } catch (e: IllegalArgumentException) {
            // Manejar errores si el formato del color hexadecimal es incorrecto
            Color.BLACK // Valor predeterminado en caso de error
        }
    }
}