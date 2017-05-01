package me.dags.toolkit.tool;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class LongCommand {

    @Permission("toolkit.longcommand")
    @Command(alias = {"lcmd", "longcmd"})
    public void command(@Caller Player player) {
        Optional<List<Text>> pages = player.getItemInHand(HandTypes.MAIN_HAND).flatMap(i -> i.get(Keys.BOOK_PAGES));
        if (pages.isPresent() && pages.get().size() > 0) {
            StringBuilder command = new StringBuilder();
            for (Text page : pages.get()) {
                String message = page.toPlain().replaceAll("[^\\S ]+"," ");
                command.append(message);
            }
            Sponge.getCommandManager().process(player, command.toString());
        }
    }
}
