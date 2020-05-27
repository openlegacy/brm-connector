package io.ol.provider.brm

import io.ol.core.properties.TimeoutProperties
import io.ol.core.rpc.RpcConnection
import io.ol.core.rpc.interceptor.RpcInterceptorChain
import io.ol.provider.brm.serialize.BrmInputRpcData
import io.ol.provider.brm.serialize.BrmOutputRpcData
import io.ol.provider.brm.connector.BrmRpcConnector

import io.vertx.core.Vertx

class BrmRpcConnection(
        override val vertx: Vertx,
        override val connector: BrmRpcConnector,
        override val interceptorChain: RpcInterceptorChain,
        override val timeoutProperties: TimeoutProperties
) : RpcConnection<BrmInputRpcData, BrmOutputRpcData>
