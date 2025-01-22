package com.panomc.platform.route.api.panel.permission


import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.auth.PanelPermission
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.model.Permission
import com.panomc.platform.db.model.PermissionGroup
import com.panomc.platform.error.CantUpdatePermGroupYourself
import com.panomc.platform.error.NoPermissionToUpdateAdminUser
import com.panomc.platform.error.SomePermissionsArentExists
import com.panomc.platform.error.SomeUsersArentExists
import com.panomc.platform.model.*
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.RequestPredicate
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Bodies.json
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaParser
import io.vertx.json.schema.common.dsl.Schemas.*
import io.vertx.sqlclient.SqlClient

@Endpoint
class PanelAddPermissionGroupAPI(
    private val databaseManager: DatabaseManager,
    private val authProvider: AuthProvider
) : PanelApi() {
    override val paths = listOf(Path("/api/panel/permissionGroups", RouteType.POST))

    override fun getValidationHandler(schemaParser: SchemaParser): ValidationHandler =
        ValidationHandlerBuilder.create(schemaParser)
            .body(
                json(
                    objectSchema()
                        .requiredProperty("name", stringSchema())
                        .requiredProperty("addedUsers", arraySchema().items(stringSchema()))
                        .requiredProperty(
                            "permissions",
                            arraySchema()
                                .items(
                                    objectSchema()
                                        .requiredProperty("id", numberSchema())
                                        .requiredProperty("selected", booleanSchema())
                                )
                        )
                )
            )
            .predicate(RequestPredicate.BODY_REQUIRED)
            .build()

    override suspend fun handle(context: RoutingContext): Result {
        authProvider.requirePermission(PanelPermission.MANAGE_PERMISSION_GROUPS, context)

        val parameters = getParameters(context)
        val data = parameters.body().jsonObject

        var name = data.getString("name")

        val addedUsers = data.getJsonArray("addedUsers").map { it.toString() }
        val permissions = data.getJsonArray("permissions").map { it as JsonObject }

        val userId = authProvider.getUserIdFromRoutingContext(context)

        validateForm(name)

        name = getSystematicName(name)

        val sqlClient = getSqlClient()

        validateSelfUpdating(addedUsers, userId, sqlClient)

        validateIsPermissionGroupNameExists(name, sqlClient)

        val permissionsInDb = databaseManager.permissionDao.getPermissions(sqlClient)

        validatePermissions(permissions, permissionsInDb)

        val adminPermissionGroupId =
            databaseManager.permissionGroupDao.getPermissionGroupIdByName("admin", sqlClient)!!

        validateAddedUsersContainAdminAndHasUserPerm(adminPermissionGroupId, addedUsers, sqlClient, context)

        validateAreAddedUsersExist(addedUsers, sqlClient)

        val id = databaseManager.permissionGroupDao.add(PermissionGroup(name = name), sqlClient)

        permissions.filter { it.getBoolean("selected") }.forEach { permission ->
            val permissionId = permission.getLong("id")

            databaseManager.permissionGroupPermsDao.addPermission(id, permissionId, sqlClient)
        }

        if (addedUsers.isNotEmpty()) {
            databaseManager.userDao.setPermissionGroupByUsernames(id, addedUsers, sqlClient)
        }

        return Successful(mapOf("id" to id))
    }

    private suspend fun validateAreAddedUsersExist(addedUsers: List<String>, sqlClient: SqlClient) {
        if (addedUsers.isNotEmpty()) {
            val areAddedUsersExists = databaseManager.userDao.areUsernamesExists(addedUsers, sqlClient)

            if (!areAddedUsersExists) {
                throw SomeUsersArentExists()
            }
        }
    }

    private suspend fun validateAddedUsersContainAdminAndHasUserPerm(
        adminPermissionGroupId: Long,
        addedUsers: List<String>,
        sqlClient: SqlClient,
        context: RoutingContext
    ) {
        val admins = databaseManager.userDao.getUsernamesByPermissionGroupId(adminPermissionGroupId, -1, sqlClient)
            .map { it.lowercase() }

        val isAdmin = context.get<Boolean>("isAdmin") ?: false

        admins.forEach { admin ->
            if (addedUsers.find { it.lowercase() == admin } != null && !isAdmin) {
                throw NoPermissionToUpdateAdminUser()
            }
        }
    }

    private fun validatePermissions(
        permissions: List<JsonObject>,
        permissionsInDb: List<Permission>
    ) {
        val notExistingPermissions =
            permissionsInDb.filter { permissionInDb -> permissions.find { it.getLong("id") == permissionInDb.id } == null }

        if (notExistingPermissions.isNotEmpty()) {
            throw SomePermissionsArentExists()
        }
    }

    private suspend fun validateIsPermissionGroupNameExists(
        name: String,
        sqlClient: SqlClient
    ) {
        val isTherePermissionGroup =
            databaseManager.permissionGroupDao.isThereByName(name, sqlClient)

        if (isTherePermissionGroup) {
            throw Errors(mapOf("name" to true))
        }
    }

    private suspend fun validateSelfUpdating(
        addedUsers: List<String>,
        userId: Long,
        sqlClient: SqlClient
    ) {
        val username = databaseManager.userDao.getUsernameFromUserId(userId, sqlClient)!!.lowercase()

        if (addedUsers.any { it.lowercase() == username }) {
            throw CantUpdatePermGroupYourself()
        }
    }

    private fun validateForm(
        name: String
    ) {
        val errors = mutableMapOf<String, Boolean>()

        if (name.isEmpty() || name.length > 32)
            errors["name"] = true

        if (errors.isNotEmpty()) {
            throw Errors(errors)
        }
    }

    private fun getSystematicName(name: String) = name.lowercase().replace("\\s+".toRegex(), "-")
}