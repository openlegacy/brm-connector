package io.ol.provider.brm.dsl

import io.ol.core.properties.TimeoutProperties
import io.ol.core.rpc.interceptor.RpcInterceptorChain
import io.ol.core.rpc.serialize.RpcDeserializer
import io.ol.core.rpc.serialize.RpcSerializer
import io.ol.core.tracing.TracingExecutor
import io.ol.impl.common.properties.OLDebugProperties
import io.ol.provider.brm.BrmRpcConnectionFactory
import io.ol.provider.brm.properties.OLBrmProperties.ProjectBrmProperties
import io.ol.provider.brm.serialize.BrmInputRpcData
import io.ol.provider.brm.serialize.BrmOutputRpcData
import io.vertx.core.Vertx
import org.openlegacy.impl.tracing.TracingExecutorImpl
import org.openlegacy.impl.tracing.tracer.NoOpTracer

class BrmProviderOptions(
  val vertx: Vertx = Vertx.vertx(),
  val serializer: RpcSerializer<BrmInputRpcData>,
  val deserializer: RpcDeserializer<BrmOutputRpcData>,
  val tracingExecutor: TracingExecutor = TracingExecutorImpl(NoOpTracer()),
  val interceptorChain: RpcInterceptorChain = RpcInterceptorChain(),
  val olDebugProperties: OLDebugProperties = OLDebugProperties(),
  val timeoutProperties: TimeoutProperties = TimeoutProperties()
)

fun brmRpcConnectionFactory(
  sdkProperties: ProjectBrmProperties,
  options: BrmProviderOptions
): BrmRpcConnectionFactory {
  return BrmRpcConnectionFactory(
    sdkProperties = sdkProperties,
    serializer = options.serializer,
    deserializer = options.deserializer,
    vertx = options.vertx,
    tracingExecutor = options.tracingExecutor,
    interceptorChain = options.interceptorChain,
    timeoutProperties = options.timeoutProperties,
    olDebugProperties = options.olDebugProperties
  )
}
