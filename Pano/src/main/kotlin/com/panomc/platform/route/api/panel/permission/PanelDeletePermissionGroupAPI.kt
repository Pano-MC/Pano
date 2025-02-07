package com.panomc.platform.route.api.panel.permission

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.auth.PanelPermission
import com.panomc.platform.auth.panel.log.DeletedPermissionGroupLog
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.error.CantDeleteAdminPermission
import com.panomc.platform.error.NotExists
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Parameters.param
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaParser
import io.vertx.json.schema.common.dsl.Schemas.numberSchema

@Endpoint
class PanelDeletePermissionGroupAPI(
    private val databaseManager: DatabaseManager,
    private val authProvider: AuthProvider
) : PanelApi() {
    override val paths = listOf(Path("/api/panel/permissions/:id", RouteType.DELETE))

    override fun getValidationHandler(schemaParser: SchemaParser): ValidationHandler =
        ValidationHandlerBuilder.create(schemaParser)
            .pathParameter(param("id", numberSchema()))
            .build()

    override suspend fun handle(context: RoutingContext): Result {
        authProvider.requirePermission(PanelPermission.MANAGE_PERMISSION_GROUPS, context)

        val parameters = getParameters(context)

        val permissionGroupId = parameters.pathParameter("id").long

        val sqlClient = getSqlClient()

        val permissionGroup =
            databaseManager.permissionGroupDao.getPermissionGroupById(permissionGroupId, sqlClient) ?: throw NotExists()

        if (permissionGroup.name == "admin") {
            throw CantDeleteAdminPermission()
        }

        databaseManager.permissionGroupPermsDao.removePermissionGroup(permissionGroupId, sqlClient)

        databaseManager.userDao.removePermissionGroupByPermissionGroupId(permissionGroupId, sqlClient)

        databaseManager.permissionGroupDao.deleteById(permissionGroupId, sqlClient)

        val userId = authProvider.getUserIdFromRoutingContext(context)
        val username = databaseManager.userDao.getUsernameFromUserId(userId, sqlClient)!!

        databaseManager.panelActivityLogDao.add(
            DeletedPermissionGroupLog(userId, username, permissionGroup.name),
            sqlClient
        )

        return Successful()
    }
}