package net.teamfruit.signpic.command;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.teamfruit.signpic.Config;
import net.teamfruit.signpic.Log;
import net.teamfruit.signpic.information.Informations;

public class CommandVersion extends SubCommand {
	private final @Nonnull SubCommand cmdcheck;
	private final @Nonnull SubCommand cmdupdate;

	public CommandVersion() {
		super("version");
		addChildCommand(this.cmdcheck = new CommandVersion.CommandVersionCheck());
		addChildCommand(this.cmdupdate = new CommandVersion.CommandVersionUpdate());
		addChildCommand(new CommandEnableJoinBeta());
		setPermLevel(PermLevel.EVERYONE);
	}

	private class CommandEnableJoinBeta extends SubCommand {
		private CommandEnableJoinBeta() {
			super("beta");
			addChildCommand(CommandVersion.this.cmdcheck);
			addChildCommand(CommandVersion.this.cmdupdate);
			setPermLevel(PermLevel.EVERYONE);
		}

		@Override
		public void processCommandCompat(final @Nullable ICommandSender sender, final @Nullable String[] args) throws CommandException {
			Config.getConfig().informationJoinBeta.set(false);
			super.processCommandCompat(sender, args);
		}
	}

	private static class CommandVersionCheck extends SubCommand {
		private CommandVersionCheck() {
			super("check");
			setPermLevel(PermLevel.EVERYONE);
		}

		@Override
		public void processSubCommand(final @Nonnull ICommandSender sender, final @Nonnull String[] args) {
			final long cooldown = TimeUnit.HOURS.toMillis(2l);
			if (Informations.instance.shouldCheck(cooldown)) {
				Log.notice(I18n.format("signpic.versioning.check.start"));
				Informations.instance.onlineCheck(() -> Informations.instance.check());
			} else
				//final long d = System.currentTimeMillis()-Informations.instance.getLastCheck();
				// Client.notice(I18n.format("signpic.versioning.check.cooldown", TimeUnit.MILLISECONDS.toHours(d)));
				Informations.instance.check();
		}
	}

	private static class CommandVersionUpdate extends SubCommand {
		private CommandVersionUpdate() {
			super("update");
			setPermLevel(PermLevel.EVERYONE);
		}

		@Override
		public void processSubCommand(final @Nonnull ICommandSender sender, final @Nonnull String[] args) {
			// ChatBuilder.sendPlayerChat(var1, ChatBuilder.create("signpic.versioning.disabled").setStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
			final long cooldown = TimeUnit.HOURS.toMillis(2l);
			if (Informations.instance.shouldCheck(cooldown))
				Informations.instance.onlineCheck(() -> {
					if (Informations.instance.isUpdateRequired())
						Informations.instance.runUpdate();
					else
						Log.notice(I18n.format("signpic.versioning.noupdate"));
				});
			else if (Informations.instance.isUpdateRequired())
				Informations.instance.runUpdate();
			else
				Log.notice(I18n.format("signpic.versioning.noupdate"));
		}
	}
}