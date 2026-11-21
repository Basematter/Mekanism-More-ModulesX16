package org.shuangfa114.moremekasuitunits.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

/**
 * 饮水用水质判定（1.20.1，纯度存在流体 NBT 的 Purity 里）。
 * <p>
 * Thirst Was Taken 的纯净度是 0~3：0 = Dirty、1 = Slightly Dirty、2 = Acceptable、3 = Purified。
 * MekaSuit 头盔自带过滤，因此除「脏水」外都接受：没有 Purity 标记的水、1、2、3 均可，只拒绝 0。
 */
public final class DrinkingWaterUtil {
    private static final String PURITY_TAG = "Purity";

    private DrinkingWaterUtil() {
    }

    public static boolean isAcceptable(FluidStack stack) {
        if (stack.isEmpty() || !stack.getFluid().isSame(Fluids.WATER)) {
            return false;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return true;
        }
        return !tag.contains(PURITY_TAG) || tag.getInt(PURITY_TAG) > 0;
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

    /** 排出一份水，返回实际排出量（mB）。 */
    public static int drainOnce(IFluidHandlerItem handler, int amount) {
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            FluidStack contents = handler.getFluidInTank(tank);
            if (!isAcceptable(contents)) {
                continue;
            }
            FluidStack request = contents.copy();
            request.setAmount(Math.min(amount, contents.getAmount()));
            int got = handler.drain(request, IFluidHandler.FluidAction.EXECUTE).getAmount();
            if (got > 0) {
                return got;
            }
        }
        return 0;
    }
}
