package com.gly091020.mixin;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.github.tartaricacid.netmusic.tileentity.TileEntityMusicPlayer;
import com.gly091020.NetMusicList;
import com.gly091020.NetMusicListItem;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityMusicPlayer.class)
public abstract class PlayModeMixin {
    @Inject(method = "tick", at = @At("RETURN"))
    private static void nextMusic(World level, BlockPos blockPos, BlockState blockState, TileEntityMusicPlayer te, CallbackInfo ci){
        if(!te.isPlay() && te.getStack(0).isOf(NetMusicList.MUSIC_LIST_ITEM)){
            ItemStack stackInSlot = te.getItems().get(0);
            if (stackInSlot.isEmpty()) {
                return;
            }

            te.setPlay(true);
            te.markDirty();
            NetMusicListItem.nextMusic(stackInSlot);
            ItemMusicCD.SongInfo songInfo = NetMusicListItem.getSongInfo(stackInSlot);
            if (songInfo != null) {
                te.setPlayToClient(songInfo);
            }
        }
    }
}
