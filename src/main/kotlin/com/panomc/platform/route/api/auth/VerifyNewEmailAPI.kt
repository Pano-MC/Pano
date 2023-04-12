package com.panomc.platform.route.api.auth

import com.panomc.platform.ErrorCode
import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import com.panomc.platform.token.TokenProvider
import com.panomc.platform.token.TokenType
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Bodies
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaParser
import io.vertx.json.schema.common.dsl.Schemas

@Endpoint
class VerifyNewEmailAPI(
    private val databaseManager: DatabaseManager,
    private val tokenProvider: TokenProvider,
    private val authProvider: AuthProvider
) : Api() {
    override val paths = listOf(Path("/api/auth/verifyNewEmail", RouteType.POST))

    override fun getValidationHandler(schemaParser: SchemaParser): ValidationHandler =
        ValidationHandlerBuilder.create(schemaParser)
            .body(
                Bodies.json(
                    Schemas.objectSchema()
                        .property("token", Schemas.stringSchema())
//                TODO: Add recaptcha
                )
            )
            .build()

    override suspend fun handle(context: RoutingContext): Result {
        val parameters = getParameters(context)
        val data = parameters.body().jsonObject

        val token = data.getString("token")

        validateInput(token)

        val sqlClient = getSqlClient()

        val isValid = tokenProvider.isTokenValid(token, TokenType.CHANGE_EMAIL, sqlClient)

        if (!isValid) {
            throw Error(ErrorCode.INVALID_LINK)
        }

        val userId = authProvider.getUserIdFromToken(token)

        tokenProvider.invalidateTokensBySubjectAndType(userId.toString(), TokenType.CHANGE_EMAIL, sqlClient)

        val pendingEmail = databaseManager.userDao.getPendingEmailById(userId, sqlClient)

        val emailExists = databaseManager.userDao.isEmailExists(pendingEmail, sqlClient)

        if (emailExists) {
            throw Error(ErrorCode.INVALID_LINK)
        }

        databaseManager.userDao.setEmailById(userId, pendingEmail, sqlClient)

        databaseManager.userDao.updatePendingEmailById(userId, "", sqlClient)

        return Successful()
    }

    private fun validateInput(token: String) {
        if (token.isBlank()) {
            throw Error(ErrorCode.INVALID_LINK)
        }
    }
}