package com.panomc.platform.route.api.setup

import com.panomc.platform.AppConstants
import com.panomc.platform.AppConstants.AVAILABLE_LOCALES
import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.auth.AuthProvider
import com.panomc.platform.config.ConfigManager
import com.panomc.platform.db.DatabaseManager
import com.panomc.platform.model.*
import com.panomc.platform.setup.SetupManager
import com.panomc.platform.util.CSRFTokenGenerator
import com.panomc.platform.util.RegisterUtil
import com.panomc.platform.util.UIHelper
import io.vertx.core.http.Cookie
import io.vertx.core.http.HttpClient
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.RequestPredicate
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Bodies
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaParser
import io.vertx.json.schema.common.dsl.Schemas.*
import org.springframework.context.annotation.Lazy

@Endpoint
class FinishAPI(
    private val setupManager: SetupManager,
    private val databaseManager: DatabaseManager,
    private val authProvider: AuthProvider,
    private val configManager: ConfigManager,
    private val httpClient: HttpClient,
    @Lazy private val router: Router
) : SetupApi(setupManager) {
    override val paths = listOf(Path("/api/setup/finish", RouteType.POST))

    override fun getValidationHandler(schemaParser: SchemaParser): ValidationHandler =
        ValidationHandlerBuilder.create(schemaParser)
            .body(
                Bodies.json(
                    objectSchema()
                        .requiredProperty("username", stringSchema())
                        .requiredProperty("email", stringSchema())
                        .requiredProperty("password", stringSchema())
                        .requiredProperty(
                            "setupLocale",
                            enumSchema(*AVAILABLE_LOCALES.toTypedArray())
                        )
                )
            )
            .predicate(RequestPredicate.BODY_REQUIRED)
            .build()

    override suspend fun handle(context: RoutingContext): Result {
        if (setupManager.getStep() != 4) {
            return Successful(setupManager.getCurrentStepData().map)
        }

        val parameters = getParameters(context)
        val data = parameters.body().jsonObject

        val username = data.getString("username")
        val email = data.getString("email")
        val password = data.getString("password")
        val setupLocale = data.getString("setupLocale")

        val remoteIP = context.request().remoteAddress().host()

        RegisterUtil.validateForm(
            username,
            email,
            password,
            password,
            true,
            "",
            null
        )

        val sqlClient = getSqlClient()

        databaseManager.initDatabase(sqlClient)

        RegisterUtil.register(
            databaseManager,
            sqlClient,
            username,
            email,
            password,
            remoteIP,
            isAdmin = true,
            isSetup = true
        )

        val token = authProvider.login(username, sqlClient)

        configManager.getConfig().put("locale", setupLocale)

        configManager.saveConfig()

        setupManager.finishSetup()

        UIHelper.prepareUI(setupManager, httpClient, router)

        val response = context.response()

        val csrfToken = CSRFTokenGenerator.nextToken()

        val jwtCookie = Cookie.cookie(AppConstants.COOKIE_PREFIX + AppConstants.JWT_COOKIE_NAME, token)
        val csrfTokenCookie = Cookie.cookie(AppConstants.COOKIE_PREFIX + AppConstants.CSRF_TOKEN_COOKIE_NAME, csrfToken)

        jwtCookie.path = "/"
        jwtCookie.isHttpOnly = true

        csrfTokenCookie.path = "/"
        csrfTokenCookie.isHttpOnly = true

        response.addCookie(jwtCookie)
        response.addCookie(csrfTokenCookie)

        return Successful(
            mapOf(
                "jwt" to token
            )
        )
    }
}