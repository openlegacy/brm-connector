package io.ol.provider.brm.integration

import io.ol.core.properties.RpcSdkProperties
import io.ol.provider.brm.entity.EntityPlaceholder
import io.ol.provider.brm.mock.BrmTestApplication
import io.ol.provider.brm.properties.OLBrmProperties
import mu.KLogging
import openlegacy.test.utils.ConnectivityUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openlegacy.core.rpc.RpcSession
import org.openlegacy.core.rpc.actions.RpcActions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [BrmTestApplication::class])
class BrmConnectorTest @Autowired constructor(
  val rpcSession: RpcSession
) {

  companion object : KLogging() {
    @BeforeAll
    @JvmStatic
    fun init() {
      ConnectivityUtils.checkTcpConnection("localhost", 11960, 5000)
    }
  }

  @Test
  fun postSearchForPerson() {
    // GIVEN
    var rpcEntity = EntityPlaceholder()
    // WHEN
    rpcEntity = rpcSession.doAction(RpcActions.execute(), rpcEntity)
    // THEN
  }
}
