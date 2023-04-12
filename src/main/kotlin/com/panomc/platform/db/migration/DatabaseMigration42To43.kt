package com.panomc.platform.db.migration

import com.panomc.platform.annotation.Migration
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.DatabaseMigration
import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.SqlClient

@Migration
class DatabaseMigration42To43(databaseManager: DatabaseManager) : DatabaseMigration(databaseManager) {
    override val FROM_SCHEME_VERSION = 42
    override val SCHEME_VERSION = 43
    override val SCHEME_VERSION_INFO =
        "Add last_panel_activity_time field to user table."

    override val handlers: List<suspend (sqlClient: SqlClient) -> Unit> =
        listOf(
            addLastPanelActivityTimeFieldToUserTable(),
        )

    private fun addLastPanelActivityTimeFieldToUserTable(): suspend (sqlClient: SqlClient) -> Unit =
        { sqlClient: SqlClient ->
            sqlClient
                .query("ALTER TABLE `${getTablePrefix()}user` ADD `last_panel_activity_time` BIGINT NOT NULL DEFAULT 0;")
                .execute()
                .await()
        }
}