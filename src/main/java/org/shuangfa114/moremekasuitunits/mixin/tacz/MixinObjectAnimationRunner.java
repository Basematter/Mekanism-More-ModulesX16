package org.shuangfa114.moremekasuitunits.mixin.tacz;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.tacz.guns.api.client.animation.ObjectAnimation;
import com.tacz.guns.api.client.animation.ObjectAnimationRunner;
import mekanism.api.gear.IModule;
import mekanism.api.math.FloatingLong;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import org.shuangfa114.moremekasuitunits.init.tacz.TaczModulesInit;
import org.shuangfa114.moremekasuitunits.module.gear.tacz.ModuleQuickReloadingUnit;
import org.shuangfa114.moremekasuitunits.util.UnitUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nonnull;

/**
 * 换弹动画加速。
 * <p>
 * TACZ 1.2.0（含 1.20.1 版）把「本帧耗时」那行减法从 {@code update()} 抽成了私有方法
 * {@code elapsedSinceLastUpdate()}，原来的 {@code @Expression("? - this.lastUpdateNs")}
 * 在新版上扫不到目标，配合 mixins.json 的 {@code injectors.defaultRequire = 1}
 * 会抛 {@code InjectionError: Critical injection failure} 直接崩游戏。
 * <p>
 * 两个注入点都标 {@code require = 0}：1.2.0+ 走新方法，1.1.x 及更早走旧表达式，
 * 谁命中谁生效，另一条静默跳过。
 */
@Mixin(value = ObjectAnimationRunner.class, remap = false)
public abstract class MixinObjectAnimationRunner {
    @Shadow
    @Final
    @Nonnull
    private ObjectAnimation animation;

    /** TACZ 1.1.x 及更早：耗时直接写在 update() 里。 */
    @Definition(id = "lastUpdateNs", field = "Lcom/tacz/guns/api/client/animation/ObjectAnimationRunner;lastUpdateNs:J")
    @Expression("? - this.lastUpdateNs")
    @ModifyExpressionValue(method = "update", at = @At("MIXINEXTRAS:EXPRESSION"), require = 0)
    public long reloadingTimeLegacy(long original) {
        return applyReloadSpeed(original);
    }

    /** TACZ 1.2.0+：耗时被抽成独立方法，直接改返回值。 */
    @ModifyReturnValue(method = "elapsedSinceLastUpdate", at = @At("RETURN"), require = 0)
    public long reloadingTime(long original) {
        return applyReloadSpeed(original);
    }

    private long applyReloadSpeed(long original) {
        if (this.animation == null || this.animation.name == null || !this.animation.name.contains("reload")) {
            return original;
        }
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null) {
            return original;
        }
        IModule<ModuleQuickReloadingUnit> module =
                UnitUtil.getUnit(localPlayer, TaczModulesInit.MODULE_QUICK_RELOADING_UNIT, EquipmentSlot.CHEST);
        if (UnitUtil.isValidWithNull(module, localPlayer, FloatingLong.ZERO)) {
            return (long) (original * (1 / module.getCustomInstance().getReloadingTime()));
        }
        return original;
    }
}
