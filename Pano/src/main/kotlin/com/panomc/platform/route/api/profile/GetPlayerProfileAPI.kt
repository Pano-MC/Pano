package com.panomc.platform.route.api.profile


import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.error.NotExists
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Parameters
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaParser
import io.vertx.json.schema.common.dsl.Schemas

@Endpoint
class GetPlayerProfileAPI(
    private val databaseManager: DatabaseManager
) : Api() {
    override val paths = listOf(Path("/api/profiles/:username", RouteType.GET))

    override fun getValidationHandler(schemaParser: SchemaParser): ValidationHandler =
        ValidationHandlerBuilder.create(schemaParser)
            .pathParameter(Parameters.param("username", Schemas.stringSchema()))
            .build()

    override suspend fun handle(context: RoutingContext): Result {
        val parameters = getParameters(context)

        val username = parameters.pathParameter("username").string

        val sqlClient = getSqlClient()

        val user = databaseManager.userDao.getByUsername(username, sqlClient) ?: throw NotExists()

        val response = mutableMapOf<String, Any?>()

        response["registerDate"] = user.registerDate

        return Successful(response)
    }
}