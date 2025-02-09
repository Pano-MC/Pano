package com.panomc.platform.route.api.panel.permission


import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.auth.panel.permission.ManagePermissionGroupsPermission
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.model.PermissionGroup
import com.panomc.platform.error.PageNotFound
import com.panomc.platform.model.*
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Parameters
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaParser
import io.vertx.json.schema.common.dsl.Schemas.numberSchema
import kotlin.math.ceil

@Endpoint
class PanelGetPermissionGroupsAPI(
    private val databaseManager: DatabaseManager,
    private val authProvider: AuthProvider
) : PanelApi() {
    override val paths = listOf(Path("/api/panel/permissionGroups", RouteType.GET))

    override fun getValidationHandler(schemaParser: SchemaParser): ValidationHandler =
        ValidationHandlerBuilder.create(schemaParser)
            .queryParameter(Parameters.optionalParam("page", numberSchema()))
            .build()

    override suspend fun handle(context: RoutingContext): Result {
        authProvider.requirePermission(ManagePermissionGroupsPermission(), context)

        val parameters = getParameters(context)
        val page = parameters.queryParameter("page")?.long ?: 0L

        val result = mutableMapOf<String, Any?>()

        val sqlClient = getSqlClient()

        val permissionGroupCount = databaseManager.permissionGroupDao.countPermissionGroups(sqlClient)
        val permissions = databaseManager.permissionDao.getPermissions(sqlClient)

        var totalPage = ceil(permissionGroupCount.toDouble() / 10).toLong()

        if (totalPage < 1)
            totalPage = 1

        if (page != 0L && (page > totalPage || page < 1)) {
            throw PageNotFound()
        }

        val permissionGroups = if (page == 0L)
            databaseManager.permissionGroupDao.getPermissionGroups(sqlClient)
        else
            databaseManager.permissionGroupDao.getPermissionGroupsByPage(page, sqlClient)

        val permissionGroupList: List<MutableMap<String, Any?>> = permissionGroups.map { permissionGroup ->
            mutableMapOf(
                "id" to permissionGroup.id,
                "name" to permissionGroup.name
            )
        }

        val getPermissionGroupData: suspend (PermissionGroup) -> Unit = { permissionGroup ->
            val foundPermissionGroup = permissionGroupList.find { it["id"] == permissionGroup.id }!!

            val userCount =
                databaseManager.userDao.getCountOfUsersByPermissionGroupId(permissionGroup.id, sqlClient)
            val usernameList =
                databaseManager.userDao.getUsernamesByPermissionGroupId(permissionGroup.id, 3, sqlClient)
            val permissionCount = databaseManager.permissionGroupPermsDao.countPermissionsByPermissionGroupId(
                permissionGroup.id,
                sqlClient
            )

            foundPermissionGroup["userCount"] = userCount
            foundPermissionGroup["users"] = usernameList
            foundPermissionGroup["permissionCount"] = permissionCount
        }

        permissionGroups.forEach {
            getPermissionGroupData(it)
        }

        val permissionGroupPerms = databaseManager.permissionGroupPermsDao.getPermissionGroupPerms(sqlClient)

        val permissionGroupPermIdListMap = permissionGroupPerms
            .distinctBy { it.permissionGroupId }
            .associateBy({ it.permissionGroupId }, { mutableListOf<Long>() })

        permissionGroupPerms.forEach { perm ->
            permissionGroupPermIdListMap[perm.permissionGroupId]!!.add(perm.permissionId)
        }

        result["permissionGroups"] = permissionGroupList
        result["permissionGroupCount"] = permissionGroupCount
        result["totalPage"] = totalPage
        result["permissionGroupPerms"] = permissionGroupPermIdListMap
        result["permissions"] = permissions

        return Successful(result)
    }
}