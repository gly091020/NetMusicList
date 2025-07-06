package com.gly091020.mixin;

import com.github.tartaricacid.netmusic.init.InitItems;
import com.github.tartaricacid.netmusic.inventory.CDBurnerMenu;
import com.gly091020.NetMusicList;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CDBurnerMenu.class)
public class CanBurnerCDListMixin {
    @Mutable
    @Shadow @Final public Slot input;

    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/inventory/Inventory;)V", at = @At(value = "RETURN"))
    private void setSlot(int syncId, PlayerInventory playerInventory, Inventory inventory, CallbackInfo ci){
        this.input = new Slot(new SimpleInventory(1), 0, 147, 14) {
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() == InitItems.MUSIC_CD || stack.getItem() == NetMusicList.MUSIC_LIST_ITEM;
            }
        };
    }
}
