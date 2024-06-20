package com.billsv.facturaelectronica

import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.content.Intent
import android.text.InputFilter
import android.text.InputType
import androidx.fragment.app.Fragment
import com.couchbase.lite.Database

class MenuActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawerLayout: DrawerLayout
    private val PIN_PREFS_NAME = "pins_prefs"
    private lateinit var database: Database
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()
            navigationView.setCheckedItem(R.id.nav_home)
        }
    }
    fun navigateToFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment()).commit()

            R.id.nav_crear_pin -> showCreatePinDialog()

            R.id.nav_restauracion -> {
                // Iniciar la actividad correspondiente para la restauración de datos
                val intent = Intent(this, RestauracionActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }

            R.id.nav_respaldo -> {
                // Iniciar BackupActivity al seleccionar "Respaldo de datos"
                val intent = Intent(this, BackupActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }

            R.id.nav_resumenes -> {
                // Iniciar ResMensualesActivity al seleccionar "Resúmenes mensuales"
                val intent = Intent(this, ResMensualCCFActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }

            R.id.nav_configuracion ->{
                // Iniciar ResMensualesActivity al seleccionar "Conf.Datos del contribuyente"
                val intent = Intent(this, InfoEmisorActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
            R.id.nav_confiHacienda ->{
                // Iniciar ResMensualesActivity al seleccionar "Conf.Datos del contribuyente"
                val intent = Intent(this, ConfHacienda::class.java)
                startActivity(intent)
                finish()
                return true
            }

            R.id.nav_registro -> {
                // Iniciar ReClienteActivity al seleccionar "Registro del cliente"
                val intent = Intent(this, ImportarClientes::class.java)
                intent.putExtra("letra", "s")
                startActivity(intent)
                finish()
                return true
            }

            R.id.nav_cerrar_sesion -> {
                // Cerrar sesión: iniciar LoginActivity y finalizar MenuActivity
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showCreatePinDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Crear Nuevo PIN")

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.filters = arrayOf(InputFilter.LengthFilter(6)) // Limita la entrada a 6 caracteres
        builder.setView(input)

        builder.setPositiveButton("Guardar") { dialog, _ ->
            val newPin = input.text.toString()
            if (newPin.length == 6) {
                savePin(newPin)
                Toast.makeText(this, "Nuevo PIN guardado correctamente", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "El PIN debe tener exactamente 6 dígitos", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun savePin(newPin: String) {
        val savedPins = loadPins()
        savedPins.add(newPin)
        savePins(savedPins)
    }

    private fun loadPins(): MutableList<String> {
        val sharedPreferences = getSharedPreferences(PIN_PREFS_NAME, MODE_PRIVATE)
        val pinsString = sharedPreferences.getString("pins", null)
        return if (pinsString != null) {
            Gson().fromJson(pinsString, object : TypeToken<List<String>>() {}.type)
        } else {
            mutableListOf()
        }
    }

    private fun savePins(pins: MutableList<String>) {
        val sharedPreferences = getSharedPreferences(PIN_PREFS_NAME, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("pins", Gson().toJson(pins))
        editor.apply()
    }
}
