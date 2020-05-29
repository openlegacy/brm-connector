package io.ol.provider.brm.serialize

import io.ol.core.rpc.serialize.DynamicFieldFunctionsRegistry
import io.ol.core.rpc.serialize.RpcSerializeRequest
import io.ol.core.rpc.serialize.RpcSerializeResult
import io.ol.core.rpc.serialize.RpcSerializer
import io.ol.provider.brm.BrmConstants
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import mu.KLogging
import org.openlegacy.core.model.field.RpcClassFieldDefinition
import org.openlegacy.core.model.field.RpcCollectionFieldDefinition
import org.openlegacy.core.model.field.RpcFieldDefinition
import org.openlegacy.core.model.field.RpcFieldType
import org.openlegacy.core.model.field.RpcPrimitiveFieldDefinition
import org.openlegacy.core.model.legacy.type.HttpLegacyTypes
import org.openlegacy.core.model.legacy.type.NoneLegacyType
import org.openlegacy.utils.RpcFieldDefinitionUtil
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class BrmRpcSerializer(
  override val dynamicFieldFunctionsRegistry: DynamicFieldFunctionsRegistry
) : RpcSerializer<BrmInputRpcData> {

  companion object : KLogging() {
    // only fields which has the following legacyType values will be added to the result request body
    private val DEFAULT_BODY_TYPES = listOf(
      NoneLegacyType::class.java,
      HttpLegacyTypes.Body::class.java
    )
  }

  override fun serialize(
    rpcData: BrmInputRpcData,
    request: RpcSerializeRequest
  ): RpcSerializeResult<BrmInputRpcData> {
    logger.debug { "BRM serialization start ${rpcData.body}" }
    // sets the root JSON Object. From now on all modifications will be performed inside of it
    val rootJson = JsonObject()
    val result = super.serialize(rpcData.copy(parentElement = rootJson, rootElement = rootJson), request)
    val resultData = result.body
    // if a field with NonStructural field type has been found in the root of the entity - extracts it and makes it a new root
    val resultJson = resultData.rootNonStructuralField?.let { rootJson.getValue(it) } ?: rootJson
    val headers = resultData.headers
    // adds headers from entity action properties
    request.operationDefinition.properties
      .entries
      .stream()
      .filter { !BrmConstants.isConstant(it.key) }
      .forEach { headers[it.key] = it.value }

    return RpcSerializeResult(
      properties = request.properties,
      body = BrmInputRpcData(
        body = Buffer.buffer("""
0 PIN_FLD_LAST_NAME                 STR [0] "Mouse"
0 PIN_FLD_FIRST_NAME                STR [0] "Mickey"
0 PIN_FLD_POID                      POID [0] 0.0.0.1 -1 0
      """.trimIndent()), // Buffer.buffer(resultJson.toString()),
        operationDefinition = request.operationDefinition,
        headers = headers,
        relativeRequestUri = resultData.relativeRequestUri,
        properties = resultData.properties,
        projectProperties = resultData.projectProperties
      )
    )
  }

  private fun updateDataElement(rpcData: BrmInputRpcData, field: RpcFieldDefinition, value: Any?): BrmInputRpcData {
    val parent = rpcData.parentElement
    // uses original name of the field if possible
    val key = RpcFieldDefinitionUtil.getOriginalName(field)
    // root non structural field will be extracted from hierarchy after serialization complete, now just records its name
    if (field.fieldType == RpcFieldType.NonStructural::class.java) {
      if (rpcData.rootNonStructuralField == null && rpcData.rootElement == rpcData.parentElement) {
        rpcData.rootNonStructuralField = key
      }
    }
    when (parent) {
      is JsonObject -> parent.put(key, value)
      is JsonArray -> parent.add(value)
    }
    logger.debug { "Added to parent object key: '$key' with value: '$value', parent object '$parent'" }
    // sets the new value as the parent which allows going down the hierarchy. If the new value is a primitive (i.e. not a list or object) - it is OK as there is no more hierarchy below it
    return rpcData.copy(parentElement = value)
  }

  override fun primitive(rpcData: BrmInputRpcData, input: Any?, fieldDefinition: RpcPrimitiveFieldDefinition, request: RpcSerializeRequest) {
    logger.debug { "primitive: '${fieldDefinition.name}', input: '$input'" }
    var value = input ?: RpcFieldDefinitionUtil.getDefaultValue(fieldDefinition)
    value = RpcFieldDefinitionUtil.toJsonData(fieldDefinition, value)
    when (fieldDefinition.legacyType) {
      in DEFAULT_BODY_TYPES -> updateDataElement(rpcData, fieldDefinition, value)
    }
    updateRequestUri(rpcData, value, fieldDefinition)
  }

  override fun part(rpcData: BrmInputRpcData, input: JsonObject?, classFieldDefinition: RpcClassFieldDefinition, request: RpcSerializeRequest, classDefinitionsMap: MutableMap<String, RpcClassFieldDefinition>) {
    val updatedRpcData = updateDataElement(rpcData, classFieldDefinition, JsonObject())
    super.part(updatedRpcData, input, classFieldDefinition, request, classDefinitionsMap)
  }

  override fun fixedCollection(rpcData: BrmInputRpcData, input: JsonArray?, collectionFieldDefinition: RpcCollectionFieldDefinition, request: RpcSerializeRequest, classDefinitionsMap: MutableMap<String, RpcClassFieldDefinition>) {
    val updatedRpcData = updateDataElement(rpcData, collectionFieldDefinition, JsonArray())
    super.fixedCollection(updatedRpcData, input, collectionFieldDefinition, request, classDefinitionsMap)
  }

  override fun dynamicCollection(rpcData: BrmInputRpcData, input: JsonArray?, collectionFieldDefinition: RpcCollectionFieldDefinition, request: RpcSerializeRequest, classDefinitionsMap: MutableMap<String, RpcClassFieldDefinition>) {
    val updatedRpcData = updateDataElement(rpcData, collectionFieldDefinition, JsonArray())
    super.dynamicCollection(updatedRpcData, input, collectionFieldDefinition, request, classDefinitionsMap)
  }

  private fun updateRequestUri(rpcData: BrmInputRpcData, data: Any?, fieldDefinition: RpcPrimitiveFieldDefinition) {
    data?.let {
      val path = rpcData.relativeRequestUri
      rpcData.relativeRequestUri = when (fieldDefinition.legacyType) {
        HttpLegacyTypes.Header::class.java -> {
          rpcData.headers.add(fieldDefinition.originalName, it.toString())
          path
        }
        HttpLegacyTypes.Query::class.java -> "%s%s%s=%s".format(path, if (path.contains("?")) "&" else "?", fieldDefinition.originalName, URLEncoder.encode(it.toString(), StandardCharsets.UTF_8))
        HttpLegacyTypes.Path::class.java -> path.replace("{${fieldDefinition.originalName}}", it.toString())
        else -> path
      }
    }
  }
}
