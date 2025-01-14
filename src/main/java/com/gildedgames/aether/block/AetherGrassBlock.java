package com.gildedgames.aether.block;

import com.gildedgames.aether.registry.AetherBlocks;
import net.minecraft.block.material.Material;
import net.minecraft.level.Level;
import net.modificationstation.stationapi.api.registry.Identifier;
import net.modificationstation.stationapi.api.template.block.TemplateBlockBase;

import java.util.*;

public class AetherGrassBlock extends TemplateBlockBase {

    public AetherGrassBlock(Identifier identifier) {
        super(identifier, Material.DIRT);
        setTicksRandomly(true);
    }

    @Override
    public void onScheduledTick(Level level, int x, int y, int z, Random rand) {
        if(!level.isClient) {
            if (level.getLightLevel(x, y + 1, z) < 4 && level.getMaterial(x, y + 1, z).canBlockGrass()) {
                if (rand.nextInt(4) == 0) {
                    level.setTile(x, y, z, AetherBlocks.AETHER_DIRT.id);
                }
            } else if (level.getLightLevel(x, y + 1, z) >= 9) {
                int l = (x + rand.nextInt(3)) - 1;
                int i1 = (y + rand.nextInt(5)) - 3;
                int j1 = (z + rand.nextInt(3)) - 1;
                if (level.getTileId(l, i1, j1) == AetherBlocks.AETHER_DIRT.id && level.getLightLevel(l, i1 + 1, j1) >= 4 && !level.getMaterial(l, i1 + 1, j1).canBlockGrass()) {
                    level.setTile(l, i1, j1, AetherBlocks.AETHER_GRASS_BLOCK.id);
                }
            }
        }
    }
}
