package com.panomc.platform.db.entity

import com.panomc.platform.db.DaoImpl
import com.panomc.platform.db.dao.PostCategoryDao
import com.panomc.platform.db.model.PostCategory
import com.panomc.platform.model.Result
import com.panomc.platform.model.Successful
import io.vertx.core.AsyncResult
import io.vertx.mysqlclient.MySQLClient
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.SqlConnection
import io.vertx.sqlclient.Tuple

class PostCategoryDaoImpl(override val tableName: String = "post_category") : DaoImpl(), PostCategoryDao {

    override fun init(): (sqlConnection: SqlConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> Unit =
        { sqlConnection, handler ->
            sqlConnection
                .query(
                    """
                            CREATE TABLE IF NOT EXISTS `${getTablePrefix() + tableName}` (
                              `id` int NOT NULL AUTO_INCREMENT,
                              `title` MEDIUMTEXT NOT NULL,
                              `description` text NOT NULL,
                              `url` varchar(255) NOT NULL,
                              `color` varchar(6) NOT NULL,
                              PRIMARY KEY (`id`)
                            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Post category table.';
                        """
                )
                .execute {
                    handler.invoke(it)
                }
        }

    override fun isExistsByID(
        id: Int,
        sqlConnection: SqlConnection,
        handler: (exists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM `${getTablePrefix() + tableName}` where `id` = ?"

        sqlConnection
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    id
                )
            ) { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()

                    handler.invoke(rows.toList()[0].getInteger(0) == 1, queryResult)
                } else
                    handler.invoke(null, queryResult)
            }
    }

    override fun deleteByID(
        id: Int,
        sqlConnection: SqlConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "DELETE FROM `${getTablePrefix() + tableName}` WHERE id = ?"

        sqlConnection
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    id
                )
            ) { queryResult ->
                if (queryResult.succeeded())
                    handler.invoke(Successful(), queryResult)
                else
                    handler.invoke(null, queryResult)
            }
    }

    override fun getCount(sqlConnection: SqlConnection, handler: (count: Int?, asyncResult: AsyncResult<*>) -> Unit) {
        val query =
            "SELECT COUNT(id) FROM `${getTablePrefix() + tableName}`"

        sqlConnection
            .preparedQuery(query)
            .execute { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()

                    handler.invoke(rows.toList()[0].getInteger(0), queryResult)
                } else
                    handler.invoke(null, queryResult)
            }
    }

    override fun getCategories(
        sqlConnection: SqlConnection,
        handler: (categories: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT id, title, description, url, color FROM `${getTablePrefix() + tableName}` ORDER BY id DESC"

        sqlConnection
            .preparedQuery(query)
            .execute { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()
                    val categories = mutableListOf<Map<String, Any>>()

                    if (rows.size() > 0)
                        rows.forEach { row ->
                            categories.add(
                                mapOf(
                                    "id" to row.getInteger(0),
                                    "title" to row.getString(1),
                                    "description" to row.getString(2),
                                    "url" to row.getString(3),
                                    "color" to row.getString(4)
                                )
                            )
                        }

                    handler.invoke(categories, queryResult)
                } else
                    handler.invoke(null, queryResult)
            }
    }

    override fun getCategories(
        page: Int,
        sqlConnection: SqlConnection,
        handler: (categories: List<PostCategory>?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT id, title, description, url, color FROM `${getTablePrefix() + tableName}` ORDER BY id DESC LIMIT 10 OFFSET ${(page - 1) * 10}"

        sqlConnection
            .preparedQuery(query)
            .execute { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()
                    val categories = mutableListOf<PostCategory>()

                    if (rows.size() > 0)
                        rows.forEach { row ->
                            categories.add(
                                PostCategory(
                                    row.getInteger(0),
                                    row.getString(1),
                                    row.getString(2),
                                    row.getString(3),
                                    row.getString(4)
                                )
                            )
                        }

                    handler.invoke(categories, queryResult)
                } else
                    handler.invoke(null, queryResult)
            }
    }

    override fun isExistsByURL(
        url: String,
        sqlConnection: SqlConnection,
        handler: (exists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM `${getTablePrefix() + tableName}` where `url` = ?"

        sqlConnection
            .preparedQuery(query)
            .execute(Tuple.of(url)) { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()

                    handler.invoke(rows.toList()[0].getInteger(0) == 1, queryResult)
                } else
                    handler.invoke(null, queryResult)
            }
    }

    override fun isExistsByURLNotByID(
        url: String,
        id: Int,
        sqlConnection: SqlConnection,
        handler: (exists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM `${getTablePrefix() + tableName}` where `url` = ? and id != ?"

        sqlConnection
            .preparedQuery(query)
            .execute(Tuple.of(url, id)) { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()

                    handler.invoke(rows.toList()[0].getInteger(0) == 1, queryResult)
                } else
                    handler.invoke(null, queryResult)
            }
    }

    override fun add(
        postCategory: PostCategory,
        sqlConnection: SqlConnection,
        handler: (id: Long?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "INSERT INTO `${getTablePrefix() + tableName}` (`title`, `description`, `url`, `color`) VALUES (?, ?, ?, ?)"

        sqlConnection
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    postCategory.title,
                    postCategory.description,
                    postCategory.url,
                    postCategory.color.replace("#", "")
                )
            ) { queryResult ->
                if (queryResult.succeeded()) {
                    val rows: RowSet<Row> = queryResult.result()

                    handler.invoke(rows.property(MySQLClient.LAST_INSERTED_ID), queryResult)
                } else
                    handler.invoke(null, queryResult)
            }
    }

    override fun update(
        postCategory: PostCategory,
        sqlConnection: SqlConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "UPDATE `${getTablePrefix() + tableName}` SET title = ?, description = ?, url = ?, color = ? WHERE `id` = ?"

        sqlConnection
            .preparedQuery(query)
            .execute(
                Tuple.of(
                    postCategory.title,
                    postCategory.description,
                    postCategory.url,
                    postCategory.color.replace("#", ""),
                    postCategory.id
                )
            ) { queryResult ->
                if (queryResult.succeeded())
                    handler.invoke(Successful(), queryResult)
                else
                    handler.invoke(null, queryResult)
            }
    }
}