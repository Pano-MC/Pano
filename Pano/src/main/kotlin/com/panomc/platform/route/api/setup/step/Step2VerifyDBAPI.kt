package com.panomc.platform.route.api.setup.step

import com.panomc.platform.annotation.Endpoint
import com.panomc.platform.error.InvalidData
import com.panomc.platform.model.*
import com.panomc.platform.setup.SetupManager
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.validation.RequestPredicate
import io.vertx.ext.web.validation.ValidationHandler
import io.vertx.ext.web.validation.builder.Bodies
import io.vertx.ext.web.validation.builder.ValidationHandlerBuilder
import io.vertx.json.schema.SchemaParser
import io.vertx.json.schema.common.dsl.Schemas
import io.vertx.kotlin.coroutines.await
import io.vertx.mysqlclient.MySQLConnectOptions
import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.PoolOptions
import org.slf4j.Logger

@Endpoint
class Step2VerifyDBAPI(private val logger: Logger, setupManager: SetupManager) : SetupApi(setupManager) {
    override val paths = listOf(Path("/api/setup/steps/2/verify", RouteType.POST))

    override fun getValidationHandler(schemaParser: SchemaParser): ValidationHandler =
        ValidationHandlerBuilder.create(schemaParser)
            .body(
                Bodies.json(
                    Schemas.objectSchema()
                        .requiredProperty("host", Schemas.stringSchema())
                        .requiredProperty("dbName", Schemas.stringSchema())
                        .requiredProperty("username", Schemas.stringSchema())
                        .optionalProperty("password", Schemas.stringSchema())
                )
            )
            .predicate(RequestPredicate.BODY_REQUIRED)
            .build()

    override suspend fun handle(context: RoutingContext): Result {
        val parameters = getParameters(context)
        val data = parameters.body().jsonObject

        var port = 3306
        var host = data.getString("host")

        if (host.contains(":")) {
            val splitHost = host.split(":")

            host = splitHost[0]

            port = splitHost[1].toInt()
        }

        val connectOptions = MySQLConnectOptions()
            .setPort(port)
            .setHost(host)
            .setDatabase(data.getString("dbName"))
            .setUser(data.getString("username"))

        if (!data.getString("password").isNullOrEmpty())
            connectOptions.password = data.getString("password")

        val poolOptions = PoolOptions()
            .setMaxSize(1)

        val mySQLPool = MySQLPool.pool(context.vertx(), connectOptions, poolOptions)

        try {
            val connection = mySQLPool.connection.await()

            connection.close().await()

            mySQLPool.close().await()
        } catch (e: java.lang.Exception) {
            logger.error(e.toString())

            throw InvalidData()
        }

        return Successful()
    }
}