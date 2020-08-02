package net.fabricmc.example.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;

import net.fabricmc.example.ExampleMod;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(PlayerEntity.class)
public class PlayerEntityPatch {
	@Redirect(at = @At(target = "Lnet/minecraft/entity/player/PlayerEntity;isSpectator()Z", value = "INVOKE"),method = "tick")
	public boolean isSpectator(PlayerEntity dis){
		if(ExampleMod.freecam){
			return true;
		}else{
			return dis.isSpectator();
		}
	}
}
