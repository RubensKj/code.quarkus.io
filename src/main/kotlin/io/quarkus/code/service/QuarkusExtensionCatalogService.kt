package io.quarkus.code.service

import com.google.common.base.Preconditions.checkState
import io.quarkus.code.config.CodeQuarkusConfig
import io.quarkus.code.config.ExtensionProcessorConfig
import io.quarkus.code.config.QuarkusPlatformConfig
import io.quarkus.code.misc.QuarkusExtensionUtils
import io.quarkus.code.misc.QuarkusExtensionUtils.toShortcut
import io.quarkus.code.model.CodeQuarkusExtension
import io.quarkus.devtools.project.QuarkusProjectHelper
import io.quarkus.platform.tools.ToolsUtils
import io.quarkus.runtime.StartupEvent
import org.eclipse.microprofile.config.spi.ConfigProviderResolver
import java.lang.IllegalArgumentException
import java.util.logging.Level
import java.util.logging.Logger
import java.util.stream.Collectors
import javax.enterprise.event.Observes
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuarkusExtensionCatalogService {

    companion object {
        private val LOG = Logger.getLogger(QuarkusExtensionCatalogService::class.java.name)

        @JvmStatic
        internal val platformGroupId = ConfigProviderResolver.instance().config.getOptionalValue("io.quarkus.code.quarkus-platform.group-id", String::class.java).orElse("io.quarkus")

        @JvmStatic
        internal val platformArtifactId = ConfigProviderResolver.instance().config.getOptionalValue("io.quarkus.code.quarkus-platform.artifact-id", String::class.java).orElse("quarkus-universe-bom")

        @JvmStatic
        internal val platformVersion = ConfigProviderResolver.instance().config.getValue("io.quarkus.code.quarkus-platform.version", String::class.java)

        @JvmStatic
        internal val bundledQuarkusVersion = ConfigProviderResolver.instance().config.getValue("io.quarkus.code.quarkus-version", String::class.java)

        @JvmStatic
        internal val catalog = ToolsUtils.resolvePlatformDescriptorDirectly(platformGroupId, platformArtifactId, platformVersion, QuarkusProjectHelper.artifactResolver(), QuarkusProjectHelper.messageWriter())

        init {
            checkState(platformVersion.isNotEmpty()) { "io.quarkus.code.quarkus-platform-version must not be null or empty" }
            checkState(bundledQuarkusVersion.isNotEmpty()) { "io.quarkus.code.quarkus-version must not be null or empty" }
        }
    }

    @Inject
    lateinit var config: CodeQuarkusConfig

    @Inject
    lateinit var extensionProcessorConfig: ExtensionProcessorConfig

    lateinit var extensions: List<CodeQuarkusExtension>

    lateinit var extensionsByShortId: Map<String, CodeQuarkusExtension>
    lateinit var extensionsById: Map<String, CodeQuarkusExtension>

    fun onStart(@Observes e: StartupEvent) {
        extensions = QuarkusExtensionUtils.processExtensions(catalog, extensionProcessorConfig)
        extensionsByShortId = extensions.associateBy { it.shortId }
        extensionsById = extensions.associateBy { it.id }
        LOG.log(Level.INFO) {"""
            Extensions Catalog has been processed with ${extensions.size} extensions:
                Quarkus platform: $platformGroupId:$platformArtifactId:$platformVersion
                tagsFrom: ${extensionProcessorConfig.tagsFrom}
        """.trimIndent()}
    }


    fun checkAndMergeExtensions(extensionsIds: Set<String>?, rawShortExtensions: String?): Set<String> {
        val fromId = (extensionsIds ?: setOf())
                .stream()
                .filter { it.isNotBlank() }
                .map { findById(it) }
                .collect(Collectors.toSet())
        val fromShortId = parseShortExtensions(rawShortExtensions).stream()
                .map { (this.extensionsByShortId[it] ?: throw IllegalArgumentException("Invalid shortId: $it")).id }
                .collect(Collectors.toSet())
        return fromId union fromShortId
    }

    private fun findById(id: String): String {
        if(this.extensionsById.containsKey(id)) {
            return this.extensionsById[id]!!.id
        }
        val found = extensionsById.entries
            .filter { toShortcut(it.key) == toShortcut(id) }
        if (found.size == 1) {
            return found[0].value.id
        }
        throw IllegalArgumentException("Invalid extension: $id")
    }

    private fun parseShortExtensions(shortExtension: String?): Set<String> {
        return if (shortExtension.isNullOrBlank()) {
            setOf()
        } else {
            shortExtension.split(".").filter { it.isNotBlank() }.toSet()
        }
    }

}