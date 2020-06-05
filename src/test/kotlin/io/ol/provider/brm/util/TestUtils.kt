package io.ol.provider.brm.util

import io.ol.provider.brm.OlPoid
import io.ol.provider.brm.entity.FListExampleEntity
import org.openlegacy.core.rpc.RpcEntity
import java.nio.charset.Charset
import java.util.Date

object TestUtils {

  fun initFlistExampleEntity(listCount: Int = 1): RpcEntity {
    val rpcEntity = FListExampleEntity()
    rpcEntity.pinFldPoid = OlPoid(1, "/account", -1, 0)
    rpcEntity.pinFldIntVal = 1
    rpcEntity.pinFldProgramName = "program"
    rpcEntity.pinFldPayinfo = mutableListOf()
    for (i in 1..listCount) {
      val pinFldPayinfo: FListExampleEntity.PinFldPayinfo = FListExampleEntity.PinFldPayinfo()
      rpcEntity.pinFldPayinfo.add(pinFldPayinfo)

      pinFldPayinfo.pinFldPoid = OlPoid(1, "/account", -1, 0)
      pinFldPayinfo.pinFldInheritedInfo = FListExampleEntity.PinFldInheritedInfo()
      pinFldPayinfo.pinFldInheritedInfo.pinFldCcInfo = mutableListOf()

      for (j in 1..listCount) {
        val pinFldCcInfo = FListExampleEntity.PinFldCcInfo()
        pinFldPayinfo.pinFldInheritedInfo.pinFldCcInfo.add(pinFldCcInfo)
        // for this FList field type (PIN_FLD_DEBIT_EXP), any value will be hidden under "XXXX" when adding it to a FList
        pinFldCcInfo.pinFldDebitExp = "exp $j"
        pinFldCcInfo.pinFldAmount = "$j.$j".toBigDecimal()
        pinFldCcInfo.pinFldResidenceFlag = j
        // using non common encoding - 037 instead of UTF-8 to prove that byte array data will survive all transformations (RpcEntity -> JSON base64 -> FList -> Server -> FList -> JSON base64 -> RpcEntity)
        pinFldCcInfo.pinFldProviderIpaddr = "Hello".toByteArray(Charset.forName("037"))
        pinFldCcInfo.pinFldBuffer = byteArrayOf(0x1, 0x2, 0x3, 0x4, 0x5)
        // subtracts j days from the Thu, 04 Jun 20 00:00:00 date
        pinFldCcInfo.pinFldDueDateT = Date(1591228800000 - (j * 1000 * 60 * 60 * 24))
      }
    }
    return rpcEntity
  }
}
