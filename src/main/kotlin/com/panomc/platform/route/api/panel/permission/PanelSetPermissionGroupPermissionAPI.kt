package com.panomc.platform.route.api.panel.permission

import com.panomc.platform.ErrorCode
import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import com.panomc.platform.setup.SetupManager
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Bodies
import io.vertx.ext.web.validation.builder.Parameters
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaParser
import io.vertx.json.schema.common.dsl.Schemas

@Endpoint
class PanelSetPermissionGroupPermissionAPI(
    private val databaseManager: DatabaseManager,
    setupManager: SetupManager,
    authProvider: AuthProvider
) : PanelApi(setupManager, authProvider) {
    override val paths =
        listOf(Path("/api/panel/permissionGroups/:permissionGroupId/permissions/:permissionId", RouteType.PUT))

    override fun getValidationHandler(schemaParser: SchemaParser): ValidationHandler =
        ValidationHandlerBuilder.create(schemaParser)
            .pathParameter(Parameters.param("permissionGroupId", Schemas.numberSchema()))
            .pathParameter(Parameters.param("permissionId", Schemas.numberSchema()))
            .body(
                Bodies.json(
                    Schemas.objectSchema()
                        .property("mode", Schemas.stringSchema())
                )
            )
            .build()

    override suspend fun handler(context: RoutingContext): Result {
        val parameters = getParameters(context)
        val data = parameters.body().jsonObject

        val permissionGroupId = parameters.pathParameter("permissionGroupId").long
        val permissionId = parameters.pathParameter("permissionId").long
        val mode = data.getString("mode")

        if (mode != "ADD" && mode != "DELETE") {
            throw Error(ErrorCode.UNKNOWN)
        }

        val sqlConnection = createConnection(databaseManager, context)

        val isTherePermissionGroup =
            databaseManager.permissionGroupDao.isThereById(permissionGroupId, sqlConnection)

        if (!isTherePermissionGroup) {
            throw Error(ErrorCode.NOT_EXISTS)
        }

        val permissionGroup =
            databaseManager.permissionGroupDao.getPermissionGroupById(permissionGroupId, sqlConnection)
                ?: throw Error(ErrorCode.UNKNOWN)

        if (permissionGroup.name == "admin") {
            throw Error(ErrorCode.CANT_UPDATE_ADMIN_PERMISSION)
        }

        val isTherePermission = databaseManager.permissionDao.isTherePermissionById(permissionId, sqlConnection)

        if (!isTherePermission) {
            throw Error(ErrorCode.NOT_EXISTS)
        }

        val doesPermissionGroupHavePermission =
            databaseManager.permissionGroupPermsDao.doesPermissionGroupHavePermission(
                permissionGroupId,
                permissionId,
                sqlConnection
            )

        if (mode == "ADD" && !doesPermissionGroupHavePermission)
            databaseManager.permissionGroupPermsDao.addPermission(permissionGroupId, permissionId, sqlConnection)
        else if (doesPermissionGroupHavePermission)
            databaseManager.permissionGroupPermsDao.removePermission(permissionGroupId, permissionId, sqlConnection)

        val body = mutableMapOf<String, Any?>()

        body["mode"] = if (mode == "ADD" && !doesPermissionGroupHavePermission) "ADD" else "DELETE"

        return Successful(body)
    }
}