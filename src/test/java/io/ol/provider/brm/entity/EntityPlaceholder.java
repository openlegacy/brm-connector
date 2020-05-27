package io.ol.provider.brm.entity;

import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.openlegacy.core.annotations.rpc.Action;
import org.openlegacy.core.annotations.rpc.RpcActions;
import org.openlegacy.core.annotations.rpc.RpcEntity;
import org.openlegacy.core.model.entity.RpcEntityDefinition;

@RpcEntity
@RpcActions(actions = {
        @Action(action = org.openlegacy.core.rpc.actions.RpcActions.EXECUTE.class, path = "PCM_OP_TEST_LOOPBACK_0", displayName = "LOOPBACK", alias = "LOOPBACK")
})
public class EntityPlaceholder implements org.openlegacy.core.rpc.RpcEntity {

    public EntityPlaceholder() {
    }

    @NotNull
    @Override
    public RpcEntityDefinition entityDefinition() {
        return EntityPlaceholderEntityHelper.entityDefinition;
    }

    @NotNull
    @Override
    public RpcEntityDefinition inputEntityDefinition() {
        return EntityPlaceholderEntityHelper.inputEntityDefinition;
    }

    @NotNull
    @Override
    public RpcEntityDefinition outputEntityDefinition() {
        return EntityPlaceholderEntityHelper.outputEntityDefinition;
    }

    @NotNull
    @Override
    public JsonObject toJsonObject() {
        return EntityPlaceholderEntityHelper.toJson(this);
    }

    @Override
    public void populateFromJson(@NotNull JsonObject jsonObject) {
        EntityPlaceholderEntityHelper.populateFromJson(jsonObject, this);
    }

}
