package io.ol.provider.brm.serialize

import com.portal.pcm.ByteBuffer
import com.portal.pcm.FList
import com.portal.pcm.FileBuffer
import com.portal.pcm.Poid
import com.portal.pcm.SparseArray
import io.ol.core.rpc.serialize.DynamicFieldFunctionsRegistry
import io.ol.core.rpc.serialize.RpcDeserializeRequest
import io.ol.core.rpc.serialize.RpcDeserializeResult
import io.ol.core.rpc.serialize.RpcDeserializer
import io.ol.provider.brm.BrmLegacyTypes
import io.ol.provider.brm.utils.FListUtils
import io.ol.provider.brm.utils.PoidUtils
import io.vertx.core.json.JsonArray
import mu.KLogging
import org.apache.commons.io.IOUtils
import org.openlegacy.core.model.field.FieldDefinition
import org.openlegacy.core.model.field.RpcClassFieldDefinition
import org.openlegacy.core.model.field.RpcCollectionFieldDefinition
import org.openlegacy.core.model.field.RpcFieldDefinition
import org.openlegacy.core.model.field.RpcPrimitiveFieldDefinition
import org.openlegacy.utils.RpcFieldDefinitionUtil
import java.util.Enumeration

class BrmRpcDeserializer(
  override val dynamicFieldFunctionsRegistry: DynamicFieldFunctionsRegistry
) : RpcDeserializer<BrmOutputRpcData> {

  companion object : KLogging()

  override fun deserialize(
    request: RpcDeserializeRequest<BrmOutputRpcData>
  ): RpcDeserializeResult {
    val responseBody = request.body.body
    val updatedBody = request.body.copy(element = responseBody)
    logger.debug { "BRM deserialization start" }
    return super.deserialize(request.copy(body = updatedBody))
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
    val element = data.element
    val key = RpcFieldDefinitionUtil.getOriginalName(fieldDefinition)
    val flistField = FListUtils.getFlistField(key)
    val value = when (element) {
      is FList -> element.get(flistField)
      is Poid -> PoidUtils.getPoidProperty(element, key)
      else -> null
    }
    logger.debug { "Got new element: '$value' by key: '$key' from the parent element '$element'" }
    return data.copy(element = value)
  }

  override fun primitive(data: BrmOutputRpcData, fieldDefinition: RpcPrimitiveFieldDefinition, request: RpcDeserializeRequest<BrmOutputRpcData>): Any? {
    logger.debug { "primitive: '${fieldDefinition.name}', data element: '${data.element}'" }
    var value = data.element ?: RpcFieldDefinitionUtil.getDefaultValue(fieldDefinition)
    value = when (fieldDefinition.legacyType) {
      BrmLegacyTypes.BUF::class.java.canonicalName -> when (value) {
        is FileBuffer -> IOUtils.toByteArray(value.inputStream)
        is ByteBuffer -> value.bytes
        else -> value
      }
      else -> value
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
    val elementsArray = data.element as? SparseArray ?: return
    var count = collectionFieldDefinition.count
    if (count <= 0) {
      elementsArray.size().let { count = it }
    }
    val result = JsonArray()
    val valueEnum: Enumeration<*> = elementsArray.valueEnumerator
    var index = 0
    while (valueEnum.hasMoreElements()) {
      if (index >= count) {
        break
      }
      val flist = valueEnum.nextElement() as FList
      deserialize(
        fieldDefinition = elementFieldDefinition,
        data = data.copy(element = flist, elementNeedsUpdate = false),
        request = request.copy(parent = result, indexes = request.indexes + index),
        classDefinitionsMap = innerClasses
      )
      index++
    }
    if (!result.isEmpty) {
      addChild(request.parent, elementFieldDefinition.name, result)
    }
  }
}
