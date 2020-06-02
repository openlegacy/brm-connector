package io.ol.provider.brm.serialize

import com.portal.pcm.FList
import io.ol.core.rpc.serialize.RpcData
import io.vertx.core.MultiMap

data class BrmOutputRpcData(
  override val body: FList,
  override val headers: MultiMap = MultiMap.caseInsensitiveMultiMap(),
  override val properties: Map<String, String> = mapOf(),
  /**
   * Currently processed element node of the response (represents part, field, etc). Used during deserialization.
   */
  val element: Any? = null,
  /**
   * Index of the currently processed element inside collection. Used during deserialization.
   */
  val elementIndex: Int? = null,
  /**
   * Indicates that the current element needs to be updated in accordance with the hierarchy of the currently processed entity field. Used during deserialization.
   * Could be **temporary** set to "false" if the current element has been already updated for the currently processed field.
   */
  val elementNeedsUpdate: Boolean = true
) : RpcData<FList>
