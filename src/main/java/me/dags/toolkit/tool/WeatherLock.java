package me.dags.toolkit.tool;

import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.world.ChangeWorldWeatherEvent;
import org.spongepowered.api.text.Text;

/**
 * @author dags <dags@dags.me>
 */
public class WeatherLock {

    private boolean lock = true;

    @Command(aliases = {"weatherlock", "wl"}, perm = "toolkit.weatherlock")
    public void lock(@Caller CommandSource source) {
        lock = !lock;
        source.sendMessage(Text.of("Set weather lock: " + (lock ? "on" : "off")));
    }

    @Listener
    public void onWeatherChange(ChangeWorldWeatherEvent event) {
        if (lock) {
            event.setWeather(event.getOriginalWeather());
        }
    }
}
