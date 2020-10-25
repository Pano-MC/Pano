package com.panomc.platform.route.api.get.panel

import com.panomc.platform.ErrorCode
import com.panomc.platform.Main.Companion.getComponent
import com.panomc.platform.model.*
import com.panomc.platform.util.ConfigManager
import com.panomc.platform.util.Connection
import com.panomc.platform.util.DatabaseManager
import com.panomc.platform.util.NotificationStatus
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class PanelQuickNotificationsAPI : PanelApi() {
    override val routeType = RouteType.GET

    override val routes = arrayListOf("/api/panel/quickNotifications")

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    @Inject
    lateinit var configManager: ConfigManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val token = context.getCookie("pano_token").value

        databaseManager.createConnection { connection, _ ->
            if (connection == null)
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))
            else
                getUserIDFromToken(connection, token, handler) { userID ->
                    getNotifications(connection, userID, handler) { notifications ->
                        getNotificationsCount(connection, userID, handler) { count ->

                            val result = mutableMapOf<String, Any?>(
                                "notifications" to notifications,
                                "notifications_count" to count
                            )

                            databaseManager.closeConnection(connection) {
                                handler.invoke(Successful(result))
                            }
                        }
                    }
                }
        }
    }

    private fun getUserIDFromToken(
        connection: Connection,
        token: String,
        resultHandler: (result: Result) -> Unit,
        handler: (userID: Int) -> Unit
    ) {
        val query =
            "SELECT `user_id` FROM ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}token where `token` = ?"

        databaseManager.getSQLConnection(connection).queryWithParams(query, JsonArray().add(token)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0))
            else
                databaseManager.closeConnection(connection) {
                    resultHandler.invoke(Error(ErrorCode.PANEL_QUICK_NOTIFICATIONS_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_74))
                }
        }
    }

    private fun getNotifications(
        connection: Connection,
        userID: Int,
        resultHandler: (result: Result) -> Unit,
        handler: (notifications: List<Map<String, Any>>) -> Unit
    ) {
        val query =
            "SELECT `id`, `user_id`, `type_ID`, `date`, `status` FROM ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}panel_notification WHERE `user_id` = ? OR `user_id` = ? ORDER BY `date` DESC, `id` DESC LIMIT 5"

        databaseManager.getSQLConnection(connection)
            .queryWithParams(query, JsonArray().add(userID).add(-1)) { queryResult ->
                if (queryResult.succeeded()) {
                    val notifications = mutableListOf<Map<String, Any>>()

                    if (queryResult.result().results.size > 0)
                        queryResult.result().results.forEach { categoryInDB ->
                            if (categoryInDB.getString(4) == NotificationStatus.NOT_READ.toString())
                                notifications.add(
                                    mapOf(
                                        "id" to categoryInDB.getInteger(0),
                                        "type_ID" to categoryInDB.getString(2),
                                        "date" to categoryInDB.getString(3).toLong(),
                                        "status" to categoryInDB.getString(4),
                                        "isPersonal" to (categoryInDB.getInteger(1) == userID)
                                    )
                                )
                        }

                    handler.invoke(notifications)
                } else
                    databaseManager.closeConnection(connection) {
                        resultHandler.invoke(Error(ErrorCode.PANEL_QUICK_NOTIFICATIONS_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_73))
                    }
            }
    }

    private fun getNotificationsCount(
        connection: Connection,
        userID: Int,
        resultHandler: (result: Result) -> Unit,
        handler: (count: Int) -> Unit
    ) {
        val query =
            "SELECT count(`id`) FROM ${(configManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()}panel_notification WHERE (`user_id` = ? OR `user_id` = ?) AND `status` = ? ORDER BY `date` DESC, `id` DESC"

        databaseManager.getSQLConnection(connection)
            .queryWithParams(query, JsonArray().add(userID).add(-1).add(NotificationStatus.NOT_READ)) { queryResult ->
                if (queryResult.succeeded())
                    handler.invoke(queryResult.result().results[0].getInteger(0))
                else
                    databaseManager.closeConnection(connection) {
                        resultHandler.invoke(Error(ErrorCode.PANEL_QUICK_NOTIFICATIONS_API_SORRY_AN_ERROR_OCCURRED_ERROR_CODE_67))
                    }
            }
    }
}