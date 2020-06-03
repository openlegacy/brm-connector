package io.ol.provider.brm.integration

import io.ol.provider.brm.OlPoid
import io.ol.provider.brm.entity.FListExampleEntity
import io.ol.provider.brm.mock.BrmTestApplication
import mu.KLogging
import openlegacy.test.utils.ConnectivityUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openlegacy.core.rpc.RpcEntity
import org.openlegacy.core.rpc.RpcSession
import org.openlegacy.core.rpc.actions.RpcActions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.nio.charset.Charset
import java.util.Date

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
    val inputEntity = initFlistExampleEntity(1)
    // WHEN
    val outputEntity = rpcSession.doAction(RpcActions.execute(), inputEntity)
    // THEN
  }

  @Test
  fun checkMultipleArrayItems() {
    // GIVEN
    val inputEntity = initFlistExampleEntity()
    // WHEN
    val outputEntity = rpcSession.doAction(RpcActions.execute(), inputEntity)
    // THEN
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
        pinFldCcInfo.pinFldAmount = "$j.$j".toBigDecimal()
        pinFldCcInfo.pinFldResidenceFlag = j
        // using non common encoding - 037 instead of UTF-8 to prove that byte array data will survive all transformations (RpcEntity -> JSON base64 -> FList -> Server -> FList -> JSON base64 -> RpcEntity)
        pinFldCcInfo.pinFldProviderIpaddr = "Hello".toByteArray(Charset.forName("037"))
        pinFldCcInfo.pinFldBuffer = byteArrayOf(0x1, 0x2, 0x3, 0x4, 0x5)
        // subtracts j days from the current date
        pinFldCcInfo.pinFldDueDateT = Date(System.currentTimeMillis() - (j * 1000 * 60 * 60 * 24))
      }
    }
    return rpcEntity
  }
}
