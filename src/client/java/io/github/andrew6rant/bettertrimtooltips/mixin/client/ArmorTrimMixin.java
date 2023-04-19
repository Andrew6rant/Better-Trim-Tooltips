package io.github.andrew6rant.bettertrimtooltips.mixin.client;

import com.mojang.serialization.DataResult;
import net.minecraft.item.ItemStack;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

import static io.github.andrew6rant.bettertrimtooltips.BetterTrimTooltips.isStackedTrimsEnabled;
import static net.minecraft.item.trim.ArmorTrim.getTrim;

@Mixin(ArmorTrim.class)
public class ArmorTrimMixin {

    @Inject(at = @At("TAIL"), method = "appendTooltip(Lnet/minecraft/item/ItemStack;Lnet/minecraft/registry/DynamicRegistryManager;Ljava/util/List;)V")
    private static void appendTooltip(ItemStack stack, DynamicRegistryManager registryManager, List<Text> tooltip, CallbackInfo info) {
        Optional<ArmorTrim> optional = getTrim(registryManager, stack);
        if (optional.isPresent()) {
            // remove all the existing trim tooltips
            // using removeIf to avoid ConcurrentModificationException
            tooltip.removeIf(text -> {
                TextContent textContent = text.getContent();
                if (textContent instanceof TranslatableTextContent) {
                    return ((TranslatableTextContent) textContent).getKey().equals("item.minecraft.smithing_template.upgrade");
                }
                if(text.getSiblings().size() == 1) {
                    TextContent siblingContext = text.getSiblings().get(0).getContent();
                    if (siblingContext instanceof TranslatableTextContent) {
                        return ((TranslatableTextContent) siblingContext).getKey().contains("trim");
                    }
                }
                return false;
            });

            if (isStackedTrimsEnabled) {       // If StackedTrims mod is installed, iterate through the list of trims
                assert stack.getNbt() != null; // this code is modified from StackedTrims' ArmorTrimItemMixin
                NbtList nbtList = stack.getNbt().getList("Trim", 10);
                for (NbtElement nbtElement : nbtList) {
                    DataResult<ArmorTrim> result = ArmorTrim.CODEC.parse(RegistryOps.of(NbtOps.INSTANCE, registryManager), nbtElement);
                    ArmorTrim armorTrim = result.result().orElse(null);
                    if (armorTrim == null) continue;
                    appendTrimTooltip(tooltip, armorTrim);
                }
            } else {
                ArmorTrim armorTrim = optional.get();
                appendTrimTooltip(tooltip, armorTrim);
            }
        }
    }

    private static void appendTrimTooltip(List<Text> tooltip, ArmorTrim armorTrim) {
        Style materialStyle = armorTrim.getMaterial().value().description().getStyle();
        tooltip.add(Text.literal("") // I am unable to start with the description, so I have to add empty text at the start to append to
                .append(armorTrim.getPattern().value().description()).formatted(Formatting.GRAY)
                .append(ScreenTexts.space()
                .append(Text.literal("(").setStyle(materialStyle)) // I made the "(" and ")" in code instead of in en_us.json to better support other languages
                .append(armorTrim.getMaterial().value().description()))
                .append(Text.literal(")").setStyle(materialStyle)));
    }
}