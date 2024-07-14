package github.mkaksoy.mixin;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL12;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.SimpleDateFormat;
import java.util.*;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {
    @Shadow @Final private MinecraftClient client;

    @Shadow protected abstract void drawText(DrawContext context, List<String> text, boolean left);

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    public void render(@NotNull DrawContext context, @NotNull CallbackInfo ci) {
        this.client.getProfiler().push("debug");

        BlockPos blockPos = Objects.requireNonNull(this.client.getCameraEntity()).getBlockPos();
        String blockPosString = String.format(Locale.ROOT, "Position: %d, %d, %d", blockPos.getX(), blockPos.getY(), blockPos.getZ());

        float yaw = MathHelper.wrapDegrees(this.client.getCameraEntity().getYaw());
        List<String> leftList = getDebugText(yaw, blockPosString);
        List<String> rightList = getAdditionalDebugText();

        context.draw(() -> {
            drawText(context, leftList, true);
            drawText(context, rightList, false);
        });

        this.client.getProfiler().pop();
        ci.cancel();
    }

    @Unique
    private @NotNull List<String> getDebugText(float yaw, String blockPosString) {
        String directionString = getDirection(yaw);
        String gameTime = getGameTime();
        String realTime = getRealTime();
        String fpsLimit = getFpsLimit(false);

        List<String> list = Lists.newArrayList();
        list.add("§bDroplet Rendering 1.0.2 - Release");
        list.add(blockPosString);
        list.add(String.format(Locale.ROOT, "FPS: %d", MinecraftClient.getInstance().getCurrentFps()));
        list.add("FPS Limit: " + fpsLimit);
        list.add(String.format("Direction: %s (%.1f°)", directionString, yaw));
        list.add("Game Time: " + gameTime);
        list.add("Real Time: " + realTime);

        return list;
    }

    @Unique
    private @NotNull List<String> getAdditionalDebugText() {
        List<String> list = Lists.newArrayList();
        list.add("Graphics Card Information:");
        list.add(GL12.glGetString(GL12.GL_RENDERER));
        list.add("");

        list.add("");

        HitResult hitResult = this.client.crosshairTarget;
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos targetBlockPos = ((BlockHitResult) hitResult).getBlockPos();
            assert this.client.world != null;
            BlockState blockState = this.client.world.getBlockState(targetBlockPos);
            String blockName = blockState.getBlock().getName().getString();

            // Su ve lav blokları için isim ayarı
            if (blockState.getBlock() == Blocks.WATER) {
                blockName = "Water";
            } else if (blockState.getBlock() == Blocks.LAVA) {
                blockName = "Lava";
            }

            list.add("Targeted Block: " + blockName);
        } else {
            list.add("Targeted Block: None");
        }

        assert this.client.world != null;
        BlockPos playerPos = Objects.requireNonNull(this.client.getCameraEntity()).getBlockPos();
        String biomeKey = this.client.world.getBiomeAccess().getBiome(playerPos).getKey().toString();
        biomeKey = biomeKey.substring(58, biomeKey.length() - 2);
        String[] biomeKeyArr = biomeKey.split("_");

        for (int i = 0; i < biomeKeyArr.length; i++) {
            if (!biomeKeyArr[i].isEmpty()) {
                char[] chars = biomeKeyArr[i].toCharArray();
                chars[0] = Character.toUpperCase(chars[0]);
                biomeKeyArr[i] = new String(chars);
            }
        }

        StringJoiner joiner = new StringJoiner(" ");
        for (String element : biomeKeyArr) {
            joiner.add(element);
        }

        biomeKey = joiner.toString();

        list.add("Biome: " + biomeKey);


        return list;
    }

    @Contract(pure = true)
    @Unique
    private @NotNull String getDirection(float yaw) {
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
    private @NotNull String getGameTime() {
        assert this.client.world != null;
        long timeOfDay = this.client.world.getTimeOfDay();
        long hours = (timeOfDay / 1000 + 6) % 24;
        long minutes = (timeOfDay % 1000) * 60 / 1000;
        String period = (timeOfDay >= 0 && timeOfDay < 12000) ? "Day" : "Night";

        return String.format(Locale.ROOT, "%02d:%02d (%s)", hours, minutes, period);
    }

    @Unique
    private @NotNull String getRealTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date());
    }

    @Contract(pure = true)
    @Unique
    private @NotNull String getFpsLimit(boolean isFpsLimited) {
        return isFpsLimited ? "0" : "Infinity";
    }
}
