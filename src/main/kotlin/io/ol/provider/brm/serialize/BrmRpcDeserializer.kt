package io.ol.provider.brm.serialize

import io.ol.core.rpc.serialize.DynamicFieldFunctionsRegistry
import io.ol.core.rpc.serialize.RpcDeserializeRequest
import io.ol.core.rpc.serialize.RpcDeserializeResult
import io.ol.core.rpc.serialize.RpcDeserializer
import io.ol.core.util.DateTimeUtil
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import mu.KLogging
import org.openlegacy.core.model.field.FieldDefinition
import org.openlegacy.core.model.field.RpcClassFieldDefinition
import org.openlegacy.core.model.field.RpcCollectionFieldDefinition
import org.openlegacy.core.model.field.RpcDateFieldDefinition
import org.openlegacy.core.model.field.RpcFieldDefinition
import org.openlegacy.core.model.field.RpcFieldType
import org.openlegacy.core.model.field.RpcPrimitiveFieldDefinition
import org.openlegacy.core.model.legacy.type.HttpLegacyTypes
import org.openlegacy.utils.RpcFieldDefinitionUtil
import java.nio.charset.StandardCharsets

class BrmRpcDeserializer(
  override val dynamicFieldFunctionsRegistry: DynamicFieldFunctionsRegistry
) : RpcDeserializer<BrmOutputRpcData> {

  companion object : KLogging()

  override fun deserialize(
    request: RpcDeserializeRequest<BrmOutputRpcData>
  ): RpcDeserializeResult {
    val responseBody = request.body.body.toString(StandardCharsets.UTF_8)
    val rootJson = readDataRoot(responseBody)
    val updatedBody = request.body.copy(element = rootJson)
    logger.debug { "BRM deserialization start $rootJson" }
    return super.deserialize(request.copy(body = updatedBody))
  }

  private fun readDataRoot(responseBody: String): Any? {
    return null
  }

  /**
   * This method is always being executed when changing the hierarchy level of the entity during its traversing,
   * that is why it is a good place to call [updateDataElement].
   */
  override fun deserialize(fieldDefinition: RpcFieldDefinition, data: BrmOutputRpcData, request: RpcDeserializeRequest<BrmOutputRpcData>, classDefinitionsMap: MutableMap<String, RpcClassFieldDefinition>) {
    val updatedData = updateDataElement(data, fieldDefinition)
    return super.deserialize(fieldDefinition, updatedData, request, classDefinitionsMap)
  }

  /**
   * Updates data with a corresponding JSON element. Finds a corresponding JSON element based on the current fieldDefinition.
   */
  private fun updateDataElement(data: BrmOutputRpcData, fieldDefinition: FieldDefinition): BrmOutputRpcData {
    // if an element is marked as it doesn't need an update - it means that it is already updated to the current field hierarchy in another place
    if (!data.elementNeedsUpdate) {
      // resets this flag and returns the same element
      return data.copy(elementNeedsUpdate = true)
    }
    if (fieldDefinition.fieldType == RpcFieldType.NonStructural::class.java) {
      logger.debug { "Skipping updating Data element from NonStructural parent element, field name: '${fieldDefinition.name}'" }
      return data
    }
    val element = data.element
    val key = RpcFieldDefinitionUtil.getOriginalName(fieldDefinition)
    val value = when (element) {
      is JsonObject -> element.getValue(key)
      is JsonArray -> data.elementIndex?.let { element.getValue(it) }
      else -> null
    }
    logger.debug { "Got new element: '$value' by key: '$key' from the parent element '$element'" }
    return data.copy(element = value)
  }

  override fun primitive(data: BrmOutputRpcData, fieldDefinition: RpcPrimitiveFieldDefinition, request: RpcDeserializeRequest<BrmOutputRpcData>): Any? {
    logger.debug { "primitive: '${fieldDefinition.name}', data element: '${data.element}'" }
    var value: Any? = null
    when (fieldDefinition.legacyType) {
      HttpLegacyTypes.StatusCode::class.java,
      HttpLegacyTypes.StatusMessage::class.java -> {
        value = request.properties[fieldDefinition.legacyType.name]
      }
    }
    if (value == null) {
      value = data.element ?: RpcFieldDefinitionUtil.getDefaultValue(fieldDefinition)
    }
    if (fieldDefinition is RpcDateFieldDefinition) {
      value = DateTimeUtil.parseDate(
        value as String?,
        fieldDefinition.pattern,
        fieldDefinition.locale,
        fieldDefinition.timeZoneId)
    }
    return RpcFieldDefinitionUtil.toJsonData(fieldDefinition, value)
  }

  override fun fixedCollection(data: BrmOutputRpcData, collectionFieldDefinition: RpcCollectionFieldDefinition, request: RpcDeserializeRequest<BrmOutputRpcData>, innerClasses: MutableMap<String, RpcClassFieldDefinition>) {
    handleCollection(data, collectionFieldDefinition, request, innerClasses)
  }

  override fun dynamicCollection(data: BrmOutputRpcData, collectionFieldDefinition: RpcCollectionFieldDefinition, request: RpcDeserializeRequest<BrmOutputRpcData>, innerClasses: MutableMap<String, RpcClassFieldDefinition>) {
    handleCollection(data, collectionFieldDefinition, request, innerClasses)
  }

  private fun handleCollection(
    data: BrmOutputRpcData,
    collectionFieldDefinition: RpcCollectionFieldDefinition,
    request: RpcDeserializeRequest<BrmOutputRpcData>,
    innerClasses: MutableMap<String, RpcClassFieldDefinition>
  ) {
    val elementFieldDefinition: RpcFieldDefinition = collectionFieldDefinition.element
    val elementsArray = data.element as? JsonArray
    var count = collectionFieldDefinition.count
    if (count <= 0) {
      elementsArray?.size()?.let { count = it }
    }
    val result = JsonArray()
    (0 until count).forEach { index ->
      deserialize(
        fieldDefinition = elementFieldDefinition,
        data = data.copy(element = elementsArray?.getValue(index), elementNeedsUpdate = false),
        request = request.copy(parent = result, indexes = request.indexes + index),
        classDefinitionsMap = innerClasses
      )
    }
    if (!result.isEmpty) {
      addChild(request.parent, elementFieldDefinition.name, result)
    }
  }
}
