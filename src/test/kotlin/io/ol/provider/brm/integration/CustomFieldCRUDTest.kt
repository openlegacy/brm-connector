package io.ol.provider.brm.integration

import io.ol.provider.brm.entity.CustomFieldEntity
import io.ol.provider.brm.mock.BrmTestApplication
import mu.KLogging
import openlegacy.test.utils.ConnectivityUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openlegacy.core.rpc.RpcSession
import org.openlegacy.core.rpc.actions.RpcActions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * Test which Creates, Updates, Reads, Deletes the custom field in the BRM system.
 *
 * Based on https://tridenstechnology.com/custom-fields-in-oracle-brm/
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [BrmTestApplication::class])
class CustomFieldCRUDTest @Autowired constructor(
  val rpcSession: RpcSession
) {

  companion object : KLogging() {
    const val CUSTOM_FIELD_DESCRIPTON = "CUSTOM FIELD DESCRIPTON"
    const val CUSTOM_FIELD_DESCRIPTON_UPDATED = "CUSTOM FIELD DESCRIPTON UPDATED"
    const val CUSTOM_FIELD_NAME = "C_FLD_OL_INTEGRATION_TEST"
    const val CUSTOM_FIELD_NUMBER = 11111
    const val CUSTOM_FIELD_TYPE = 5

    @BeforeAll
    @JvmStatic
    fun init() {
      ConnectivityUtils.checkTcpConnection("localhost", 11960, 5000)
    }
  }

  /**
   * Test which Creates, Updates, Reads, Deletes the custom field in the BRM system.
   *
   * Based on this article - https://tridenstechnology.com/custom-fields-in-oracle-brm/
   */
  @Test
  fun customFieldCRUDTest() {
    var rpcEntity = createCustomFieldEntity()
    // ensures that BRM system doesn't have this field before starting integration test, if it doesn't exist - an exception will be thrown
    try {
      logger.debug { "Clear before test" }
      rpcSession.doAction(RpcActions.delete(), rpcEntity)
    } catch (ignore: Exception) {
    }
    logger.debug { "Create" }
    rpcEntity = createCustomFieldEntity()
    rpcEntity = rpcSession.doAction(RpcActions.create(), rpcEntity)
    Assertions.assertTrue(rpcEntity.pinFldField.isEmpty())

    logger.debug { "Read" }
    rpcEntity = createCustomFieldEntity()
    rpcEntity = rpcSession.doAction(RpcActions.read(), rpcEntity)
    Assertions.assertEquals(CUSTOM_FIELD_DESCRIPTON, rpcEntity.pinFldField[0].pinFldDescr)
    Assertions.assertEquals(CUSTOM_FIELD_NAME, rpcEntity.pinFldField[0].pinFldFieldName)
    Assertions.assertEquals(CUSTOM_FIELD_NUMBER, rpcEntity.pinFldField[0].pinFldFieldNum)
    Assertions.assertEquals(CUSTOM_FIELD_TYPE, rpcEntity.pinFldField[0].pinFldFieldType)

    logger.debug { "Update" }
    rpcEntity = createCustomFieldEntity(CUSTOM_FIELD_DESCRIPTON_UPDATED)
    rpcEntity = rpcSession.doAction(RpcActions.update(), rpcEntity)
    Assertions.assertTrue(rpcEntity.pinFldField.isEmpty())

    logger.debug { "Read after update" }
    rpcEntity = createCustomFieldEntity(CUSTOM_FIELD_DESCRIPTON_UPDATED)
    rpcEntity = rpcSession.doAction(RpcActions.read(), rpcEntity)
    Assertions.assertEquals(CUSTOM_FIELD_DESCRIPTON_UPDATED, rpcEntity.pinFldField[0].pinFldDescr)

    logger.debug { "Delete" }
    rpcEntity = createCustomFieldEntity(CUSTOM_FIELD_DESCRIPTON_UPDATED)
    rpcEntity = rpcSession.doAction(RpcActions.delete(), rpcEntity)
    Assertions.assertTrue(rpcEntity.pinFldField.isEmpty())
  }

  private fun createCustomFieldEntity(fieldDescription: String = CUSTOM_FIELD_DESCRIPTON): CustomFieldEntity {
    val inputEntity = CustomFieldEntity()
    val field = CustomFieldEntity.PinFldField()
    inputEntity.pinFldField = listOf(field)

    field.pinFldDescr = fieldDescription
    field.pinFldFieldName = CUSTOM_FIELD_NAME
    field.pinFldFieldNum = CUSTOM_FIELD_NUMBER
    field.pinFldFieldType = CUSTOM_FIELD_TYPE
    return inputEntity
  }
}
