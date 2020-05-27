package io.ol.provider.brm

import io.ol.core.properties.TimeoutProperties
import io.ol.core.rpc.RpcConnectionFactory
import io.ol.core.rpc.interceptor.RpcInterceptorChain
import io.ol.core.rpc.serialize.RpcDeserializer
import io.ol.core.rpc.serialize.RpcSerializer
import io.ol.core.tracing.TracingExecutor
import io.ol.impl.common.properties.OLDebugProperties
import io.ol.provider.brm.connector.BrmRpcConnector
import io.ol.provider.brm.properties.OLBrmProperties.ProjectBrmProperties
import io.ol.provider.brm.serialize.BrmInputRpcData
import io.ol.provider.brm.serialize.BrmOutputRpcData
import io.vertx.core.Vertx
import org.openlegacy.impl.debug.InternalDebugWrapper

class BrmRpcConnectionFactory(
  private val sdkProperties: ProjectBrmProperties,
  private val serializer: RpcSerializer<BrmInputRpcData>,
  private val deserializer: RpcDeserializer<BrmOutputRpcData>,
  private val vertx: Vertx,
  private val tracingExecutor: TracingExecutor,
  private val interceptorChain: RpcInterceptorChain,
  private val timeoutProperties: TimeoutProperties,
  private val olDebugProperties: OLDebugProperties
) : RpcConnectionFactory<BrmRpcConnection> {

  override fun getConnection(): BrmRpcConnection {
    return BrmRpcConnection(
      vertx = vertx,
      connector = getConnector(),
      interceptorChain = interceptorChain,
      timeoutProperties = timeoutProperties
    )
  }

  /**
   * Extracted to a separate function specifically for usage in unit tests, because connector is being created only here (i.e. no bean available) and because Kotlin final classes with private variables are hard to mock
   */
  fun getConnector(): BrmRpcConnector {
    return BrmRpcConnector(
      serializer = serializer,
      deserializer = deserializer,
      sdkProperties = sdkProperties,
      tracingExecutor = tracingExecutor,
      vertx = vertx,
      internalDebug = InternalDebugWrapper(olDebugProperties)
    )
  }

  override fun getConnection(user: String, password: String): BrmRpcConnection {
    return getConnection()
  }
}
