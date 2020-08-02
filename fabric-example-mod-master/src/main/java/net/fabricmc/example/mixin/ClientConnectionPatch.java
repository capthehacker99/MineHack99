package net.fabricmc.example.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fabricmc.example.ExampleMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdatePlayerAbilitiesC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.PacketListener;

@Mixin(ClientConnection.class)
public class ClientConnectionPatch {
	@Inject(at = @At("HEAD"), method = "sendImmediately", cancellable = true)
	private void sendImmediately(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> genericFutureListener_1,CallbackInfo ci) {
		if(ExampleMod.freecam && ExampleMod.freecame != null && packet instanceof PlayerMoveC2SPacket){
			((PlayerMoveC2SPacketAccessor)packet).setx(ExampleMod.freecame.getX());
			((PlayerMoveC2SPacketAccessor)packet).sety(ExampleMod.freecame.getY());
			((PlayerMoveC2SPacketAccessor)packet).setz(ExampleMod.freecame.getZ());
			((PlayerMoveC2SPacketAccessor)packet).setonground(true);
		}
		if(ExampleMod.freecam && packet instanceof UpdatePlayerAbilitiesC2SPacket){
			((UpdatePlayerAbilitiesC2SPacketAccessor)packet).setflying(false);
			//((UpdatePlayerAbilitiesC2SPacketAccessor)packet).setallowflying(false);
		}
	}
	@Inject(at = @At("HEAD"), method = "handlePacket", cancellable = true)
	private static void handlePacket(Packet<?> packet_1, PacketListener packetListener_1,CallbackInfo ci) {
		if(packetListener_1 instanceof ClientPlayNetworkHandler){
			MinecraftClient mc = MinecraftClient.getInstance();
			if(packet_1 instanceof PlayerPositionLookS2CPacket && mc.player != null){
				((PlayerPositionLookS2CPacketAccessor)packet_1).setpitch(mc.player.pitch);
				((PlayerPositionLookS2CPacketAccessor)packet_1).setyaw(mc.player.yaw);
			}
		}
	}
}
