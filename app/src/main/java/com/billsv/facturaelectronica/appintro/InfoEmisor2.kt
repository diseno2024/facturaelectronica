package com.billsv.facturaelectronica.appintro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.billsv.facturaelectronica.R

class InfoEmisor2 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_info_emisor2, container, false)
    }

    companion object {
        // Puedes pasar argumentos aquí si lo necesitas
        fun newInstance(): InfoEmisor2  {
            return InfoEmisor2 ()
        }
    }
}
