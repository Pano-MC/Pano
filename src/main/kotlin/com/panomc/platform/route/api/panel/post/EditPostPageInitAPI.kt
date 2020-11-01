package com.panomc.platform.route.api.panel.post

import com.panomc.platform.ErrorCode
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext

class EditPostPageInitAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/initPage/editPost")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson

        val id = data.getInteger("id")

        databaseManager.createConnection { sqlConnection, _ ->
            if (sqlConnection == null) {
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

                return@createConnection
            }
            databaseManager.getDatabase().postDao.isExistsByID(
                id,
                sqlConnection
            ) { exists, _ ->
                when {
                    exists == null -> databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_100))
                    }
                    exists -> databaseManager.getDatabase().postDao.getByID(
                        id,
                        sqlConnection
                    ) { post, _ ->
                        if (post == null)
                            databaseManager.closeConnection(sqlConnection) {
                                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_99))
                            }
                        else
                            databaseManager.closeConnection(sqlConnection) {
                                handler.invoke(
                                    Successful(
                                        mapOf(
                                            "post" to post
                                        )
                                    )
                                )
                            }
                    }
                    else -> databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.POST_NOT_FOUND))
                    }
                }

            }
        }
    }
}