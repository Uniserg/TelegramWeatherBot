package telegramweatherbot.parsers;

import java.util.HashMap;
import java.util.Map;

public class WeatherUtils {

    public final static Map<String, String> weatherIconsCodes = new HashMap<>();
    
    static {
        weatherIconsCodes.put("Clear", "☀");
        weatherIconsCodes.put("Rain", "☔");
        weatherIconsCodes.put("Snow", "❄");
        weatherIconsCodes.put("Clouds", "☁");
        weatherIconsCodes.put("Mist", "🌁️");
        weatherIconsCodes.put("Morning", "🌇");
        weatherIconsCodes.put("Noon", "🏙");
        weatherIconsCodes.put("Evening", "🌆");
        weatherIconsCodes.put("Night", "🌌");
        weatherIconsCodes.put("Temperature", "🌡");
        weatherIconsCodes.put("Visibility", "👀");
        weatherIconsCodes.put("Wind", "💨");
        
    }
}
