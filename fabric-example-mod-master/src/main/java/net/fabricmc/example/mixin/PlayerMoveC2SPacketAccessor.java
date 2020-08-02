package net.fabricmc.example.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

@Mixin(PlayerMoveC2SPacket.class)
public interface PlayerMoveC2SPacketAccessor {
	@Accessor(value = "x")
	abstract void setx(double value);
	@Accessor(value = "y")
	abstract void sety(double value);
	@Accessor(value = "z")
	abstract void setz(double value);
	@Accessor(value = "onGround")
	abstract void setonground(boolean value);
}
