package me.dags.toolkit.tool;

import me.dags.toolkit.utils.Utils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author dags <dags@dags.me>
 */
public class CommandBook {

    @Listener
    public void itemUse(InteractItemEvent.Primary.MainHand event, @Root Player player) {
        Optional<List<Text>> pages = player.getItemInHand(HandTypes.MAIN_HAND).flatMap(i -> i.get(Keys.BOOK_PAGES));
        if (pages.isPresent() && pages.get().size() > 0 && player.hasPermission("toolkit.commandbook")) {
            if (player.get(Keys.IS_SNEAKING).orElse(false)) {
                return;
            }

            StringBuilder command = new StringBuilder();

            for (Text page : pages.get()) {
                String message = page.toPlain().replaceAll("[^\\S ]+", " ");
                command.append(message);
            }

            Utils.notify(player, "Executing book command...");
            event.setCancelled(true);

            Location<World> location = event.getInteractionPoint()
                    .filter(pos -> player.getWorld().getBlockType(pos.toInt()) != BlockTypes.AIR)
                    .map(pos -> new Location<>(player.getWorld(), pos.toInt()))
                    .orElse(player.getLocation());

            CommandBookSource source = new CommandBookSource(player, location);
            Sponge.getCommandManager().process(source, command.toString());
        }
    }

    private static class CommandBookSource implements CommandSource, Locatable {

        private final CommandSource source;
        private final Location<World> location;

        private CommandBookSource(CommandSource source, Location<World> location) {
            this.source = source;
            this.location = location;
        }

        @Override
        public Location<World> getLocation() {
            return location;
        }

        @Override
        public String getName() {
            return source.getName();
        }

        @Override
        public Optional<CommandSource> getCommandSource() {
            return source.getCommandSource();
        }

        @Override
        public SubjectCollection getContainingCollection() {
            return source.getContainingCollection();
        }

        @Override
        public SubjectData getSubjectData() {
            return source.getSubjectData();
        }

        @Override
        public SubjectData getTransientSubjectData() {
            return source.getTransientSubjectData();
        }

        @Override
        public Tristate getPermissionValue(Set<Context> set, String s) {
            return source.getPermissionValue(set, s);
        }

        @Override
        public boolean isChildOf(Set<Context> set, Subject subject) {
            return source.isChildOf(set, subject);
        }

        @Override
        public List<Subject> getParents(Set<Context> set) {
            return source.getParents(set);
        }

        @Override
        public Optional<String> getOption(Set<Context> set, String s) {
            return source.getOption(set, s);
        }

        @Override
        public String getIdentifier() {
            return source.getIdentifier();
        }

        @Override
        public Set<Context> getActiveContexts() {
            return source.getActiveContexts();
        }

        @Override
        public void sendMessage(Text text) {
            source.sendMessage(text);
        }

        @Override
        public MessageChannel getMessageChannel() {
            return source.getMessageChannel();
        }

        @Override
        public void setMessageChannel(MessageChannel messageChannel) {
            source.setMessageChannel(messageChannel);
        }
    }
}
