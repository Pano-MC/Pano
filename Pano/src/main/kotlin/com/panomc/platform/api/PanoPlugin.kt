package com.panomc.platform.api

import com.panomc.platform.Main
import com.panomc.platform.PluginEventManager
import com.panomc.platform.PluginUiManager
import com.panomc.platform.ReleaseStage
import com.panomc.platform.api.event.PluginEventListener
import io.vertx.core.Vertx
import kotlinx.coroutines.runBlocking
import org.pf4j.Plugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.context.annotation.AnnotationConfigApplicationContext

abstract class PanoPlugin : Plugin() {
    lateinit var pluginId: String
        internal set
    lateinit var vertx: Vertx
        internal set
    lateinit var pluginEventManager: PluginEventManager
        internal set
    lateinit var pluginUiManager: PluginUiManager
        internal set
    lateinit var environmentType: Main.Companion.EnvironmentType
        internal set
    lateinit var releaseStage: ReleaseStage
        internal set
    lateinit var pluginBeanContext: AnnotationConfigApplicationContext
        internal set
    lateinit var pluginGlobalBeanContext: AnnotationConfigApplicationContext
        internal set

    internal lateinit var applicationContext: AnnotationConfigApplicationContext

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val registeredBeans = mutableListOf<Any>()

    fun registerSingletonGlobal(bean: Any) {
        if (registeredBeans.contains(bean)) {
            return
        }

        println()
        println(pluginId)
        println(bean.javaClass.name)

        println()

        pluginGlobalBeanContext.beanFactory.registerSingleton(bean.javaClass.name, bean)

        registeredBeans.add(bean)
    }

    fun register(eventListener: PluginEventListener) {
        pluginEventManager.register(this, eventListener)
    }

    fun unRegisterGlobal(bean: Any) {
        if (!registeredBeans.contains(bean)) {
            return
        }

        val registry = pluginGlobalBeanContext.beanFactory as DefaultListableBeanFactory

        registry.destroySingleton(bean.javaClass.name)

        registeredBeans.remove(bean)
    }

    fun unRegister(eventListener: PluginEventListener) {
        pluginEventManager.unRegister(this, eventListener)
    }

    @Deprecated("Use onEnable method.")
    override fun start() {
        runBlocking {
            onEnable()
        }
    }

    @Deprecated("Use onDisable method.")
    override fun stop() {
        pluginBeanContext.close()

        val copyOfRegisteredBeans = registeredBeans.toList()

        copyOfRegisteredBeans.forEach {
            try {
                unRegisterGlobal(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        pluginEventManager.unregisterPlugin(this)
        pluginUiManager.unRegisterPlugin(this)

        runBlocking {
            onDisable()
        }
    }

    open suspend fun onLoad() {}

    open suspend fun onEnable() {}
    open suspend fun onDisable() {}
}