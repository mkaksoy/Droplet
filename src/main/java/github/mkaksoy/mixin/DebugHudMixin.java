package github.mkaksoy.mixin;

import com.google.common.collect.Lists;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {
    @Shadow @Final private MinecraftClient client;

    @Shadow protected abstract void drawText(DrawContext context, List<String> text, boolean left);

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    public void render(DrawContext context, CallbackInfo ci) {
        this.client.getProfiler().push("debug");

        BlockPos blockPos = Objects.requireNonNull(this.client.getCameraEntity()).getBlockPos();
        String blockPosString = String.format(Locale.ROOT, "Position: %d, %d, %d", blockPos.getX(), blockPos.getY(), blockPos.getZ());

        float yaw = MathHelper.wrapDegrees(this.client.getCameraEntity().getYaw());
        List<String> list = getDebugText(yaw, blockPosString);

        context.draw(() -> drawText(context, list, true));

        this.client.getProfiler().pop();
        ci.cancel();
    }

    @Unique
    private @NotNull List<String> getDebugText(float yaw, String blockPosString) {
        String directionString = getDirection(yaw);
        String gameTime = getGameTime();
        String realTime = getRealTime();

        List<String> list = Lists.newArrayList();
        list.add("§bDroplet Rendering 1.0.1 - Release");
        list.add(blockPosString);
        list.add(String.format(Locale.ROOT, "FPS: %d", MinecraftClient.getInstance().getCurrentFps()));
        list.add(String.format("Direction: %s (%.1f°)", directionString, yaw));
        list.add("Game Time: " + gameTime);
        list.add("Real Time: " + realTime);

        return list;
    }

    @Unique
    private String getDirection(float yaw) {
        if (yaw >= -22.5 && yaw < 22.5) {
            return "South";
        } else if (yaw >= 22.5 && yaw < 67.5) {
            return "Southwest";
        } else if (yaw >= 67.5 && yaw < 112.5) {
            return "West";
        } else if (yaw >= 112.5 && yaw < 157.5) {
            return "Northwest";
        } else if (yaw >= 157.5 || yaw < -157.5) {
            return "North";
        } else if (yaw >= -157.5 && yaw < -112.5) {
            return "Northeast";
        } else if (yaw >= -112.5 && yaw < -67.5) {
            return "East";
        } else if (yaw >= -67.5 && yaw < -22.5) {
            return "Southeast";
        } else {
            return "Unknown";
        }
    }

    @Unique
    private String getGameTime() {
        assert this.client.world != null;
        long timeOfDay = this.client.world.getTimeOfDay();
        long hours = (timeOfDay / 1000 + 6) % 24;
        long minutes = (timeOfDay % 1000) * 60 / 1000;
        String period = (timeOfDay >= 0 && timeOfDay < 12000) ? "Day" : "Night";

        return String.format(Locale.ROOT, "%02d:%02d (%s)", hours, minutes, period);
    }

    @Unique
    private String getRealTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date());
    }
}
