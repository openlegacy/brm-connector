package io.ol.provider.brm.operation;

import io.ol.core.annotation.Properties;
import io.ol.core.annotation.Property;
import io.ol.core.annotation.RpcOperation;
import io.ol.core.annotation.RpcOperationOutput;
import io.ol.core.rpc.operation.Operation;
import io.ol.core.rpc.operation.OperationDefinition;
import io.ol.provider.brm.BrmConstants;
import io.ol.provider.brm.entity.FListExampleEntity;
import org.jetbrains.annotations.NotNull;
import org.openlegacy.core.rpc.RpcEntity;

import java.util.Map;

@Properties({
        @Property(key = BrmConstants.OPCODE_FLAG, value = "0")
})
@RpcOperation
public class FListExampleOperation implements Operation<FListExampleEntity> {
    private final FListExampleEntity input;
    private final String path = "PCM_OP_TEST_LOOPBACK";
    @RpcOperationOutput(statusCode = 200, entityType = FListExampleEntity.class)
    private final Map<Integer, RpcEntity> outputMap;

    public FListExampleOperation(FListExampleEntity fListExampleEntity) {
        this.input = fListExampleEntity;
        this.outputMap = FListExampleOperationHelper.initOutputMap();
    }

    @NotNull
    @Override
    public FListExampleEntity getInput() {
        return input;
    }

    @NotNull
    @Override
    public String getPath() {
        return path;
    }

    @NotNull
    @Override
    public Map<Integer, RpcEntity> getOutputMap() {
        return outputMap;
    }

    @NotNull
    @Override
    public OperationDefinition operationDefinition() {
        return FListExampleOperationHelper.operationDefinition;
    }

}
