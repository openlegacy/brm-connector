package io.ol.provider.brm.serialize

import com.portal.pcm.FList
import io.ol.core.rpc.serialize.RpcDeserializeRequest
import io.ol.provider.brm.entity.FListExampleEntity
import io.ol.provider.brm.mock.BrmTestApplication
import mu.KLogging
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openlegacy.core.exceptions.OpenLegacyRuntimeException
import org.openlegacy.core.rpc.RpcEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Paths

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [BrmTestApplication::class])
class BrmRpcDeserializerTest @Autowired constructor(
  val deserializer: BrmRpcDeserializer
) {

  companion object : KLogging() {
    private val SAMPLES_DIR = BrmRpcDeserializerTest::class.java.classLoader.getResource("flist_samples")
    private var samplesDir: File = Paths.get(SAMPLES_DIR.toURI()).toFile()
    const val FLIST_EXAMPLE_SINGLE_ITEM = "single_item_example.flist"
    const val FLIST_EXAMPLE_MULTIPLE_ITEMS = "multiple_items_example.flist"
    const val FLIST_EMPTY = "empty.flist"
    const val FLIST_INVALID = "invalid.flist"
    const val FLIST_EXAMPLE_SINGLE_ITEM_EXPECTED = "single_item_example.expected.json"
    const val FLIST_EXAMPLE_MULTIPLE_ITEMS_EXPECTED = "multiple_items_example.expected.json"
  }

  private fun readFileContent(fileName: String): String {
    return IOUtils.toString(File(samplesDir, fileName).toURI(), StandardCharsets.UTF_8)
  }

  private fun createDeserializeRequest(rpcEntity: RpcEntity, responseFileName: String): RpcDeserializeRequest<BrmOutputRpcData> {
    return RpcDeserializeRequest(
      body = BrmOutputRpcData(
        body = FList.createFromString(readFileContent(responseFileName))
      ),
      classDefinition = rpcEntity.outputEntityDefinition()
    )
  }

  @Test
  fun deserializeSingleArrayItem() {
    // GIVEN
    val rpcEntity = FListExampleEntity()
    // WHEN
    val result = deserializer.deserialize(createDeserializeRequest(rpcEntity, FLIST_EXAMPLE_SINGLE_ITEM))
    rpcEntity.populateFromJson(result.body)
    // THEN
    Assertions.assertEquals(readFileContent(FLIST_EXAMPLE_SINGLE_ITEM_EXPECTED), rpcEntity.toJsonObject().encodePrettily())
  }

  @Test
  fun deserializeMultipleArrayItems() {
    // GIVEN
    val rpcEntity = FListExampleEntity()
    // WHEN
    val result = deserializer.deserialize(createDeserializeRequest(rpcEntity, FLIST_EXAMPLE_MULTIPLE_ITEMS))
    rpcEntity.populateFromJson(result.body)
    // THEN
    Assertions.assertEquals(readFileContent(FLIST_EXAMPLE_MULTIPLE_ITEMS_EXPECTED), rpcEntity.toJsonObject().encodePrettily())
  }

  @Test
  fun deserializeEmptyResponse() {
    // GIVEN
    val rpcEntity = FListExampleEntity()
    // WHEN
    val result = deserializer.deserialize(createDeserializeRequest(rpcEntity, FLIST_EMPTY))
    rpcEntity.populateFromJson(result.body)
    // THEN
    Assertions.assertTrue(rpcEntity.pinFldPayinfo.isEmpty())
    logger.debug(rpcEntity.toJsonObject().encodePrettily())
  }

  @Test
  fun deserializeInvalidResponse() {
    // GIVEN
    val rpcEntity = FListExampleEntity()
    // WHEN
    val expected: Exception = Assertions.assertThrows(java.lang.NumberFormatException::class.java) {
      deserializer.deserialize(createDeserializeRequest(rpcEntity, FLIST_INVALID))
    }
    // THEN
    Assertions.assertEquals("For input string: \"\"text\"", expected.message)
  }
}
