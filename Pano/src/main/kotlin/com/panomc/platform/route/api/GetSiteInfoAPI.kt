package com.panomc.platform.route.api

import com.panomc.platform.Main.Companion.VERSION
import com.panomc.platform.PluginManager
import com.panomc.platform.PluginUiManager
import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.config.ConfigManager
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext
import io.vertx.json.schema.SchemaParser

@Endpoint
class GetSiteInfoAPI(
    private val configManager: ConfigManager,
    private val pluginManager: PluginManager,
    private val pluginUiManager: PluginUiManager
) : Api() {
    override val paths = listOf(Path("/api/siteInfo", RouteType.GET))

    override fun getValidationHandler(schemaParser: SchemaParser) = null

    override suspend fun handle(context: RoutingContext): Result {
        val response = mutableMapOf<String, Any>()
        val config = configManager.config

        response["locale"] = config.locale
        response["websiteName"] = config.websiteName
        response["websiteDescription"] = config.websiteDescription
        response["supportEmail"] = config.supportEmail
        response["keywords"] = config.keywords
        response["panoVersion"] = VERSION

        response["plugins"] = pluginUiManager.getRegisteredPlugins().toList().associate {
            it.first.pluginId to mapOf(
                "version" to pluginManager.getPlugin(it.first.pluginId).descriptor.version,
                "uiHashes" to it.second
            )
        }

        return Successful(response)
    }
}