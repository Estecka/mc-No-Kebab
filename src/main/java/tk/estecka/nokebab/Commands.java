package tk.estecka.nokebab;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.command.argument.EntityArgumentType.entities;
import static net.minecraft.command.argument.EntityArgumentType.getEntities;

public class Commands
{
	static private final String SRC_ARG = "source";
	static private final String DST_ARG = "destination";

	static private final String SUCCESS_MSG = "command.nokebab.migrate.success";
	static private final String FAILURE_MSG = "command.nokebab.migrate.failure";

	static public void	Register(){
		CommandRegistrationCallback.EVENT.register(Commands::RegisterWith);
	}

	static private MutableText ServersideTranslatable(String key, Object ... args){
		return Text.translatableWithFallback(key, I18n.translate(key, args), args);
	}

	static public void RegisterWith(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, RegistrationEnvironment env){
		final var root = literal("nokebab").requires(s->s.hasPermissionLevel(3));

		final var migrate = literal("migrate");

		migrate.then(literal("literal")
			.then(argument(SRC_ARG, string())
				.then(argument(DST_ARG, string())
					.executes(Commands::MigrateLiteral)
				)
			)
		);

		migrate.then(literal("regex")
			.then(argument(SRC_ARG, string())
				.then(argument(DST_ARG, string())
					.executes(Commands::MigrateRegex)
				)
			)
		);

		root.then(migrate);
		dispatcher.register(root);
	}

	static private int MigrateLiteral(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		int r = Migration.Literal(
			getString(context, SRC_ARG),
			getString(context, DST_ARG),
			context.getSource().getWorld()
		);

		if (r > 0){
			context.getSource().sendFeedback(ServersideTranslatable(SUCCESS_MSG, r), true);
			return 1;
		}
		else{
			context.getSource().sendError(ServersideTranslatable(FAILURE_MSG));
			return 0;
		}
	}

	static private int MigrateRegex(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		Pattern regex;
		try {
			regex = Pattern.compile(getString(context, SRC_ARG));
		}
		catch (PatternSyntaxException e){
			context.getSource().sendError(Text.literal("Bad regex"));
			return -1;
		}

		int r = Migration.Regex(
			regex,
			getString(context, DST_ARG),
			context.getSource().getWorld()
		);
		if (r > 0){
			context.getSource().sendFeedback(ServersideTranslatable(SUCCESS_MSG, r), true);
			return 1;
		}
		else{
			context.getSource().sendError(ServersideTranslatable(FAILURE_MSG));
			return 0;
		}
	}

}
