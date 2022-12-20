package com.panomc.platform.route.api.panel

import com.panomc.platform.Main
import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.model.*
import com.panomc.platform.setup.SetupManager
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaParser

@Endpoint
class PanelGetAboutAPI(
    setupManager: SetupManager,
    authProvider: AuthProvider
) : PanelApi(setupManager, authProvider) {
    override val paths = listOf(Path("/api/panel/settings/about", RouteType.GET))

    override fun getValidationHandler(schemaParser: SchemaParser): ValidationHandler =
        ValidationHandlerBuilder.create(schemaParser)
            .build()

    override suspend fun handler(context: RoutingContext): Result {
        val result = mutableMapOf<String, Any?>()

        result["platformVersion"] = Main.VERSION
        result["platformStage"] = Main.STAGE.toString()

        return Successful(result)
    }
}