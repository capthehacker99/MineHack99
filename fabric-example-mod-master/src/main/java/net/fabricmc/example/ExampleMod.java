package net.fabricmc.example;

import java.util.ArrayList;
import java.util.Iterator;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;





public class ExampleMod implements ModInitializer {
	/*
	public static synchronized void faceEntity(MinecraftClient mc,Entity entity) {
		try{
		final float[] rotations = getRotationsNeeded(mc,entity);



		if (rotations != null) {

			mc.player.yaw = rotations[0];

			mc.player.pitch = rotations[1] + 1.0F;// 14

		}
		}catch(Exception e){
			
		}
	}
*/
    public static double enemyoffsetx = 0;
	public static double enemyoffsety = 0;
	public static double enemyoffsetz = 0;
/*
	public static float[] getRotationsNeeded(MinecraftClient mc,Entity entity) {

		if (entity == null) {

			return null;

		}



		final double diffX = (entity.x+enemyoffsetx) - mc.player.x;

		final double diffZ = (entity.z+enemyoffsetz) - mc.player.z;

		double diffY;



		if (entity instanceof PlayerEntity) {

			final PlayerEntity entityLivingBase = (PlayerEntity) entity;

			diffY = entityLivingBase.y + entityLivingBase.getEyeHeight(mc.player.getPose()) - (mc.player.x + mc.player.getEyeHeight(mc.player.getPose()));

		} else if(entity instanceof MobEntity){

			diffY = (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0D - (mc.player.y + mc.player.getEyeHeight(mc.player.getPose()));

		}else{

			diffY = (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0D - (mc.player.y + mc.player.getEyeHeight(mc.player.getPose()));

		}



		final double dist = MathHelper.sqrt(diffX * diffX + diffZ * diffZ);

		final float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;

		final float pitch = (float) -(Math.atan2(diffY, dist) * 180.0D / Math.PI);

		return new float[] {

			mc.player.yaw + MathHelper.wrapDegrees(yaw - mc.player.yaw), mc.player.pitch + MathHelper.wrapDegrees(pitch - mc.player.pitch)

		};

	}
    
	
	*/
	public static ArrayList<Block> xrayBlocks = new ArrayList<Block>();
	
	public static boolean xray = false;
	public static boolean freecam = false;
	public static boolean fbright = false;
	
	public static void initxray(){
		xrayBlocks.add(Blocks.COAL_ORE);
		xrayBlocks.add(Blocks.IRON_ORE);
		xrayBlocks.add(Blocks.GOLD_ORE);
		xrayBlocks.add(Blocks.LAPIS_ORE);
		xrayBlocks.add(Blocks.DIAMOND_ORE);
		xrayBlocks.add(Blocks.SPAWNER);
		xrayBlocks.add(Blocks.RAIL);
		xrayBlocks.add(Blocks.CHEST);
		xrayBlocks.add(Blocks.TRAPPED_CHEST);
		xrayBlocks.add(Blocks.HOPPER);
		xrayBlocks.add(Blocks.DISPENSER);
		xrayBlocks.add(Blocks.DROPPER);
		xrayBlocks.add(Blocks.NETHER_QUARTZ_ORE);
		xrayBlocks.add(Blocks.GLOWSTONE);
		xrayBlocks.add(Blocks.LAVA);
		xrayBlocks.add(Blocks.REDSTONE_ORE);
		xrayBlocks.add(Blocks.NETHERITE_BLOCK);
		xrayBlocks.add(Blocks.NETHER_GOLD_ORE);
		xrayBlocks.add(Blocks.ANCIENT_DEBRIS);
	}
	public static Timer saytimer = new Timer();
	public static boolean gonnasay = false;
	public static String tosay = "";
	public static double gamma = 0;
	public static boolean thing = false;
	public static double oldxfc = 0;
	public static double oldyfc = 0;
	public static double oldzfc = 0;
	public static boolean isassiston = false;
	public static OtherClientPlayerEntity freecame;
	private Vec3d addMotion = Vec3d.ZERO;
	private boolean wassmartplace = false;
	private Vec3d lastplacepos = Vec3d.ZERO;
	public static ArrayList<Vec3d> lastvelos = new ArrayList<Vec3d>();
	public static AutoFish autoFish = new AutoFish();
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		
		
		
		initxray();
		
		KeyBinding xrayk = new KeyBinding("xray", 88, "key.categories.movement");
		
		KeyBinding brightk = new KeyBinding("bright", 76, "key.categories.movement");
		
		KeyBinding freecamk = new KeyBinding("freecam", 90, "key.categories.movement");
		
		/*
		UseBlockCallback.EVENT.register((PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) -> {
			MinecraftClient mc = MinecraftClient.getInstance();
				final BlockPos pos = hitResult.getBlockPos();
				
				if (!thing && !pos.equals((Object)lastTargetPos) && (lastTargetPos == null || !pos.equals((Object)lastTargetPos.offset(lastTargetSide)))) {
					((MinecraftClientPatch)mc).setitemUseCooldown(0);
					thing = true;
				}
				else if (thing && pos.equals((Object)lastTargetPos) && hitResult.getSide() == lastTargetSide) {
					((MinecraftClientPatch)mc).setitemUseCooldown(4);
					thing = false;
				}
				lastTargetPos = pos.toImmutable();
				lastTargetSide = hitResult.getSide();
			return ActionResult.PASS;
		});
		*/
		
		ClientTickCallback.EVENT.register((MinecraftClient mc) -> {
			APSHUDOverlayHandler.onClientTick();
			autoFish.onClientTick();
			if(mc.player != null && mc.world != null){
					
				if(gonnasay && saytimer.check(1500)){
					gonnasay = false;
					mc.inGameHud.getChatHud().addToMessageHistory(tosay);
					mc.player.sendChatMessage(tosay);
				}
				
				/*
				if

				(

					mc.player.getHealth() > 0

					&& mc.player.onGround

					&& mc.player.getStackInHand(Hand.MAIN_HAND) != null

					&& mc.player.getStackInHand(Hand.MAIN_HAND).getItem().isFood()

					&& mc.player.getHungerManager().isNotFull()

					&& mc.options.keyUse.isPressed()

				)

				{

						mc.player.eatFood(mc.world,mc.player.getStackInHand(Hand.MAIN_HAND));
						mc.player.networkHandler.sendPacket(new C03PacketPlayer(false));

				}
				*/
				/*
				
				if(mc.options.keyAttack.isPressed() == true && isassiston){
					if(mc.hitResult != null && mc.hitResult.getType() == Type.ENTITY){
						if(((EntityHitResult)mc.hitResult).getEntity() instanceof LivingEntity){
							followenemytimer.reset();
						}
					}
					
				}
				if(!followenemytimer.check(50)){
					if(enemy != null && enemy.isAlive() && !enemy.isInvisible()){
						faceEntity(mc,enemy);
						
					}
				}
				*/
				if((!mc.player.horizontalCollision) && (mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed > 0) && !mc.player.isSubmergedInWater()) {
					
						mc.player.setSprinting(true);
			

				}else if(mc.player.isSubmergedInWater()){
				
				}else{

					mc.player.setSprinting(false);

				}
				Iterable<Entity> entitys = mc.world.getEntities();
				Iterator<Entity> entitysiterator = entitys.iterator();
				
				while(entitysiterator.hasNext()) {
			         entitysiterator.next().setInvisible(false);
			      }
				
				
				//mc.player.abilities.allowFlying = true;
				
				if(brightk.wasPressed()){
					fbright = !fbright;
					
					mc.worldRenderer.reload();
				}
				
				if(xrayk.wasPressed()){
					xray = !xray;
	
					mc.worldRenderer.reload();
				}
				
				if(freecamk.wasPressed()){
					freecam = !freecam;
					if(freecam){
						
						freecame = new OtherClientPlayerEntity(mc.world,mc.player.getGameProfile());
						mc.world.addEntity(-99, freecame);
						//freecame.setPositionAnglesAndUpdate(mc.player.x, mc.player.y, mc.player.z, mc.player.yaw, mc.player.pitch);
						//oldxfc = mc.player.x;
						//oldyfc = mc.player.y;
						//oldzfc = mc.player.z;
						freecame.copyFrom(mc.player);
						mc.player.abilities.flying = true;
					}else{
						mc.player.abilities.flying = false;
						//mc.player.x = oldxfc;
						//mc.player.y = oldyfc;
						//mc.player.z = oldzfc;
						//mc.player.setPosition(oldxfc, oldyfc, oldzfc);
						//mc.player.setPositionAnglesAndUpdate(freecame.x, freecame.y, freecame.z, freecame.yaw, freecame.pitch);
						mc.player.copyPositionAndRotation(freecame);
						freecame.remove();
						freecame = null;
					}
				}
				
				
				if(mc.player.hasStatusEffect(StatusEffects.BLINDNESS)){
					mc.player.removeStatusEffect(StatusEffects.BLINDNESS);
				}
				if(mc.player.hasStatusEffect(StatusEffects.NAUSEA)){
					mc.player.removeStatusEffect(StatusEffects.NAUSEA);
				}
				//if(freecame != null){
					//freecame.setPosition(freecame.x, freecame.y-0.1, freecame.z);
					//freecame.setVelocity(freecame.getVelocity().getY() / 10,freecame.getVelocity().getX() / 10,freecame.getVelocity().getZ() / 10);
				//}
				if(freecam){
					mc.player.abilities.flying = true;
					mc.player.setOnGround(false);
				}
				if(xray || freecam){
					mc.chunkCullingEnabled = false;
				}else{
					mc.chunkCullingEnabled = true;
				}
				/*
				if(mc.options.keyRight.isPressed() == true && mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK){
					if(Math.floor(mc.hitResult.getPos().getX()) == lastplacepos.getX() || Math.floor(mc.hitResult.getPos().getY()) == lastplacepos.getY() || Math.floor(mc.hitResult.getPos().getZ()) == lastplacepos.getZ()){
						wassmartplace = true;
						//lastplacepos = new Vec3d(Math.floor(mc.hitResult.getPos().getX()),Math.floor(mc.hitResult.getPos().getY()),Math.floor(mc.hitResult.getPos().getZ()));
						((MinecraftClientPatch)mc).setitemUseCooldown(0);
					}else{
						if(wassmartplace){
							lastplacepos = Vec3d.ZERO;
							((MinecraftClientPatch)mc).setitemUseCooldown(4);
						}
						wassmartplace = false;
					}
					lastplacepos = new Vec3d(Math.floor(mc.hitResult.getPos().getX()),Math.floor(mc.hitResult.getPos().getY()),Math.floor(mc.hitResult.getPos().getZ()));
				}
				*/
				if(mc.options.keyAttack.isPressed() == true && mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK){
					
					if(mc.player.getStackInHand(Hand.MAIN_HAND).getItem() == Items.IRON_PICKAXE){

					if(mc.player.getStackInHand(Hand.MAIN_HAND).toHoverableText().equals("Explosive Pickaxe")){
						
						if( mc.world.getBlockState(new BlockPos(mc.crosshairTarget.getPos())).getBlock() != Blocks.AIR){
							//Nuke(mc,new BlockPos(mc.hitResult.getPos()));
							mc.world.playSound((PlayerEntity)mc.player,new BlockPos(mc.crosshairTarget.getPos()), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (mc.world.random.nextFloat() - mc.world.random.nextFloat()) * 0.2F) * 0.7F);
							//ExplosionParticles(mc,mc.hitResult.getPos().x,mc.hitResult.getPos().y,mc.hitResult.getPos().z,16.0F);
							//ExplosionParticles(mc,mc.hitResult.getPos().x,mc.hitResult.getPos().y,mc.hitResult.getPos().z,16.0F);
							//ExplosionParticles(mc,mc.hitResult.getPos().x,mc.hitResult.getPos().y,mc.hitResult.getPos().z,16.0F);
							//ExplosionParticles(mc,mc.hitResult.getPos().x,mc.hitResult.getPos().y,mc.hitResult.getPos().z,16.0F);
							//ExplosionParticles(mc,mc.hitResult.getPos().x,mc.hitResult.getPos().y,mc.hitResult.getPos().z,100.0F);
						}
						}
						
						}
					}
				
				
			
			if(mc.player.hurtTime > 0){
				mc.player.hurtTime = 0;
				//mc.player.setInPortal(blockPos_1);
		            Vec3d vec3d = mc.player.getRotationVecClient();
		            //vec3d = vec3d.multiply(-1);
		            //float f = MathHelper.sqrt(vec3d.x * vec3d.x + vec3d.z * vec3d.z);
		           // mc.player.setVelocity(ble_1dou, double_2, double_3); = 0;
		           // mc.player.addVelocity(, 0, vec3d.z*mc.player.getVelocity().z);
		           // mc.player.setVelocity(mc.player.getVelocity().x*Math.min(Math.max(vec3d.x*mc.player.getVelocity().x, 1),0), mc.player.getVelocity().y, mc.player.getVelocity().z*Math.min(Math.max(vec3d.z*mc.player.getVelocity().z, 1),0));
					//mc.player.setVelocity(0, mc.player.getVelocity().y, 0);
		           // if(!lastvelos.isEmpty()){
		            	//mc.player.setVelocity(lastvelos.get(lastvelos.size()-1));
		            	//lastvelos.clear();
		            //}else{
		            	mc.player.setVelocity(mc.player.getVelocity().x*Math.min(Math.max(vec3d.x*mc.player.getVelocity().x, 1),0), mc.player.getVelocity().y, mc.player.getVelocity().z*Math.min(Math.max(vec3d.z*mc.player.getVelocity().z, 1),0));
		           // }
		            //mc.player.addVelocity(-(vec3d.x / (double)f * (double)0.4), 0, -(vec3d.z / (double)f * (double)0.4));
		           /*
		            if (mc.player.onGround)
		            {
		            	mc.player.setVelocity(mc.player.getVelocity().x, mc.player.getVelocity().y / 2.0D, mc.player.getVelocity().z);
		            	mc.player.setVelocity(mc.player.getVelocity().x, mc.player.getVelocity().y + 0.4, mc.player.getVelocity().z);

		                if (mc.player.getVelocity().y > 0.4000000059604645D)
		                {
		                	mc.player.setVelocity(mc.player.getVelocity().x, 0.4000000059604645D, mc.player.getVelocity().z);
		                }
		            }
		            
		        */
			}else{
				//lastvelos.add(mc.player.getVelocity());
				//if(lastvelos.size() > 10){
				//	lastvelos.remove(0);
				//}
			}
			}else{
				
				if(fbright){
			
					fbright = false;
				}else if(xray){
				
					xray = false;
				}
			}
		});
	}
}
