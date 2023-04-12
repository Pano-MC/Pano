package com.panomc.platform.db.migration

import com.panomc.platform.annotation.Migration
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.DatabaseMigration
import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.SqlClient

@Migration
class DatabaseMigration31To32(databaseManager: DatabaseManager) : DatabaseMigration(databaseManager) {
    override val FROM_SCHEME_VERSION = 31
    override val SCHEME_VERSION = 32
    override val SCHEME_VERSION_INFO =
        "Drop image field & add thumbnail_url field in post table."

    override val handlers: List<suspend (sqlClient: SqlClient) -> Unit> =
        listOf(
            dropImageFieldInPostTable(),
            addThumbnailUrlToPostTable()
        )

    private fun dropImageFieldInPostTable(): suspend (sqlClient: SqlClient) -> Unit =
        { sqlClient: SqlClient ->
            sqlClient
                .query("ALTER TABLE `${getTablePrefix()}post` DROP COLUMN `image`;")
                .execute()
                .await()
        }

    private fun addThumbnailUrlToPostTable(): suspend (sqlClient: SqlClient) -> Unit =
        { sqlClient: SqlClient ->
            sqlClient
                .query("ALTER TABLE `${getTablePrefix()}post` ADD `thumbnail_url` mediumtext not null;")
                .execute()
                .await()
        }
}