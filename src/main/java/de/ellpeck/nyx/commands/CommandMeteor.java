package de.ellpeck.nyx.commands;

import de.ellpeck.nyx.entities.FallingMeteor;
import net.minecraft.command.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandMeteor extends CommandBase {
    @Override
    public String getName() {
        return "nyxmeteor";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "command.nyx.meteor.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 0 && args.length != 2 && args.length != 3 && args.length != 4)
            throw new WrongUsageException(this.getUsage(sender));

        double x = sender.getPosition().getX();
        double z = sender.getPosition().getZ();
        Integer size = null;
        boolean homing = false;
        if (args.length >= 2) {
            x = parseDouble(x, args[0], false);
            z = parseDouble(z, args[1], false);
            if (args.length >= 3) {
                size = parseInt(args[2], 1);
                if (args.length >= 4)
                    homing = parseBoolean(args[3]);
            }
        }

        BlockPos pos = new BlockPos(x, 0, z);
        FallingMeteor meteor = FallingMeteor.spawn(sender.getEntityWorld(), pos);
        if (size != null)
            meteor.getDataManager().set(FallingMeteor.SIZE, size);
        meteor.homing = homing;
        pos = meteor.getPosition();
        notifyCommandListener(sender, this, "command.nyx.meteor.success", pos.getX(), pos.getY(), pos.getZ());
    }
}
