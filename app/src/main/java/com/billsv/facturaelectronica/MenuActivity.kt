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
import android.content.Intent
import android.text.InputFilter
import android.text.InputType
import android.view.View
import androidx.fragment.app.Fragment
import com.couchbase.lite.Database
import android.util.Log
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.widget.ArrayAdapter
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import com.couchbase.lite.*
import android.widget.Button
import android.widget.ListView
import android.view.ViewGroup
import android.widget.ImageButton

class MenuActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var drawerLayout: DrawerLayout
    private val PIN_PREFS_NAME = "pins_prefs"
    private lateinit var database: Database
    private var pinsListDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = application as MyApp
        database = app.database

        setContentView(R.layout.activity_menu)
        // Configura tu NavigationView
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

// Encuentra la vista del toggle dentro del toolbar
        val toggleView = toolbar.getChildAt(1) as View

        /*TapTargetView.showFor(
            this, // La Activity o Fragment
            TapTarget.forView(
                toggleView, // Aquí se usa la vista del toggle encontrada
                "Este es un botón", "Aquí puedes hacer clic para acceder al menú"
            )
                .outerCircleColor(R.color.verde_menta_2) // Color del círculo exterior
                .outerCircleAlpha(0.2f) // Transparencia del círculo exterior
                .targetCircleColor(R.color.verde_menta_1) // Color del círculo objetivo
                .titleTextSize(20) // Tamaño del texto del título
                .titleTextColor(R.color.white) // Color del texto del título
                .descriptionTextSize(16) // Tamaño del texto de la descripción
                .descriptionTextColor(R.color.white) // Color del texto de la descripción
                .textColor(R.color.white) // Color del texto
                .dimColor(R.color.black) // Color de atenuación del fondo
                .drawShadow(true) // Dibujar sombra debajo del TapTarget
                .cancelable(false) // Hacer que el TapTarget no se pueda cancelar con un toque fuera
                .tintTarget(true) // Tintar el objetivo
                .transparentTarget(true), // Hacer transparente el objetivo
            object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView?) {
                    super.onTargetClick(view)
                    // Acción a realizar cuando se toca el TapTarget
                    drawerLayout.openDrawer(GravityCompat.START) // Abre el drawer
                }
            }
        )*/
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

            R.id.nav_crear_pin -> showPinsDialog()

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
                val intent = Intent(this, ResumenesActivity::class.java)
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

    private fun showPinsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_recover_pin, null)
        val nameCommercialEditText: EditText = dialogView.findViewById(R.id.nameCommercialEditText)
        val nrcEditText: EditText = dialogView.findViewById(R.id.nrcEditText)

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Administrar PINs")
        builder.setView(dialogView)
        builder.setCancelable(false)

        // Botón Cancelar
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        // Botón Comprobar
        builder.setPositiveButton("Comprobar", null) // Lo dejamos en null para manejarlo luego
        val dialog = builder.create()

        dialog.show()

        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setOnClickListener {
            val nameCommercial = nameCommercialEditText.text.toString()
            val nrc = nrcEditText.text.toString()

            // Validar datos introducidos
            if (validateData(nameCommercial, nrc)) {
                // Si los datos son correctos, mostrar un cuadro de diálogo con la lista de PINs
                showPinsListDialog()
                dialog.dismiss() // Cerramos el diálogo actual
            } else {
                // Si los datos son incorrectos, mostrar un mensaje de error
                Toast.makeText(this, "Datos incorrectos. Inténtelo nuevamente.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Validar los datos introducidos contra los almacenados en la base de datos
    private fun validateData(nameCommercial: String, nrc: String): Boolean {
        val query = QueryBuilder.select(SelectResult.expression(Meta.id))
            .from(DataSource.database(database))
            .where(
                Expression.property("tipo").equalTo(Expression.string("ConfEmisor"))
                    .and(Expression.property("nombreC").equalTo(Expression.string(nameCommercial)))
                    .and(Expression.property("nrc").equalTo(Expression.string(nrc)))
            )

        try {
            val resultSet = query.execute()
            return resultSet.count() > 0  // Si se encuentra el documento, los datos son correctos
        } catch (e: CouchbaseLiteException) {
            Log.e("PinManager", "Error al validar los datos de recuperación de PIN", e)
            return false
        }
    }

    private fun showPinsListDialog() {
        val pinManager = PinManager(this)

        // Obtener la lista de PINs almacenados
        val pinsList = pinManager.loadPins()

        // Mostrar el diálogo con la lista de PINs
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Lista de PINs")

        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_pin_list, null)
        val listView = dialogView.findViewById<ListView>(R.id.pinsListView)

        // Adaptador personalizado para la lista de PINs
        val adapter = object : ArrayAdapter<String>(this, R.layout.pin_list_item, pinsList) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: inflater.inflate(R.layout.pin_list_item, parent, false)
                val pinTextView = view.findViewById<TextView>(R.id.pinTextView)
                val editButton = view.findViewById<ImageButton>(R.id.editButton)
                val deleteButton = view.findViewById<ImageButton>(R.id.deleteButton)

                val pin = getItem(position)
                pinTextView.text = pin

                // Asignar funcionalidad al botón de eliminar
                deleteButton.setOnClickListener {
                    showDeletePinDialog(pin!!, pinManager)
                }

                // Asignar funcionalidad al botón de editar
                editButton.setOnClickListener {
                    // Cerrar el diálogo de lista de PINs antes de editar
                    pinsListDialog?.dismiss()
                    showEditPinDialog(pin!!, pinManager)
                }

                return view
            }
        }

        listView.adapter = adapter

        // Añadir la vista al diálogo
        builder.setView(dialogView)

        // Botón para crear nuevo PIN
        builder.setPositiveButton("Crear nuevo PIN") { dialog, _ ->
            // Llamar a la función para crear un nuevo PIN
            showCreatePinDialog(pinManager)
        }

        // Botón de cerrar
        builder.setNegativeButton("Cerrar") { dialog, _ -> dialog.dismiss() }

        // Crear y mostrar el diálogo, y guardar la referencia
        pinsListDialog = builder.create()
        pinsListDialog?.show()
    }

    private fun showCreatePinDialog(pinManager: PinManager) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Crear nuevo PIN")

        // Crear una vista para el diálogo con un EditText
        val input = EditText(this)
        input.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(6))
        input.inputType = InputType.TYPE_CLASS_NUMBER
        builder.setView(input)

        // Botón para confirmar la creación
        builder.setPositiveButton("Crear") { dialog, _ -> }

        // Botón de cancelar
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }

        // Mostrar el diálogo
        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val newPin = input.text.toString()

                // Validar si el nuevo PIN tiene exactamente 6 dígitos
                if (newPin.length == 6) {
                    // Agregar el nuevo PIN en la base de datos
                    pinManager.addPin(newPin)

                    // Notificar al usuario que el PIN fue creado
                    Toast.makeText(this, "Nuevo PIN creado", Toast.LENGTH_SHORT).show()

                    // Cerrar el diálogo de creación
                    dialog.dismiss()

                    // Volver a mostrar la lista de PINs actualizada
                    showPinsListDialog()
                } else {
                    Toast.makeText(this, "El PIN debe tener exactamente 6 dígitos", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    private fun showDeletePinDialog(pin: String, pinManager: PinManager) {
        // Cierra el diálogo de la lista de PINs antes de mostrar el diálogo de eliminación
        pinsListDialog?.dismiss()

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Eliminar PIN")
        builder.setMessage("¿Estás seguro de que deseas eliminar este PIN?")

        builder.setPositiveButton("Eliminar") { dialog, _ ->
            // Eliminar el PIN de la base de datos
            pinManager.removePin(pin)

            // Notificar al usuario que el PIN fue eliminado
            Toast.makeText(this, "PIN eliminado", Toast.LENGTH_SHORT).show()

            // Volver a mostrar la lista de PINs actualizada
            showPinsListDialog()
            dialog.dismiss() // Opcional: puedes dejar esto si deseas cerrarlo
        }

        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }

        builder.show()
    }


    private fun showEditPinDialog(pin: String, pinManager: PinManager) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Editar PIN")

        // Crear una vista para el diálogo con un EditText
        val input = EditText(this)
        input.setText(pin) // Prellenar con el valor actual del PIN

        // Limitar la longitud máxima a 6 caracteres
        input.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(6))

        // Configurar el EditText para que acepte solo números y muestre el teclado numérico
        input.inputType = InputType.TYPE_CLASS_NUMBER

        builder.setView(input)

        // Botón para confirmar la edición
        builder.setPositiveButton("Guardar") { dialog, _ -> }

        // Botón de cancelar
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.dismiss() }

        // Mostrar el diálogo
        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val newPin = input.text.toString()

                // Validar si el nuevo PIN tiene exactamente 6 dígitos
                if (newPin.length == 6) {
                    // Actualizar el PIN en la base de datos
                    pinManager.updatePin(pin, newPin)

                    // Notificar al usuario que el PIN fue actualizado
                    Toast.makeText(this, "PIN actualizado", Toast.LENGTH_SHORT).show()

                    // Cerrar el diálogo de edición
                    dialog.dismiss()

                    // Cerrar el diálogo de la lista de PINs si está abierto
                    pinsListDialog?.dismiss()

                    // Mostrar nuevamente la lista de PINs
                    showPinsListDialog()
                } else {
                    Toast.makeText(this, "El PIN debe tener exactamente 6 dígitos", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

}