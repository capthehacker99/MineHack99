package net.fabricmc.example.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.example.ExampleMod;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;

@Mixin(AbstractBlock.class)
public class AbstractBlockPatch {
	@Inject(at = @At("HEAD"), method = "isSideInvisible", cancellable = true)
	public void isSideInvisible(BlockState state, BlockState stateFrom, Direction direction,CallbackInfoReturnable<Boolean> ci) {
		if(ExampleMod.xray){
			ci.setReturnValue(true);
		}
		return;
	}
}
