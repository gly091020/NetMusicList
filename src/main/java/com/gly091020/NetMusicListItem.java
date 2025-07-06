package com.gly091020;

import com.github.tartaricacid.netmusic.init.InitBlocks;
import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.client.MusicSelectionScreen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Language;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NetMusicListItem extends ItemMusicCD {
    private static final String listKey = "NetMusicSongInfoList";
    public NetMusicListItem(Settings settings) {
        super(settings);
    }

    public static List<SongInfo> getSongInfoList(ItemStack stack) {
        if (stack.getItem() == NetMusicList.MUSIC_LIST_ITEM) {
            NbtCompound tag = stack.getOrCreateNbt();
            if(tag != null && tag.contains(listKey)){
                var l = new ArrayList<SongInfo>();
                for(Object compound: tag.getList(listKey, NbtElement.COMPOUND_TYPE).toArray()){
                    var c1 = ((NbtCompound)compound);
                    l.add(ItemMusicCD.SongInfo.deserializeNBT(c1));
                }
                return l;
            }
        }

        return new ArrayList<>();
    }

    public static void nextMusic(ItemStack stack){
        if(stack.isOf(NetMusicList.MUSIC_LIST_ITEM)){
            switch (getPlayMode(stack)){
                case RANDOM -> {
                    var i = Random.create().nextBetween(0, getSongInfoList(stack).size() - 1);
                    setSongIndex(stack, i);
                }
                case SEQUENTIAL -> {
                    var i = getSongIndex(stack) + 1;
                    if(i >= getSongInfoList(stack).size()){
                        i = 0;
                    }
                    setSongIndex(stack, i);
                }
            }
        }
    }

    public static SongInfo getSongInfo(ItemStack stack) {
        var l = getSongInfoList(stack);
        if(l.isEmpty()){
            return null;
        }
        var i = getSongIndex(stack);
        if(i >= l.size()){return null;}
        return l.get(i);
    }

    public static Integer getSongIndex(ItemStack stack){
        if (stack.getItem() == NetMusicList.MUSIC_LIST_ITEM) {
            var l = stack.getNbt();
            if(l == null) {
                var l1 = new NbtCompound();
                l1.putInt("index", 0);
                l1.put(listKey, new NbtList());
                stack.setNbt(l1);
                return 0;
            } else if (!l.contains("index")) {
                var l1 = new NbtCompound();
                l1.putInt("index", 0);
                var l2 = getSongInfoList(stack);
                var nl1 = new NbtList();
                for (SongInfo songInfo : l2) {
                    var n1 = new NbtCompound();
                    SongInfo.serializeNBT(songInfo, n1);
                    nl1.add(n1);
                }
                l1.put(listKey, nl1);
                stack.setNbt(l1);
                return 0;
            }else{
                return l.getInt("index");
            }
        }
        return -1;
    }

    public static void setSongIndex(ItemStack stack, Integer index){
        if (stack.getItem() == NetMusicList.MUSIC_LIST_ITEM) {
            var n = stack.getNbt();
            if(n == null){
                n = new NbtCompound();
            }
            n.putInt("index", index);
        }
    }

    public static ItemStack setSongInfo(SongInfo info, ItemStack stack) {
        if (stack.getItem() == NetMusicList.MUSIC_LIST_ITEM) {
            var l = getSongInfoList(stack);
            NbtCompound oldCompound = new NbtCompound();
            {
                var l1 = new NbtList();
                for(SongInfo songInfo: l){
                    var n1 = new NbtCompound();
                    SongInfo.serializeNBT(songInfo, n1);
                    l1.add(n1);
                }
                oldCompound.put(listKey, l1);
            }

            NbtCompound tag = stack.getOrCreateNbt();
            var l1 = getSongInfoList(stack);
            if(getSongIndex(stack) >= l1.size()){
                var nl = tag.getList(listKey, NbtElement.COMPOUND_TYPE);
                var sn = new NbtCompound();
                SongInfo.serializeNBT(info, sn);
                nl.add(sn);
                tag.put(listKey, nl);
                setSongIndex(stack, l1.size() + 1);
                stack.setNbt(tag);
                return stack;
            }
            var d = l1.get(getSongIndex(stack));
            if (d == null){
                return stack;
            }

            var nl = tag.getList(listKey, NbtElement.COMPOUND_TYPE);
            var sn = new NbtCompound();
            SongInfo.serializeNBT(info, sn);
            nl.setElement(getSongIndex(stack), sn);
            tag.put(listKey, nl);
            stack.setNbt(tag);
        }

        return stack;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        String name;
        String text;
        name = Text.translatable("tooltip.net_music_list.play_mode").getString();
        text = "§a▍ §7" + name + ": §6" + getPlayMode(stack).getName().getString();
        if(getSongInfoList(stack).isEmpty()){
            tooltip.add(Text.translatable("tooltips.netmusic.cd.empty").formatted(Formatting.RED));
        }

        tooltip.add(Text.literal(text));
        SongInfo info = getSongInfo(stack);
        Language language = Language.getInstance();
        if (info != null) {
            if(info.transName != null && !info.transName.isEmpty()){
                name = language.get("tooltips.netmusic.cd.trans_name");
                text = "§a▍ §7" + name + ": §6" + info.transName;
                tooltip.add(Text.literal(text));
            }

            if (info.artists != null && !info.artists.isEmpty()) {
                text = StringUtils.join(info.artists, " | ");
                name = language.get("tooltips.netmusic.cd.artists");
                text = "§a▍ §7" + name + ": §3" + text;
                tooltip.add(Text.literal(text));
            }

            name = language.get("tooltips.netmusic.cd.time");
            text = "§a▍ §7" + name + ": §5" + this.getSongTime(info.songTime);
            tooltip.add(Text.literal(text));
        }
    }

    private String getSongTime(int songTime) {
        int min = songTime / 60;
        int sec = songTime % 60;
        String minStr = min <= 9 ? "0" + min : "" + min;
        String secStr = sec <= 9 ? "0" + sec : "" + sec;
        String format = Language.getInstance().get("tooltips.netmusic.cd.time.format");
        return String.format(format, minStr, secStr);
    }

    public static PlayMode getPlayMode(ItemStack stack){
        var n = stack.getNbt();
        if(n == null || !n.contains("play_mode")){setPlayMode(stack, PlayMode.LOOP);return PlayMode.LOOP;}
        return PlayMode.getMode(n.getInt("play_mode"));
    }

    public static void setPlayMode(ItemStack stack, PlayMode mode){
        var n = stack.getNbt();
        if(n == null){
            n = new NbtCompound();
        }
        n.putInt("play_mode", mode.ordinal());
        stack.setNbt(n);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        var stack = context.getStack();
        if(stack == null || !stack.isOf(NetMusicList.MUSIC_LIST_ITEM)){
            return ActionResult.PASS;
        }
        if(context.getWorld().getBlockState(context.getBlockPos()).getBlock().equals(InitBlocks.MUSIC_PLAYER)){
            if(getSongIndex(stack) >= getSongInfoList(stack).size()){
                setSongIndex(stack, getSongInfoList(stack).size() - 1);
            }
            return ActionResult.PASS;
        }
        if(context.getWorld().isClient){
            var l = getSongInfoList(stack);
            MusicSelectionScreen.open(l, getPlayMode(stack), getSongIndex(stack));
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public Text getName(ItemStack stack) {
        if(Objects.equals(super.getName(stack), Text.translatable(getTranslationKey()))){
            return Text.translatable("item.net_music_list.name", getSongInfoList(stack).size());
        }
        return Text.translatable("item.net_music_list.info", super.getName(stack));
    }
}
