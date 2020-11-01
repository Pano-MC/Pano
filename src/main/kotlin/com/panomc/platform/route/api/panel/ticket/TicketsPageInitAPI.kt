package com.panomc.platform.route.api.panel.ticket

import com.panomc.platform.ErrorCode
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext
import kotlin.math.ceil

class TicketsPageInitAPI : PanelApi() {
    override val routeType = RouteType.POST

    override val routes = arrayListOf("/api/panel/initPage/ticketPage")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson
        val pageType = data.getInteger("page_type")
        val page = data.getInteger("page")

        databaseManager.createConnection { sqlConnection, _ ->
            if (sqlConnection == null) {
                handler.invoke(Error(ErrorCode.CANT_CONNECT_DATABASE))

                return@createConnection
            }

            databaseManager.getDatabase().ticketDao.getCountByPageType(
                pageType,
                sqlConnection
            ) { count, _ ->
                if (count == null)
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_78))
                    }
                else {
                    var totalPage = ceil(count.toDouble() / 10).toInt()

                    if (totalPage < 1)
                        totalPage = 1

                    if (page > totalPage || page < 1) {
                        databaseManager.closeConnection(sqlConnection) {
                            handler.invoke(Error(ErrorCode.PAGE_NOT_FOUND))
                        }

                        return@getCountByPageType
                    }

                    databaseManager.getDatabase().ticketDao.getAllByPageAndPageType(
                        page,
                        pageType,
                        sqlConnection
                    ) { tickets, _ ->
                        if (tickets == null)
                            databaseManager.closeConnection(sqlConnection) {
                                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_76))
                            }
                        else {

                            val result = mutableMapOf<String, Any?>(
                                "tickets" to tickets,
                                "tickets_count" to count,
                                "total_page" to totalPage
                            )

                            databaseManager.closeConnection(sqlConnection) {
                                handler.invoke(Successful(result))
                            }
                        }
                    }
                }
            }
        }
    }
}