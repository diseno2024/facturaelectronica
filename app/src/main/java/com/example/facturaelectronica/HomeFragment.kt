package com.example.facturaelectronica

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomnavigation.BottomNavigationView

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
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.boton_factura -> {
                    (activity as MenuActivity).navigateToFragment(FacturaFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.boton_creditoFiscal -> {
                    (activity as MenuActivity).navigateToFragment(CFiscalFragment())
                    return@setOnNavigationItemSelectedListener true
                }
                // Agrega más casos según sea necesario para más fragmentos
                else -> return@setOnNavigationItemSelectedListener false
            }
        }
    }
}