package com.panomc.platform.db.implementation

import com.panomc.platform.annotation.Dao
import com.panomc.platform.db.dao.PermissionDao
import com.panomc.platform.db.model.Permission
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple

@Dao
class PermissionDaoImpl : PermissionDao() {
    override suspend fun init(sqlClient: SqlClient) {
        sqlClient
            .query(
                """
                            CREATE TABLE IF NOT EXISTS `${getTablePrefix() + tableName}` (
                              `id` bigint NOT NULL AUTO_INCREMENT,
                              `name` varchar(128) NOT NULL UNIQUE,
                              `iconName` varchar(128) NOT NULL DEFAULT '',
                              PRIMARY KEY (`id`)
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Permission Table';
                        """
            )
            .execute()
            .coAwait()

        val permissions = listOf(
            Permission(name = "access_panel", iconName = "fa-cubes"),
            Permission(name = "manage_servers", iconName = "fa-cubes"),
            Permission(name = "manage_posts", iconName = "fa-sticky-note"),
            Permission(name = "manage_tickets", iconName = "fa-ticket-alt"),
            Permission(name = "manage_players", iconName = "fa-users"),
            Permission(name = "manage_view", iconName = "fa-palette"),
            Permission(name = "manage_addons", iconName = "fa-puzzle-piece"),
            Permission(name = "manage_platform_settings", iconName = "fa-cog"),
            Permission(name = "manage_permission_groups", iconName = "fa-lock-open")
        )

        permissions.forEach { add(it, sqlClient) }
    }

    override suspend fun isTherePermission(
        permission: Permission,
        sqlClient: SqlClient
    ): Boolean {
        val query =
            "SELECT COUNT(`name`) FROM `${getTablePrefix() + tableName}` where `name` = ?"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    permission.name
                )
            ).coAwait()

        return rows.toList()[0].getLong(0) != 0L
    }

    override suspend fun isTherePermissionById(
        id: Long,
        sqlClient: SqlClient
    ): Boolean {
        val query =
            "SELECT COUNT(`id`) FROM `${getTablePrefix() + tableName}` where `id` = ?"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    id
                )
            ).coAwait()

        return rows.toList()[0].getLong(0) != 0L
    }

    override suspend fun add(
        permission: Permission,
        sqlClient: SqlClient
    ) {
        val query = "INSERT INTO `${getTablePrefix() + tableName}` (`name`, `iconName`) VALUES (?, ?)"

        sqlClient
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    permission.name,
                    permission.iconName
                )
            ).coAwait()
    }

    override suspend fun getPermissionId(
        permission: Permission,
        sqlClient: SqlClient
    ): Long {
        val query =
            "SELECT id FROM `${getTablePrefix() + tableName}` where `name` = ?"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    permission.name
                )
            ).coAwait()

        return rows.toList()[0].getLong(0)
    }

    override suspend fun getPermissionById(
        id: Long,
        sqlClient: SqlClient
    ): Permission? {
        val query =
            "SELECT `id`, `name`, `iconName` FROM `${getTablePrefix() + tableName}` where `id` = ?"

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

    override suspend fun getPermissions(
        sqlClient: SqlClient
    ): List<Permission> {
        val query =
            "SELECT `id`, `name`, `iconName` FROM `${getTablePrefix() + tableName}`"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute()
            .coAwait()

        return rows.toEntities()
    }

    override suspend fun arePermissionsExist(idList: List<Long>, sqlClient: SqlClient): Boolean {
        var listText = ""

        idList.forEach { permissionId ->
            if (listText == "")
                listText = "'$permissionId'"
            else
                listText += ", '$permissionId'"
        }

        val query =
            "SELECT COUNT(`id`) FROM `${getTablePrefix() + tableName}` where `id` IN ($listText)"

        val rows: RowSet<Row> = sqlClient
            .preparedQuery(query)
            .execute()
            .coAwait()

        return rows.toList()[0].getLong(0) == idList.size.toLong()
    }
}