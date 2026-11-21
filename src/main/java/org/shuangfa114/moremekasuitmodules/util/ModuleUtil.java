package org.shuangfa114.moremekasuitmodules.util;

import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.gear.ModuleData;
import mekanism.common.util.UnitDisplayUtils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.shuangfa114.moremekasuitmodules.gear.WithOffMode;

public class ModuleUtil {
    public static <T extends ICustomModule<T>>@Nullable IModule<T> getUnit(LivingEntity livingEntity, DeferredHolder<ModuleData<?>, ModuleData<T>> unit, EquipmentSlot equipmentSlot) {
        if (livingEntity instanceof Player player) {
            ItemStack itemStack = getEquipment(player,equipmentSlot);
            return IModuleHelper.INSTANCE.getModule(itemStack, unit);
        }
        return null;
    }
    public static <T extends ICustomModule<T>> @Nullable IModule<T> getMekaToolUnit(LivingEntity livingEntity, DeferredHolder<ModuleData<?>, ModuleData<T>> unit){
        if (livingEntity instanceof Player player) {
            ItemStack itemStack = player.getInventory().getSelected();
            return IModuleHelper.INSTANCE.getModule(itemStack,unit);
        }
        return null;
    }
    public static ItemStack getEquipment(Player player, EquipmentSlot equipmentSlot){
        return player.getItemBySlot(equipmentSlot);
    }
    public static boolean isValidWithNull(IModule<?> module, LivingEntity livingEntity,ItemStack itemStack, long value){
        return module!=null&&isValid(module,livingEntity,itemStack,value);
    }
    public static boolean isValid(@NotNull IModule<?> module, LivingEntity livingEntity,ItemStack itemStack, long fe){
        boolean t = true;
        if(module.getCustomInstance() instanceof WithOffMode withOffMode){
            t=!withOffMode.isOffMode();
        }
        return module.isEnabled()&&t&&module.canUseEnergy(livingEntity,itemStack, feToJ(fe));
    }

    /**
     * 能量统一以 FE 表示：配置值与各处 usage 变量都是 FE，
     * 只有与 Mekanism 交互时（canUseEnergy / useEnergy 内部单位是焦耳）换算一次。
     * 1 FE = 2.5 J，由 Mekanism 的 EnergyUnit.FORGE_ENERGY 负责。
     */
    public static long feToJ(long fe){
        return UnitDisplayUtils.EnergyUnit.FORGE_ENERGY.convertFrom(fe);
    }
}
