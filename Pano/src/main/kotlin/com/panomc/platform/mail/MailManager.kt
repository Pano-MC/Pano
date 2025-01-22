package com.panomc.platform.mail

import com.panomc.platform.config.ConfigManager
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.error.NotExists
import com.panomc.platform.token.TokenProvider
import io.vertx.core.json.JsonObject
import io.vertx.ext.mail.MailMessage
import io.vertx.ext.web.templ.handlebars.HandlebarsTemplateEngine
import io.vertx.kotlin.coroutines.await
import io.vertx.sqlclient.SqlClient
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Lazy
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class MailManager(
    private val configManager: ConfigManager,
    private val templateEngine: HandlebarsTemplateEngine,
    private val mailClientProvider: MailClientProvider,
    private val databaseManager: DatabaseManager,
    private val tokenProvider: TokenProvider
) {
    private val mailClient by lazy {
        mailClientProvider.provide()
    }

    suspend fun sendMail(sqlClient: SqlClient, userId: Long, mail: Mail, email: String? = null) {
        val emailAddress =
            email ?: databaseManager.userDao.getEmailFromUserId(userId, sqlClient)
            ?: throw NotExists()

        val emailConfig = configManager.getConfig().getJsonObject("email")
        val message = MailMessage()

        message.from = emailConfig.getString("address")
        message.subject = mail.subject
        message.setTo(emailAddress)

        message.html = templateEngine.render(
            mail.parameterGenerator(
                emailAddress,
                userId,
                configManager.getConfig().getString("ui-address"),
                databaseManager,
                sqlClient,
                tokenProvider
            ),
            mail.templatePath
        ).await().toString()

        mailClient.sendMail(message).await()
    }

    suspend fun verifyConfiguration(configuration: JsonObject) {

    }
}