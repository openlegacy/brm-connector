package io.ol.provider.brm.properties

import io.ol.core.properties.RpcSdkProperties
import io.ol.core.properties.TimeoutProperties
import io.ol.provider.brm.BrmRpcConnectionFactory
import io.ol.provider.brm.properties.OLBrmProperties.ProjectBrmProperties
import io.ol.provider.brm.serialize.BrmRpcDeserializer
import io.ol.provider.brm.serialize.BrmRpcSerializer
import org.openlegacy.core.beans.RpcBeanNames
import org.openlegacy.core.definitions.BackendSolution
import org.openlegacy.impl.SpringConditions.conditionalOnMissingBean
import org.openlegacy.impl.properties.ProjectProperties
import org.openlegacy.impl.rpc.config.RpcConfigurationDsl
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.beans
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
@ConfigurationProperties(prefix = "ol.brm")
data class OLBrmProperties(
  override var timeoutProperties: TimeoutProperties = TimeoutProperties()
) : ProjectProperties<ProjectBrmProperties>, RpcConfigurationDsl {
  companion object {
    /**
     * Default timeout value (no timeout).
     * If [TimeoutProperties] are provided, default value will be overridden with provided values in the init() method
     */
    const val DEFAULT_TIMEOUT = 0
  }

  /**
   * Map of project specific RPC properties. Key is orchestrated-key.
   */
  override var project: MutableMap<String, ProjectBrmProperties> = mutableMapOf()

  @PostConstruct
  fun init() {
    val backendDefaultTimeout = this.timeoutProperties.backend[BackendSolution.MF_RPC] // TODO: Change it to BRM when integrating into openlegacy-core repository
    if (backendDefaultTimeout != null) {
      this.project.values.forEach { olBrmProjectProperties ->
        if (olBrmProjectProperties.timeout == DEFAULT_TIMEOUT) {
          // olBrmProjectProperties.timeout = backendDefaultTimeout.toInt() // TODO: Uncomment it when integrating into openlegacy-core repository
        }
      }
    }
  }

  override fun loadBeans(applicationContext: GenericApplicationContext) {
    beans {
      project.entries.forEach { entry ->
        val orchestratedKey = entry.key
        val sdkProperties = entry.value

        commonOrchestratedBeans(entry, applicationContext)

        // BRM RPC Serializer
        conditionalOnMissingBean("$orchestratedKey${RpcBeanNames.RPC_SERIALIZER_SUFFIX}", applicationContext) {
          bean(name = "$orchestratedKey${RpcBeanNames.RPC_SERIALIZER_SUFFIX}") {
            BrmRpcSerializer(ref())
          }
        }
        // BRM RPC Deserializer
        conditionalOnMissingBean("$orchestratedKey${RpcBeanNames.RPC_DESERIALIZER_SUFFIX}", applicationContext) {
          bean(name = "$orchestratedKey${RpcBeanNames.RPC_DESERIALIZER_SUFFIX}") {
            BrmRpcDeserializer(ref())
          }
        }
        // BRM RPC Connection Factory
        conditionalOnMissingBean("$orchestratedKey${RpcBeanNames.RPC_CONNECTION_FACTORY_SUFFIX}", applicationContext) {
          bean(name = "$orchestratedKey${RpcBeanNames.RPC_CONNECTION_FACTORY_SUFFIX}") {
            BrmRpcConnectionFactory(
              sdkProperties = sdkProperties,
              serializer = ref("$orchestratedKey${RpcBeanNames.RPC_SERIALIZER_SUFFIX}"),
              deserializer = ref("$orchestratedKey${RpcBeanNames.RPC_DESERIALIZER_SUFFIX}"),
              vertx = ref(),
              tracingExecutor = ref(),
              interceptorChain = ref(),
              timeoutProperties = timeoutProperties,
              olDebugProperties = ref()
            )
          }
        }
      }
    }.initialize(applicationContext)
  }

  /**
   * Properties which represents BRM Infranet properties based on the documentation - https://docs.oracle.com/cd/E16754_01/doc.75/e16702/prg_client_javapcm.htm#CHDDJIEC
   */
  data class ProjectBrmProperties(
    /**
     * Login type: 0 or 1. The default is 1.
     * A type 1 login requires the application to provide a username and password.
     * A type 0 login is a trusted login that comes through a CM Proxy, for example, and does not require a username and password in the properties.
     */
    var login_type: Int = 1,
    /**
     * hostname without protocol, e.g. localhost or 127.0.0.1, etc.
     */
    var host: String = "",
    /**
     * port.
     */
    var port: Int = 11960,
    /**
     * service name, e.g. /service/admin_client.
     */
    var service: String = "/service/admin_client",
    /**
     * A type 0 login requires a full POID (Portal Object ID) of the service
     * A type 1 login uses a default value 1
     */
    var service_poid: Int = 1,
    /**
     * Required when Login type is 0.
     * The number assigned to BRM database when the BRM Data Manager was installed.
     */
    var database_no: String = "",
    /**
     * User with permissions to access the host.
     */
    var username: String = "",
    /**
     * Password of the user.
     */
    var password: String = "",
    /**
     * Sets the time in milliseconds to wait before the BRM client will drop the request.
     */
    var timeout: Int = DEFAULT_TIMEOUT
  ) : RpcSdkProperties {
    override fun backendSolution(): BackendSolution = BackendSolution.MF_RPC // TODO: Change it to BRM when integrating into openlegacy-core repository
  }
}
