package com.panomc.platform.db.migration

import com.panomc.platform.annotation.Migration
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.DatabaseMigration
import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.SqlClient

@Migration
class DatabaseMigration24To25(databaseManager: DatabaseManager) : DatabaseMigration(databaseManager) {
    override val FROM_SCHEME_VERSION = 24
    override val SCHEME_VERSION = 25
    override val SCHEME_VERSION_INFO =
        "Drop 'public_key' and 'secret_key' columns in user table."

    override val handlers: List<suspend (sqlClient: SqlClient) -> Unit> =
        listOf(
            dropPublicKeyFromUserTable(),
            dropSecretKeyFromUserTable(),
        )

    private fun dropPublicKeyFromUserTable(): suspend (sqlClient: SqlClient) -> Unit =
        { sqlClient: SqlClient ->
            sqlClient
                .query("ALTER TABLE `${getTablePrefix()}user` DROP COLUMN `public_key`;")
                .execute()
                .await()
        }

    private fun dropSecretKeyFromUserTable(): suspend (sqlClient: SqlClient) -> Unit =
        { sqlClient: SqlClient ->
            sqlClient
                .query("ALTER TABLE `${getTablePrefix()}user` DROP COLUMN `secret_key`;")
                .execute()
                .await()
        }
}