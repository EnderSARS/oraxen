package io.th0rgal.oraxen.font;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.th0rgal.oraxen.config.settings.Settings;
import io.th0rgal.oraxen.utils.AdventureUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import team.unnamed.creative.font.FontProvider;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;


public class Glyph {

    public static final Character WHITESPACE_GLYPH = '\ue000';

    private boolean fileChanged = false;

    private final String name;
    private final Key font;
    private final boolean isEmoji;
    private final boolean tabcomplete;
    private final Character character;
    private Key texture;
    private final int ascent;
    private final int height;
    private final String permission;
    private final String[] placeholders;
    private final BitMapEntry bitmapEntry;
    public final Pattern baseRegex;
    public final Pattern escapedRegex;

    public Glyph(final String glyphName, final ConfigurationSection glyphSection, char newChars) {
        name = glyphName;

        isEmoji = glyphSection.getBoolean("is_emoji", false);

        final ConfigurationSection chatSection = glyphSection.getConfigurationSection("chat");
        placeholders = chatSection != null ? chatSection.getStringList("placeholders").toArray(new String[0]) : new String[0];
        permission = chatSection != null ? chatSection.getString("permission", "") : "";
        tabcomplete = chatSection != null && chatSection.getBoolean("tabcomplete", false);

        String placeholderRegex = String.join("|", Arrays.stream(placeholders).map(Pattern::quote).toArray(String[]::new));
        String baseRegex = "((<(glyph|g):" + name + ")(:(c|colorable))*>" + (placeholders.length > 0 ?  "|" + placeholderRegex : "") + ")";
        this.baseRegex = Pattern.compile("(?<!\\\\)" + baseRegex);
        escapedRegex = Pattern.compile("\\\\" + baseRegex);

        if (glyphSection.contains("code")) {
            if (glyphSection.isInt("code")) glyphSection.set("char", (char) glyphSection.getInt("code"));
            glyphSection.set("code", null);
            fileChanged = true;
        }

        if (!glyphSection.contains("char") && !Settings.DISABLE_AUTOMATIC_GLYPH_CODE.toBool()) {
            glyphSection.set("char", newChars);
            fileChanged = true;
        }

        character = glyphSection.get("char") != null ? glyphSection.getString("char", "").charAt(0) : null;


        ConfigurationSection bitmapSection = glyphSection.getConfigurationSection("bitmap");
        bitmapEntry = bitmapSection != null ? new BitMapEntry(bitmapSection.getString("id"), bitmapSection.getInt("row"), bitmapSection.getInt("column")) : null;
        ascent = bitmap() != null ? bitmap().ascent() : glyphSection.getInt("ascent", 8);
        height = bitmap() != null ? bitmap().height() : glyphSection.getInt("height", 8);
        texture = bitmap() != null ? bitmap().texture() : Key.key(glyphSection.getString("texture", "required/exit_icon").replaceAll("^(?!.*\\.png$)", "") + ".png");
        font = bitmap() != null ? bitmap().font() : Key.key(glyphSection.getString("font", "minecraft:default"));
    }

    public record BitMapEntry(String id, int row, int column) {
    }

    public BitMapEntry getBitmapEntry() {
        return bitmapEntry;
    }

    public String getBitmapId() {
        return bitmapEntry != null ? bitmapEntry.id : null;
    }

    public boolean hasBitmap() {
        return getBitmapId() != null;
    }

    public boolean isBitMap() {
        return FontManager.getGlyphBitMap(getBitmapId()) != null;
    }

    public FontManager.GlyphBitMap bitmap() {
        return FontManager.getGlyphBitMap(getBitmapId());
    }

    public boolean isFileChanged() {
        return fileChanged;
    }

    public String name() {
        return name;
    }

    public String character() {
        return character != null ? character.toString() : "";
    }

    public Key texture() {
        return texture;
    }

    public void texture(Key texture) {
        this.texture = texture;
    }

    public int ascent() {
        return ascent;
    }

    public int height() {
        return height;
    }

    public String permission() {
        return permission;
    }

    public String[] placeholders() {
        return placeholders;
    }

    public boolean isEmoji() {
        return isEmoji;
    }

    public boolean hasTabCompletion() {
        return tabcomplete;
    }

    public JsonObject toJson() {
        final JsonObject output = new JsonObject();
        final JsonArray chars = new JsonArray();
        chars.add(character());
        output.add("chars", chars);
        output.addProperty("file", texture.asString());
        output.addProperty("ascent", ascent);
        output.addProperty("height", height);
        output.addProperty("type", "bitmap");
        return output;
    }

    public FontProvider fontProvider() {
        return FontProvider.bitMap()
                .file(texture)
                .ascent(ascent)
                .height(height)
                .characters(List.of(character.toString()))
                .build();
    }

    public boolean hasPermission(Player player) {
        return player == null || permission.isEmpty() || player.hasPermission(permission);
    }

    /**
     * Useful to easily get the MiniMessage-tag for a glyph
     */
    public String glyphTag() {
        return '<' + "glyph:" + name + '>';
    }

    public Component glyphComponent() {
        return Component.textOfChildren(Component.text(character, NamedTextColor.WHITE).font(font).hoverEvent(glyphHoverText()));
    }

    @Nullable
    public HoverEventSource glyphHoverText() {
        String hoverText = Settings.GLYPH_HOVER_TEXT.toString();
        TagResolver hoverResolver = TagResolver.builder().tag("glyph_placeholder", Tag.selfClosingInserting(Component.text(Arrays.stream(placeholders).findFirst().orElse("")))).build();
        Component hoverComponent = AdventureUtils.MINI_MESSAGE.deserialize(hoverText, hoverResolver);
        if (hoverText.isEmpty() || hoverComponent == Component.empty()) return null;
        return HoverEvent.showText(hoverComponent);
    }

    public Key font() {
        return font;
    }
}
