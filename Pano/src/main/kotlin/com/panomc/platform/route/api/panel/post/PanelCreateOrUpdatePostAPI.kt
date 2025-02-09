package com.panomc.platform.route.api.panel.post

import com.panomc.platform.AppConstants
import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.auth.panel.log.PublishedPostLog
import com.panomc.platform.auth.panel.log.UpdatedPostLog
import com.panomc.platform.auth.panel.permission.ManagePostsPermission
import com.panomc.platform.config.ConfigManager
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.db.model.Post
import com.panomc.platform.db.model.Post.Companion.deleteThumbnailFile
import com.panomc.platform.error.NotExists
import com.panomc.platform.model.*
import com.panomc.platform.util.FileUploadUtil
import com.panomc.platform.util.PostStatus
import com.panomc.platform.util.TextUtil
import io.vertx.ext.web.FileUpload
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.RequestPredicate
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Bodies.multipartFormData
import io.vertx.ext.web.validation.builder.Parameters.optionalParam
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaParser
import io.vertx.json.schema.common.dsl.Schemas.*
import java.io.File

@Endpoint
class PanelCreateOrUpdatePostAPI(
    private val databaseManager: DatabaseManager,
    private val authProvider: AuthProvider,
    private val configManager: ConfigManager
) : PanelApi() {
    override val paths = listOf(
        Path("/api/panel/posts/:id", RouteType.PUT),
        Path("/api/panel/post", RouteType.POST)
    )

    override fun getValidationHandler(schemaParser: SchemaParser): ValidationHandler =
        ValidationHandlerBuilder.create(schemaParser)
            .pathParameter(optionalParam("id", numberSchema()))
            .body(
                multipartFormData(
                    objectSchema()
                        .requiredProperty("title", stringSchema())
                        .requiredProperty("category", numberSchema())
                        .requiredProperty("text", stringSchema())
                        .optionalProperty("publish", booleanSchema())
                        .optionalProperty("removeThumbnail", booleanSchema())
                )
            )
            .predicate(RequestPredicate.BODY_REQUIRED)
            .build()

    override suspend fun handle(context: RoutingContext): Result {
        authProvider.requirePermission(ManagePostsPermission(), context)

        val parameters = getParameters(context)
        val data = parameters.body().jsonObject

        val fileUploads = context.fileUploads()

        val id = parameters.pathParameter("id")?.long
        val title = data.getString("title")
        val categoryId = data.getLong("category")
        val text = data.getString("text")
        val removeThumbnail = data.getBoolean("removeThumbnail") ?: false
        val publish = data.getBoolean("publish") ?: true
        val url = TextUtil.convertStringToUrl(title, 32)

        var thumbnailUrl = ""
        val body = mutableMapOf<String, Any?>()

        val userId = authProvider.getUserIdFromRoutingContext(context)

        val sqlClient = getSqlClient()

        var postInDb: Post? = null

        if (id == null) {
            thumbnailUrl = saveUploadedFileAndGetThumbnailUrl(fileUploads, null)
        } else {
            postInDb = databaseManager.postDao.getById(id, sqlClient) ?: throw NotExists()

            if (removeThumbnail) {
                postInDb.deleteThumbnailFile(configManager)
            } else {
                thumbnailUrl = saveUploadedFileAndGetThumbnailUrl(fileUploads, postInDb)
            }
        }

        val post = Post(
            id = id ?: -1,
            title = title,
            categoryId = categoryId,
            writerUserId = userId,
            text = text,
            thumbnailUrl = thumbnailUrl,
            status = if (postInDb == null) // when creating post first time
                if (publish)
                    PostStatus.PUBLISHED
                else // for save button
                    PostStatus.DRAFT
            else // when the post is already exists
                if (publish)
                    PostStatus.PUBLISHED
                else // for save button
                    postInDb.status,
            url = if (id == null) url else "$url-$id"
        )

        val username = databaseManager.userDao.getUsernameFromUserId(userId, sqlClient)!!

        if (id == null) {
            val postId = databaseManager.postDao.insert(post, sqlClient)

            databaseManager.postDao.updatePostUrlByUrl(url, "$url-$postId", sqlClient)

            body["id"] = postId


            databaseManager.panelActivityLogDao.add(PublishedPostLog(userId, username, title), sqlClient)
        } else {
            databaseManager.postDao.update(userId, post, sqlClient)

            databaseManager.panelActivityLogDao.add(UpdatedPostLog(userId, username, title), sqlClient)
        }

        return Successful(body)
    }

    private fun saveUploadedFileAndGetThumbnailUrl(fileUploads: List<FileUpload>, postInDb: Post?): String {
        var thumbnailUrl = postInDb?.thumbnailUrl ?: ""
        val savedFiles = FileUploadUtil.saveFiles(fileUploads, Post.acceptedFileFields, configManager)

        if (savedFiles.isNotEmpty()) {
            postInDb?.deleteThumbnailFile(configManager)

            thumbnailUrl = AppConstants.POST_THUMBNAIL_URL_PREFIX + savedFiles[0].path.split(File.separator).last()
        }

        return thumbnailUrl
    }
}