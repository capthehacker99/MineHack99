package net.fabricmc.example;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.dimension.DimensionType;

/**
 * The primary state and logic controller for the AutoFish functionality
 * 
 * @author FreneticFeline
 *
 */
public class AutoFish {

    private MinecraftClient minecraftClient;
    private PlayerEntity player;
    private boolean notificationShownToPlayer = false;
    private long castScheduledAt = 0L;
    private long startedReelDelayAt = 0L;
    private long startedCastDelayAt = 0L;
    private boolean isFishing = false;
    private long closeWaterWakeDetectedAt = 0L;
//    private long exactWaterWakeDetectedAt = 0L;
    private long xpLastAddedAt = 0L;
    private float startedyaw = 0;
    private long closeBobberSplashDetectedAt = 0L;
//    private long exactBobberSplashDetectedAt = 0L;
    private Random rand;
    
    private static final String NOTIFICATION_TEXT_AUTOFISH_ENABLED = "AutoFish is %s.  Type 'o' while holding a fishing rod for more options";

    private static final int TICKS_PER_SECOND = 20;

    /** How long to suppress checking for a bite after starting to reel in.  If we check for a bite while reeling
        in, we may think we have a bite and try to reel in again, which will actually cause a re-cast and lose the fish */
    private static final int REEL_TICK_DELAY = 15;

    /** How long to wait after casting to check for Entity Clear.  If we check too soon, the hook entity
        isn't in the world yet, and will trigger a false alarm and cause infinite recasting. */
    private static final int CAST_TICK_DELAY = 20;

    /** When Break Prevention is enabled, how low to let the durability get before stopping or switching rods */
    private static final int AUTOFISH_BREAKPREVENT_THRESHOLD = 2;

    /** The threshold for vertical movement of the fish hook that determines when a fish is biting, if using
        the movement method of detection. 
        and the movement threshold that, combined with other factors, is a probable indicator that a fish is biting */
    private static final double MOTION_Y_THRESHOLD = -0.05d;
    private static final double MOTION_Y_MAYBE_THRESHOLD = -0.03d;
    
    /** The number of ticks to set as the "catchable delay" when Fast Fishing is enabled. *
     * (Vanilla ticksCatchableDelay is random between 20 and 80, but we seem to have trouble catching
     * it if it is less than 40) **/
    private static final int FAST_FISH_CATCHABLE_DELAY_TICKS = 40;
    private static final int FAST_FISH_DELAY_VARIANCE = 40;
    
    /** The maximum number of ticks that is is reasonable for a fish hook to be flying in the air after a cast */
    private static final int MAX_HOOK_FLYING_TIME_TICKS = 120;
    
    /** The amount of time to wait for a fish before something seems wrong and we want to recast **/
    private static final int MAX_WAITING_TIME_SECONDS = 60;
    
    /** The distance (squared) threshold for determining that a water wake is "close" to the fish Hook 
     * and "most certainly at" the fish Hook **/
    private static final double CLOSE_WATER_WAKE_THRESHOLD = 1.0d;
//    private static final double EXACT_WATER_WAKE_THRESHOLD = 0.3d;
    
    /** The number of ticks to wait after detecting a "close" or "exact" water wake before reeling in **/
    private static final int CLOSE_WATER_WAKE_DELAY_TICKS = 30;
//    private static final int EXACT_WATER_WAKE_DELAY_TICKS = 20;
    
    /** The distance (squared) threshold for determining that a bobber splash sound is "close" to the fish Hook
     * and "most certainly at" the fish Hook **/
    private static final double CLOSE_BOBBER_SPLASH_THRESHOLD = 2.0d;
//    private static final double EXACT_BOBBER_SPLASH_THRESHOLD = 0.5d;
    
    
    /*************  CONSTRUCTOR  ***************/
    
    
    public AutoFish() {
        this.minecraftClient = MinecraftClient.getInstance();
        this.rand = new Random();
    }

    
    
    /*************  EVENTS *************/
    
    /**
     * Callback from EventListener for ClientTickEvent
     */
    public void onClientTick() {
        this.player = this.minecraftClient.player;
        if (this.player != null && !this.notificationShownToPlayer) {
            showNotificationToPlayer();
        }
        if (true) {
            update();
        }
    }
    
    public void onBobberSplashDetected(float x, float y, float z) {
        if (playerHookInWater(this.player)) {
            FishingBobberEntity hook = this.player.fishHook;
//                double yDifference = Math.abs(hook.posY - y);
            // Ignore Y component when calculating distance from hook
            double xzDistanceFromHook = hook.squaredDistanceTo(x, hook.getY(), z);
            if (xzDistanceFromHook <= CLOSE_BOBBER_SPLASH_THRESHOLD) {
//                    AutoFishLogger.info("[%d] Close bobber splash at %f /  %f", getGameTime(), xzDistanceFromHook, yDifference);
                this.closeBobberSplashDetectedAt = getGameTime();
//                    if (xzDistanceFromHook <= EXACT_BOBBER_SPLASH_THRESHOLD) {
//    //                    AutoFishLogger.info("[%d] Exact bobber splash at %f /  %f", getGameTime(), xzDistanceFromHook, yDifference);
//                        this.exactBobberSplashDetectedAt = getGameTime();
//                    } 
            }
        }
    }
    
    /**
     * Update tracking state each time the player starts or stops fishing.
     * Triggered each time the player right-clicks with an item (and when a right-click
     * has been programmatically triggered).
     * 
     */
    public void onPlayerUseItem(Hand hand) {
        if (!playerIsHoldingRod()) {
            return;
        }
        // If player is holding a usable item in MAIN_HAND and a fishing rod in OFF_HAND,
        // then this function will be called twice in the same tick, one for each hand.
        // We need to ignore the non-fishing rod call.
        if (isUseOfNonRodInMainHand(hand)) {
            return;
        }
        if (!rodIsCast()) {
            // Player is casting
            resetReelDelay();
            resetCastSchedule();
            resetBiteTracking();
            this.isFishing = true;
            startCastDelay();
        } else {
            // Player is reeling in
            this.isFishing = false;
            resetCastDelay();
            // Bug in Forge that doesn't delete the fishing hook entity
//                Logger.info("fishEntity: %s", this.player.fishEntity);
            //this.player.fishHook = null;
        }
    }
    
    private boolean isUseOfNonRodInMainHand(Hand hand) {
        return hand == Hand.MAIN_HAND && !isUsableFishingRod(this.player.getStackInHand(Hand.MAIN_HAND));
    }



    /**
     * Callback from the WorldEventListener to tell us whenever a WATER_WAKE particle
     * is spawned in the world.
     * 
     * @param x
     * @param y
     * @param z
     */
    public void onWaterWakeDetected(double x, double y, double z) {
        if (this.minecraftClient != null && this.minecraftClient.player != null && playerHookInWater(this.minecraftClient.player)) {
        	FishingBobberEntity hook = this.minecraftClient.player.fishHook;
            double distanceFromHook = new BlockPos(x, y, z).getSquaredDistance(hook.getX(), hook.getY(), hook.getZ(),false);
            if (distanceFromHook <= CLOSE_WATER_WAKE_THRESHOLD) {
                if (this.closeWaterWakeDetectedAt <= 0) {
//                    AutoFishLogger.info("[%d] Close water wake at %f", getGameTime(), distanceFromHook);
                    this.closeWaterWakeDetectedAt = getGameTime();
                }
//                if (distanceFromHook <= EXACT_WATER_WAKE_THRESHOLD) {
//                    if (this.exactWaterWakeDetectedAt <=0) {
////                        AutoFishLogger.info("[%d] Exact water wake at %f", getGameTime(), distanceFromHook);
//                        this.exactWaterWakeDetectedAt = getGameTime();
//                    }
//                }
            }
        }
    }

    /**
     * Callback from the WorldEventListener to tell us whenever an XP Orb is 
     * added to the world.
     * 
     * Use this information to try to determine whether we actually caught something
     * last time we reeled in.
     * 
     * @param entity
     */
    public void onXpOrbAdded(double x, double y, double z) {
        if (this.player != null) {
            double distanceFromPlayer = this.player.getPos().squaredDistanceTo(x, y, z);
            if (distanceFromPlayer < 2.0d) {
                this.xpLastAddedAt = getGameTime();
            }
        }
    }

    
    /***********  CORE LOGIC ****************/

    
    /**
     * Update the state of everything related to AutoFish functionality,
     * and trigger appropriate actions.
     */
    
    private int isturning = -1;
    
    private void update() {
        if (this.player != null) {
            if (playerIsHoldingRod() || waitingToRecast()) {
                if ((playerHookInWater(this.player) && !isDuringReelDelay() && isFishBiting())
                        || somethingSeemsWrong()) {
                    startReelDelay();
                    reelIn();
                    scheduleNextCast();
                } else {
                	if(isDuringCastDelay()){
                		this.minecraftClient.player.yaw = MathHelper.wrapDegrees(this.minecraftClient.player.yaw+2F);
                	}
                	if (isTimeToCast()) {
	                    if (!rodIsCast()) {
	                        if (needToSwitchRods()) {
	                            tryToSwitchRods();
	                        }
	                        if (playerCanCast()) {
	                        	startFishing();
	                        }
	                    } else {
	                       // Logger.debug("Player cast manually while recast was scheduled");
	                    }                        
	                    resetReelDelay();
	                    resetCastSchedule();
	                    resetBiteTracking();
                	}
                }
                
                if (true) {
                    checkForEntityClear();
                }
                
                checkForMissedBite();
                
                /**
                 * This method works, but has been disabled in favor of the method that affects all
                 * players in the single-player world.
                 * See onServerTickEvent()
                 * 
                if (ModAutoFish.config_autofish_fastFishing && playerHookInWater() && !isDuringReelDelay()) {
                    triggerBite();
                }
                */                
            } else {
                this.isFishing = false;
            }
        }
    }
    

    /***********  BITE DETECTION ****************/
    
    
    /**
     * Determine whether a fish is currently biting the player's fish hook.
     * Different methods are used for single player and multiplayer.
     * 
     * @return
     */
    private boolean isFishBiting() {
        //PlayerEntity serverPlayerEntity = getServerPlayerEntity();
       
            /** If multiplayer, we must rely on client world conditions to guess when a bite occurs **/
            return isFishBiting_fromClientWorld();
       
    }

    /**
     * Determine whether a fish is biting the player's hook, using the server-side player entity.
     * This only works in Single Player, but is 100% accurate.
     * 
     * @param serverPlayerEntity
     * @return
     * @throws NumberFormatException
     * @throws NoSuchFieldException
     * @throws SecurityException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     */
    
    

    /**
     * Try to determine if a fish is biting the player's hook based on the state of the client world.
     * It is not 100% accurate, but it's pretty good.
     * 
     * @return
     */
    private boolean isFishBiting_fromClientWorld() {
        /**
         * Strategies:
         * (-) MOVEMENT of the fish hook entity.  This is about 85% accurate by itself, with a very low chance
         *     of false positives, if the threshold is set right.
         * (-) BOBBER_SPLASH sound played at the fish hook.  Pretty accurate indication of a bite,
         *     but event may not always trigger from remote servers, and another player's fish hook very
         *     near to our fish hook can cause a false positive.
         * (-) WATER_WAKE particle spawned near the fish hook.  Accurate indication that a bite is about
         *     to occur, but a bit of a guess as to exactly when the bite will occur.  Used alone,
         *     this may trigger a reel-in too soon or too late; and another player's fish hook
         *     very near to our fish hook can cause a false positive.
         * (-) Combination of the above.  The hook MOVEMENT method catches most of the fish, but if we see that
         *     there is a little hook movement (not enough to cross the threshold) and also another indication,
         *     then it's probably a bite, and we can get about 10% more accuracy, still with very few false positives.
         */
        return isFishBiting_fromMovement() || isFishBiting_fromBobberSound() || isFishBiting_fromWaterWake() || isFishBiting_fromAll(); 
    }
    
    private boolean isFishBiting_fromBobberSound() {
        /** If a bobber sound has been played at the fish hook, a fish is already biting **/
        if (true && this.closeBobberSplashDetectedAt > 0) {
           // Logger.debug("Detected bite by BOBBER_SPLASH");
            return true;
        }
        return false;
    }
    
    private boolean isFishBiting_fromWaterWake() {
        /** An water wake indicates a probable bite "very soon", so make sure enough time has passed **/
        if (true
                && this.closeWaterWakeDetectedAt > 0 
                && getGameTime() > this.closeWaterWakeDetectedAt + CLOSE_WATER_WAKE_DELAY_TICKS) {
           // Logger.debug("Detected bite by WATER_WAKE");
            return true;
        }
        return false;
    }

    private boolean isFishBiting_fromMovement() {
        FishingBobberEntity fishEntity = this.player.fishHook;
        if (fishEntity != null 
                // Checking for no X and Z motion prevents a false alarm when the hook is moving through the air
                && fishEntity.getVelocity().getX()  == 0 
                && fishEntity.getVelocity().getZ()  == 0 
                && fishEntity.getVelocity().getY()  < MOTION_Y_THRESHOLD) {
           // Logger.debug("Detected bite by MOVEMENT");
            return true;
        }
        return false;
    }
    
    private boolean isFishBiting_fromAll() {
        /** Assume a bit if the following conditions are true:
         * (1) There is at least a little Y motion of the fish hook
         * (2) Either (a) There has been a "close" bobber splash very recently; OR
         *            (b) A "close" water wake was detected long enough ago  
         */
        FishingBobberEntity fishEntity = this.player.fishHook;
        if (fishEntity != null 
                // Checking for no X and Z motion prevents a false alarm when the hook is moving through the air
                && fishEntity.getVelocity().getX() == 0 
                && fishEntity.getVelocity().getZ()  == 0 
                && fishEntity.getVelocity().getY()  < MOTION_Y_MAYBE_THRESHOLD) {
//            long totalWorldTime = getGameTime();
            if (recentCloseBobberSplash() || recentCloseWaterWake()) {
                //Logger.debug("Detected bite by ALL");
                return true;
            }
        }
        return false;
    }
    
    
    /******************  STATE HELPERS  *****************/
    
    
    private boolean isDuringReelDelay() {
        return (this.startedReelDelayAt != 0 && getGameTime() < this.startedReelDelayAt + REEL_TICK_DELAY);
    }
    
    private boolean isDuringCastDelay() {
        return (this.startedCastDelayAt != 0 && getGameTime() < this.startedCastDelayAt + CAST_TICK_DELAY);
    }
    
    private boolean playerHookInWater(PlayerEntity player) {
        if (player == null || player.fishHook == null) {
            return false;
        }
        // Sometimes, particularly around the time of a bite, the hook entity comes slightly out of the
        // water block, so also check a fraction of a block distance lower to see if that is water.
        // (EntityFishHook.isInWater() seems to be completely broken in 1.13)
        BlockState hookBlockState = player.fishHook.getEntityWorld().getBlockState(new BlockPos(player.fishHook.getPos()));
        BlockState justBelowHookBlockState = player.fishHook.getEntityWorld().getBlockState(new BlockPos(player.fishHook.getX(), player.fishHook.getY() - 0.25, player.fishHook.getZ()));
        boolean hookIsInWater = hookBlockState.getMaterial() == net.minecraft.block.Material.WATER || justBelowHookBlockState.getMaterial() == net.minecraft.block.Material.WATER;
        return hookIsInWater;
    }

    public boolean playerIsHoldingRod() {
        return findActiveFishingRod() != null;
    }
    
    private boolean isUsableFishingRod(ItemStack itemStack) {
        return (itemStack != null
                && itemStack.getItem() instanceof FishingRodItem
                && itemStack.getDamage() <= itemStack.getMaxDamage());
    }

    private boolean recentCloseBobberSplash() {
        /** Close bobber sound must have been quite recent to indicate probable bite **/
        if (this.closeBobberSplashDetectedAt > 0 
                && getGameTime() < this.closeBobberSplashDetectedAt + 20) {
            return true;
        }
        return false;
    }
    
    private boolean recentCloseWaterWake() {
        /** A close water wake indicates probable bite "soon", so make sure enough time has passed **/
        if (this.closeWaterWakeDetectedAt > 0
                && getGameTime() > this.closeWaterWakeDetectedAt + CLOSE_WATER_WAKE_DELAY_TICKS) {
            return true;
        }
        return false;
    }
    
    private boolean somethingSeemsWrong() {
        if (rodIsCast() && !isDuringCastDelay() && !isDuringReelDelay() && hookShouldBeInWater()) {
            if ((playerHookInWater(this.player) || true) && waitedLongEnough()) {
               
                return true;
            }
            if (true) {
                if (hookedAnEntity()) {
            
                    return true;
                }
                if (!playerHookInWater(this.player)) {
                    
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean hookedAnEntity() {
        if (this.player.fishHook != null && this.player.fishHook.getHookedEntity() != null) {
            return true;
        }
        return false;
    }
    
    private boolean waitedLongEnough() {
        return this.startedCastDelayAt > 0 && getGameTime() > this.startedCastDelayAt + (MAX_WAITING_TIME_SECONDS * TICKS_PER_SECOND);
    }
    
    private boolean hookShouldBeInWater() {
        return this.startedCastDelayAt > 0 && getGameTime() > this.startedCastDelayAt + MAX_HOOK_FLYING_TIME_TICKS;
    }
    
    private boolean rodIsCast() {
        if (!playerIsHoldingRod()) {
            return false;
        }
        //ItemStack activeFishingRod = findActiveFishingRod();
        return false;//activeFishingRod.getItem(). //getPropertyGetter(new ResourceLocation("cast")).call(activeFishingRod, this.minecraftClient.world, this.player) > 0F;
    }
    
    private boolean needToSwitchRods() {
        return true && !playerCanCast();
    }

    private boolean isTimeToCast() {
        return (this.castScheduledAt > 0 && getGameTime() > this.castScheduledAt + (1 * TICKS_PER_SECOND));
    }
    
    private boolean waitingToRecast() {
        return (this.castScheduledAt > 0);
    }

    private boolean playerCanCast() {
        if (!playerIsHoldingRod()) {
            return false;
        } else {
            ItemStack activeFishingRod = findActiveFishingRod();
            if (activeFishingRod == null) {
                return false;
            }
    
            return true;
        }
    }
    
    private ItemStack findActiveFishingRod() {
        if (this.player == null) {
            return null;
        }
        ItemStack heldItem = this.player.getStackInHand(Hand.MAIN_HAND);
        ItemStack heldItemOffhand = this.player.getStackInHand(Hand.OFF_HAND);
        return isUsableFishingRod(heldItem) ? heldItem :
            isUsableFishingRod(heldItemOffhand) ? heldItemOffhand : null;
    }


    /******************  ACTION HELPERS  *****************/

    
    private void showNotificationToPlayer() {
       // String notification = String.format(NOTIFICATION_TEXT_AUTOFISH_ENABLED, AutoFishModConfig.autofishEnabled() ? "enabled" : "disabled");
       // this.player.sendMessage(new TextComponentString(notification));
        //this.notificationShownToPlayer = true;
    }
    
    private void reelIn() {
        playerUseRod();
        // Bug in Forge that doesn't delete the fishing hook entity
       // this.player.bobb = null;
    }

    private void startFishing() {
        if (this.xpLastAddedAt <= 0) {
           // Logger.debug("No XP found since last cast.  Maybe nothing was caught");
        }
        playerUseRod();
        startCastDelay();
    }

    private void resetCastSchedule() {
        this.castScheduledAt = 0L;
    }
    
    private void resetCastDelay() {
        this.startedCastDelayAt = 0L;
    }
    
    private void scheduleNextCast() {
        this.castScheduledAt = getGameTime();
    }

    /*
     *  Trigger a delay so we don't use the rod multiple times for the same bite,
     *  which can persist for 2-3 ticks.
     */
    private void startReelDelay() {
        this.startedReelDelayAt = getGameTime();
    }

    /*
     * Trigger a delay so that entity clear protection doesn't kick in during cast.
     */
    private void startCastDelay() {
        this.startedCastDelayAt = getGameTime();
    }

    private void resetReelDelay() {
        startedReelDelayAt = 0;
    }


    /**
     * For all players in the specified world, if they are fishing, trigger a bite.
     * 
     * @param world
     */
    
    
    /**
     * For the current player, trigger a bite on the fish hook.
     */
    @SuppressWarnings("unused")
    private void triggerBite() {
        PlayerEntity serverPlayerEntity = getServerPlayerEntity();
        if (serverPlayerEntity != null) {
            /*
             * If we are single player and have access to the server player entity, try to hack the fish hook entity
             * to make fish bite sooner.
             */
            FishingBobberEntity serverFishEntity = serverPlayerEntity.fishHook;
            int ticks = FAST_FISH_CATCHABLE_DELAY_TICKS + MathHelper.nextInt(this.rand, 0, FAST_FISH_DELAY_VARIANCE);
           // setTicksCatchableDelay(serverFishEntity, ticks);
        }
    }
    
    

    private PlayerEntity getServerPlayerEntity() {
           return null;
      
    }

    private ActionResult playerUseRod() {
        return this.minecraftClient.interactionManager.interactItem(
                this.player, 
                this.minecraftClient.world,
                isUsableFishingRod(this.player.getStackInHand(Hand.MAIN_HAND)) ? Hand.MAIN_HAND : Hand.OFF_HAND);
    }
    
//    private boolean isInOffHand(ItemStack itemStack) {
//        return findActiveFishingRod().getEquipmentSlot() == EntityEquipmentSlot.OFFHAND;
//    }
    
    private void resetBiteTracking() {
        this.xpLastAddedAt = 0L;
        this.closeWaterWakeDetectedAt = 0L;
//        this.exactWaterWakeDetectedAt = 0L;
        this.closeBobberSplashDetectedAt = 0L;
//        this.exactBobberSplashDetectedAt = 0L;
    }

    private void tryToSwitchRods() {
        PlayerInventory inventory = this.player.inventory;
        for (int i = 0; i < 9; i++) {
            ItemStack curItemStack = inventory.main.get(i);
            if (curItemStack != null 
                    && curItemStack.getItem() instanceof FishingRodItem
                ) {
                inventory.selectedSlot = i;
                break;
            }
        }
    }
    
    private void checkForEntityClear() {
//        Logger.info("Checking for Entity Clear.  isFishing: %b; fishEntity: %s", this.isFishing, this.player.fishEntity);
        if (this.isFishing && !isDuringCastDelay() && this.player.fishHook == null) {
         //   Logger.info("Entity Clear detected.  Re-casting.");
            this.isFishing = false;
            startFishing();
        }
    }
    
    private void checkForMissedBite() {
        if (playerHookInWater(this.player)) {
          if (this.closeBobberSplashDetectedAt > 0 && getGameTime() > this.closeBobberSplashDetectedAt + 45) {
             // Logger.debug("I think we missed a fish");
              resetBiteTracking();
          }
      }
    }
    
    private long getGameTime() {
        return this.minecraftClient.world.getTime();
    }

}