package org.shuangfa114.moremekasuitmodules.gear.thirst;

import dev.ghen.thirst.foundation.common.capability.ModAttachment;
import mekanism.api.gear.*;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.util.MekanismUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.shuangfa114.moremekasuitmodules.MoreMekasuitModules;
import org.shuangfa114.moremekasuitmodules.config.ModConfig;
import org.shuangfa114.moremekasuitmodules.util.DrinkingWaterUtil;
import org.shuangfa114.moremekasuitmodules.util.ModuleUtil;

import java.util.function.Consumer;

/**
 * 自过滤饮水单元：口渴时自动饮用头盔储水槽里的水。
 * <p>
 * 水质由 {@link DrinkingWaterUtil} 判定：除脏水（purity = 0）外都接受，
 * 不再要求必须是 Purified（3）—— 那档水只以瓶子物品存在，进不了流体管道。
 * <p>
 * 能量按焦耳（J）统一计算：配置值本身就是 J，与 {@code getContainerEnergy} 同单位，
 * 不再做 FE 换算（原先 J 除以 FE 会把可用电量高估 2.5 倍）。
 */
public class ModuleAutomaticDrinkingUnit implements ICustomModule<ModuleAutomaticDrinkingUnit> {
    public static ResourceLocation icon = MoreMekasuitModules.rl(MekanismUtils.ResourceType.GUI_HUD.getPrefix() + "automatic_drinking_unit.png");

    public ModuleAutomaticDrinkingUnit() {
    }

    @Override
    public void tickServer(IModule<ModuleAutomaticDrinkingUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player) {
        if (!MekanismUtils.isPlayingMode(player)) {
            return;
        }
        int missing = 20 - player.getData(ModAttachment.PLAYER_THIRST).getThirst();
        if (missing <= 0) {
            return;
        }
        IFluidHandlerItem handler = Capabilities.FLUID.getCapability(stack);
        if (handler == null) {
            return;
        }
        int mbPerDrinking = ModConfig.base.drinkingMBPerDrinking.get();
        int available = DrinkingWaterUtil.getStoredAmount(handler) / mbPerDrinking;
        if (available <= 0) {
            return;
        }
        long energyPerDrinkingJ = ModuleUtil.feToJ(Math.max(1L, ModConfig.base.energyAutomaticDrinking.get()));
        long energyStored = module.getContainerEnergy(stack);
        int affordable = (int) Math.min(Integer.MAX_VALUE, energyStored / energyPerDrinkingJ);
        int toDrink = Math.min(missing, Math.min(available, affordable));
        if (toDrink <= 0) {
            return;
        }
        // 储水槽每次排液有速率上限（cleanWaterTransferRate），按「一份」为单位循环排出，
        // 保证水量、电量、口渴值三者严格同步，不会出现多排少记。
        int portions = 0;
        while (portions < toDrink && drainOnce(handler, mbPerDrinking) == mbPerDrinking) {
            portions++;
        }
        if (portions <= 0) {
            return;
        }
        module.useEnergy(player, stack, energyPerDrinkingJ * portions);
        player.getData(ModAttachment.PLAYER_THIRST).drink(
                ModConfig.base.thirstPerDrinking.get() * portions,
                ModConfig.base.quenchedPerDrinking.get() * portions);
    }

    /** 排出一份水（mbPerDrinking），返回实际排出量。 */
    private int drainOnce(IFluidHandlerItem handler, int amount) {
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            FluidStack contents = handler.getFluidInTank(tank);
            if (!DrinkingWaterUtil.isAcceptable(contents)) {
                continue;
            }
            FluidStack request = contents.copyWithAmount(Math.min(amount, contents.getAmount()));
            int got = handler.drain(request, IFluidHandler.FluidAction.EXECUTE).getAmount();
            if (got > 0) {
                return got;
            }
        }
        return 0;
    }

    @Override
    public void addHUDElements(IModule<ModuleAutomaticDrinkingUnit> module, IModuleContainer moduleContainer, ItemStack stack, Player player, Consumer<IHUDElement> hudElementAdder) {
        if (module.isEnabled()) {
            double ratio = 0;
            IFluidHandlerItem handler = Capabilities.FLUID.getCapability(stack);
            if (handler != null) {
                int stored = DrinkingWaterUtil.getStoredAmount(handler);
                int max = ModConfig.base.cleanWaterMaxStorage.getAsInt();
                ratio = max <= 0 ? 0 : Math.min(1, (double) stored / max);
            }
            hudElementAdder.accept(IModuleHelper.INSTANCE.hudElementPercent(icon, ratio));
        }
    }
}
