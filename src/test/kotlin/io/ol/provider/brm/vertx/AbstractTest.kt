package io.ol.provider.brm.vertx

import io.ol.core.properties.TimeoutProperties
import io.ol.core.rpc.serialize.DynamicFieldFunctionsRegistry
import io.ol.impl.common.debug.InternalDebugType
import io.ol.impl.common.properties.OLDebugProperties
import io.ol.provider.brm.dsl.BrmProviderOptions
import io.ol.provider.brm.dsl.brmRpcConnectionFactory
import io.ol.provider.brm.properties.OLBrmProperties.ProjectBrmProperties
import io.ol.provider.brm.serialize.BrmRpcDeserializer
import io.ol.provider.brm.serialize.BrmRpcSerializer
import io.vertx.core.Vertx

abstract class AbstractTest {
  companion object {
    val vertx = Vertx.vertx()
  }

  private val sdkProperties = ProjectBrmProperties().apply {
    // must match to the corresponding properties in the application.yml file
    host = "localhost"
    username = "root.0.0.0.1"
    password = "password"
  }

  private val rpcConnectionFactory = brmRpcConnectionFactory(
    sdkProperties = sdkProperties,
    options = BrmProviderOptions(
      vertx = vertx,
      serializer = BrmRpcSerializer(DynamicFieldFunctionsRegistry()),
      deserializer = BrmRpcDeserializer(DynamicFieldFunctionsRegistry()),
      olDebugProperties = OLDebugProperties().apply {
        internal.type = InternalDebugType.LOG
      },
      // No Timeout for this test
      timeoutProperties = TimeoutProperties()
    )
  )

  val rpcConnection = rpcConnectionFactory.getConnection()
}
