/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.bukkit.util;

import com.sk89q.util.ReflectionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommandRegistration {

    static {
      Bukkit.getServer().getHelpMap().registerHelpTopicFactory(DynamicPluginCommand.class, new DynamicPluginCommandHelpTopic.Factory());
    }

    protected final Plugin plugin;
    protected final CommandExecutor executor;
    private CommandMap fallbackCommands;

    public CommandRegistration(Plugin plugin) {
        this(plugin, plugin);
    }

    public CommandRegistration(Plugin plugin, CommandExecutor executor) {
        this.plugin = plugin;
        this.executor = executor;
    }

    public boolean register(List<CommandInfo> registered) {
        CommandMap commandMap = getCommandMap();
        if (registered == null || commandMap == null) {
            return false;
        }
        for (CommandInfo command : registered) {
            DynamicPluginCommand cmd = new DynamicPluginCommand(command.getAliases(),
                    command.getDesc(), "/" + command.getAliases()[0] + " " + command.getUsage(), executor, command.getRegisteredWith(), plugin);
            cmd.setPermissions(command.getPermissions());
            commandMap.register(plugin.getDescription().getName(), cmd);
        }
        return true;
    }

    public CommandMap getCommandMap() {
// Solar start - use proper method
        return plugin.getServer().getCommandMap();
// Solar end
    }

    public boolean unregisterCommands() {
        CommandMap commandMap = getCommandMap();
        List<String> toRemove = new ArrayList<String>();
// Solar start - use proper method
        Map<String, org.bukkit.command.Command> knownCommands = commandMap.getKnownCommands();
// Solar end
        for (Iterator<org.bukkit.command.Command> i = knownCommands.values().iterator(); i.hasNext();) {
            org.bukkit.command.Command cmd = i.next();
            if (cmd instanceof DynamicPluginCommand && ((DynamicPluginCommand) cmd).getOwner().equals(executor)) {
                i.remove();
                for (String alias : cmd.getAliases()) {
                    org.bukkit.command.Command aliasCmd = knownCommands.get(alias);
                    if (cmd.equals(aliasCmd)) {
                        // aliases.remove(alias); // Solar            
                        toRemove.add(alias);
                    }
                }
            }
        }
        for (String string : toRemove) {
            knownCommands.remove(string);
        }
        return true;
    }

}
