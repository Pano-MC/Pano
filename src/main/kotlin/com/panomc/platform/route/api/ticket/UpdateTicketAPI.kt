package com.panomc.platform.route.api.ticket

import com.panomc.platform.ErrorCode
import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.model.PanelNotification
import com.panomc.platform.model.*
import com.panomc.platform.notification.Notifications
import com.panomc.platform.setup.SetupManager
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Bodies
import io.vertx.ext.web.validation.builder.Parameters
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaParser
import io.vertx.json.schema.common.dsl.Schemas.*

@Endpoint
class UpdateTicketAPI(
    private val databaseManager: DatabaseManager,
    setupManager: SetupManager,
    private val authProvider: AuthProvider
) : LoggedInApi(setupManager, authProvider) {
    override val paths = listOf(Path("/api/tickets/:id", RouteType.PUT))

    override fun getValidationHandler(schemaParser: SchemaParser): ValidationHandler =
        ValidationHandlerBuilder.create(schemaParser)
            .pathParameter(Parameters.param("id", numberSchema()))
            .body(
                Bodies.json(
                    objectSchema()
                        .optionalProperty("status", stringSchema())
                )
            )
            .build()

    override suspend fun handler(context: RoutingContext): Result {
        val parameters = getParameters(context)
        val data = parameters.body().jsonObject

        val id = parameters.pathParameter("id").long
        val ticketStatus = data.getString("status")
        val userId = authProvider.getUserIdFromRoutingContext(context)

        val sqlConnection = createConnection(databaseManager, context)

        val isExists = databaseManager.ticketDao.isExistsById(id, sqlConnection)

        if (!isExists) {
            throw Error(ErrorCode.NOT_EXISTS)
        }

        val isBelong = databaseManager.ticketDao.isBelongToUserIdsById(id, userId, sqlConnection)

        if (!isBelong) {
            throw Error(ErrorCode.NO_PERMISSION)
        }

        if (ticketStatus != null && ticketStatus == "close") {
            databaseManager.ticketDao.closeTicketById(id, sqlConnection)
        }

        val adminList = authProvider.getAdminList(sqlConnection)

        val notifications = adminList.map { admin ->
            val adminId = databaseManager.userDao.getUserIdFromUsername(admin, sqlConnection)!!

            PanelNotification(
                userId = adminId,
                typeId = Notifications.PanelNotification.TICKET_CLOSED_BY_USER.typeId,
                action = Notifications.PanelNotification.TICKET_CLOSED_BY_USER.action,
                properties = JsonObject().put("id", id)
            )
        }

        databaseManager.panelNotificationDao.addAll(notifications, sqlConnection)

        return Successful()
    }
}