package org.shuangfa114.moremekasuitmodules.event;

import com.tacz.guns.api.event.common.GunFireEvent;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import org.shuangfa114.moremekasuitmodules.config.ModConfig;
import org.shuangfa114.moremekasuitmodules.gear.tacz.ModuleRecoilOffsetUnit;
import org.shuangfa114.moremekasuitmodules.init.tacz.TaczModulesInit;
import org.shuangfa114.moremekasuitmodules.util.ModuleUtil;

/**
 * 后座抵消单元的能量消耗。
 * <p>
 * 原先在 TACZ 的 CameraSetupEvent 里扣电，但那是个纯客户端事件（每发两侧各触发一次相机后座计算），
 * 扣掉的只是客户端物品副本，服务端从未扣过 —— 物品一同步，画面上的电量就会「弹回去」。
 * <p>
 * 现在改在服务端开火事件里按发扣电，电量始终是服务端权威数据。
 */
public class GunFireEnergy {
    @SubscribeEvent
    public void onGunFire(GunFireEvent event) {
        if (event.getLogicalSide() != LogicalSide.SERVER) {
            return;
        }
        LivingEntity shooter = event.getShooter();
        ItemStack chest = shooter.getItemBySlot(EquipmentSlot.CHEST);
        IModule<ModuleRecoilOffsetUnit> module = IModuleHelper.INSTANCE.getModule(chest, TaczModulesInit.MODULE_RECOIL_OFFSET_UNIT);
        if (module == null || !module.isEnabled()) {
            return;
        }
        float recoil = module.getCustomInstance().getRecoil();
        if (recoil <= 0) {
            return;
        }
        long usage = (long) (ModConfig.base.energyRecoilOffset.get() * (1 / recoil));
        if (ModuleUtil.isValid(module, shooter, chest, usage)) {
            module.useEnergy(shooter, chest, ModuleUtil.feToJ(usage));
        }
    }
}
