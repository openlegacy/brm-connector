package io.ol.provider.brm.operation;

import io.ol.core.annotation.RpcOperation;
import io.ol.core.annotation.RpcOperationOutput;
import io.ol.core.rpc.operation.Operation;
import io.ol.core.rpc.operation.OperationDefinition;
import io.ol.provider.brm.entity.EntityPlaceholder;
import org.jetbrains.annotations.NotNull;
import org.openlegacy.core.rpc.RpcEntity;

import java.util.Map;

@RpcOperation
public class PlaceholderOperation implements Operation<EntityPlaceholder> {
    private final EntityPlaceholder input;
    private final String path = "PCM_OP_TEST_LOOPBACK:0";
    @RpcOperationOutput(statusCode = 200, entityType = EntityPlaceholder.class)
    private final Map<Integer, RpcEntity> outputMap;

    public PlaceholderOperation(EntityPlaceholder entityPlaceholder) {
        this.input = entityPlaceholder;
        this.outputMap = PlaceholderOperationHelper.initOutputMap();
    }

    @NotNull
    @Override
    public EntityPlaceholder getInput() {
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
        return PlaceholderOperationHelper.operationDefinition;
    }

}
