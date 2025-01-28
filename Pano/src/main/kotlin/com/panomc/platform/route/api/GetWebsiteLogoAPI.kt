package com.panomc.platform.route.api

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.config.ConfigManager
import com.panomc.platform.model.Api
import com.panomc.platform.model.Path
import com.panomc.platform.model.Result
import com.panomc.platform.model.RouteType
import io.vertx.ext.web.RoutingContext
import io.vertx.json.schema.SchemaParser
import java.io.File

@Endpoint
class GetWebsiteLogoAPI(private val configManager: ConfigManager) : Api() {
    override val paths = listOf(Path("/api/websiteLogo", RouteType.GET))

    override fun getValidationHandler(schemaParser: SchemaParser) = null

    override suspend fun handle(context: RoutingContext): Result? {
        val websiteLogoPath = configManager.config.filePaths["websiteLogo"]

        if (websiteLogoPath == null) {
            sendDefault(context)

            return null
        }

        val path = configManager.config.fileUploadsFolder + File.separator +
                websiteLogoPath

        val file = File(path)

        if (!file.exists()) {
            sendDefault(context)

            return null
        }

        context.response().sendFile(path)

        return null
    }

    private fun sendDefault(context: RoutingContext) {
        context.response().sendFile("assets/img/minecraft-logo.png")
    }
}