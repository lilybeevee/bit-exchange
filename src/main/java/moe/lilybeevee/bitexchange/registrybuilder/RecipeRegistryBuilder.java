package moe.lilybeevee.bitexchange.registrybuilder;

import com.google.common.collect.Lists;
import moe.lilybeevee.bitexchange.BitExchange;
import moe.lilybeevee.bitexchange.api.BitInfo;
import moe.lilybeevee.bitexchange.api.BitRegistry;
import moe.lilybeevee.bitexchange.api.BitRegistryBuilder;
import moe.lilybeevee.bitexchange.api.BitStorageItem;
import moe.lilybeevee.bitexchange.mixin.SmithingRecipeMixin;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.Level;

import java.util.*;
import java.util.function.Function;

public class RecipeRegistryBuilder implements BitRegistryBuilder {
    public static final int RESEARCH_DEFAULT = 4;
    private static final HashSet<Recipe<Inventory>> processedRecipes = new HashSet<>();
    private static final HashMap<Item, Double> processedItems = new HashMap<>();
    private static final HashMap<Item, List<Recipe<Inventory>>> recipeMap = new HashMap<>();
    private static final List<RecipeType> recipeTypes = Lists.newArrayList();
    private static final HashMap<RecipeType, Function<Recipe<Inventory>, List<Ingredient>>> ingredientGetters = new HashMap<>();

    static {
        setIngredientGetter(RecipeType.SMITHING, (recipe) ->
                Lists.newArrayList(((SmithingRecipeMixin)recipe).getBase(), ((SmithingRecipeMixin)recipe).getAddition()));
    }

    public static void setIngredientGetter(RecipeType recipeType, Function<Recipe<Inventory>, List<Ingredient>> getter) {
        ingredientGetters.put(recipeType, getter);
    }

    @Override
    public void build(MinecraftServer server) {
        for (RecipeType recipeType : Registry.RECIPE_TYPE) {
            processRecipeType(server, recipeType);
        }
        for (Item item : recipeMap.keySet()) {
            processItem(item);
        }
        processedRecipes.clear();
        processedItems.clear();
        recipeMap.clear();
    }

    private void processRecipeType(MinecraftServer server, RecipeType recipeType) {
        List<Recipe> list = server.getRecipeManager().listAllOfType(recipeType);
        for (Recipe recipe : list) {
            Item item = recipe.getOutput().getItem();
            if (BitRegistry.get(item) == 0) {
                if (!recipeMap.containsKey(item)) {
                    recipeMap.put(item, Lists.newArrayList((Recipe<Inventory>)recipe));
                } else {
                    recipeMap.get(item).add((Recipe<Inventory>)recipe);
                }
            }
        }
    }

    private double processItem(Item item) {
        try {
            long bits = BitRegistry.get(item);
            if (bits > 0) {
                return bits;
            } else if (processedItems.containsKey(item)) {
                return processedItems.get(item);
            } else if (recipeMap.containsKey(item)) {
                double smallestBits = -1;
                for (Recipe<Inventory> recipe : recipeMap.get(item)) {
                    if (processedRecipes.contains(recipe)) {
                        continue;
                    }
                    processedRecipes.add(recipe);
                    double newBits = processItemRecipe(item, recipe);
                    if (smallestBits < 0) {
                        smallestBits = newBits;
                    } else {
                        smallestBits = Math.min(smallestBits, newBits);
                    }
                }
                smallestBits = Math.max(smallestBits, 0);
                if (Math.floor(smallestBits) == smallestBits) {
                    BitRegistry.add(item, new BitInfo((long)Math.floor(smallestBits), RESEARCH_DEFAULT, false), false);
                }
                processedItems.put(item, smallestBits);
                return smallestBits;
            }
            return 0;
        } catch (Exception e) {
            BitExchange.error("Error occured processing item " + item.getName().getString(), e);
            return 0;
        }
    }

    private double processItemRecipe(Item item, Recipe<Inventory> recipe) {
        double finalBits = 0;
        boolean failed = false;
        List<Ingredient> ingredients = ingredientGetters.getOrDefault(recipe.getType(), Recipe::getPreviewInputs).apply(recipe);
        for (Ingredient ingredient : ingredients) {
            ItemStack[] stacks = ingredient.getMatchingStacksClient();
            if (!ingredient.isEmpty() && stacks.length > 0) {
                double smallestBits = 0;
                for (ItemStack itemStack : stacks) {
                    Item currentItem = itemStack.getItem();
                    double newBits = processItem(currentItem);
                    if (currentItem.hasRecipeRemainder()) {
                        newBits = Math.max(newBits - Math.max(processItem(currentItem.getRecipeRemainder()), 0), 0);
                    }
                    if (smallestBits == 0) {
                        smallestBits = newBits;
                    } else if (newBits > 0) {
                        smallestBits = Math.min(smallestBits, newBits);
                    }
                }
                if (smallestBits == 0) {
                    failed = true;
                    break;
                }
                finalBits += smallestBits;
            }
        }
        if (!failed && finalBits > 0) {
            int count = recipe.getOutput().getCount();
            return finalBits / count;
        }
        return -1;
    }
}
