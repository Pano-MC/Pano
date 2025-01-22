package com.panomc.platform.route.api.panel.permission

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.auth.PanelPermission
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.error.CantUpdateAdminPermission
import com.panomc.platform.error.NoPermission
import com.panomc.platform.error.NotExists
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.RequestPredicate
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Bodies
import io.vertx.ext.web.validation.builder.Parameters
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaParser
import io.vertx.json.schema.common.dsl.Schemas

@Endpoint
class PanelSetPermissionGroupPermissionAPI(
    private val databaseManager: DatabaseManager,
    private val authProvider: AuthProvider
) : PanelApi() {
    override val paths =
        listOf(Path("/api/panel/permissionGroups/:permissionGroupId/permissions/:permissionId", RouteType.PUT))

    override fun getValidationHandler(schemaParser: SchemaParser): ValidationHandler =
        ValidationHandlerBuilder.create(schemaParser)
            .pathParameter(Parameters.param("permissionGroupId", Schemas.numberSchema()))
            .pathParameter(Parameters.param("permissionId", Schemas.numberSchema()))
            .body(
                Bodies.json(
                    Schemas.objectSchema()
                        .requiredProperty("mode", Schemas.stringSchema())
                )
            )
            .predicate(RequestPredicate.BODY_REQUIRED)
            .build()

    override suspend fun handle(context: RoutingContext): Result {
        authProvider.requirePermission(PanelPermission.MANAGE_PERMISSION_GROUPS, context)

        val parameters = getParameters(context)
        val data = parameters.body().jsonObject

        val permissionGroupId = parameters.pathParameter("permissionGroupId").long
        val permissionId = parameters.pathParameter("permissionId").long
        val mode = data.getString("mode")

        if (mode != "ADD" && mode != "DELETE") {
            throw NoPermission()
        }

        val sqlClient = getSqlClient()

        val isTherePermissionGroup =
            databaseManager.permissionGroupDao.isThereById(permissionGroupId, sqlClient)

        if (!isTherePermissionGroup) {
            throw NotExists()
        }

        val permissionGroup =
            databaseManager.permissionGroupDao.getPermissionGroupById(permissionGroupId, sqlClient)!!

        if (permissionGroup.name == "admin") {
            throw CantUpdateAdminPermission()
        }

        val isTherePermission = databaseManager.permissionDao.isTherePermissionById(permissionId, sqlClient)

        if (!isTherePermission) {
            throw NotExists()
        }

        val doesPermissionGroupHavePermission =
            databaseManager.permissionGroupPermsDao.doesPermissionGroupHavePermission(
                permissionGroupId,
                permissionId,
                sqlClient
            )

        if (mode == "ADD" && !doesPermissionGroupHavePermission)
            databaseManager.permissionGroupPermsDao.addPermission(permissionGroupId, permissionId, sqlClient)
        else if (doesPermissionGroupHavePermission)
            databaseManager.permissionGroupPermsDao.removePermission(permissionGroupId, permissionId, sqlClient)

        val body = mutableMapOf<String, Any?>()

        body["mode"] = if (mode == "ADD" && !doesPermissionGroupHavePermission) "ADD" else "DELETE"

        return Successful(body)
    }
}