package wily.legacy.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.validation.DirectoryValidator;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import wily.legacy.client.screen.LegacyLoadingScreen;
import wily.legacy.util.ScreenUtil;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow protected abstract void updateScreenAndTick(Screen screen);

    @Shadow @Nullable public ClientLevel level;

    @Shadow public abstract DirectoryValidator directoryValidator();

    @Redirect(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;isServerControlledInventory()Z"))
    private boolean handleKeybinds(MultiPlayerGameMode instance){
        return false;
    }
    @Redirect(method = "setLevel",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/Minecraft;updateScreenAndTick(Lnet/minecraft/client/gui/screens/Screen;)V"))
    public void setLevelLoadingScreen(Minecraft instance, Screen screen, ClientLevel level) {
        boolean lastOd = isOtherDimension(this.level);
        boolean od = isOtherDimension(level);
        LegacyLoadingScreen s = new LegacyLoadingScreen(od || lastOd ? Component.translatable("legacy.menu." + (lastOd ? "leaving" : "entering"), getDimensionName((lastOd ? this.level : level).dimension())) : Component.empty(), Component.empty());
        if (od || lastOd) s.genericLoading = true;
        updateScreenAndTick(s);
    }
    private Component getDimensionName(ResourceKey<Level> dimension){
        String s = dimension.location().toLanguageKey("dimension");
        return Component.translatable(ScreenUtil.hasTip(s) ? s : "dimension.minecraft");

    }
    private boolean isOtherDimension(Level level){
        return level != null && level.dimension() !=Level.OVERWORLD;
    }

    @ModifyArg(method = "resizeDisplay",at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;setGuiScale(D)V"))
    public double resizeDisplay(double d) {
        return d * 2/3;
    }
}
