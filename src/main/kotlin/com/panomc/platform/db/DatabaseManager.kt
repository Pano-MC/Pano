package com.panomc.platform.db

import com.panomc.platform.config.ConfigManager
import com.panomc.platform.db.migration.*
import com.panomc.platform.util.SetupManager
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import io.vertx.mysqlclient.MySQLConnectOptions
import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.SqlConnection

class DatabaseManager(
    private val mVertx: Vertx,
    private val mLogger: Logger,
    private val mConfigManager: ConfigManager,
    setupManager: SetupManager
) {
    private lateinit var mPool: Pool

    private val mDatabase by lazy {
        Database()
    }

    private val mMigrations by lazy {
        listOf(
            DatabaseMigration_1_2(),
            DatabaseMigration_2_3(),
            DatabaseMigration_3_4(),
            DatabaseMigration_4_5(),
            DatabaseMigration_5_6(),
            DatabaseMigration_6_7(),
            DatabaseMigration_7_8(),
            DatabaseMigration_8_9(),
            DatabaseMigration_9_10(),
            DatabaseMigration_10_11(),
            DatabaseMigration_11_12(),
            DatabaseMigration_12_13(),
            DatabaseMigration_13_14(),
            DatabaseMigration_14_15(),
            DatabaseMigration_15_16(),
            DatabaseMigration_16_17(),
            DatabaseMigration_17_18()
        )
    }

    companion object {
        const val DATABASE_SCHEME_VERSION = 18
        const val DATABASE_SCHEME_VERSION_INFO = "Add banned field to user table."
    }

    init {
        if (setupManager.isSetupDone())
            checkMigration()
    }

    private fun checkMigration() {
        createConnection { sqlConnection, _ ->
            if (sqlConnection != null) {
                mDatabase.schemeVersionDao.getLastSchemeVersion(sqlConnection) { schemeVersion, _ ->
                    if (schemeVersion == null)
                        mLogger.error("Database Error: Database scheme is not correct, please reinstall platform")
                    else {
                        val databaseVersion = schemeVersion.key.toIntOrNull() ?: 0

                        if (databaseVersion == 0)
                            mLogger.error("Database Error: Database scheme is not correct, please reinstall platform")
                        else
                            migrate(sqlConnection, databaseVersion)
                    }
                }
            }
        }
    }

    private fun migrate(sqlConnection: SqlConnection, databaseVersion: Int) {
        val handlers = mMigrations.map { it.migrate(sqlConnection) }

        var currentIndex = 0

        fun invoke() {
            val localHandler: (AsyncResult<*>) -> Unit = {
                fun check() {
                    when {
                        it.failed() -> closeConnection(sqlConnection) {
                            mLogger.error("Database Error: Migration failed from version ${mMigrations[currentIndex].FROM_SCHEME_VERSION} to ${mMigrations[currentIndex].SCHEME_VERSION}")
                        }
                        currentIndex == handlers.lastIndex -> closeConnection(sqlConnection)
                        else -> {
                            currentIndex++

                            invoke()
                        }
                    }
                }

                if (it.succeeded())
                    mMigrations[currentIndex].updateSchemeVersion(sqlConnection)
                        .invoke { updateSchemeVersion ->
                            if (updateSchemeVersion.failed())
                                closeConnection(sqlConnection) {
                                    mLogger.error("Database Error: Migration failed from version ${mMigrations[currentIndex].FROM_SCHEME_VERSION} to ${mMigrations[currentIndex].SCHEME_VERSION}")
                                }
                            else
                                check()
                        }
                else
                    check()
            }

            if (mMigrations[currentIndex].isMigratable(databaseVersion)) {
                if (currentIndex <= handlers.lastIndex)
                    handlers[currentIndex].invoke(localHandler)
            } else if (currentIndex == handlers.lastIndex)
                closeConnection(sqlConnection)
            else {
                currentIndex++

                invoke()
            }
        }

        invoke()
    }

    fun createConnection(handler: (sqlConnection: SqlConnection?, asyncResult: AsyncResult<SqlConnection>) -> Unit) {
        if (!::mPool.isInitialized) {
            val databaseConfig = (mConfigManager.getConfig()["database"] as Map<*, *>)

            var port = 3306
            var host = databaseConfig["host"] as String

            if (host.contains(":")) {
                val splitHost = host.split(":")

                host = splitHost[0]

                port = splitHost[1].toInt()
            }

            val connectOptions = MySQLConnectOptions()
                .setPort(port)
                .setHost(host)
                .setDatabase(databaseConfig["name"] as String)
                .setUser(databaseConfig["username"] as String)

            if (databaseConfig["password"] != "")
                connectOptions.password = databaseConfig["password"] as String

            val poolOptions = PoolOptions()
                .setMaxSize(10)

            mPool = MySQLPool.pool(mVertx, connectOptions, poolOptions)
        }

        mPool.getConnection { getConnection ->
            if (getConnection.succeeded())
                handler.invoke(getConnection.result(), getConnection)
            else {
                mLogger.error("Failed to connect database! Please check your configuration! Error is: ${getConnection.cause()}")

                handler.invoke(null, getConnection)
            }
        }
    }

    fun closeConnection(sqlConnection: SqlConnection, handler: ((asyncResult: AsyncResult<Void?>?) -> Unit)? = null) {
        sqlConnection.close {
            handler?.invoke(it)
        }
    }

    fun initDatabase(sqlConnection: SqlConnection, handler: (asyncResult: AsyncResult<*>) -> Unit = {}) {
        val databaseInitProcessHandlers = mDatabase.init()

        var currentIndex = 0

        fun invoke() {
            val localHandler: (AsyncResult<*>) -> Unit = {
                when {
                    it.failed() || currentIndex == databaseInitProcessHandlers.lastIndex -> closeConnection(
                        sqlConnection
                    ) { _ ->
                        handler.invoke(it)
                    }
                    else -> {
                        currentIndex++

                        invoke()
                    }
                }
            }

            if (currentIndex <= databaseInitProcessHandlers.lastIndex)
                databaseInitProcessHandlers[currentIndex].invoke(sqlConnection, localHandler)
        }

        invoke()
    }

    fun getDatabase() = mDatabase

    fun getTablePrefix() = (mConfigManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()
}