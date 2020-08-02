package net.fabricmc.example.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.network.packet.c2s.play.UpdatePlayerAbilitiesC2SPacket;;

@Mixin(UpdatePlayerAbilitiesC2SPacket.class)
public interface UpdatePlayerAbilitiesC2SPacketAccessor {
	@Accessor(value = "flying")
	abstract void setflying(boolean value);
	/*
	@Accessor(value = "allowflying")
	abstract void setallowflying(boolean value);
	*/
}
