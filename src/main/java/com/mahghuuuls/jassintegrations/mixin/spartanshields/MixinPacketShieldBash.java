package com.mahghuuuls.jassintegrations.mixin.spartanshields;

import com.mahghuuuls.jassintegrations.integration.spartanshields.BashHookApplied;
import com.mahghuuuls.jassintegrations.integration.spartanshields.ShieldBashCharger;
import com.oblivioussp.spartanshields.network.PacketShieldBash;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Asks {@link ShieldBashCharger} before Spartan Shields' server bash handler does anything, and
 * cancels the whole handler when the bash is refused. Targets the concrete handler, not the
 * {@code IMessage} bridge. {@code require = 0}: another Spartan Shields version keeps a free bash.
 */
@Mixin(value = PacketShieldBash.class, remap = false)
public abstract class MixinPacketShieldBash implements BashHookApplied {

    @Shadow
    protected EnumHand hand;

    @Inject(method = "handleServerSide(Lcom/oblivioussp/spartanshields/network/PacketShieldBash;Lnet/minecraft/entity/player/EntityPlayerMP;)V",
            at = @At("HEAD"), cancellable = true, remap = false, require = 0)
    private void jassintegrations$chargeBash(PacketShieldBash message, EntityPlayerMP player, CallbackInfo ci) {
        if (message != null && !ShieldBashCharger.allowBash(player, ((MixinPacketShieldBash) (Object) message).hand)) {
            ci.cancel();
        }
    }
}
