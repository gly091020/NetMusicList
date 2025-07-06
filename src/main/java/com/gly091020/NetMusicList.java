package com.gly091020;

import com.github.tartaricacid.netmusic.init.InitItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class NetMusicList implements ModInitializer {
    public static final String ModID = "net_music_list";
    public static final NetMusicListItem MUSIC_LIST_ITEM = new NetMusicListItem(new Item.Settings());
    @Override
    public void onInitialize() {
        Registry.register(Registries.ITEM, new Identifier(ModID, "music_list"), MUSIC_LIST_ITEM);
        ServerPlayNetworking.registerGlobalReceiver(new Identifier(ModID, "send_data"), (minecraftServer, serverPlayerEntity, serverPlayNetworkHandler, packetByteBuf, packetSender) -> {
            var stack = serverPlayerEntity.getMainHandStack();
            if(stack.isOf(MUSIC_LIST_ITEM)){
                NetMusicListItem.setSongIndex(stack, packetByteBuf.readInt());
                NetMusicListItem.setPlayMode(stack, PlayMode.getMode(packetByteBuf.readInt()));
            }
        });
        ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(RegistryKeys.ITEM_GROUP,
                new Identifier("netmusic", "netmusic_group"))).register(fabricItemGroupEntries ->
                fabricItemGroupEntries.addAfter(new ItemStack(InitItems.MUSIC_CD),
                        new ItemStack(MUSIC_LIST_ITEM)));
    }
}
