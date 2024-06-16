package tk.estecka.nokebab;

import java.util.IllegalFormatException;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.command.argument.EntityArgumentType.entities;
import static net.minecraft.command.argument.EntityArgumentType.getEntities;
import static tk.estecka.nokebab.RegistryUtil.*;

public class Commands
{
	static private final String SRC_ARG = "source";
	static private final String DST_ARG = "destination";
	static private final String VARIANT_ARG = "variant";

	static private final String SUCCESS_MSG_LIT = "command.nokebab.migrate.success.literal";
	static private final String SUCCESS_MSG = "command.nokebab.migrate.success";
	static private final String FAILURE_MSG = "command.nokebab.migrate.failure";
	static private final String SIZEERROR_MSG = "command.nokebab.migrate.sizeError";
	static private final String BAD_REGEX_MSG = "command.nokebab.badRegex";

	static private MutableText ServersideTranslatable(String key, Object ... args){
		String fallback = Language.getInstance().get(key);
		for (int i=0; i<args.length; ++i)
			args[i] = args[i].toString();

		try {
			fallback = String.format(fallback, args);
		}
		catch(IllegalFormatException e){
			NoKebab.LOGGER.error("Could not translate {}\n{}", key, e);
		}

		return Text.translatableWithFallback(key, fallback, args);
	}


	static public void	Register(){
		CommandRegistrationCallback.EVENT.register(Commands::RegisterWith);
	}

	static private void RegisterWith(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, RegistrationEnvironment env){
		final var root = literal("nokebab").requires(s->s.hasPermissionLevel(3));

		final var migrate = literal("migrate");

		migrate.then(literal("literal")
			.then(argument(SRC_ARG, string())
				.suggests(Commands::LoadedPaintingSuggestion)
				.then(argument(DST_ARG, string())
					.suggests(Commands::ValidPaintingSuggestion)
					.executes(Commands::MigrateLiteral)
				)
			)
		);

		migrate.then(literal("regex")
			.then(argument(SRC_ARG, string())
				.then(argument(DST_ARG, string())
					.suggests(Commands::ValidPaintingSuggestion)
					.executes(Commands::MigrateRegex)
				)
			)
		);

		root.then(literal("give")
			.then(argument(VARIANT_ARG, greedyString())
				.executes(Commands::GiveVariant)
			)
		);

		root.then(migrate);
		dispatcher.register(root);
	}

	static private void	SuggestWhenAppropriate(SuggestionsBuilder builder, String suggestion){
		if (suggestion.contains(builder.getRemaining())){
			builder.suggest('"'+suggestion+'"');
		}
	}

	static private CompletableFuture<Suggestions> ValidPaintingSuggestion(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder){
		for (Identifier id : PaintingsOf(context.getSource().getWorld()).getIds())
			SuggestWhenAppropriate(builder, id.toString());
		return builder.buildFuture();
	}

	static private CompletableFuture<Suggestions> LoadedPaintingSuggestion(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder){
		for (Entity e : context.getSource().getWorld().iterateEntities())
		if  (e instanceof PaintingEntity painting)
			SuggestWhenAppropriate(builder, IPaintingEntityDuck.Of(painting).nokebab$GetState().GetIntendedName());
		return builder.buildFuture();
	}

	static int SendFeedback(CommandContext<ServerCommandSource> context, Migration.Result result, Text success){
		if (result.total() <= 0) {
			context.getSource().sendError(ServersideTranslatable(FAILURE_MSG));
			return 0;
		}

		int r = 0;
		if (result.success() > 0){
			context.getSource().sendFeedback(()->success, true);
			r = 1;
		}
		if (result.error() > 0){
			context.getSource().sendError(ServersideTranslatable(SIZEERROR_MSG, result.error()));
			r = -1;
		}

		return r;
	}

	static private int MigrateLiteral(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		String src = getString(context, SRC_ARG);
		String dst = getString(context, DST_ARG);

		ServerWorld world = context.getSource().getWorld();
		Migration.Result result = new Migration.Literal(src, dst, PaintingsOf(world)).Run(world.iterateEntities());
		return SendFeedback(context, result, ServersideTranslatable(SUCCESS_MSG_LIT, result.success(), src, dst));
	}

	static private int MigrateRegex(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		String substitution = getString(context, DST_ARG);
		Pattern regex;
		try {
			regex = Pattern.compile(getString(context, SRC_ARG));
		}
		catch (PatternSyntaxException e){
			context.getSource().sendError(ServersideTranslatable(BAD_REGEX_MSG));
			return -1;
		}

		ServerWorld world = context.getSource().getWorld();
		Migration.Result result = new Migration.Regex(regex, substitution, PaintingsOf(world)).Run(world.iterateEntities());
		return SendFeedback(context, result, ServersideTranslatable(SUCCESS_MSG, result.success()));
	}

	static private int GiveVariant(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		if (!(context.getSource().getEntity() instanceof ServerPlayerEntity player)){
			return -1;
		}

		String rawVariant = getString(context, VARIANT_ARG);
		NbtCompound entityData = new NbtCompound();
		entityData.putString("id", "minecraft:painting");
		entityData.putString("variant", rawVariant);

		ItemStack stack = new ItemStack(Items.PAINTING);
		stack.set(DataComponentTypes.ENTITY_DATA, NbtComponent.of(entityData));
	
		return player.getInventory().insertStack(stack) ? 1 : 0;
	}

}
