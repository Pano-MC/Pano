package com.panomc.platform.route.api.panel.player

import com.panomc.platform.ErrorCode
import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.auth.PanelPermission
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.mail.MailManager
import com.panomc.platform.mail.notification.BannedMail
import com.panomc.platform.model.*
import com.panomc.platform.token.TokenProvider
import com.panomc.platform.token.TokenType
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Bodies.json
import io.vertx.ext.web.validation.builder.Parameters
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaParser
import io.vertx.json.schema.common.dsl.Schemas.*

@Endpoint
class PanelBanPlayerAPI(
    private val authProvider: AuthProvider,
    private val databaseManager: DatabaseManager,
    private val mailManager: MailManager,
    private val tokenProvider: TokenProvider
) : PanelApi() {
    override val paths = listOf(Path("/api/panel/players/:username/ban", RouteType.POST))

    override fun getValidationHandler(schemaParser: SchemaParser): ValidationHandler =
        ValidationHandlerBuilder.create(schemaParser)
            .pathParameter(Parameters.param("username", stringSchema()))
            .body(
                json(
                    objectSchema()
                        .optionalProperty("sendNotification", booleanSchema())
                )
            )
            .build()

    override suspend fun handle(context: RoutingContext): Result {
        authProvider.requirePermission(PanelPermission.MANAGE_PLAYERS, context)

        val parameters = getParameters(context)
        val data = parameters.body().jsonObject

        val username = parameters.pathParameter("username").string

        val sendNotification = data.getBoolean("sendNotification") ?: false

        val sqlClient = getSqlClient()

        val exists = databaseManager.userDao.existsByUsername(username, sqlClient)

        if (!exists) {
            throw Error(ErrorCode.NOT_EXISTS)
        }

        val userId =
            databaseManager.userDao.getUserIdFromUsername(username, sqlClient) ?: throw Error(ErrorCode.NOT_EXISTS)
        val authUserId = authProvider.getUserIdFromRoutingContext(context)

        if (userId == authUserId) {
            throw Error(ErrorCode.CANT_BAN_YOURSELF)
        }

        val isBanned = databaseManager.userDao.isBanned(userId, sqlClient)

        if (isBanned) {
            throw Error(ErrorCode.ALREADY_BANNED)
        }

        val userPermissionGroupId = databaseManager.userDao.getPermissionGroupIdFromUserId(userId, sqlClient)!!

        if (userPermissionGroupId != -1L) {
            val userPermissionGroup =
                databaseManager.permissionGroupDao.getPermissionGroupById(userPermissionGroupId, sqlClient)!!

            if (userPermissionGroup.name == "admin") {
                val isAdmin = context.get<Boolean>("isAdmin") ?: false

                if (!isAdmin) {
                    throw Error(ErrorCode.NO_PERMISSION)
                }

                val count = databaseManager.userDao.getCountOfUsersByPermissionGroupId(
                    userPermissionGroupId,
                    sqlClient
                )

                if (count == 1L) {
                    throw Error(ErrorCode.LAST_ADMIN)
                }
            }
        }

        databaseManager.userDao.banPlayer(userId, sqlClient)

        tokenProvider.invalidateTokensBySubjectAndType(userId.toString(), TokenType.AUTHENTICATION, sqlClient)

        if (sendNotification) {
            mailManager.sendMail(sqlClient, userId, BannedMail())
        }

        return Successful()
    }
}