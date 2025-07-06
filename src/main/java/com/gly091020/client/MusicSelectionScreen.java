package com.gly091020.client;

import com.github.tartaricacid.netmusic.item.ItemMusicCD;
import com.gly091020.NetMusicList;
import com.gly091020.PlayMode;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class MusicSelectionScreen extends Screen {
    private final List<String> musicList;
    private static final Identifier BACKGROUND_TEXTURE = new Identifier(NetMusicList.ModID,
            "textures/gui/bg.png");
    private final int backgroundWidth = 256;
    private final int backgroundHeight = 230;
    private int left, top;
    private PlayModeButton playModeButton;
    private MusicListWidget listWidget;
    private Integer index;
    private final PlayMode mode;

    public MusicSelectionScreen(List<String> musicList, PlayMode mode, Integer index) {
        super(Text.translatable("gui.net_music_list.title"));
        this.musicList = musicList;
        this.mode = mode;
        this.index = index;
    }

    @Override
    protected void init() {
        super.init();

        // 计算UI位置（居中）
        this.left = (this.width - this.backgroundWidth) / 2;
        this.top = (this.height - this.backgroundHeight) / 2;

        // 创建音乐列表组件（非全屏）
        listWidget = new MusicListWidget();

        // 添加所有音乐条目
        for (String music : musicList) {
            listWidget.addMusicEntry(music);
        }
        listWidget.addMusicEntry(Text.translatable("gui.net_music_list.add").getString());

        listWidget.setSelected(listWidget.children().get(index));
        this.addDrawableChild(listWidget);

        // 关闭按钮
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.net_music_list.close"), button -> this.close())
                .dimensions(left + backgroundWidth / 2 - 50, top + backgroundHeight - 24, 100, 20)
                .build());
        playModeButton = new PlayModeButton(left + 10, top + backgroundHeight - 90, button -> {
            playModeButton.playMode = playModeButton.playMode.getNext();
            playModeButton.setTooltip(Tooltip.of(playModeButton.playMode.getName()));
            sendPackage();
        }, mode);
        this.addDrawableChild(playModeButton);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 渲染半透明背景
        this.renderBackground(context);
        var fontHeight = MinecraftClient.getInstance().textRenderer.fontHeight;

        // 渲染背景
        context.drawTexture(BACKGROUND_TEXTURE, left, top, 0, 0, backgroundWidth, backgroundHeight);

        // 渲染标题
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title,
                left + backgroundWidth / 2,
                top + 6,
                0x404040
        );

        context.drawText(
                this.textRenderer,
                Text.translatable("gui.net_music_list.play_list"),
                left + 10,
                top + 6 + fontHeight + 6,
                0x000000, false
        );

        super.render(context, mouseX, mouseY, delta);
    }

    private class MusicListEntry extends AlwaysSelectedEntryListWidget.Entry<MusicListEntry> {
        private final String musicName;

        public MusicListEntry(String musicName) {
            this.musicName = musicName;
        }

        @Override
        public Text getNarration() {
            return Text.literal(musicName);
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            // 渲染背景
            if (hovered) {
                context.fill(x, y, x + entryWidth - 3, y + entryHeight, 0x80FFFFFF);
            }

            // 渲染文本
            context.drawTextWithShadow(
                    textRenderer,
                    Text.literal(musicName),
                    x + 5,
                    y + (entryHeight - 10) / 2,
                    0xFFFFFF
            );
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            MusicSelectionScreen.this.index = MusicSelectionScreen.this.listWidget.getSelectedIndex();
            sendPackage();
            return true;
        }
    }

    public void sendPackage(){
        this.index = listWidget.getSelectedIndex();
        var p = PacketByteBufs.create();
        p.writeInt(index);
        p.writeInt(playModeButton.playMode.ordinal());
        ClientPlayNetworking.send(new Identifier(NetMusicList.ModID, "send_data"), p);
    }

    private class MusicListWidget extends AlwaysSelectedEntryListWidget<MusicListEntry> {
        public MusicListWidget() {
            super(MusicSelectionScreen.this.client, backgroundWidth - 10,
                    backgroundHeight - 50, MusicSelectionScreen.this.top + 24 + textRenderer.fontHeight,
                    backgroundHeight + MusicSelectionScreen.this.top - 100,
                    textRenderer.fontHeight + 1);
            this.left = MusicSelectionScreen.this.left + 5;
            this.right = backgroundWidth + MusicSelectionScreen.this.left - 5;
            this.setRenderHorizontalShadows(false);
        }

        @Override
        protected int getScrollbarPositionX() {
            return this.left + this.width - 6;
        }

        @Override
        public int getRowWidth() {
            return this.width - 16;
        }

        public void addMusicEntry(String musicName) {
            this.addEntry(new MusicListEntry(musicName));
        }

        @Override
        public void setRenderSelection(boolean renderSelection) {
            super.setRenderSelection(renderSelection);
        }

        @Override
        protected void renderBackground(DrawContext context) {
            context.fill(left, top, right, bottom, 0x000000);
        }

        public Integer getSelectedIndex(){
            return this.children().indexOf(this.getSelectedOrNull());
        }
    }

    public static void open(List<ItemMusicCD.SongInfo> musicList, PlayMode mode, Integer index) {
        var l = new ArrayList<String>();
        for(ItemMusicCD.SongInfo info: musicList){
            if(info.artists.isEmpty()){
                l.add(info.songName);
            }else {
                var a = new StringBuilder();
                for(String artist: info.artists){
                    a.append(artist);
                    a.append("、");
                }
                l.add(String.format("%s —— %s", a, info.songName));
            }
        }
        MinecraftClient.getInstance().setScreen(new MusicSelectionScreen(l, mode, index));
    }

    public static class PlayModeButton extends ButtonWidget{
        public PlayMode playMode;
        protected PlayModeButton(int x, int y, PressAction onPress, PlayMode playMode) {
            super(x, y, 22, 22, Text.empty(), onPress, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
            this.playMode = playMode;
            setTooltip(Tooltip.of(this.playMode.getName()));
        }

        @Override
        protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
            context.drawTexture(BACKGROUND_TEXTURE, this.getX(), this.getY(),
                    this.isHovered() ? 22 : 0, 230, this.width, this.height);
            var x = 0;
            switch (this.playMode){
                case SEQUENTIAL -> x = 44;
                case RANDOM -> x = 66;
                case LOOP -> x = 88;
            }
            context.drawTexture(BACKGROUND_TEXTURE, this.getX(), this.getY(),
                    x, 230, this.width, this.height);
        }
    }
}