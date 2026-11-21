package org.shuangfa114.moremekasuitunits.module.gear.thirst;

import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.IHUDElement;
import mekanism.api.gear.IModule;
import mekanism.api.gear.IModuleHelper;
import mekanism.api.math.FloatingLong;
import mekanism.common.util.MekanismUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.shuangfa114.moremekasuitunits.MoreMekasuitUnits;
import org.shuangfa114.moremekasuitunits.init.ModConfig;
import org.shuangfa114.moremekasuitunits.util.DrinkingWaterUtil;
import org.shuangfa114.moremekasuitunits.util.UnitUtil;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * 自过滤饮水单元：口渴时自动饮用头盔储水槽里的水。
 * <p>
 * 水质由 {@link DrinkingWaterUtil} 判定：除脏水（Purity = 0）外都接受，
 * 不再要求必须是 Purified（3）—— 那档水只以瓶子物品存在，进不了流体管道。
 * <p>
 * 能量统一以 FE 表示，扣除时换算成焦耳；按「一份」循环排液，保证水量/电量/口渴值同步。
 */
public class ModuleAutomaticDrinkingUnit implements ICustomModule<ModuleAutomaticDrinkingUnit> {
    public static ResourceLocation icon = new ResourceLocation(MoreMekasuitUnits.MODID, MekanismUtils.ResourceType.GUI_HUD.getPrefix() + "automatic_drinking_unit.png");

    public ModuleAutomaticDrinkingUnit() {
    }

    @Override
    public void tickServer(IModule<ModuleAutomaticDrinkingUnit> module, Player player) {
        if (!MekanismUtils.isPlayingMode(player)) {
            return;
        }
        player.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(cap -> {
            int missing = 20 - cap.getThirst();
            if (missing <= 0) {
                return;
            }
            ItemStack container = module.getContainer();
            Optional<IFluidHandlerItem> capability = FluidUtil.getFluidHandler(container).resolve();
            if (capability.isEmpty()) {
                return;
            }
            IFluidHandlerItem handler = capability.get();
            int mbPerDrinking = ModConfig.base.drinkingMBPerDrinking.get();
            int available = DrinkingWaterUtil.getStoredAmount(handler) / mbPerDrinking;
            if (available <= 0) {
                return;
            }
            FloatingLong energyPerDrinkingJ = UnitUtil.feToJ(ModConfig.base.energyUsageAutomaticDrinking.get());
            int affordable = energyPerDrinkingJ.isZero()
                    ? Integer.MAX_VALUE
                    : module.getContainerEnergy().divideToInt(energyPerDrinkingJ);
            int toDrink = Math.min(missing, Math.min(available, affordable));
            if (toDrink <= 0) {
                return;
            }
            int portions = 0;
            while (portions < toDrink && DrinkingWaterUtil.drainOnce(handler, mbPerDrinking) == mbPerDrinking) {
                portions++;
            }
            if (portions <= 0) {
                return;
            }
            // 流体能力作用在副本上，排液后必须写回，否则水实际没被扣掉
            ItemStack drainedContainer = handler.getContainer();
            if (drainedContainer != container) {
                container.setTag(drainedContainer.getTag());
            }
            module.useEnergy(player, energyPerDrinkingJ.multiply(portions));
            cap.drink(player, ModConfig.base.thirstPerDrinking.get() * portions,
                    ModConfig.base.quenchedPerDrinking.get() * portions);
        });
    }

    @Override
    public void addHUDElements(IModule<ModuleAutomaticDrinkingUnit> module, Player player, Consumer<IHUDElement> hudElementAdder) {
        if (!module.isEnabled()) {
            return;
        }
        double ratio = 0;
        Optional<IFluidHandlerItem> capability = FluidUtil.getFluidHandler(module.getContainer()).resolve();
        if (capability.isPresent()) {
            int stored = DrinkingWaterUtil.getStoredAmount(capability.get());
            int max = ModConfig.base.cleanWaterMaxStorage.getAsInt();
            ratio = max <= 0 ? 0 : Math.min(1, (double) stored / max);
        }
        hudElementAdder.accept(IModuleHelper.INSTANCE.hudElementPercent(icon, ratio));
    }
}
