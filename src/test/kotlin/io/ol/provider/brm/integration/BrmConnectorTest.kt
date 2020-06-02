package io.ol.provider.brm.integration

import io.ol.provider.brm.OlPoid
import io.ol.provider.brm.entity.FListExampleEntity
import io.ol.provider.brm.mock.BrmTestApplication
import mu.KLogging
import openlegacy.test.utils.ConnectivityUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openlegacy.core.rpc.RpcEntity
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
  fun checkSingleArrayItem() {
    // GIVEN
    var rpcEntity = initFlistExampleEntity(1)
    // WHEN
    rpcEntity = rpcSession.doAction(RpcActions.execute(), rpcEntity)
    // THEN
    Assertions.assertNotNull(rpcEntity)
  }

  @Test
  fun checkMultipleArrayItems() {
    // GIVEN
    var rpcEntity = initFlistExampleEntity()
    // WHEN
    rpcEntity = rpcSession.doAction(RpcActions.execute(), rpcEntity)
    // THEN
    Assertions.assertNotNull(rpcEntity)
  }

  private fun initFlistExampleEntity(listCount: Int = 3): RpcEntity {
    var rpcEntity = FListExampleEntity()
    rpcEntity.pinFldPoid = OlPoid(1, -1, "/account")
    rpcEntity.pinFldIntVal = 1
    rpcEntity.pinFldProgramName = "program"
    rpcEntity.pinFldPayinfo = mutableListOf()
    for (i in 1..listCount) {
      val pinFldPayinfo: FListExampleEntity.PinFldPayinfo = FListExampleEntity.PinFldPayinfo()
      rpcEntity.pinFldPayinfo.add(pinFldPayinfo)

      pinFldPayinfo.pinFldPoid = OlPoid(1, -1, "/account")
      pinFldPayinfo.pinFldInheritedInfo = FListExampleEntity.PinFldInheritedInfo()
      pinFldPayinfo.pinFldInheritedInfo.pinFldCcInfo = mutableListOf()

      for (j in 1..listCount) {
        val pinFldCcInfo = FListExampleEntity.PinFldCcInfo()
        pinFldPayinfo.pinFldInheritedInfo.pinFldCcInfo.add(pinFldCcInfo)

        pinFldCcInfo.pinFldDebitExp = "exp $j"
        pinFldCcInfo.pinFldDebitNum = "num $j"
        pinFldCcInfo.pinFldName = "name $j"
      }
    }
    return rpcEntity
  }
}
