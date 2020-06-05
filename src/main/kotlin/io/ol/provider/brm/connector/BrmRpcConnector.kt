package io.ol.provider.brm.connector

import com.portal.pcm.EBufException
import com.portal.pcm.FList
import com.portal.pcm.PortalContext
import com.portal.pcm.PortalOp
import io.ol.core.rpc.connector.RpcConnector
import io.ol.core.rpc.connector.RpcSendResult
import io.ol.core.rpc.operation.OperationDefinition
import io.ol.core.rpc.serialize.RpcDeserializer
import io.ol.core.rpc.serialize.RpcSerializeRequest
import io.ol.core.rpc.serialize.RpcSerializer
import io.ol.core.tracing.TracingExecutor
import io.ol.impl.common.debug.InternalDebug
import io.ol.impl.common.debug.InternalDebugType
import io.ol.provider.brm.BrmConstants
import io.ol.provider.brm.properties.OLBrmProperties
import io.ol.provider.brm.serialize.BrmInputRpcData
import io.ol.provider.brm.serialize.BrmOutputRpcData
import io.vertx.core.Vertx
import mu.KLogging
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.math.NumberUtils
import org.openlegacy.utils.TimeUtils
import java.io.File
import java.io.FileInputStream
import java.nio.charset.StandardCharsets
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

  private val brmConnectionProperties = loadBrmConnectionProperties(sdkProperties)
  private val portalContext: PortalContext = PortalContext()

  override fun close() {
    closePortalContext()
  }

  override suspend fun send(rpcData: BrmInputRpcData, timeout: Long): RpcSendResult<BrmOutputRpcData> {
    return tracingExecutor.traceAwait("brm-rpc-inner-send") {
      val requestBody = rpcData.body
      val opCode = rpcData.operationDefinition.getOpCode()
      val opCodeFlag = rpcData.operationDefinition.getOpCodeFlag()
      if (internalDebug.type != InternalDebugType.NONE) {
        internalDebug.handleRequest(request = requestBody.asString().toByteArray(StandardCharsets.UTF_8), correlationId = System.currentTimeMillis().toString(), prefix = "brm")
      }
      // sets timeout
      val actualTimeout = TimeUtils.getTimeoutByPropagation(timeout, sdkProperties.timeout.toLong())
      brmConnectionProperties[BrmPropertiesConstants.PCM_TIMEOUT_IN_MSECS] = actualTimeout.toString()
      connectPortalContext()
      // Calls the opcode
      val outFlist: FList = sendFlist(opCode, requestBody, opCodeFlag)
      closePortalContext()
      if (internalDebug.type != InternalDebugType.NONE) {
        internalDebug.handleResponse(response = outFlist.asString().toByteArray(StandardCharsets.UTF_8), correlationId = System.currentTimeMillis().toString(), prefix = "brm")
      }
      return@traceAwait RpcSendResult(
        statusCode = 200,
        body = BrmOutputRpcData(body = outFlist)
      )
    }
  }

  override fun prepareRpcData(request: RpcSerializeRequest): BrmInputRpcData {
    return BrmInputRpcData(
      body = FList(),
      operationDefinition = request.operationDefinition,
      headers = request.headers,
      properties = request.properties,
      projectProperties = sdkProperties
    )
  }

  private fun connectPortalContext() {
    try {
      portalContext.connect(brmConnectionProperties)
      // prints out some info about the connection
      logger.debug("BRM connection successfully created: current DB: ${portalContext.currentDB}, user ID: ${portalContext.userID}")
    } catch (ebufex: EBufException) {
      logger.error("BRM connection to the server failed, error: $ebufex")
      throw ebufex
    }
  }

  private fun sendFlist(opcode: Int, flist: FList, opcodeFlag: Int = 0): FList {
    try {
      return portalContext.opcode(opcode, opcodeFlag, flist)
    } catch (ebufex: EBufException) {
      logger.error("Sending a request failed, error: $ebufex")
      throw ebufex
    }
  }

  private fun closePortalContext() {
    // closes the connection
    portalContext.close(true)
    logger.debug("BRM connection closed.")
  }

  private fun loadBrmConnectionProperties(sdkProperties: OLBrmProperties.ProjectBrmProperties): Properties {
    val props = Properties()
    // if path to the Infranet.propeties file is specified - loads all BRM connection properties from it, ignores OpenLegacy BRM connection properties.
    if (StringUtils.isNotBlank(sdkProperties.infranet_properties_file_path) && File(sdkProperties.infranet_properties_file_path).exists()) {
      FileInputStream(sdkProperties.infranet_properties_file_path).use {
        props.load(it)
        return props
      }
    }
    // otherwise, loads all BRM connection properties from OpenLegacy properties
    props[BrmPropertiesConstants.LOGIN_TYPE] = sdkProperties.login_type.toString()
    var connectionString = ""
    when (sdkProperties.login_type) {
      // For a type 1 login, the URL must include a user name and password. You must specify the service name and service (Portal Object ID) POID ("1")
      // The connection string is of the form: pcp://<username>:<password>@<hostname>:<port><service>:<service_poid>
      1 -> connectionString = "pcp://${sdkProperties.username}:${sdkProperties.password}@${sdkProperties.host}:${sdkProperties.port}/${sdkProperties.service.removePrefix("/")}:${sdkProperties.service_poid}"
      // A type 0 login requires a full POID, including the database number.
      // The connection string is of the form: pcp://<hostname>:<port>/<database_no>/<service>:<service_poid>
      0 -> connectionString = "pcp://${sdkProperties.host}:${sdkProperties.port}/${sdkProperties.database_no}/${sdkProperties.service.removePrefix("/")}:${sdkProperties.service_poid}"
    }
    props[BrmPropertiesConstants.CONNECTION] = connectionString
    return props
  }

  object BrmPropertiesConstants {
    /**
     * Infranet properties https://docs.oracle.com/cd/E16754_01/doc.75/e16702/prg_client_javapcm.htm#CHDDJIEC
     */
    const val LOGIN_TYPE = "infranet.login.type"
    const val CONNECTION = "infranet.connection"
    /**
     * Timeout in milliseconds to drop the connection if the server doesn't reponse in case of error - https://docs.oracle.com/html/E16719_01/adm_monitor.htm
     */
    const val PCM_TIMEOUT_IN_MSECS = "infranet.PcmTimeoutInMsecs"
  }

  /**
   * The path of the operation must contain opcode in the format <Opcode_constant_name>, e.g. PCM_OP_CUST_FIND or <Opcode_int_value> e.g. 51
   */
  private fun OperationDefinition.getOpCode(): Int {
    var opCodeString = this.path
    // opcode could be represented as a number, e.g. 80703
    if (NumberUtils.isParsable(opCodeString)) {
      return NumberUtils.toInt(opCodeString)
    }
    // opcode could be represented as a string constant, e.g. PCM_OP_CUST_FIND
    opCodeString = opCodeString.substringAfter("PCM_OP_")
    return PortalOp.stringToOp(opCodeString)
  }

  /**
   * The properties of the operation could contain addition opcode flag
   */
  private fun OperationDefinition.getOpCodeFlag(): Int {
    return this.properties.getOrDefault(BrmConstants.OPCODE_FLAG, "0").toInt()
  }
}
