package com.panomc.platform.route.api.panel.server


import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.auth.panel.permission.ManageServersPermission
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.model.PanelConfig
import com.panomc.platform.error.NotExists
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Parameters.param
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaParser
import io.vertx.json.schema.common.dsl.Schemas.numberSchema

@Endpoint
class PanelSelectServerAPI(
    private val databaseManager: DatabaseManager,
    private val authProvider: AuthProvider
) : PanelApi() {
    override val paths = listOf(Path("/api/panel/servers/:id/select", RouteType.POST))

    override fun getValidationHandler(schemaParser: SchemaParser): ValidationHandler =
        ValidationHandlerBuilder.create(schemaParser)
            .pathParameter(param("id", numberSchema()))
            .build()

    override suspend fun handle(context: RoutingContext): Result {
        authProvider.requirePermission(ManageServersPermission(), context)

        val parameters = getParameters(context)
        val id = parameters.pathParameter("id").long
        val userId = authProvider.getUserIdFromRoutingContext(context)

        val sqlClient = getSqlClient()

        val exists = databaseManager.serverDao.existsById(id, sqlClient)

        if (!exists) {
            throw NotExists()
        }

        val panelConfig = databaseManager.panelConfigDao.byUserIdAndOption(userId, "selected_server", sqlClient)

        if (panelConfig == null) {
            databaseManager.panelConfigDao.add(
                PanelConfig(
                    userId = userId,
                    option = "selected_server",
                    value = "$id"
                ),
                sqlClient
            )
        } else {
            databaseManager.panelConfigDao.updateValueById(panelConfig.id, "$id", sqlClient)
        }

        return Successful()
    }
}