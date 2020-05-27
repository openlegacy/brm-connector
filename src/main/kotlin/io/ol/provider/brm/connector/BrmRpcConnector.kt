package io.ol.provider.brm.connector

import io.ol.core.rpc.connector.RpcConnector
import io.ol.core.rpc.connector.RpcSendResult
import io.ol.core.rpc.serialize.RpcDeserializer
import io.ol.core.rpc.serialize.RpcSerializeRequest
import io.ol.core.rpc.serialize.RpcSerializer
import io.ol.core.tracing.TracingExecutor
import io.ol.impl.common.debug.InternalDebug
import io.ol.impl.common.debug.InternalDebugType
import io.ol.provider.brm.properties.OLBrmProperties
import io.ol.provider.brm.serialize.BrmInputRpcData
import io.ol.provider.brm.serialize.BrmOutputRpcData
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.HttpRequest
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.kotlin.core.net.pemKeyCertOptionsOf
import io.vertx.kotlin.ext.web.client.sendAwait
import io.vertx.kotlin.ext.web.client.sendBufferAwait
import mu.KLogging
import org.openlegacy.core.model.legacy.type.HttpLegacyTypes
import org.openlegacy.utils.TimeUtils
import org.openlegacy.utils.UrlUtils
import org.openlegacy.utils.extensions.excludeHttpMethodFromPath

class BrmRpcConnector(
  override val serializer: RpcSerializer<BrmInputRpcData>,
  override val deserializer: RpcDeserializer<BrmOutputRpcData>,
  override val tracingExecutor: TracingExecutor,
  private val sdkProperties: OLBrmProperties.ProjectBrmProperties,
  private val vertx: Vertx,
  private val internalDebug: InternalDebug
) : RpcConnector<BrmInputRpcData, BrmOutputRpcData> {

  companion object : KLogging()

  private val webClientOptions = WebClientOptions().apply {
    if (sdkProperties.host.startsWith("https://", true)) {
      isSsl = true
      if (sdkProperties.isTrustSelfSigned) {
        this.isVerifyHost = false
        this.isTrustAll = true
      }
    }
    val clientCertificate = sdkProperties.clientCertificate
    val clientPrivateKey = sdkProperties.clientPrivateKey

    if (clientCertificate.isNotEmpty() && clientPrivateKey.isNotEmpty()) {
      this.pemKeyCertOptions = pemKeyCertOptionsOf(
        certValue = Buffer.buffer(clientCertificate),
        keyValue = Buffer.buffer(clientPrivateKey)
      )
    }
  }

  private val webClient: WebClient by lazy { WebClient.create(vertx, webClientOptions) }

  override fun close() {
    webClient.close()
  }

  override suspend fun send(rpcData: BrmInputRpcData, timeout: Long): RpcSendResult<BrmOutputRpcData> {
    return tracingExecutor.traceAwait("brm-rpc-inner-send") {
      val relativePath = rpcData.relativeRequestUri
      val httpMethod = null
      val url = if (sdkProperties.host.isNotBlank()) UrlUtils.build(sdkProperties.host, relativePath) else relativePath
      // sets timeout
      val actualTimeout = TimeUtils.getTimeoutByPropagation(timeout, sdkProperties.timeout.toLong())

      val httpRequest: HttpRequest<Buffer> = webClient
        .requestAbs(httpMethod, url)
        .timeout(actualTimeout)
        .apply {
          // adds authentication header
          if (sdkProperties.user.isNotBlank()) {
            basicAuthentication(sdkProperties.user, sdkProperties.password)
          }
          // adds headers
          headers().addAll(sdkProperties.headers)
          headers().addAll(rpcData.headers)
        }
      val requestBody = rpcData.body
      // sends body payload only if HTTP method is not GET
      val sendBodyPayload = httpMethod != HttpMethod.GET
      if (internalDebug.type != InternalDebugType.NONE) {
        internalDebug.handleRequest(request = requestBody.bytes, correlationId = System.currentTimeMillis().toString(), prefix = "brm")
      }
      val response: HttpResponse<Buffer> = if (sendBodyPayload) httpRequest.sendBufferAwait(requestBody) else httpRequest.sendAwait()
      val responseBody = response.body()
      if (internalDebug.type != InternalDebugType.NONE) {
        internalDebug.handleResponse(response = responseBody.bytes, correlationId = System.currentTimeMillis().toString(), prefix = "brm")
      }
      val properties: MutableMap<String, String> = mutableMapOf()
      properties[HttpLegacyTypes.StatusCode::class.java.name] = response.statusCode().toString()
      properties[HttpLegacyTypes.StatusMessage::class.java.name] = response.statusMessage()

      return@traceAwait RpcSendResult(
        statusCode = response.statusCode(),
        body = BrmOutputRpcData(body = responseBody ?: Buffer.buffer()),
        headers = response.headers(),
        properties = properties
      )
    }
  }

  override fun prepareRpcData(request: RpcSerializeRequest): BrmInputRpcData {
    val rpcDataBody = when (val body = request.input) {
      is JsonObject -> body.toBuffer()
      is JsonArray -> body.toBuffer()
      else -> Buffer.buffer()
    }
    return BrmInputRpcData(
      body = rpcDataBody,
      operationDefinition = request.operationDefinition,
      headers = request.headers,
      properties = request.properties,
      relativeRequestUri = request.operationDefinition.excludeHttpMethodFromPath(),
      projectProperties = sdkProperties
    )
  }
}
