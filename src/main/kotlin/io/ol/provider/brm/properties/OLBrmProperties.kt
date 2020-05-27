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
     * If OLTimeoutProperties are provided, default value will be overridden with provided values in the init() method
     */
    const val DEFAULT_TIMEOUT = -1
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

  data class ProjectBrmProperties(
    /**
     * hostname.
     */
    var host: String = "",
    /**
     * User with permissions to access the host.
     */
    var user: String = "",
    /**
     * Password of the user.
     */
    var password: String = "",
    /**
     * By setting this to true, the internal HTTP client will trust self signed certificates. Note that this is not
     * recommended. It is better to use a signed certificates or import the certificate in your JVM truststore.
     */
    var isTrustSelfSigned: Boolean = false,
    /**
     * Sets the time in milliseconds before the HTTP client will drop the request.
     *
     * A timeout value of zero is interpreted as an infinite timeout.
     * Setting zero or a negative value disables the timeout.
     */
    var timeout: Int = DEFAULT_TIMEOUT,
    var headers: Map<String, String> = mutableMapOf(),
    var decimalSeparator: String = ".",
    /**
     * PEM formatted client certificate key
     */
    var clientCertificate: String = "",
    /**
     * PEM formatted client's private key
     * To extract PEM key from JKS keystore check out
     * @see <a href="https://dzone.com/articles/extracting-a-private-key-from-java-keystore-jks">Extracting Private Key from JKS Keystore</a>
     */
    var clientPrivateKey: String = ""
  ) : RpcSdkProperties {
    override fun backendSolution(): BackendSolution = BackendSolution.MF_RPC // TODO: Change it to BRM when integrating into openlegacy-core repository
  }
}
