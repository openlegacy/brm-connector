package io.ol.provider.brm.connector

import com.portal.pcm.EBufException
import com.portal.pcm.FList
import com.portal.pcm.Poid
import com.portal.pcm.PortalContext
import com.portal.pcm.PortalOp
import com.portal.pcm.fields.FldFirstName
import com.portal.pcm.fields.FldLastName
import com.portal.pcm.fields.FldPoid
import io.ol.core.rpc.connector.RpcConnector
import io.ol.core.rpc.connector.RpcSendResult
import io.ol.core.rpc.serialize.RpcDeserializer
import io.ol.core.rpc.serialize.RpcSerializeRequest
import io.ol.core.rpc.serialize.RpcSerializer
import io.ol.core.tracing.TracingExecutor
import io.ol.impl.common.debug.InternalDebug
import io.ol.provider.brm.properties.OLBrmProperties
import io.ol.provider.brm.serialize.BrmInputRpcData
import io.ol.provider.brm.serialize.BrmOutputRpcData
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import mu.KLogging
import org.apache.commons.lang3.StringUtils
import org.openlegacy.utils.extensions.excludeHttpMethodFromPath
import java.io.File
import java.io.FileInputStream
import java.util.Properties

class BrmRpcConnector(
  override val serializer: RpcSerializer<BrmInputRpcData>,
  override val deserializer: RpcDeserializer<BrmOutputRpcData>,
  override val tracingExecutor: TracingExecutor,
  private val sdkProperties: OLBrmProperties.ProjectBrmProperties,
  private val vertx: Vertx,
  private val internalDebug: InternalDebug
) : RpcConnector<BrmInputRpcData, BrmOutputRpcData> {

  companion object : KLogging()

  private val portalContext: PortalContext = createPortalContext(sdkProperties)

  override fun close() {
    // closes the connection
    portalContext.close(true)
    logger.info("PCM connection closed.")
  }

  override suspend fun send(rpcData: BrmInputRpcData, timeout: Long): RpcSendResult<BrmOutputRpcData> {
//    return tracingExecutor.traceAwait("brm-rpc-inner-send") {
//      val relativePath = rpcData.relativeRequestUri
//      val httpMethod = null
//      val url = if (sdkProperties.host.isNotBlank()) UrlUtils.build(sdkProperties.host, relativePath) else relativePath
//      // sets timeout
//      val actualTimeout = TimeUtils.getTimeoutByPropagation(timeout, sdkProperties.timeout.toLong())
//
//
//      val requestBody = rpcData.body
//      // sends body payload only if HTTP method is not GET
//      val sendBodyPayload = httpMethod != HttpMethod.GET
//      if (internalDebug.type != InternalDebugType.NONE) {
//        internalDebug.handleRequest(request = requestBody.bytes, correlationId = System.currentTimeMillis().toString(), prefix = "brm")
//      }
//      val response: HttpResponse<Buffer> = if (sendBodyPayload) httpRequest.sendBufferAwait(requestBody) else httpRequest.sendAwait()
//      val responseBody = response.body()
//      if (internalDebug.type != InternalDebugType.NONE) {
//        internalDebug.handleResponse(response = responseBody.bytes, correlationId = System.currentTimeMillis().toString(), prefix = "brm")
//      }
//      val properties: MutableMap<String, String> = mutableMapOf()
//      properties[HttpLegacyTypes.StatusCode::class.java.name] = response.statusCode().toString()
//      properties[HttpLegacyTypes.StatusMessage::class.java.name] = response.statusMessage()
//
//      return@traceAwait RpcSendResult(
//        statusCode = response.statusCode(),
//        body = BrmOutputRpcData(body = responseBody ?: Buffer.buffer()),
//        headers = response.headers(),
//        properties = properties
//      )
//    }
    val inflist = FList()
    // adds data to the flist
    inflist.set(FldPoid.getInst(), Poid(1))
    inflist.set(FldFirstName.getInst(), "Mickey")
    inflist.set(FldLastName.getInst(), "Mouse")
    // Calls the opcode
    logger.debug { "Input: $inflist" }
    val outflist: FList = portalContext.opcode(PortalOp.TEST_LOOPBACK, inflist)
    logger.debug { "Output: $outflist" }
      return RpcSendResult(
        body = BrmOutputRpcData(body = Buffer.buffer()),
        statusCode = 200
      )
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

  private fun createPortalContext(sdkProperties: OLBrmProperties.ProjectBrmProperties): PortalContext {
    return try {
      val ctx = PortalContext()
      ctx.connect(loadBrmConnectionProperties(sdkProperties))
      // prints out some info about the connection
      logger.info("""
        Context successfully created
        current DB: ${ctx.currentDB} 
        user ID: ${ctx.userID}""")
      ctx
    } catch (ebufex: EBufException) {
      logger.warn("Connection to the server failed, error: $ebufex")
      throw ebufex
    }
  }

  private fun loadBrmConnectionProperties(sdkProperties: OLBrmProperties.ProjectBrmProperties): Properties {
    val props = Properties()
    // if path to the Infranet.propeties file is specified - loads all BRM connection properties from it, ignores OpenLegacy BRM connection properties.
    if (StringUtils.isNotBlank(sdkProperties.infranetPropertiesFilePath) && File(sdkProperties.infranetPropertiesFilePath).exists()) {
      FileInputStream(sdkProperties.infranetPropertiesFilePath).use {
        props.load(it)
        return props
      }
    }
    // otherwise, loads all BRM connection properties from OpenLegacy properties
    props["infranet.login.type"] = sdkProperties.login_type.toString()
    var connectionString = ""
    when (sdkProperties.login_type) {
      // For a type 1 login, the URL must include a user name and password. You must specify the service name and service (Portal Object ID) POID ("1")
      // The connection string is of the form: pcp://<username>:<password>@<hostname>:<port><service>:<service_poid>
      1 -> connectionString = "pcp://${sdkProperties.username}:${sdkProperties.password}@${sdkProperties.host}:${sdkProperties.port}/${sdkProperties.service.removePrefix("/")}:${sdkProperties.service_poid}"
      // A type 0 login requires a full POID, including the database number.
      // The connection string is of the form: pcp://<hostname>:<port>/<database_no>/<service>:<service_poid>
      0 -> connectionString = "pcp://${sdkProperties.host}:${sdkProperties.port}/${sdkProperties.database_no}/${sdkProperties.service.removePrefix("/")}:${sdkProperties.service_poid}"
    }
    props["infranet.connection"] = connectionString
    return props
  }
}
