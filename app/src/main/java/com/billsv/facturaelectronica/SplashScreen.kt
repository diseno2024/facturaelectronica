package com.billsv.facturaelectronica

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.billsv.facturaelectronica.appintro.MyCustomAppIntro
import com.couchbase.lite.CouchbaseLiteException
import com.couchbase.lite.DataSource
import com.couchbase.lite.Database
import com.couchbase.lite.Expression
import com.couchbase.lite.Meta
import com.couchbase.lite.MutableDocument
import com.couchbase.lite.QueryBuilder
import com.couchbase.lite.SelectResult

class SplashScreen : AppCompatActivity() {
    private var condicion = "no"
    private lateinit var database: Database
    companion object {
        const val SPLASH_TIMER: Long = 3000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val app = application as MyApp
        database = app.database
        condicion = mostrado()
        Handler().postDelayed({
            if(condicion=="si") {
                val intent = Intent(this@SplashScreen, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }else if(condicion=="no") {
                val intent = Intent(this@SplashScreen, MyCustomAppIntro::class.java)
                startActivity(intent)
                finish()
            }
        }, SPLASH_TIMER)
    }

    private fun mostrado(): String {
        // Crea una consulta para seleccionar todos los documentos con tipo = "cliente"
        val query = QueryBuilder.select(SelectResult.all())
            .from(DataSource.database(database))
            .where(Expression.property("tipo").equalTo(Expression.string("intro")))

        // Ejecuta la consulta
        val result = query.execute()

        // Lista para almacenar los datos obtenidos
        var dataList = "no"

        // Itera sobre todos los resultados de la consulta
        result.allResults().forEach { result ->
            // Obtiene el diccionario del documento del resultado actual
            val dict = result.getDictionary(database.name)

            // Extrae los valores de los campos del documento
            val valor = dict?.getString("valor")
            // Formatea los datos como una cadena y la agrega a la lista
            if (valor != null) {
                dataList = valor
            }
        }

        return dataList
    }

}