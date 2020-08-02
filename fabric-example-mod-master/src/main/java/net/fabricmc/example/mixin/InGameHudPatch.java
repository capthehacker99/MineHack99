package net.fabricmc.example.mixin;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fabricmc.example.APSHUDOverlayHandler;

@Mixin(InGameHud.class)
public class InGameHudPatch
{
	@Inject(at = @At("HEAD"), method = "renderStatusBars")
	private void renderFoodPre(MatrixStack stack, CallbackInfo info)
	{
		APSHUDOverlayHandler.onPreRender(stack);
	}

	@Inject(at = @At("RETURN"), method = "renderStatusBars")
	private void renderFoodPost(MatrixStack stack, CallbackInfo info)
	{
		APSHUDOverlayHandler.onRender(stack);
	}
}