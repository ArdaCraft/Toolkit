package me.dags.toolkit.tool;

import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.annotation.Src;
import me.dags.commandbus.fmt.Fmt;
import me.dags.toolkit.Toolkit;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.world.ChangeWorldWeatherEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.weather.Weather;

/**
 * @author dags <dags@dags.me>
 */
public class WeatherLock {

    private boolean lock = true;

    @Permission("toolkit.weatherlock")
    @Command("weatherlock|wl")
    public void lock(@Src CommandSource source) {
        lock = !lock;
        source.sendMessage(Text.of("Set weather lock: " + (lock ? "on" : "off")));
    }

    @Permission("toolkit.weather")
    @Command("forecast <weather>")
    public void weather(@Src Player player, Weather weather) {
        weather(player, player.getWorld(), weather);
    }

    @Permission("toolkit.weather")
    @Command("forecast <world> <weather>")
    public void weather(@Src CommandSource source, World world, Weather weather) {
        if (lock) {
            lock = false;
            Task.builder().execute(() -> lock = true).delayTicks(2).submit(Toolkit.getInstance());
        }
        Fmt.info("Setting weather: ").stress(weather).info(" in world ").stress(world.getName()).tell(source);
        world.setWeather(weather);
    }

    @Listener
    public void onWeatherChange(ChangeWorldWeatherEvent event) {
        if (lock) {
            event.setCancelled(true);
        }
    }
}
