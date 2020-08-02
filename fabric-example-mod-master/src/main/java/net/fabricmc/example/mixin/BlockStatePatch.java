package net.fabricmc.example.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.example.ExampleMod;
import net.minecraft.block.AbstractBlock.AbstractBlockState;



@Mixin(AbstractBlockState.class)
public class BlockStatePatch {

	
	@Inject(at = @At("HEAD"), method = "getLuminance", cancellable = true)
	public void getLuminance(CallbackInfoReturnable<Integer> ci) {
		if(ExampleMod.xray || ExampleMod.fbright){
			ci.setReturnValue(15);
		}
	}
	
}
