package com.panomc.platform.route.api.panel

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.config.ConfigManager
import com.panomc.platform.db.model.User
import com.panomc.platform.model.*
import com.panomc.platform.util.PlatformCodeManager
import io.vertx.core.AsyncResult
import io.vertx.ext.web.RoutingContext
import io.vertx.sqlclient.SqlConnection
import javax.inject.Inject

class BasicDataAPI : PanelApi() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/panel/basicData")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var platformCodeManager: PlatformCodeManager

    @Inject
    lateinit var configManager: ConfigManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val token = context.getCookie("pano_token").value

        databaseManager.createConnection((this::createConnectionHandler)(handler, context, token))
    }

    private fun createConnectionHandler(
        handler: (result: Result) -> Unit,
        context: RoutingContext,
        token: String
    ) = handler@{ sqlConnection: SqlConnection?, _: AsyncResult<SqlConnection> ->
        if (sqlConnection == null) {
            handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

            return@handler
        }

        databaseManager.getDatabase().tokenDao.getUserIDFromToken(
            token,
            sqlConnection,
            (this::getUserIDFromTokenHandler)(handler, context, sqlConnection)
        )
    }

    private fun getUserIDFromTokenHandler(
        handler: (result: Result) -> Unit,
        context: RoutingContext,
        sqlConnection: SqlConnection
    ) = handler@{ userID: Int?, _: AsyncResult<*> ->
        if (userID == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_8))
            }

            return@handler
        }

        databaseManager.getDatabase().userDao.getByID(
            userID,
            sqlConnection,
            (this::getByIDHandler)(handler, context, sqlConnection, userID)
        )
    }

    private fun getByIDHandler(
        handler: (result: Result) -> Unit,
        context: RoutingContext,
        sqlConnection: SqlConnection,
        userID: Int
    ) = handler@{ user: User?, _: AsyncResult<*> ->
        if (user == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_7))
            }

            return@handler
        }

        databaseManager.getDatabase().panelNotificationDao.getCountOfNotReadByUserID(
            userID,
            sqlConnection,
            (this::getCountByUserIDHandler)(handler, context, sqlConnection, user)
        )
    }

    private fun getCountByUserIDHandler(
        handler: (result: Result) -> Unit,
        context: RoutingContext,
        sqlConnection: SqlConnection,
        user: User
    ) = handler@{ count: Int?, _: AsyncResult<*> ->
        databaseManager.closeConnection(sqlConnection) {
            if (count == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_68))

                return@closeConnection
            }

            handler.invoke(
                Successful(
                    mapOf(
                        "user" to mapOf(
                            "username" to user.username,
                            "email" to user.email,
                            "permission_id" to user.permissionID
                        ),
                        "website" to mapOf(
                            "name" to configManager.getConfig()["website-name"],
                            "description" to configManager.getConfig()["website-description"]
                        ),
                        "platform_server_match_key" to platformCodeManager.getPlatformKey(),
                        "platform_server_match_key_time_started" to platformCodeManager.getTimeStarted(),
                        "platform_host_address" to context.request().host(),
                        "servers" to listOf<Map<String, Any?>>(),
                        "notifications_count" to count
                    )
                )
            )
        }
    }
}