package com.billsv.facturaelectronica.appintro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.billsv.facturaelectronica.R

class Autentificacion : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_autentificacion, container, false)
    }

    companion object {
        // Puedes pasar argumentos aquí si lo necesitas
        fun newInstance(): Autentificacion {
            return Autentificacion ()
        }
    }
}
