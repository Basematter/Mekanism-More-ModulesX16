package org.shuangfa114.moremekasuitunits.event;

import com.tacz.guns.api.event.common.GunFireEvent;
import mekanism.api.gear.IModule;
import mekanism.api.math.FloatingLong;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import org.shuangfa114.moremekasuitunits.init.ModConfig;
import org.shuangfa114.moremekasuitunits.init.tacz.TaczModulesInit;
import org.shuangfa114.moremekasuitunits.module.gear.tacz.ModuleRecoilOffsetUnit;
import org.shuangfa114.moremekasuitunits.util.UnitUtil;

/**
 * 后座抵消单元的能量消耗。
 * <p>
 * 原先在 TACZ 的 CameraSetupEvent 里扣电，但那是纯客户端事件，
 * 扣掉的只是客户端物品副本，服务端从未扣过 —— 物品一同步，电量就会「弹回去」。
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
        IModule<ModuleRecoilOffsetUnit> module = UnitUtil.getUnit(shooter, TaczModulesInit.MODULE_RECOIL_OFFSET_UNIT, EquipmentSlot.CHEST);
        if (module == null || !module.isEnabled()) {
            return;
        }
        float recoil = module.getCustomInstance().getRecoil();
        if (recoil <= 0) {
            return;
        }
        FloatingLong usage = ModConfig.base.energyUsageRecoilOffset.get().multiply(1 / recoil);
        if (UnitUtil.isValid(module, shooter, usage)) {
            module.useEnergy(shooter, UnitUtil.feToJ(usage));
        }
    }
}
