package com.panomc.platform.db.implementation

import com.panomc.platform.annotation.Dao
import com.panomc.platform.db.dao.NotificationDao
import com.panomc.platform.db.model.Notification
import com.panomc.platform.notification.NotificationStatus
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple

@Dao
class NotificationDaoImpl : NotificationDao() {

    override suspend fun init(sqlClient: SqlClient) {
        sqlClient
            .query(
                """
                            CREATE TABLE IF NOT EXISTS `${getTablePrefix() + tableName}` (
                              `id` bigint NOT NULL AUTO_INCREMENT,
                              `userId` bigint NOT NULL,
                              `type` varchar(255) NOT NULL,
                              `properties` mediumtext NOT NULL,
                              `date` BIGINT(20) NOT NULL,
                              `status` varchar(255) NOT NULL,
                              PRIMARY KEY (`id`)
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Notification table.';
                        """
            )
            .execute()
            .coAwait()
    }

    override suspend fun add(
        notification: Notification,
        sqlClient: SqlClient
    ) {
        val query =
            "INSERT INTO `${getTablePrefix() + tableName}` (`userId`, `type`, `properties`, `date`, `status`) " +
                    "VALUES (?, ?, ?, ?, ?)"

        sqlClient
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    notification.userId,
                    notification.type,
                    notification.properties.toString(),
                    notification.date,
                    notification.status
                )
            ).coAwait()
    }

    override suspend fun addAll(notifications: List<Notification>, sqlClient: SqlClient) {
        val tuple = Tuple.tuple()
        var listText = ""

        notifications.forEach { notification ->
            tuple.addValue(notification.userId)
            tuple.addValue(notification.type)
            tuple.addValue(notification.properties.toString())
            tuple.addValue(notification.date)
            tuple.addValue(notification.status)

            if (listText == "")
                listText = "(?, ?, ?, ?, ?)"
            else
                listText += ", (?, ?, ?, ?, ?)"
        }

        val query =
            "INSERT INTO `${getTablePrefix() + tableName}` (`userId`, `type`, `properties`, `date`, `status`) " +
                    "VALUES $listText"

        sqlClient
            .preparedQuery(query)
            .execute(tuple)
            .coAwait()
    }

    override suspend fun getCountOfNotReadByUserId(
        userId: Long,
        sqlClient: SqlClient
    ): Long {
        val query =
            "SELECT count(`id`) FROM `${getTablePrefix() + tableName}` WHERE `userId` = ? AND `status` = ? ORDER BY `date` DESC, `id` DESC"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    userId,
                    NotificationStatus.NOT_READ,
                )
            ).coAwait()

        return rows.toList()[0].getLong(0)
    }

    override suspend fun getCountByUserId(
        userId: Long,
        sqlClient: SqlClient
    ): Long {
        val query =
            "SELECT count(`id`) FROM `${getTablePrefix() + tableName}` WHERE `userId` = ? ORDER BY `date` DESC, `id` DESC"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    userId
                )
            ).coAwait()

        return rows.toList()[0].getLong(0)
    }

    override suspend fun getLast10ByUserId(
        userId: Long,
        sqlClient: SqlClient
    ): List<Notification> {
        val query =
            "SELECT `id`, `userId`, `type`, `properties`, `date`, `status` FROM `${getTablePrefix() + tableName}` WHERE `userId` = ? ORDER BY `date` DESC, `id` DESC LIMIT 10"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    userId
                )
            ).coAwait()

        return rows.toEntities()
    }

    override suspend fun get10ByUserIdAndStartFromId(
        userId: Long,
        notificationId: Long,
        sqlClient: SqlClient
    ): List<Notification> {
        val query =
            "SELECT `id`, `userId`, `type`, `properties`, `date`, `status` FROM `${getTablePrefix() + tableName}` WHERE `userId` = ? AND id < ? ORDER BY `date` DESC, `id` DESC LIMIT 10"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    userId,
                    notificationId
                )
            ).coAwait()

        return rows.toEntities()
    }

    override suspend fun markReadLast10(
        userId: Long,
        sqlClient: SqlClient
    ) {
        val query =
            "UPDATE `${getTablePrefix() + tableName}` SET status = ? WHERE `userId` = ? ORDER BY `date` DESC, `id` DESC LIMIT 10"

        sqlClient
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    NotificationStatus.READ,
                    userId
                )
            ).coAwait()
    }

    override suspend fun markReadLast10StartFromId(
        userId: Long,
        notificationId: Long,
        sqlClient: SqlClient
    ) {
        val query =
            "UPDATE `${getTablePrefix() + tableName}` SET status = ? WHERE `userId` = ? AND id < ?  ORDER BY `date` DESC, `id` DESC LIMIT 10"

        sqlClient
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    NotificationStatus.READ,
                    userId,
                    notificationId
                )
            ).coAwait()
    }

    override suspend fun getLast5ByUserId(
        userId: Long,
        sqlClient: SqlClient
    ): List<Notification> {
        val query =
            "SELECT `id`, `userId`, `type`, `properties`, `date`, `status` FROM `${getTablePrefix() + tableName}` WHERE `userId` = ? ORDER BY `date` DESC, `id` DESC LIMIT 5"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    userId
                )
            ).coAwait()

        return rows.toEntities()
    }

    override suspend fun markReadLast5ByUserId(
        userId: Long,
        sqlClient: SqlClient
    ) {
        val query =
            "UPDATE `${getTablePrefix() + tableName}` SET status = ? WHERE `userId` = ? ORDER BY `date` DESC, `id` DESC LIMIT 5"

        sqlClient
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    NotificationStatus.READ,
                    userId
                )
            ).coAwait()
    }

    override suspend fun existsById(
        id: Long,
        sqlClient: SqlClient
    ): Boolean {
        val query = "SELECT COUNT(id) FROM `${getTablePrefix() + tableName}` where `id` = ?"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(Tuple.of(id))
            .coAwait()

        return rows.toList()[0].getLong(0) == 1L
    }

    override suspend fun getById(
        id: Long,
        sqlClient: SqlClient
    ): Notification? {
        val query =
            "SELECT `id`, `userId`, `type`, `properties`, `date`, `status` FROM `${getTablePrefix() + tableName}` WHERE `id` = ? ORDER BY `date` DESC, `id` DESC LIMIT 5"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    id
                )
            ).coAwait()

        if (rows.size() == 0) {
            return null
        }

        val row = rows.toList()[0]

        return row.toEntity()
    }

    override suspend fun deleteById(
        id: Long,
        sqlClient: SqlClient
    ) {
        val query =
            "DELETE FROM `${getTablePrefix() + tableName}` WHERE `id` = ?"

        sqlClient
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    id
                )
            ).coAwait()
    }

    override suspend fun markReadById(id: Long, sqlClient: SqlClient) {
        val query =
            "UPDATE `${getTablePrefix() + tableName}` SET `status` = ? WHERE `id` = ?"

        sqlClient
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    NotificationStatus.READ,
                    id
                )
            ).coAwait()
    }

    override suspend fun deleteAllByUserId(
        userId: Long,
        sqlClient: SqlClient
    ) {
        val query =
            "DELETE FROM `${getTablePrefix() + tableName}` WHERE `userId` = ?"

        sqlClient
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    userId
                )
            )
            .coAwait()
    }
}