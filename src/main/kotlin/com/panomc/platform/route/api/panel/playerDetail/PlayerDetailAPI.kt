package com.panomc.platform.route.api.panel.playerDetail

import com.panomc.platform.ErrorCode
import com.panomc.platform.db.model.Permission
import com.panomc.platform.db.model.Ticket
import com.panomc.platform.db.model.TicketCategory
import com.panomc.platform.db.model.User
import com.panomc.platform.model.*
import io.vertx.core.AsyncResult
import io.vertx.ext.web.RoutingContext
import io.vertx.sqlclient.SqlConnection

class PlayerDetailAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/initPage/playerDetail")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val username = data.getString("username")

        databaseManager.createConnection((this::createConnectionHandler)(handler, username))
    }

    private fun createConnectionHandler(
        handler: (result: Result) -> Unit,
        username: String
    ) = handler@{ sqlConnection: SqlConnection?, _: AsyncResult<SqlConnection> ->
        if (sqlConnection == null) {
            handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

            return@handler
        }

        databaseManager.getDatabase().userDao.isExistsByUsername(
            username,
            sqlConnection,
            (this::isExistsByHandler)(handler, username, sqlConnection)
        )
    }

    private fun isExistsByHandler(
        handler: (result: Result) -> Unit,
        username: String,
        sqlConnection: SqlConnection,
    ) = handler@{ exists: Boolean?, _: AsyncResult<*> ->
        if (exists == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_160))
            }

            return@handler
        }

        if (!exists) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.NOT_EXISTS))
            }

            return@handler
        }

        databaseManager.getDatabase().userDao.getByUsername(
            username,
            sqlConnection,
            (this::getByUsernameHandler)(handler, sqlConnection)
        )
    }

    private fun getByUsernameHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
    ) = handler@{ user: User?, _: AsyncResult<*> ->
        if (user == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_161))
            }

            return@handler
        }

        val result = mutableMapOf<String, Any?>()

        result["player"] = mutableMapOf<String, Any?>(
            "username" to user.username,
            "registerDate" to user.registerDate,
            "isBanned" to user.banned
        )

        databaseManager.getDatabase().permissionDao.getPermissionByID(
            user.permissionID,
            sqlConnection,
            (this::getPermissionByIDHandler)(handler, sqlConnection, result, user)
        )
    }

    private fun getPermissionByIDHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        result: MutableMap<String, Any?>,
        user: User
    ) = handler@{ permission: Permission?, _: AsyncResult<*> ->
        if (permission == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_164))
            }

            return@handler
        }

        @Suppress("UNCHECKED_CAST")
        (result["player"] as MutableMap<String, Any?>)["permission"] = permission.name

        databaseManager.getDatabase().ticketDao.getAllByUserIDAndPage(
            user.id,
            1,
            sqlConnection,
            (this::getAllByUserIDAndPageHandler)(handler, sqlConnection, result, user.username)
        )
    }

    private fun getAllByUserIDAndPageHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        result: MutableMap<String, Any?>,
        username: String
    ) = handler@{ tickets: List<Ticket>?, _: AsyncResult<*> ->
        if (tickets == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_162))
            }

            return@handler
        }

        if (tickets.isEmpty()) {
            prepareTickets(
                handler,
                sqlConnection,
                result,
                tickets,
                mapOf(),
                username
            )

            return@handler
        }

        val categoryIDList = tickets.filter { it.categoryID != -1 }.distinctBy { it.categoryID }.map { it.categoryID }

        if (categoryIDList.isEmpty()) {
            prepareTickets(
                handler,
                sqlConnection,
                result,
                tickets,
                mapOf(),
                username
            )

            return@handler
        }

        databaseManager.getDatabase().ticketCategoryDao.getByIDList(
            categoryIDList,
            sqlConnection,
            (this::getByIDListHandler)(
                handler,
                sqlConnection,
                tickets,
                result,
                username
            )
        )
    }

    private fun getByIDListHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        tickets: List<Ticket>,
        result: MutableMap<String, Any?>,
        username: String
    ) = handler@{ ticketCategoryList: Map<Int, TicketCategory>?, _: AsyncResult<*> ->
        if (ticketCategoryList == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_163))
            }

            return@handler
        }

        prepareTickets(handler, sqlConnection, result, tickets, ticketCategoryList, username)
    }

    private fun prepareTickets(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        result: MutableMap<String, Any?>,
        tickets: List<Ticket>,
        ticketCategoryList: Map<Int, TicketCategory>,
        username: String
    ) {
        databaseManager.closeConnection(sqlConnection) {
            val ticketDataList = mutableListOf<Map<String, Any?>>()

            tickets.forEach { ticket ->
                ticketDataList.add(
                    mapOf(
                        "id" to ticket.id,
                        "title" to ticket.title,
                        "category" to
                                if (ticket.categoryID == -1)
                                    mapOf("id" to -1, "title" to "-")
                                else
                                    ticketCategoryList.getOrDefault(
                                        ticket.categoryID,
                                        mapOf("id" to -1, "title" to "-")
                                    ),
                        "writer" to mapOf(
                            "username" to username
                        ),
                        "date" to ticket.date,
                        "last_update" to ticket.lastUpdate,
                        "status" to ticket.status
                    )
                )
            }

            result["tickets"] = ticketDataList

            handler.invoke(Successful(result))
        }
    }
}