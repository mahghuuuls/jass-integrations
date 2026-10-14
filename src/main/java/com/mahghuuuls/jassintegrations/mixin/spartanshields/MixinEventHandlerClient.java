package com.mahghuuuls.jassintegrations.mixin.spartanshields;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mahghuuuls.jassintegrations.client.ShieldBashSwing;
import com.mahghuuuls.jassintegrations.integration.spartanshields.BashHookApplied;
import com.oblivioussp.spartanshields.event.EventHandlerClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Skips the local arm swing of a shield bash the server is going to refuse. Wraps the only
 * {@code swingArm} call in Spartan Shields' client input handler; the bash packet is still sent.
 * Client only. {@code require = 0}: another Spartan Shields version keeps its swing.
 */
@Mixin(value = EventHandlerClient.class, remap = false)
public abstract class MixinEventHandlerClient implements BashHookApplied {

    @WrapOperation(method = "onInputEvent", remap = false, require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;swingArm(Lnet/minecraft/util/EnumHand;)V", remap = true))
    private static void jassintegrations$skipRefusedSwing(EntityPlayer player, EnumHand hand, Operation<Void> original) {
        if (!ShieldBashSwing.suppress()) {
            original.call(player, hand);
        }
    }
}
