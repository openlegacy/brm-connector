package io.ol.provider.brm.serialize

import io.ol.core.rpc.operation.OperationDefinition
import io.ol.core.rpc.serialize.RpcData
import io.ol.provider.brm.properties.OLBrmProperties
import io.vertx.core.MultiMap
import io.vertx.core.buffer.Buffer

data class BrmInputRpcData(
  override val body: Buffer,
  override val headers: MultiMap = MultiMap.caseInsensitiveMultiMap(),
  override val properties: Map<String, String> = mapOf(),
  val operationDefinition: OperationDefinition,
  /**
   * Always holds the root element. Used during serialization.
   */
  val rootElement: Any? = null,
  /**
   * Parent of the currently processed element (part, field, etc) to which added new inner fields in accordance with entity structure. Used during serialization.
   */
  val parentElement: Any? = null,
  val convertedFieldName: String? = null,
  val projectProperties: OLBrmProperties.ProjectBrmProperties
) : RpcData<Buffer>
