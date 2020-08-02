package net.fabricmc.example.mixin;

import net.minecraft.entity.player.HungerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import net.fabricmc.example.APSHelpers.HungerHelper;

@Mixin(HungerManager.class)
public class HungerManagerPatch implements HungerHelper.ExhaustionManipulator
{
	@Shadow
	private float exhaustion;

	@Override
	public void setExhaustion(float value)
	{
		this.exhaustion = value;
	}

	@Override
	public float getExhaustion()
	{
		return this.exhaustion;
	}
}