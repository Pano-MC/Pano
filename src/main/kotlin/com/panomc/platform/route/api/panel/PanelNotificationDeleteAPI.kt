package com.panomc.platform.route.api.panel

import com.panomc.platform.ErrorCode
import com.panomc.platform.db.model.PanelNotification
import com.panomc.platform.model.*
import io.vertx.core.AsyncResult
import io.vertx.ext.web.RoutingContext
import io.vertx.sqlclient.SqlConnection

class PanelNotificationDeleteAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/notifications/delete")

    override fun handler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val id = data.getInteger("id")
        val userID = authProvider.getUserIDFromRoutingContext(context)

        databaseManager.createConnection((this::createConnectionHandler)(handler, userID, id))
    }

    private fun createConnectionHandler(
        handler: (result: Result) -> Unit,
        userID: Int,
        id: Int
    ) = handler@{ sqlConnection: SqlConnection?, _: AsyncResult<SqlConnection> ->
        if (sqlConnection == null) {
            handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

            return@handler
        }

        databaseManager.getDatabase().panelNotificationDao.existsByID(
            id,
            sqlConnection,
            (this::existsByIDHandler)(handler, sqlConnection, id, userID)
        )
    }

    private fun existsByIDHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        id: Int,
        userID: Int
    ) = handler@{ exists: Boolean?, _: AsyncResult<*> ->
        if (exists == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_169))
            }

            return@handler
        }

        if (!exists) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Successful())
            }

            return@handler
        }

        databaseManager.getDatabase().panelNotificationDao.getByID(
            id,
            sqlConnection,
            (this::getByIDHandler)(handler, sqlConnection, userID)
        )
    }

    private fun getByIDHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection,
        userID: Int
    ) = handler@{ notification: PanelNotification?, _: AsyncResult<*> ->
        if (notification == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_171))
            }

            return@handler
        }

        if (notification.userID != userID) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Successful())
            }

            return@handler
        }

        databaseManager.getDatabase().panelNotificationDao.deleteByID(
            notification.id,
            sqlConnection,
            (this::deleteByIDHandler)(handler, sqlConnection)
        )
    }

    private fun deleteByIDHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SqlConnection
    ) = handler@{ result: Result?, _: AsyncResult<*> ->
        databaseManager.closeConnection(sqlConnection) {
            if (result == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_172))

                return@closeConnection
            }

            handler.invoke(Successful())
        }
    }
}