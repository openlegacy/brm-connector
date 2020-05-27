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
  /**
   * Represents relative request URI, e.g. "/api/v2/pet". Used during serialization.
   * Needs to be modifiable as it is being processed on the field level of the entity hierarchy that is a dead-end from which we can't return updated data instance.
   */
  var relativeRequestUri: String = "",
  val preferredFieldName: String? = null,
  var rootNonStructuralField: String? = null,
  val projectProperties: OLBrmProperties.ProjectBrmProperties
) : RpcData<Buffer>
