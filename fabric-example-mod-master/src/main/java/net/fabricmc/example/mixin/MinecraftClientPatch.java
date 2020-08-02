package net.fabricmc.example.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.MinecraftClient;
@Mixin(MinecraftClient.class)
public class MinecraftClientPatch {

	
	@Inject(at = @At("HEAD"), method = "isModded", cancellable = true)
	public void isModded(CallbackInfoReturnable<Boolean> ci) {
			ci.setReturnValue(false);
	}
	@Inject(at = @At("HEAD"), method = "getVersionType", cancellable = true)
	public void getVersionType(CallbackInfoReturnable<String> ci) {
		ci.setReturnValue("release");
	}
	
}
