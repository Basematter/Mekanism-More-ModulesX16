package org.shuangfa114.moremekasuitmodules.mixin;

import com.bawnorton.mixinsquared.api.MixinCanceller;
import net.neoforged.fml.loading.LoadingModList;

import java.util.List;

/**
 * 口渴相关 mixin 的开关。
 * <p>
 * 原实现读 {@code MoreMekasuitModules.isThirstLoaded}，但那个静态字段是在 mod 类的
 * <b>实例构造器</b>里赋值的，而 mixin 应用发生在构造器执行之前 —— 读到的永远是默认值 false，
 * 于是「给头盔加储水槽」的 mixin 100% 被取消，自动喝水单元永远拿不到水。
 * <p>
 * 这里改用 {@link LoadingModList}，在 mixin 应用阶段就能正确判断口渴模组是否存在。
 */
public class ModMixinCanceller implements MixinCanceller {
    private static final String THIRST_MIXIN_PACKAGE = ".mixin.thirst.";

    @Override
    public boolean shouldCancel(List<String> targetClassNames, String mixinClassName) {
        if (mixinClassName.contains(THIRST_MIXIN_PACKAGE)) {
            return !isModPresent("thirst");
        }
        return false;
    }

    private static boolean isModPresent(String modId) {
        try {
            return LoadingModList.get().getModFileById(modId) != null;
        } catch (Throwable ignored) {
            // 判断不了就当作存在：mixin 内部还有运行时判断，取消错了才会真崩
            return true;
        }
    }
}
