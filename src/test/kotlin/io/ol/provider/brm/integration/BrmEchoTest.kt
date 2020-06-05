package io.ol.provider.brm.integration

import io.ol.provider.brm.mock.BrmTestApplication
import io.ol.provider.brm.util.TestUtils
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
 * Echo test uses PCM_OP_TEST_LOOPBACK Opcode to return the request as the response from the BRM system
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [BrmTestApplication::class])
class BrmEchoTest @Autowired constructor(
  val rpcSession: RpcSession
) {

  companion object : KLogging() {
    @BeforeAll
    @JvmStatic
    fun init() {
      ConnectivityUtils.checkTcpConnection("localhost", 11960, 5000)
    }
  }

  /**
   * Builds an entity in which list fields have a single list item.
   * Sends an echo request to the server which returns response identical to request.
   */
  @Test
  fun checkSingleArrayItemEchoTest() {
    val inputEntity = TestUtils.initFlistExampleEntity()
    val outputEntity = rpcSession.doAction(RpcActions.execute(), inputEntity)
    Assertions.assertEquals(inputEntity.toJsonObject(), outputEntity.toJsonObject())
  }

  /**
   * Builds an entity in which list fields have multiple list items.
   * Sends an echo request to the server which returns response identical to request.
   */
  @Test
  fun checkMultipleArrayItemsEchoTest() {
    val inputEntity = TestUtils.initFlistExampleEntity(3)
    val outputEntity = rpcSession.doAction(RpcActions.execute(), inputEntity)
    Assertions.assertEquals(inputEntity.toJsonObject(), outputEntity.toJsonObject())
  }
}
