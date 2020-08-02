package net.fabricmc.example.mixin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.example.ExampleMod;
import net.fabricmc.example.Unscrambler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatListenerHud;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.MessageType;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;

import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;

@Mixin(ChatListenerHud.class)
public class ChatListenerHudPatch {
	private static ArrayList<String> WordsOfMinecraft = new ArrayList<String>();
	private static Unscrambler WordUnscrambler;
	static {
		/*
		for(Object itm : Item.BLOCK_ITEMS.values()){
			ItemStack itemstack = new ItemStack((Item)itm);
			String DisplayName;
			DisplayName = itemstack.getName().getString();
			if(!WordsOfMinecraft.contains(DisplayName.toLowerCase())){
				System.out.println("Added: \"" + DisplayName.toLowerCase() + "\"");
				WordsOfMinecraft.add(DisplayName.toLowerCase());
			}
    	}
		*/
		Iterator<EntityType<?>> IT = Registry.ENTITY_TYPE.iterator();
		 while (IT.hasNext()){
			String DisplayName;
			DisplayName = (new TranslatableText(Util.createTranslationKey("entity", EntityType.getId(IT.next())))).asString();
			if(!WordsOfMinecraft.contains(DisplayName.toLowerCase())){
				System.out.println("Added: \"" + DisplayName.toLowerCase() + "\"");
				WordsOfMinecraft.add(DisplayName.toLowerCase());
			}
    	}
		Iterator<Item> IT2 = Registry.ITEM.iterator();
		 while (IT2.hasNext()){
			String DisplayName;
			DisplayName = new ItemStack(IT2.next()).getName().getString();
			if(!WordsOfMinecraft.contains(DisplayName.toLowerCase())){
				System.out.println("Added: \"" + DisplayName.toLowerCase() + "\"");
				WordsOfMinecraft.add(DisplayName.toLowerCase());
			}
		 }
		WordUnscrambler = new Unscrambler(WordsOfMinecraft);
		WordUnscrambler.PreprocessDictionary();
		System.out.println(WordUnscrambler.unScrambleWord("nitegra"));
	}
	@Inject(at = @At("HEAD"), method = "onChatMessage")
	public void onChatMessage(MessageType messageType, Text message, UUID senderUuid,CallbackInfo info) {
		
		if(message.getString().length() > 14 && message.getString().substring(0, 14).equals("[TryMe] Type \"")){
			ExampleMod.tosay = message.getString().substring(14,14+message.getString().substring(14).indexOf('\"'));
			ExampleMod.saytimer.reset();
			ExampleMod.gonnasay = true;
		}
		
		if(message.getString().length() > 15 && message.getString().substring(0, 15).equals("[TryMe] Decode ")){

			String tous = message.getString().substring(15,15+message.getString().substring(15).indexOf("  first")).toLowerCase();
			System.out.println("\"" + tous + "\"");
			String us = WordUnscrambler.unScrambleWord(tous);
			System.out.println("\"" + us + "\"");
			if(us.equals(tous)){
				MinecraftClient mc = MinecraftClient.getInstance();
				mc.player.sendMessage(new LiteralText(us),true);
			}else{
				ExampleMod.tosay = us;
				ExampleMod.saytimer.reset();
				ExampleMod.gonnasay = true;
			}
		}
		if(message.getString().length() > 14 && message.getString().substring(0, 14).equals("[TryMe] Solve ")){
			//System.out.println("SOLVE");
			//System.out.println(message.getString().substring(14));
			//System.out.println(message.getString().substring(14,14+message.getString().substring(14).indexOf(' ')+1));
			try{
			 ScriptEngineManager mgr = new ScriptEngineManager();
			 ScriptEngine engine = mgr.getEngineByName("JavaScript");
			 MinecraftClient mc = MinecraftClient.getInstance();
			 if(mc != null){
				ExampleMod.tosay = engine.eval(message.getString().substring(14,14+message.getString().substring(14).indexOf(' ')+1)).toString();
				ExampleMod.saytimer.reset();
				ExampleMod.gonnasay = true;
			 }
			}catch(Exception e){
				MinecraftClient mc = MinecraftClient.getInstance();
				 mc.player.sendMessage(new LiteralText(e.getMessage()),true);
			}
		}
	}
}
