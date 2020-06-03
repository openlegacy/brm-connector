package io.ol.provider.brm.serialize

import com.portal.pcm.FList
import io.ol.core.rpc.RpcConnection
import io.ol.core.rpc.operation.OperationConstants
import io.ol.core.rpc.operation.OperationDefinition
import io.ol.core.rpc.serialize.RpcSerializeRequest
import io.ol.provider.brm.BrmRpcConnectionFactory
import io.ol.provider.brm.entity.FListExampleEntity
import io.ol.provider.brm.mock.BrmTestApplication
import io.ol.provider.brm.util.TestUtils
import mu.KLogging
import org.apache.commons.io.IOUtils
import org.junit.Assert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openlegacy.core.exceptions.OpenLegacyRuntimeException
import org.openlegacy.core.rpc.RpcEntity
import org.openlegacy.core.rpc.actions.RpcAction
import org.openlegacy.core.rpc.actions.RpcActions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Paths

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [BrmTestApplication::class])
class BrmRpcSerializerTest @Autowired constructor(
  val connectionFactory: BrmRpcConnectionFactory
) {

  companion object : KLogging() {
    private val SAMPLES_DIR = BrmRpcDeserializerTest::class.java.classLoader.getResource("flist_samples")
    private var samplesDir: File = Paths.get(SAMPLES_DIR.toURI()).toFile()
    const val FLIST_EXAMPLE_SINGLE_ITEM = "single_item_example.flist"
    const val FLIST_EXAMPLE_MULTIPLE_ITEMS = "multiple_items_example.flist"
  }

  private fun readFileContent(fileName: String): String {
    return IOUtils.toString(File(samplesDir, fileName).toURI(), StandardCharsets.UTF_8)
  }

  /**
   * Logic is similar to the one in DefaultRpcSession.invoke in order to tests serialization of old entities
   */
  private fun createRequest(rpcEntity: RpcEntity, action: RpcAction): RpcSerializeRequest {
    val entityDefinition = rpcEntity.entityDefinition()
    val actionDefinition = entityDefinition.actionDefinitions.find { it.action == action }
      ?: error("Could not find action ${action::class.java.simpleName} in entity ${entityDefinition.name}")
    val actionAdditionalAttributes = actionDefinition.additionalAttributes.entries.associate {
      it.key to it.value.toString()
    }
    val operationDefinition = OperationDefinition(
      name = "${rpcEntity::class.java.simpleName}Operation",
      path = actionDefinition.programPath,
      properties = actionAdditionalAttributes,
      inputEntityDefinition = rpcEntity.inputEntityDefinition(),
      outputEntityDefinitionMap = mapOf(
        RpcConnection.BACKWARD_COMPATIBLE_STATUS_CODE to rpcEntity.outputEntityDefinition()
      )
    ).apply {
      // in case of using an old entity (without Operation), some providers must know which action has been used
      (properties as MutableMap)[OperationConstants.ACTION_NAME] = actionDefinition.action::class.java.simpleName
    }
    val requestBody = rpcEntity.toJsonObject()
    return RpcSerializeRequest(
      input = requestBody,
      operationDefinition = operationDefinition,
      rootObject = requestBody,
      indexes = emptyList()
    )
  }

  private fun serialize(rpcEntity: RpcEntity, rpcAction: RpcAction): FList {
    val rto = connectionFactory.getConnector().serialize(createRequest(rpcEntity, rpcAction))
    val inputBody = rto.body.body
    logger.debug("Serialize result: $inputBody")
    return inputBody
  }

  @Test
  fun serializeSingleArrayItem() {
    // GIVEN
    val rpcEntity = TestUtils.initFlistExampleEntity()
    // WHEN
    val inputBody = serialize(rpcEntity, RpcActions.execute())
    // THEN
    Assertions.assertEquals(readFileContent(FLIST_EXAMPLE_SINGLE_ITEM), inputBody.asString())
  }

  @Test
  fun serializeMultipleArrayItems() {
    // GIVEN
    val rpcEntity = TestUtils.initFlistExampleEntity(3)
    // WHEN
    val inputBody = serialize(rpcEntity, RpcActions.execute())
    // THEN
    Assertions.assertEquals(readFileContent(FLIST_EXAMPLE_MULTIPLE_ITEMS), inputBody.asString())
  }

  @Test
  fun serializeNonExistingActionError() {
    // GIVEN
    val rpcEntity = FListExampleEntity()
    // WHEN
    val exception = Assertions.assertThrows(IllegalStateException::class.java) {
      // setting an action which is not declared in the entity
      serialize(rpcEntity, RpcActions.delete())
    }
    Assert.assertEquals("Could not find action DELETE in entity FListExampleEntity", exception.message)
  }
}
