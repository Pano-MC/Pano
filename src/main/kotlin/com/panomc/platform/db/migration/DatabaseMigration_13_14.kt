package com.panomc.platform.db.migration

import com.panomc.platform.db.DatabaseMigration
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("ClassName")
class DatabaseMigration_13_14 : DatabaseMigration() {
    override val FROM_SCHEME_VERSION = 13
    override val SCHEME_VERSION = 14
    override val SCHEME_VERSION_INFO = "Create ticket_message table."

    override val handlers: List<(sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection> =
        listOf(
            createTicketMessageTable()
        )

    private fun createTicketMessageTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
            CREATE TABLE IF NOT EXISTS `${getTablePrefix()}ticket_message` (
              `id` int NOT NULL AUTO_INCREMENT,
              `user_id` int NOT NULL,              
              `ticket_id` int NOT NULL,
              `message` text NOT NULL,
              `date` MEDIUMTEXT NOT NULL,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Ticket message table.';
        """
            ) {
                handler.invoke(it)
            }
        }
}