package io.ol.provider.brm.serialize

import com.portal.pcm.FList
import com.portal.pcm.SparseArray
import io.ol.core.rpc.serialize.DynamicFieldFunctionsRegistry
import io.ol.core.rpc.serialize.RpcSerializeRequest
import io.ol.core.rpc.serialize.RpcSerializeResult
import io.ol.core.rpc.serialize.RpcSerializer
import io.ol.provider.brm.BrmLegacyTypes
import io.ol.provider.brm.utils.FListUtils
import io.ol.provider.brm.utils.PoidUtils
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import mu.KLogging
import org.openlegacy.core.model.field.RpcClassFieldDefinition
import org.openlegacy.core.model.field.RpcCollectionFieldDefinition
import org.openlegacy.core.model.field.RpcFieldDefinition
import org.openlegacy.core.model.field.RpcPrimitiveFieldDefinition
import org.openlegacy.utils.RpcFieldDefinitionUtil

class BrmRpcSerializer(
  override val dynamicFieldFunctionsRegistry: DynamicFieldFunctionsRegistry
) : RpcSerializer<BrmInputRpcData> {

  companion object : KLogging()

  override fun serialize(
    rpcData: BrmInputRpcData,
    request: RpcSerializeRequest
  ): RpcSerializeResult<BrmInputRpcData> {
    // sets the root FList Object. From now on all modifications will be performed inside of it
    val rootFList = FList()
    val result = super.serialize(rpcData.copy(parentElement = rootFList, rootElement = rootFList), request)
    val resultData = result.body

    return RpcSerializeResult(
      properties = request.properties,
      body = BrmInputRpcData(
        body = rootFList,
        operationDefinition = request.operationDefinition,
        properties = resultData.properties,
        projectProperties = resultData.projectProperties
      )
    )
  }

  private fun updateDataElement(rpcData: BrmInputRpcData, field: RpcFieldDefinition, value: Any?): BrmInputRpcData {
    val parent = rpcData.parentElement
    // uses original name of the field if possible
    val key = RpcFieldDefinitionUtil.getOriginalName(field)
    val flistField = FListUtils.getFlistField(key)
    when (parent) {
      // for FList instance - sets only non-null values
      is FList -> value?.let { parent.set(flistField, it) }
      is SparseArray -> parent.add(value as FList?)
    }
    logger.debug { "Added to parent object key: '$key' with value: '$value', parent object '$parent'" }
    // sets the new value as the parent which allows going down the hierarchy. If the new value is a primitive (i.e. not a list or object) - it is OK as there is no more hierarchy below it
    return rpcData.copy(parentElement = value)
  }

  override fun primitive(rpcData: BrmInputRpcData, input: Any?, fieldDefinition: RpcPrimitiveFieldDefinition, request: RpcSerializeRequest) {
    logger.debug { "primitive: '${fieldDefinition.name}', input: '$input'" }
    var value = input ?: RpcFieldDefinitionUtil.getDefaultValue(fieldDefinition)
    updateDataElement(rpcData, fieldDefinition, value)
  }

  override fun part(rpcData: BrmInputRpcData, input: JsonObject?, classFieldDefinition: RpcClassFieldDefinition, request: RpcSerializeRequest, classDefinitionsMap: MutableMap<String, RpcClassFieldDefinition>) {
    // POID is a non-primitive structure which represents Id, that is why it ended up being processed here
    if (classFieldDefinition.legacyType == BrmLegacyTypes.POID::class.java) {
      updateDataElement(rpcData, classFieldDefinition, PoidUtils.initPoidFromJson(input))
      // exits the method as inner fields of the POID structure has been already processed and there is no need to go down the hierarchy
      return
    }
    val updatedRpcData = updateDataElement(rpcData, classFieldDefinition, FList())
    super.part(updatedRpcData, input, classFieldDefinition, request, classDefinitionsMap)
  }

  override fun fixedCollection(rpcData: BrmInputRpcData, input: JsonArray?, collectionFieldDefinition: RpcCollectionFieldDefinition, request: RpcSerializeRequest, classDefinitionsMap: MutableMap<String, RpcClassFieldDefinition>) {
    val updatedRpcData = updateDataElement(rpcData, collectionFieldDefinition, SparseArray())
    super.fixedCollection(updatedRpcData, input, collectionFieldDefinition, request, classDefinitionsMap)
  }

  override fun dynamicCollection(rpcData: BrmInputRpcData, input: JsonArray?, collectionFieldDefinition: RpcCollectionFieldDefinition, request: RpcSerializeRequest, classDefinitionsMap: MutableMap<String, RpcClassFieldDefinition>) {
    val updatedRpcData = updateDataElement(rpcData, collectionFieldDefinition, SparseArray())
    super.dynamicCollection(updatedRpcData, input, collectionFieldDefinition, request, classDefinitionsMap)
  }
}
