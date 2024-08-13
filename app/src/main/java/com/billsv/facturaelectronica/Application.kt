package com.billsv.facturaelectronica
import android.app.Application
import com.couchbase.lite.CouchbaseLite
import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseConfiguration

class MyApp : Application() {
    lateinit var database: Database
    var ambiente: String = "00" // Valor por defecto
    override fun onCreate() {
        super.onCreate()

        // Inicializar Couchbase Lite
        CouchbaseLite.init(applicationContext)

        // Inicializar la base de datos
        val config = DatabaseConfiguration()
        database = Database("my_database", config)
    }

}
