package net.fabricmc.example.mixin;



import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.example.BlockOcclusionCache;
//import net.fabricmc.example.BlockOcclusionCache;
import net.fabricmc.example.ExampleMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

@Mixin(Block.class)

public class BlockPatch {
	private final static BlockOcclusionCache occlusionCache = new BlockOcclusionCache();
	@Inject(at = @At("HEAD"), method = "shouldDrawSide", cancellable = true)
	private static void shouldDrawSide(BlockState state, BlockView world, BlockPos pos, Direction facing, CallbackInfoReturnable<Boolean> ci) {
		if(ExampleMod.xray){
			if(ExampleMod.xrayBlocks.contains(state.getBlock())){
					ci.setReturnValue(true);
			}else{
				ci.setReturnValue(false);
				return;
			}
		}else{
			try{
				ci.setReturnValue(occlusionCache.shouldDrawSide(state, world, pos, facing));
			}catch(Exception e){}
			return;
		}
	}
	
	/*
	@Inject(at = @At("HEAD"), method = "skipRenderingSide", cancellable = true)
	private void skipRenderingSide(BlockState blockState_1, BlockState blockState_2, Direction direction_1,CallbackInfoReturnable<Boolean> ci) {
		if(ExampleMod.xray){
				if(ExampleMod.xrayBlocks.contains(blockState_1.getBlock())){
					ci.setReturnValue(false);
				}else{
					ci.setReturnValue(true);
				}
		}
	}
	*/
	/*
	@Inject(at = @At("HEAD"), method = "getBlockBrightness", cancellable = true)
	private void getBlockBrightness(BlockState blockState_1, ExtendedBlockView extendedBlockView_1, BlockPos blockPos_1, CallbackInfoReturnable<Integer> ci) {
		if(ExampleMod.xray || ExampleMod.fbright){
			ci.setReturnValue(15728880);
		}
	}
	*/
	/*
	@Inject(at = @At("HEAD"), method = "getRenderType", cancellable = true)
	private void getRenderType(BlockState blockState_1,CallbackInfoReturnable<BlockRenderType> ci) {
		if(ExampleMod.xray){
			if(ExampleMod.xrayBlocks.contains(blockState_1.getBlock())){
				return;
			}else{
				ci.setReturnValue(BlockRenderType.INVISIBLE);
				
			}
		}
	 }
	*/
	
	
	//@Inject(at = @At("HEAD"), method = "isTranslucent", cancellable = true)
	//private void isTranslucent(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1,CallbackInfoReturnable<Boolean> ci) {
	//	if(ExampleMod.xray){
	//		if(ExampleMod.xrayBlocks.contains(blockState_1.getBlock())){
	//			ci.setReturnValue(false);
	//		}else{
	//			ci.setReturnValue(true);
	//		}
	//	}
	//}
	//@Inject(at = @At("HEAD"), method = "isFullOpaque", cancellable = true)
	//private void isFullOpaque(BlockState blockState_1, BlockView blockView_1, BlockPos blockPos_1,CallbackInfoReturnable<Boolean> ci) {
	//	if(ExampleMod.xray){
	//		if(ExampleMod.xrayBlocks.contains(blockState_1.getBlock())){
	//			ci.setReturnValue(true);
	//		}else{
	//			ci.setReturnValue(false);
	//		}
	//	}
	 //  }
}