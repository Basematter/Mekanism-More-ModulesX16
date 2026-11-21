package org.shuangfa114.moremekasuitmodules.mixin.thirst;

import mekanism.common.attachments.IAttachmentAware;
import mekanism.common.capabilities.ICapabilityAware;
import mekanism.common.capabilities.fluid.item.FluidTankSpec;
import mekanism.common.content.gear.IModuleContainerItem;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.item.gear.ItemSpecialArmor;
import mekanism.common.item.interfaces.IJetpackItem;
import mekanism.common.registration.impl.CreativeTabDeferredRegister;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import org.shuangfa114.moremekasuitmodules.MoreMekasuitModules;
import org.shuangfa114.moremekasuitmodules.config.ModConfig;
import org.shuangfa114.moremekasuitmodules.init.thirst.ThirstModulesInit;
import org.shuangfa114.moremekasuitmodules.util.DrinkingWaterUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = ItemMekaSuitArmor.class,remap = false)
public abstract class MixinItemMekaSuitArmor extends ItemSpecialArmor implements IModuleContainerItem, IJetpackItem, CreativeTabDeferredRegister.ICustomCreativeTabContents, IAttachmentAware, ICapabilityAware {


    @Shadow
    @Final
    private List<FluidTankSpec> fluidTankSpecs;

    protected MixinItemMekaSuitArmor(Holder<ArmorMaterial> material, Type armorType, Properties properties) {
        super(material, armorType, properties);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void addCleanWaterFluidTank(Type armorType, Properties properties, CallbackInfo ci) {
        if (MoreMekasuitModules.isThirstLoaded && armorType == Type.HELMET) {
            // 除脏水（purity = 0）外的水都收：无纯净度组件、1、2、3 均可，
            // 否则要求 purity = 3 会让储水槽永远灌不进普通水。
            fluidTankSpecs.add(FluidTankSpec.createFillOnly(ModConfig.base.cleanWaterTransferRate, ModConfig.base.cleanWaterMaxStorage,
                    DrinkingWaterUtil::isAcceptable,
                    itemStack -> this.hasModule(itemStack, ThirstModulesInit.MODULE_AUTOMATIC_DRINKING_UNIT)));
        }
    }
}
