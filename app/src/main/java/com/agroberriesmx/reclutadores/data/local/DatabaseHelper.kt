package com.agroberriesmx.reclutadores.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.agroberriesmx.reclutadores.domain.model.LoginModel
import com.agroberriesmx.reclutadores.domain.model.RecordModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "reclutadores.db"
        private const val DATABASE_VERSION = 4 // si sealizan updates a la DB hay subir el número de version: RICARDO DIMAS 29/10/2025

        private const val CREATE_TABLE_LOGINS = """
            CREATE TABLE genlogin (
                controlLog INTEGER PRIMARY KEY AUTOINCREMENT,
                vNombreUsu TEXT,
                cCodigoUsu TEXT,
                vPasswordUsu TEXT
            )
        """

        private const val CREATE_TABLE_CANDIDATOS = """
            CREATE TABLE zCandidatos (
                controlLog INTEGER PRIMARY KEY AUTOINCREMENT,
                nReclutador TEXT,
                lOrigen TEXT,
                nCandidato TEXT,
                -- ⭐ CAMPO MODIFICADO: Guardará la ruta local/URL de la imagen del INE
                ineDoc TEXT,
                dCurp TEXT,
                dRFC TEXT,
                dActa TEXT,
                dNSS TEXT,
                cBanco TEXT,
                cSF TEXT,
                dFechaRegistro TEXT,
                dFechaCreacion TEXT,
                cPagado TEXT,
                isDocUploaded INTEGER DEFAULT 0,  -- 0: Pendiente de subir / 1: Subido
                docServerUrl TEXT,              -- Guarda la URL del servidor (la que enviarás a la API)
                isSynced INTEGER DEFAULT 0
            )
        """

        // ⭐ NUEVA TABLA PARA RECLUTADORES ⭐
        private const val CREATE_TABLE_ORGANIGRAMA = """
            CREATE TABLE rhu_organization_chart (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                c_codigo_org TEXT,
                v_nombre_org TEXT
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_LOGINS)
        db.execSQL(CREATE_TABLE_CANDIDATOS)
        db.execSQL(CREATE_TABLE_ORGANIGRAMA) // 👈 Agregado
    }

    private fun dropTables(db: SQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS genlogin")
        db.execSQL("DROP TABLE IF EXISTS zCandidatos")
        db.execSQL("DROP TABLE IF EXISTS rhu_organization_chart") // 👈 Agregado
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        dropTables(db)
        onCreate(db)
    }

    //Credentials
    fun getUserByCodeAndPassword(cCodigoUsu: String, vPasswordUsu: String): LoginModel? {
        val db = this.writableDatabase
        return try {
            db.query(
                "genlogin",
                null,
                "cCodigoUsu = ? AND vPasswordUsu = ?",
                arrayOf(cCodigoUsu, vPasswordUsu),
                null,
                null,
                null,
                "1"
            ).use { cursor ->
                if (cursor.moveToFirst()) {
                    LoginModel(
                        //controlLog = cursor.getLong(cursor.getColumnIndexOrThrow("controlLog")),
                        vNombreUsu = cursor.getString(cursor.getColumnIndexOrThrow("vNombreUsu")),
                        cCodigoUsu = cursor.getString(cursor.getColumnIndexOrThrow("cCodigoUsu")),
                        vPasswordUsu = cursor.getString(cursor.getColumnIndexOrThrow("vPasswordUsu"))
                    )
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.e("DBError", "Error fetching user: ${e.message}")
            null
        }
    }

    fun getAllUsers(): List<LoginModel> {
        val db = this.readableDatabase
        val users = mutableListOf<LoginModel>()

        return try {
            db.query(
                "genlogin",
                null,
                null,
                null,
                null,
                null,
                null
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    val user = LoginModel(
                        //controlLog = cursor.getLong(cursor.getColumnIndexOrThrow("controlLog")),
                        vNombreUsu = cursor.getString(cursor.getColumnIndexOrThrow("vNombreUsu")),
                        cCodigoUsu = cursor.getString(cursor.getColumnIndexOrThrow("cCodigoUsu")),
                        vPasswordUsu = cursor.getString(cursor.getColumnIndexOrThrow("vPasswordUsu"))
                    )
                    users.add(user)
                    Log.d("DBUSER", "User: $user")
                }
            }
            users
        } catch (e: Exception) {
            Log.e("DBError", "Error fetching users: ${e.message}")
            emptyList()
        }
    }

    fun insertUser(
        vNombreUsu: String,
        cCodigoUsu: String,
        vPasswordUsu: String
    ): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("vNombreUsu", vNombreUsu)
            put("cCodigoUsu", cCodigoUsu)
            put("vPasswordUsu", vPasswordUsu)
        }

        return db.insert("genlogin", null, values).also {
            //db.close()
        }
    }

    fun deleteAllUsers() {
        val db = this.writableDatabase
        try {
            db.execSQL("DELETE FROM genlogin")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            //db.close()
        }
    }

    /**
     * Inserta un nuevo candidato en la tabla zCandidatos.
     * Se asume que el documento (INE) ya ha sido guardado localmente,
     * y se inserta solo la ruta del archivo.
     *
     * @param nReclutador ID del reclutador.
     * @param lOrigen Lugar de origen.
     * @param nCandidato Nombre del candidato.
     * @param ineDocPath Ruta absoluta del archivo del documento INE en el dispositivo.
     * @param dCurp CURP.
     * @param dRFC RFC.
     * @param dActa Acta.
     * @param dNSS NSS.
     * @param cBanco Banco.
     * @param cSF SF.
     * @param dFechaRegistro Fecha de registro.
     * @param cPagado Pagado.
     * @return El ID de la fila insertada, o -1 si hubo un error.
     */
    fun insertCandidato(
        nReclutador: String,
        lOrigen: String,
        nCandidato: String,
        ineDocPath: String, // ⭐ El nombre del parámetro de la función (puede ser cualquiera)
        dCurp: String,
        dRFC: String,
        dActa: String,
        dNSS: String,
        cBanco: String,
        cSF: String,
        dFechaRegistro: String
    ): Long {
        val db = this.writableDatabase

        // Usamos ContentValues para mapear las columnas a sus valores
        val values = ContentValues().apply {
            put("nReclutador", nReclutador)
            put("lOrigen", lOrigen)
            put("nCandidato", nCandidato)

            // ⭐⭐ SOLUCIÓN: Cambiar la clave de "ineDocPath" a "ineDoc" ⭐⭐
            put("ineDoc", ineDocPath) // <-- ¡ESTA ES LA LÍNEA CLAVE!

            put("dCurp", dCurp)
            put("dRFC", dRFC)
            put("dActa", dActa)
            put("dNSS", dNSS)
            put("cBanco", cBanco)
            put("cSF", cSF)
            put("dFechaRegistro", dFechaRegistro)
            put("cPagado", 0)

            // Campos de control de sincronización:
            put("isDocUploaded", 0)
            put("isSynced", 0)
        }

        // Ejecutar la inserción y manejar el resultado
        return try {
            db.insertOrThrow("zCandidatos", null, values) // Usamos insertOrThrow para manejo de errores
        } catch (e: Exception) {
            Log.e("DBError", "Fallo al insertar candidato: ${e.message}")
            -1L // Devolver -1 si hay error
        } finally {
            // No es necesario cerrar db aquí si es un singleton o si se maneja en otro lugar,
            // pero por seguridad, si la abres en esta función, ciérrala aquí.
            //db.close()
        }
    }

    /**
     * Obtiene todos los candidatos que aún no han sido sincronizados (isSynced = 0).
     *
     * @return Una lista de Mapas, donde cada mapa contiene los datos de un candidato pendiente.
     */
    fun getUnsyncedCandidatos(): List<ContentValues> {
        val db = this.readableDatabase
        val unsyncedCandidates = mutableListOf<ContentValues>()

        // ⭐⭐ FILTRO CLAVE: isSynced = 0 ⭐⭐
        val selection = "isSynced = ?"
        val selectionArgs = arrayOf("0")

        // Columnas que queremos recuperar (todas las columnas)
        val columns = arrayOf(
            "controlLog", "nReclutador", "lOrigen", "nCandidato",
            "ineDoc", "dCurp", "dRFC", "dActa", "dNSS", "cBanco", "cSF", "cPagado",
            "isDocUploaded", "docServerUrl", "isSynced"
        )

        return try {
            db.query(
                "zCandidatos",
                columns,
                selection,
                selectionArgs,
                null,
                null,
                "controlLog ASC" // Ordenar por ID para procesarlos en orden
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    val candidateData = ContentValues().apply {
                        // Nota: Usamos getColumnIndexOrThrow para obtener el índice de forma segura
                        put("controlLog", cursor.getLong(cursor.getColumnIndexOrThrow("controlLog")))
                        put("nReclutador", cursor.getString(cursor.getColumnIndexOrThrow("nReclutador")))
                        put("lOrigen", cursor.getString(cursor.getColumnIndexOrThrow("lOrigen")))
                        put("nCandidato", cursor.getString(cursor.getColumnIndexOrThrow("nCandidato")))
                        put("ineDoc", cursor.getString(cursor.getColumnIndexOrThrow("ineDoc"))) // ⭐ Ruta local del INE
                        put("dCurp", cursor.getString(cursor.getColumnIndexOrThrow("dCurp")))
                        put("dRFC", cursor.getString(cursor.getColumnIndexOrThrow("dRFC")))
                        put("dActa", cursor.getString(cursor.getColumnIndexOrThrow("dActa")))
                        put("dNSS", cursor.getString(cursor.getColumnIndexOrThrow("dNSS")))
                        put("cBanco", cursor.getString(cursor.getColumnIndexOrThrow("cBanco")))
                        put("cSF", cursor.getString(cursor.getColumnIndexOrThrow("cSF")))
                        put("cPagado", cursor.getString(cursor.getColumnIndexOrThrow("cPagado")))
                        put("isDocUploaded", cursor.getInt(cursor.getColumnIndexOrThrow("isDocUploaded")))
                        // docServerUrl podría ser null
                        put("docServerUrl", cursor.getString(cursor.getColumnIndexOrThrow("docServerUrl")))
                        put("isSynced", cursor.getInt(cursor.getColumnIndexOrThrow("isSynced")))
                    }
                    unsyncedCandidates.add(candidateData)
                }
            }
            unsyncedCandidates
        } catch (e: Exception) {
            Log.e("DBError", "Error al obtener candidatos no sincronizados: ${e.message}")
            emptyList()
        } finally {
            //db.close()
        }
    }

    fun markAllPendingAsSynced(): Int {
        val db = this.writableDatabase

        // 1. Establecer el valor a 1 (Sincronizado)
        val values = ContentValues().apply {
            put("isSynced", 1)
        }

        // 2. Definir la condición: WHERE isSynced = 0 (Solo actualiza los pendientes)
        val selection = "isSynced = ?"
        val selectionArgs = arrayOf("0")

        // 3. Ejecutar la actualización masiva
        // db.update devuelve el número de filas afectadas
        val rowsUpdated = db.update(
            "zCandidatos", // Nombre de tu tabla
            values,
            selection,     // Condición para seleccionar solo los isSynced = 0
            selectionArgs
        )

        //db.close()

        // Devuelve el número de candidatos que fueron marcados como sincronizados
        return rowsUpdated
    }

    fun getRegisteredCountForSession(reclutador: String, lugar: String): Int {
        val db = this.readableDatabase
        var count = 0

        // 1. Definir la condición: nReclutador = ? AND lOrigen = ?
        // Esto asegura que solo contamos los registros de la sesión actual.
        val selection = "nReclutador = ? AND lOrigen = ?"
        val selectionArgs = arrayOf(reclutador, lugar)

        // Solo necesitamos contar las filas (usamos COUNT(*))
        val cursor = db.query(
            "zCandidatos",
            arrayOf("COUNT(*) AS count"), // Pedimos solo el conteo
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        cursor.use { c ->
            if (c.moveToFirst()) {
                val columnIndex = c.getColumnIndex("count")
                if (columnIndex != -1) {
                    count = c.getInt(columnIndex)
                }
            }
        }

        //db.close()
        return count
    }

    fun getUnsynchronizedRecords(): List<RecordModel>? {
        val db = this.readableDatabase

        // La consulta es correcta para filtrar isSynced = 0
        val selection = "isSynced = ?"
        val selectionArgs = arrayOf("0")

        // Usamos el bloque use {} con el cursor para asegurarnos que se cierra
        return try {
            db.query(
                "zCandidatos",
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
            ).use { cursor ->
                val records = mutableListOf<RecordModel>()

                if (cursor.moveToFirst()) {
                    do {
                        // El mapeo completo que incluiste (asumiendo que RecordModel ya tiene controlLog y cPagado)
                        val record = RecordModel(
                            controlLog = cursor.getLong(cursor.getColumnIndexOrThrow("controlLog")),

                            // Datos de Sesión (Son obligatorios, pero añadimos ?: "" por seguridad)
                            nReclutador = cursor.getString(cursor.getColumnIndexOrThrow("nReclutador")) ?: "",
                            lOrigen = cursor.getString(cursor.getColumnIndexOrThrow("lOrigen")) ?: "",
                            nCandidato = cursor.getString(cursor.getColumnIndexOrThrow("nCandidato")) ?: "",

                            // Documentación (ineDoc ya es String? en el modelo, por lo que no necesita el ?: "")
                            ineDoc = cursor.getString(cursor.getColumnIndexOrThrow("ineDoc")),

                            // ⭐⭐ CORRECCIÓN CRÍTICA: Aquí ocurre el NullPointerException
                            dCurp = cursor.getString(cursor.getColumnIndexOrThrow("dCurp")) ?: "", // Línea sospechosa
                            dRFC = cursor.getString(cursor.getColumnIndexOrThrow("dRFC")) ?: "",
                            dActa = cursor.getString(cursor.getColumnIndexOrThrow("dActa")) ?: "",
                            dNSS = cursor.getString(cursor.getColumnIndexOrThrow("dNSS")) ?: "",
                            cBanco = cursor.getString(cursor.getColumnIndexOrThrow("cBanco")) ?: "",
                            cSF = cursor.getString(cursor.getColumnIndexOrThrow("cSF")) ?: "",

                            cPagado = cursor.getString(cursor.getColumnIndexOrThrow("cPagado")) ?: "",

                            // Tiempos
                            dFechaRegistro = cursor.getString(cursor.getColumnIndexOrThrow("dFechaRegistro")) ?: "",
                            dFechaCreacion = cursor.getString(cursor.getColumnIndexOrThrow("dFechaCreacion")) ?: "", // Línea sospechosa

                            // Banderas de Sincronización (int es seguro)
                            isDocUploaded = cursor.getInt(cursor.getColumnIndexOrThrow("isDocUploaded")),
                            docServerUrl = cursor.getString(cursor.getColumnIndexOrThrow("docServerUrl")), // Es String?
                            isSynced = cursor.getInt(cursor.getColumnIndexOrThrow("isSynced"))
                        )
                        records.add(record)
                    } while (cursor.moveToNext())
                }
                if (records.isNotEmpty()) records else null

            } // El cursor se cierra automáticamente gracias a .use {}
        } catch (e: Exception) {
            Log.e("PruebaDB", "FATAL MAPPING/QUERY ERROR: ${e.message}", e)
            null
        }
    }

    fun updateCandidateRecord(
        controlLog: Int, // Identificador único de tu tabla zCandidatos
        nReclutador: String,
        lOrigen: String,
        nCandidato: String,
        ineDoc: String?, // Puede ser nulo
        dCurp: String,
        dRFC: String,
        dActa: String,
        dNSS: String,
        cBanco: String,
        cSF: String,
        dFechaRegistro: String, // La fecha que el usuario registró
        dFechaCreacion: String, // La fecha de creación local
        cPagado: String,
        isDocUploaded: Int,     // Estado de la subida del documento
        docServerUrl: String?,  // URL del documento en el servidor (puede ser nulo)
        isSynced: Int           // Estado de sincronización con la API
    ): Int {
        val db = this.writableDatabase // Obtén una instancia de la base de datos
        val values = ContentValues().apply {
            // Mapeo de parámetros a columnas de la tabla zCandidatos
            put("nReclutador", nReclutador)
            put("lOrigen", lOrigen)
            put("nCandidato", nCandidato)
            put("ineDoc", ineDoc)
            put("dCurp", dCurp)
            put("dRFC", dRFC)
            put("dActa", dActa)
            put("dNSS", dNSS)
            put("cBanco", cBanco)
            put("cSF", cSF)
            put("dFechaRegistro", dFechaRegistro)
            put("dFechaCreacion", dFechaCreacion)
            put("cPagado", cPagado)
            put("isDocUploaded", isDocUploaded)
            put("docServerUrl", docServerUrl)
            put("isSynced", isSynced)
        }

        // El WHERE clause utiliza 'controlLog' que es tu PRIMARY KEY para identificar la fila.
        return try {
            val rowsAffected = db.update(
                "zCandidatos", // 👈 Nombre de tu tabla de candidatos
                values,
                "controlLog = ?", // 👈 Cláusula WHERE con tu PRIMARY KEY
                arrayOf(controlLog.toString()) // 👈 Argumento para la cláusula WHERE
            )
            Log.d("DbHelper", "Candidato con controlLog $controlLog actualizado. Filas afectadas: $rowsAffected")
            rowsAffected
        } catch (e: Exception) {
            Log.e("Database Error", "Error al actualizar registro de candidato (controlLog: $controlLog): ${e.message}")
            0 // Retorna 0 en caso de error
        } finally {
            db.close() // Siempre cierra la base de datos
        }
    }

    // ⭐ MÉTODOS PARA RECLUTADORES (API -> LOCAL) ⭐
    fun saveReclutadoresLocal(lista: List<com.agroberriesmx.reclutadores.domain.model.ReclutadoresModel>) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            // Limpiamos para no duplicar datos
            db.delete("rhu_organization_chart", null, null)

            lista.forEach { reclutador ->
                val values = ContentValues().apply {
                    put("c_codigo_org", reclutador.cCodigoOrg)
                    put("v_nombre_org", reclutador.vNombreOrg)
                }
                db.insert("rhu_organization_chart", null, values)
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e("DBError", "Error al guardar reclutadores: ${e.message}")
        } finally {
            db.endTransaction()
        }
    }

    fun getAllReclutadoresLocal(): List<com.agroberriesmx.reclutadores.domain.model.ReclutadoresModel> {
        val lista = mutableListOf<com.agroberriesmx.reclutadores.domain.model.ReclutadoresModel>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT c_codigo_org, v_nombre_org FROM rhu_organization_chart", null)

        return try {
            cursor.use { c ->
                if (c.moveToFirst()) {
                    do {
                        lista.add(
                            com.agroberriesmx.reclutadores.domain.model.ReclutadoresModel(
                                cCodigoOrg = c.getString(0),
                                vNombreOrg = c.getString(1)
                            )
                        )
                    } while (c.moveToNext())
                }
            }
            lista
        } catch (e: Exception) {
            Log.e("DBError", "Error al leer reclutadores: ${e.message}")
            emptyList()
        }
    }
}