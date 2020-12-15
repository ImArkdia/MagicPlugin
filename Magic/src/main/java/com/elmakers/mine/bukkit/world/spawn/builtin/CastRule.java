package com.elmakers.mine.bukkit.world.spawn.builtin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.bukkit.World.Environment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import com.elmakers.mine.bukkit.world.spawn.SpawnResult;
import com.elmakers.mine.bukkit.world.spawn.SpawnRule;

public class CastRule extends SpawnRule {
    protected List<CastSpell>        spells;
    protected int                     yOffset;

    private static class CastSpell {
        protected final String            name;
        protected final String[]         parameters;

        public CastSpell(String name, String[] parameters) {
            this.name = name;
            this.parameters = parameters;
        }
    }

    @Override
    public void finalizeLoad(String worldName) {
        yOffset = parameters.getInt("y_offset", 0);
        Collection<String> spells = parameters.getStringList("spells");
        if (spells == null || spells.size() == 0) return;
        this.spells = new ArrayList<>();
        for (String spellName : spells) {
            String[] spellParameters = new String[0];
            if (spellName.contains(" ")) {
                String[] pieces = StringUtils.split(spellName, " ");
                spellName = pieces[0];
                spellParameters = new String[pieces.length - 1];
                for (int i = 1; i < pieces.length; i++) {
                    spellParameters[i - 1] = pieces[i];
                }
            }

            this.spells.add(new CastSpell(spellName, spellParameters));
            logSpawnRule("Casting " + StringUtils.join(spells, ",") + " on " + getTargetEntityTypeName() + " in " + worldName);
        }
    }

    @Override
    @Nonnull
    public SpawnResult onProcess(Plugin plugin, LivingEntity entity) {
        int y = entity.getLocation().getBlockY() + yOffset;
        if (y > 250) y = 250;
        if (entity.getWorld().getEnvironment() == Environment.NETHER && y > 118) {
            y = 118;
        }

        String[] standardParameters = {
            "pworld", entity.getLocation().getWorld().getName(),
            "px", Integer.toString(entity.getLocation().getBlockX()),
            "py", Integer.toString(y),
            "pz", Integer.toString(entity.getLocation().getBlockZ()),
            "target", "self",
            "quiet", "true"
        };

        for (CastSpell spell : spells) {
            String[] fullParameters = new String[spell.parameters.length + standardParameters.length];
            for (int index = 0; index < standardParameters.length; index++) {
                fullParameters[index] = standardParameters[index];

            }
            for (int index = 0; index < spell.parameters.length; index++) {
                fullParameters[index  + standardParameters.length] = spell.parameters[index];
            }

            controller.cast(spell.name, fullParameters);
            controller.info("Spawn rule casting: " + spell.name + " " + StringUtils.join(fullParameters, ' ') + " at " + entity.getLocation().toVector());
        }

        return SpawnResult.REMOVE;
    }
}
