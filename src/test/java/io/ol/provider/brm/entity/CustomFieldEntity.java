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

import java.util.List;


/**
 * Flist for creating/updating/deleting custom field specfification in the BRM system:
 * <p>
 * 0 PIN_FLD_POID          POID [0] 0.0.0.1 /dd/fields 0 0
 * 0 PIN_FLD_FIELD      ARRAY [0]
 * 1    PIN_FLD_DESCR          STR [0] "custom field for holding a VAT number"
 * 1    PIN_FLD_FIELD_NAME      STR [0] "C_FLD_VAT_NUMBER"
 * 1    PIN_FLD_FIELD_NUM      ENUM [0] 10000
 * 1    PIN_FLD_FIELD_TYPE      INT [0] 5
 */
@RpcEntity
@RpcActions(actions = {
        @Action(action = org.openlegacy.core.rpc.actions.RpcActions.CREATE.class, path = "PCM_OP_SDK_SET_FLD_SPECS", actionProperties = { @ActionProperty(name = BrmConstants.OPCODE_FLAG, value = "0") }),
        @Action(action = org.openlegacy.core.rpc.actions.RpcActions.UPDATE.class, path = "PCM_OP_SDK_SET_FLD_SPECS", actionProperties = { @ActionProperty(name = BrmConstants.OPCODE_FLAG, value = "0") }),
        @Action(action = org.openlegacy.core.rpc.actions.RpcActions.READ.class, path = "PCM_OP_SDK_GET_FLD_SPECS", actionProperties = { @ActionProperty(name = BrmConstants.OPCODE_FLAG, value = "0") }),
        @Action(action = org.openlegacy.core.rpc.actions.RpcActions.DELETE.class, path = "PCM_OP_SDK_DEL_FLD_SPECS", actionProperties = { @ActionProperty(name = BrmConstants.OPCODE_FLAG, value = "0") }),
})
public class CustomFieldEntity implements org.openlegacy.core.rpc.RpcEntity {

    @RpcField(originalName = "PIN_FLD_POID", legacyType = BrmLegacyTypes.POID.class)
    private OlPoid pinFldPoid = new OlPoid(1, "/dd/fields", 0, 0);

    @RpcField(originalName = "PIN_FLD_FIELD", legacyType = BrmLegacyTypes.ARRAY.class)
    @RpcList
    private List<PinFldField> pinFldField;

    @RpcPart(name = "PinFldField")
    public static class PinFldField {

        @RpcField(originalName = "PIN_FLD_DESCR", legacyType = BrmLegacyTypes.STR.class)
        private String pinFldDescr;

        @RpcField(originalName = "PIN_FLD_FIELD_NAME", legacyType = BrmLegacyTypes.STR.class)
        private String pinFldFieldName;

        @RpcNumericField
        @RpcField(originalName = "PIN_FLD_FIELD_NUM", legacyType = BrmLegacyTypes.ENUM.class)
        private Integer pinFldFieldNum;

        @RpcNumericField
        @RpcField(originalName = "PIN_FLD_FIELD_TYPE", legacyType = BrmLegacyTypes.INT.class)
        private Integer pinFldFieldType;

        public PinFldField() {
        }

        public String getPinFldDescr() {
            return pinFldDescr;
        }

        public void setPinFldDescr(String pinFldDescr) {
            this.pinFldDescr = pinFldDescr;
        }

        public String getPinFldFieldName() {
            return pinFldFieldName;
        }

        public void setPinFldFieldName(String pinFldFieldName) {
            this.pinFldFieldName = pinFldFieldName;
        }

        public Integer getPinFldFieldNum() {
            return pinFldFieldNum;
        }

        public void setPinFldFieldNum(Integer pinFldFieldNum) {
            this.pinFldFieldNum = pinFldFieldNum;
        }

        public Integer getPinFldFieldType() {
            return pinFldFieldType;
        }

        public void setPinFldFieldType(Integer pinFldFieldType) {
            this.pinFldFieldType = pinFldFieldType;
        }
    }

    public CustomFieldEntity() {
    }

    public OlPoid getPinFldPoid() {
        return pinFldPoid;
    }

    public void setPinFldPoid(OlPoid pinFldPoid) {
        this.pinFldPoid = pinFldPoid;
    }

    public List<PinFldField> getPinFldField() {
        return pinFldField;
    }

    public void setPinFldField(List<PinFldField> pinFldField) {
        this.pinFldField = pinFldField;
    }

    @NotNull
    @Override
    public RpcEntityDefinition entityDefinition() {
        return CustomFieldEntityEntityHelper.entityDefinition;
    }

    @NotNull
    @Override
    public RpcEntityDefinition inputEntityDefinition() {
        return CustomFieldEntityEntityHelper.inputEntityDefinition;
    }

    @NotNull
    @Override
    public RpcEntityDefinition outputEntityDefinition() {
        return CustomFieldEntityEntityHelper.outputEntityDefinition;
    }

    @NotNull
    @Override
    public JsonObject toJsonObject() {
        return CustomFieldEntityEntityHelper.toJson(this);
    }

    @Override
    public void populateFromJson(@NotNull JsonObject jsonObject) {
        CustomFieldEntityEntityHelper.populateFromJson(jsonObject, this);
    }

}
