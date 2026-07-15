package koolkrafter5.questrep.reputation;

import java.io.IOException;

import net.minecraft.item.ItemStack;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import betterquesting.api.utils.BigItemStack;
import cpw.mods.fml.common.registry.GameRegistry;
import koolkrafter5.questrep.QuestingReputation;

class BigItemStackAdapter extends TypeAdapter<BigItemStack> {

    @Override
    public BigItemStack read(JsonReader reader) throws IOException {
        String item = reader.nextString();
        String[] input = item.split(":");
        if (input.length < 2) {
            QuestingReputation.log
                .warn("Unable to read item stack \"{}\". Valid format is \"modid:name(:meta optional)\"", item);
            return null;
        }
        ItemStack itemStack = GameRegistry.findItemStack(input[0], input[1], 1);
        if (itemStack == null) {
            QuestingReputation.log.warn("Unable to find item stack \"{}\".", item);
            return null;
        }
        if (input.length < 3) {
            return new BigItemStack(itemStack);
        }
        try {
            itemStack.setItemDamage(Integer.parseInt(input[2]));
        } catch (NumberFormatException e) {
            QuestingReputation.log.warn("Invalid metadata value {} for item stack {}:{}", input[2], input[0], input[1]);
        }
        return new BigItemStack(itemStack);
    }

    @Override
    public void write(JsonWriter writer, BigItemStack item) throws IOException {
        // Not implemented because it is not needed.
    }
}
