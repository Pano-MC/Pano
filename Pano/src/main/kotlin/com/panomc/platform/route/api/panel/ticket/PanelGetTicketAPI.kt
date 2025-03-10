package com.panomc.platform.route.api.panel.ticket


import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.auth.panel.permission.ManageTicketsPermission
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.model.Ticket
import com.panomc.platform.db.model.TicketCategory
import com.panomc.platform.db.model.TicketMessage
import com.panomc.platform.error.NotExists
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Parameters.param
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaParser
import io.vertx.json.schema.common.dsl.Schemas.numberSchema

@Endpoint
class PanelGetTicketAPI(
    private val databaseManager: DatabaseManager,
    private val authProvider: AuthProvider
) : PanelApi() {
    override val paths = listOf(Path("/api/panel/tickets/:id", RouteType.GET))

    override fun getValidationHandler(schemaParser: SchemaParser): ValidationHandler =
        ValidationHandlerBuilder.create(schemaParser)
            .pathParameter(param("id", numberSchema()))
            .build()

    override suspend fun handle(context: RoutingContext): Result {
        authProvider.requirePermission(ManageTicketsPermission(), context)

        val parameters = getParameters(context)
        val id = parameters.pathParameter("id").long

        val sqlClient = getSqlClient()

        val exists = databaseManager.ticketDao.existsById(id, sqlClient)

        if (!exists) {
            throw NotExists()
        }

        val ticket = databaseManager.ticketDao.getById(id, sqlClient)!!

        val username = databaseManager.userDao.getUsernameFromUserId(ticket.userId, sqlClient)!!

        val messages = databaseManager.ticketMessageDao.getByTicketId(id, sqlClient)

        val userIdList = mutableListOf<Long>()

        messages
            .filter { ticketMessage -> ticketMessage.userId != -1L }
            .forEach { message ->
                if (userIdList.indexOf(message.userId) == -1)
                    userIdList.add(message.userId)
            }

        val usernameList = databaseManager.userDao.getUsernameByListOfId(userIdList, sqlClient)

        val count = databaseManager.ticketMessageDao.getCountByTicketId(ticket.id, sqlClient)

        if (ticket.categoryId == -1L) {
            return getResult(ticket, usernameList, null, username, messages, count)
        }

        val ticketCategory = databaseManager.ticketCategoryDao.getById(ticket.categoryId, sqlClient)

        return getResult(ticket, usernameList, ticketCategory, username, messages, count)
    }

    private fun getResult(
        ticket: Ticket,
        usernameList: Map<Long, String>,
        ticketCategory: TicketCategory?,
        username: String,
        ticketMessages: List<TicketMessage>,
        messageCount: Long
    ): Result {
        val messages = mutableListOf<Map<String, Any?>>()

        ticketMessages.forEach { ticketMessage ->
            messages.add(
                0,
                mapOf(
                    "id" to ticketMessage.id,
                    "userId" to ticketMessage.userId,
                    "ticketId" to ticketMessage.ticketId,
                    "username" to (usernameList[ticketMessage.userId] ?: "-"),
                    "message" to ticketMessage.message,
                    "date" to ticketMessage.date,
                    "panel" to ticketMessage.panel
                )
            )
        }

        return Successful(
            mapOf(
                "ticket" to mapOf(
                    "username" to username,
                    "title" to ticket.title,
                    "category" to
                            (ticketCategory ?: TicketCategory()),
                    "messages" to messages,
                    "status" to ticket.status,
                    "date" to ticket.date,
                    "count" to messageCount
                )
            )
        )
    }
}