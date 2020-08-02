package net.fabricmc.example.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

@Mixin(PlayerPositionLookS2CPacket.class)
public interface PlayerPositionLookS2CPacketAccessor {
	@Accessor(value = "x")
	abstract void setx(double value);
	@Accessor(value = "y")
	abstract void sety(double value);
	@Accessor(value = "z")
	abstract void setz(double value);
	@Accessor(value = "yaw")
	abstract void setyaw(float value);
	@Accessor(value = "pitch")
	abstract void setpitch(float value);
}
