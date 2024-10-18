package com.billsv.facturaelectronica

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.util.Log

class ResumenesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_resumenes)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navigation)

        // Listener para manejar la selección de los items
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            Log.d("ResumenesActivity", "Item seleccionado: ${item.itemId}")
            when (item.itemId) {
                R.id.nav_contables -> {
                    Log.d("ResumenesActivity", "Resúmenes Contables seleccionado")
                    val fragmentMensuales = ResContableFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragmentMensuales)
                        .commit()
                    true
                }
                R.id.nav_financieros -> {
                    Log.d("ResumenesActivity", "Resúmenes Financieros seleccionado")
                    val fragmentFinancieros = ResFinancieroFragment()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragmentFinancieros)
                        .commit()
                    true
                }
                else -> false
            }
        }


        // Establece el fragmento predeterminado al iniciar
        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.nav_contables
        }

    }
    override fun onBackPressed() {
        super.onBackPressed() // Llama al método onBackPressed() de la clase base
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }
}
