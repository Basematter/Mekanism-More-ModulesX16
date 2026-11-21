package org.shuangfa114.moremekasuitmodules.util;

import dev.ghen.thirst.content.registry.ThirstComponent;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

/**
 * 饮水用水质判定。
 * <p>
 * Thirst Was Taken 的纯净度是 0~3 四档：
 * 0 = Dirty（脏水）、1 = Slightly Dirty、2 = Acceptable、3 = Purified。
 * <p>
 * 本模组的 MekaSuit 头盔自带过滤，因此除「脏水」以外的水都可以用：
 * 没有纯净度组件的水（储罐/管道里的普通水）、1、2、3 全部接受，只有明确标成 0 的脏水被拒。
 * 不再要求必须是 3（Purified）—— 净化水只以瓶子物品存在，无法通过流体管道进入头盔。
 */
public final class DrinkingWaterUtil {
    private DrinkingWaterUtil() {
    }

    /** 是否是可饮用的水：水且不是脏水（purity 缺省视为可接受）。 */
    public static boolean isAcceptable(FluidStack stack) {
        if (stack.isEmpty() || stack.getFluid() != Fluids.WATER) {
            return false;
        }
        Integer purity = stack.get(ThirstComponent.PURITY);
        return purity == null || purity > 0;
    }

    /** 统计容器里可饮用水的总量（mB）。 */
    public static int getStoredAmount(IFluidHandlerItem handler) {
        int total = 0;
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            FluidStack contents = handler.getFluidInTank(tank);
            if (isAcceptable(contents)) {
                total += contents.getAmount();
            }
        }
        return total;
    }
}
