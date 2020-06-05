package io.ol.provider.brm.entity;

import io.ol.provider.brm.BrmConstants;
import io.ol.provider.brm.BrmLegacyTypes;
import io.ol.provider.brm.OlPoid;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.openlegacy.core.annotations.rpc.Action;
import org.openlegacy.core.annotations.rpc.ActionProperty;
import org.openlegacy.core.annotations.rpc.RpcActions;
import org.openlegacy.core.annotations.rpc.RpcEntity;
import org.openlegacy.core.annotations.rpc.RpcField;
import org.openlegacy.core.annotations.rpc.RpcList;
import org.openlegacy.core.annotations.rpc.RpcNumericField;
import org.openlegacy.core.annotations.rpc.RpcPart;
import org.openlegacy.core.model.entity.RpcEntityDefinition;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * Flist with a substructs, arrays and all possible field types:
 * <p>
 * 0 PIN_FLD_POID                      POID [0] 0.0.0.1 /account -1 0
 * 0 PIN_FLD_INT_VAL                    INT [0] 1
 * 0 PIN_FLD_PROGRAM_NAME               STR [0] "program"
 * 0 PIN_FLD_PAYINFO                  ARRAY [0] allocated 2, used 2
 * 1     PIN_FLD_POID                  POID [0] 0.0.0.1 /account -1 0
 * 1     PIN_FLD_INHERITED_INFO   SUBSTRUCT [0] allocated 1, used 1
 * 2         PIN_FLD_CC_INFO          ARRAY [0] allocated 6, used 6
 * 3             PIN_FLD_PROVIDER_IPADDR    BINSTR [0] 5 C885939396
 * 3             PIN_FLD_BUFFER         BUF [0] flag/size/offset 0x00 5 0 data:
 * x000000  0102030405                         .....
 * 3             PIN_FLD_RESIDENCE_FLAG    ENUM [0] 1
 * 3             PIN_FLD_AMOUNT     DECIMAL [0] 1.1
 * 3             PIN_FLD_DEBIT_EXP      STR [0] "XXXX"
 * 3             PIN_FLD_DUE_DATE_T  TSTAMP [0] (1591142400) 03/06/2020 03:00:00:000 AM
 */
@RpcEntity
@RpcActions(actions = {
        @Action(action = org.openlegacy.core.rpc.actions.RpcActions.EXECUTE.class, path = "PCM_OP_TEST_LOOPBACK",
                actionProperties = { @ActionProperty(name = BrmConstants.OPCODE_FLAG, value = "0") })
})
public class FListExampleEntity implements org.openlegacy.core.rpc.RpcEntity {

    @RpcField(originalName = "PIN_FLD_POID", legacyType = BrmLegacyTypes.POID.class)
    private OlPoid pinFldPoid = new OlPoid(1, "/account",-1, 0);

    @RpcNumericField
    @RpcField(originalName = "PIN_FLD_INT_VAL", legacyType = BrmLegacyTypes.INT.class)
    private Integer pinFldIntVal;

    @RpcField(originalName = "PIN_FLD_PROGRAM_NAME", legacyType = BrmLegacyTypes.STR.class)
    private String pinFldProgramName;

    @RpcField(originalName = "PIN_FLD_PAYINFO", legacyType = BrmLegacyTypes.ARRAY.class)
    @RpcList
    private List<PinFldPayinfo> pinFldPayinfo;

    @RpcPart(name = "PinFldPayinfo")
    public static class PinFldPayinfo {

        @RpcField(originalName = "PIN_FLD_POID", legacyType = BrmLegacyTypes.POID.class)
        private OlPoid pinFldPoid;

        @RpcField(originalName = "PIN_FLD_INHERITED_INFO", legacyType = BrmLegacyTypes.SUBSTRUCT.class)
        private PinFldInheritedInfo pinFldInheritedInfo;

        public PinFldPayinfo() {
        }

        public OlPoid getPinFldPoid() {
            return pinFldPoid;
        }

        public void setPinFldPoid(OlPoid pinFldPoid) {
            this.pinFldPoid = pinFldPoid;
        }

        public PinFldInheritedInfo getPinFldInheritedInfo() {
            return pinFldInheritedInfo;
        }

        public void setPinFldInheritedInfo(PinFldInheritedInfo pinFldInheritedInfo) {
            this.pinFldInheritedInfo = pinFldInheritedInfo;
        }
    }

    @RpcPart(name = "PinFldInheritedInfo")
    public static class PinFldInheritedInfo {

        @RpcField(originalName = "PIN_FLD_CC_INFO", legacyType = BrmLegacyTypes.ARRAY.class)
        @RpcList
        private List<PinFldCcInfo> pinFldCcInfo;

        public PinFldInheritedInfo() {
        }

        public List<PinFldCcInfo> getPinFldCcInfo() {
            return pinFldCcInfo;
        }

        public void setPinFldCcInfo(List<PinFldCcInfo> pinFldCcInfo) {
            this.pinFldCcInfo = pinFldCcInfo;
        }
    }

    @RpcPart(name = "PinFldCcInfo")
    public static class PinFldCcInfo {

        @RpcField(originalName = "PIN_FLD_DEBIT_EXP", legacyType = BrmLegacyTypes.STR.class)
        private String pinFldDebitExp;

        @RpcField(originalName = "PIN_FLD_RESIDENCE_FLAG", legacyType = BrmLegacyTypes.ENUM.class)
        private Integer pinFldResidenceFlag;

        @RpcField(originalName = "PIN_FLD_AMOUNT", legacyType = BrmLegacyTypes.DECIMAL.class)
        private BigDecimal pinFldAmount;

        @RpcField(originalName = "PIN_FLD_DUE_DATE_T", legacyType = BrmLegacyTypes.TSTAMP.class)
        private Date pinFldDueDateT;

        @RpcField(originalName = "PIN_FLD_PROVIDER_IPADDR", legacyType = BrmLegacyTypes.BINSTR.class)
        private byte[] pinFldProviderIpaddr;

        @RpcField(originalName = "PIN_FLD_BUFFER", legacyType = BrmLegacyTypes.BUF.class)
        private byte[] pinFldBuffer;

        public PinFldCcInfo() {
        }

        public String getPinFldDebitExp() {
            return pinFldDebitExp;
        }

        public void setPinFldDebitExp(String pinFldDebitExp) {
            this.pinFldDebitExp = pinFldDebitExp;
        }

        public Integer getPinFldResidenceFlag() {
            return pinFldResidenceFlag;
        }

        public void setPinFldResidenceFlag(Integer pinFldResidenceFlag) {
            this.pinFldResidenceFlag = pinFldResidenceFlag;
        }

        public BigDecimal getPinFldAmount() {
            return pinFldAmount;
        }

        public void setPinFldAmount(BigDecimal pinFldAmount) {
            this.pinFldAmount = pinFldAmount;
        }

        public Date getPinFldDueDateT() {
            return pinFldDueDateT;
        }

        public void setPinFldDueDateT(Date pinFldDueDateT) {
            this.pinFldDueDateT = pinFldDueDateT;
        }

        public byte[] getPinFldProviderIpaddr() {
            return pinFldProviderIpaddr;
        }

        public void setPinFldProviderIpaddr(byte[] pinFldProviderIpaddr) {
            this.pinFldProviderIpaddr = pinFldProviderIpaddr;
        }

        public byte[] getPinFldBuffer() {
            return pinFldBuffer;
        }

        public void setPinFldBuffer(byte[] pinFldBuffer) {
            this.pinFldBuffer = pinFldBuffer;
        }
    }

    public FListExampleEntity() {
    }

    public OlPoid getPinFldPoid() {
        return pinFldPoid;
    }

    public void setPinFldPoid(OlPoid pinFldPoid) {
        this.pinFldPoid = pinFldPoid;
    }

    public Integer getPinFldIntVal() {
        return pinFldIntVal;
    }

    public void setPinFldIntVal(Integer pinFldIntVal) {
        this.pinFldIntVal = pinFldIntVal;
    }

    public String getPinFldProgramName() {
        return pinFldProgramName;
    }

    public void setPinFldProgramName(String pinFldProgramName) {
        this.pinFldProgramName = pinFldProgramName;
    }

    public List<PinFldPayinfo> getPinFldPayinfo() {
        return pinFldPayinfo;
    }

    public void setPinFldPayinfo(List<PinFldPayinfo> pinFldPayinfo) {
        this.pinFldPayinfo = pinFldPayinfo;
    }

    @NotNull
    @Override
    public RpcEntityDefinition entityDefinition() {
        return FListExampleEntityEntityHelper.entityDefinition;
    }

    @NotNull
    @Override
    public RpcEntityDefinition inputEntityDefinition() {
        return FListExampleEntityEntityHelper.inputEntityDefinition;
    }

    @NotNull
    @Override
    public RpcEntityDefinition outputEntityDefinition() {
        return FListExampleEntityEntityHelper.outputEntityDefinition;
    }

    @NotNull
    @Override
    public JsonObject toJsonObject() {
        return FListExampleEntityEntityHelper.toJson(this);
    }

    @Override
    public void populateFromJson(@NotNull JsonObject jsonObject) {
        FListExampleEntityEntityHelper.populateFromJson(jsonObject, this);
    }

}
