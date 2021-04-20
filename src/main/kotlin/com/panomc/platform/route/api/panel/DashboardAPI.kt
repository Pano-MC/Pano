package com.panomc.platform.route.api.panel

import com.panomc.platform.ErrorCode
import com.panomc.platform.db.model.SystemProperty
import com.panomc.platform.db.model.Ticket
import com.panomc.platform.db.model.TicketCategory
import com.panomc.platform.model.*
import com.panomc.platform.util.LoginUtil
import io.vertx.core.AsyncResult
import io.vertx.ext.web.RoutingContext
import io.vertx.sqlclient.SqlConnection

class DashboardAPI : PanelApi() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/panel/initPage/dashboard")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val idOrToken = LoginUtil.getUserIDOrToken(context)

        if (idOrToken == null || (idOrToken !is Int && idOrToken !is String)) {
            handler.invoke(Error(ErrorCode.NOT_LOGGED_IN))

            return
        }

        val result = mutableMapOf<String, Any?>(
            "getting_started_blocks" to mapOf(
                "welcome_board" to false
            ),
            "registered_player_count" to 0,
            "post_count" to 0,
            "tickets_count" to 0,
            "open_tickets_count" to 0,
            "tickets" to mutableListOf<Map<String, Any?>>()
        )

        databaseManager.createConnection((this::createConnectionHandler)(handler, idOrToken, result))
    }

    private fun createConnectionHandler(
        handler: (result: Result) -> Unit,
        idOrToken: Any,
        result: MutableMap<String, Any?>
    ) = handler@{ sqlConnection: SqlConnection?, _: AsyncResult<SqlConnection> ->
        if (sqlConnection == null) {
            handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

            return@handler
        }

        if (idOrToken is Int) {
            databaseManager.getDatabase().systemPropertyDao.isUserInstalledSystemByUserID(
                idOrToken,
                sqlConnection,
                (this::isUserInstalledSystemByUserIDHandler)(handler, sqlConnection, result)
            )

            return@handler
        }

        databaseManager.getDatabase().tokenDao.getUserIDFromToken(
            idOrToken as String,
            sqlConnection,
            (this::getUserIDFromTokenHandler)(handler, sqlConnection, result)
        )
    }

    private fun getUserIDFromTokenHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        result: MutableMap<String, Any?>
    ) = handler@{ userID: Int?, _: AsyncResult<*> ->
        if (userID == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_16))
            }

            return@handler
        }

        databaseManager.getDatabase().systemPropertyDao.isUserInstalledSystemByUserID(
            userID,
            sqlConnection,
            (this::isUserInstalledSystemByUserIDHandler)(handler, sqlConnection, result)
        )
    }

    private fun isUserInstalledSystemByUserIDHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        result: MutableMap<String, Any?>
    ) = handler@{ isUserInstalled: Boolean?, _: AsyncResult<*> ->
        if (isUserInstalled == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_17))
            }

            return@handler
        }

        if (!isUserInstalled) {
            databaseManager.getDatabase().userDao.count(
                sqlConnection,
                (this::userDaoCountHandler)(handler, sqlConnection, result)
            )

            return@handler
        }

        databaseManager.getDatabase().systemPropertyDao.getValue(
            SystemProperty(
                -1,
                "show_getting_started",
                ""
            ),
            sqlConnection,
            (this::getValueHandler)(handler, sqlConnection, result)
        )
    }

    private fun getValueHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        result: MutableMap<String, Any?>
    ) = handler@{ systemProperty: SystemProperty?, _: AsyncResult<*> ->
        if (systemProperty == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_20))
            }

            return@handler
        }

        result["getting_started_blocks"] = mapOf(
            "welcome_board" to systemProperty.value.toBoolean()
        )

        databaseManager.getDatabase().userDao.count(
            sqlConnection,
            (this::userDaoCountHandler)(handler, sqlConnection, result)
        )
    }

    private fun userDaoCountHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        result: MutableMap<String, Any?>
    ) = handler@{ userCount: Int?, _: AsyncResult<*> ->
        if (userCount == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_18))
            }

            return@handler
        }

        result["registered_player_count"] = userCount

        databaseManager.getDatabase().postDao.count(
            sqlConnection,
            (this::postDaoCount)(handler, sqlConnection, result)
        )
    }

    private fun postDaoCount(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        result: MutableMap<String, Any?>
    ) = handler@{ postCount: Int?, _: AsyncResult<*> ->
        if (postCount == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_19))
            }

            return@handler
        }

        result["post_count"] = postCount

        databaseManager.getDatabase().ticketDao.count(
            sqlConnection,
            (this::ticketDaoCount)(handler, sqlConnection, result)
        )
    }

    private fun ticketDaoCount(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        result: MutableMap<String, Any?>
    ) = handler@{ ticketCount: Int?, _: AsyncResult<*> ->
        if (ticketCount == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_112))
            }

            return@handler
        }

        result["tickets_count"] = ticketCount

        if (ticketCount == 0) {
            sendResults(handler, sqlConnection, result)

            return@handler
        }

        databaseManager.getDatabase().ticketDao.countOfOpenTickets(
            sqlConnection,
            (this::countOfOpenTicketsHandler)(
                handler,
                sqlConnection,
                result
            )
        )
    }

    private fun countOfOpenTicketsHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        result: MutableMap<String, Any?>
    ) = handler@{ openTicketCount: Int?, _: AsyncResult<*> ->
        if (openTicketCount == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_118))
            }

            return@handler
        }

        result["open_tickets_count"] = openTicketCount

        databaseManager.getDatabase().ticketDao.getLast5Tickets(
            sqlConnection,
            (this::getLast5TicketsHandler)(
                handler,
                sqlConnection,
                result
            )
        )
    }

    private fun getLast5TicketsHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        result: MutableMap<String, Any?>
    ) = handler@{ tickets: List<Ticket>?, _: AsyncResult<*> ->
        if (tickets == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_76))
            }

            return@handler
        }

        val userIDList = tickets.distinctBy { it.userID }.map { it.userID }

        databaseManager.getDatabase().userDao.getUsernameByListOfID(
            userIDList,
            sqlConnection,
            (this::getUsernameByListOfIDHandler)(
                handler,
                sqlConnection,
                tickets,
                result
            )
        )
    }

    private fun getUsernameByListOfIDHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        tickets: List<Ticket>,
        result: MutableMap<String, Any?>
    ) = handler@{ usernameList: Map<Int, String>?, _: AsyncResult<*> ->
        if (usernameList == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_155))
            }

            return@handler
        }

        val categoryIDList = tickets.filter { it.categoryID != -1 }.distinctBy { it.categoryID }.map { it.categoryID }

        if (categoryIDList.isNotEmpty()) {
            databaseManager.getDatabase().ticketCategoryDao.getByIDList(
                categoryIDList,
                sqlConnection,
                (this::getByIDListHandler)(
                    handler,
                    sqlConnection,
                    tickets,
                    usernameList,
                    result
                )
            )

            return@handler
        }

        prepareTickets(handler, sqlConnection, result, tickets, mapOf(), usernameList)
    }

    private fun getByIDListHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        tickets: List<Ticket>,
        usernameList: Map<Int, String>,
        result: MutableMap<String, Any?>
    ) = handler@{ ticketCategoryList: Map<Int, TicketCategory>?, _: AsyncResult<*> ->
        if (ticketCategoryList == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_154))
            }

            return@handler
        }

        prepareTickets(handler, sqlConnection, result, tickets, ticketCategoryList, usernameList)
    }

    private fun prepareTickets(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        result: MutableMap<String, Any?>,
        tickets: List<Ticket>,
        ticketCategoryList: Map<Int, TicketCategory>,
        usernameList: Map<Int, String>
    ) {
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
                        "username" to usernameList[ticket.userID]
                    ),
                    "date" to ticket.date,
                    "last_update" to ticket.lastUpdate,
                    "status" to ticket.status
                )
            )
        }

        result["tickets"] = ticketDataList

        sendResults(handler, sqlConnection, result)
    }

    private fun sendResults(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        result: MutableMap<String, Any?>
    ) {
        databaseManager.closeConnection(sqlConnection) {
            handler.invoke(Successful(result))
        }
    }
}