package telegramweatherbot.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class HourlyForecastWeek extends Forecast{
    private final static String API_CALL_TEMPLATE = "https://api.openweathermap.org/data/2.5/forecast?q=";

    public HourlyForecastWeek(String city, String apiKey) {
        this.city = city;
        this.apiKey = apiKey;
    }
    
    
    protected String get(int daysOfWeek) {
        String json = JsonReader.readJsonFromUrl(API_CALL_TEMPLATE + city + API_KEY_TEMPLATE + apiKey);
        
        if ("404".equals(json))
            return "404";
        
        JsonElement jsonElement = JsonParser.parseString(json);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        
        JsonArray list = jsonObject.get("list").getAsJsonArray();
        
        StringBuilder sb = new StringBuilder();
        
        long timezone = jsonObject.get("city").getAsJsonObject().get("timezone").getAsLong();
        String country = jsonObject.get("city").getAsJsonObject().get("country").getAsString();
        sb.append(getFlag(country)).append(" ")
          .append('(').append(country).append(") ")
          .append(city).append(' ')
          .append(getUtc(timezone)).append('\n');
        
        SimpleDateFormat formaterDay = new SimpleDateFormat("dd-MMMM, EEEE", Locale.US);
        SimpleDateFormat formaterTime = new SimpleDateFormat("HH:mm", Locale.US);
        
        int ndays = 0;
        long curDay = 0;
        for (JsonElement jse : list) {
            if (ndays > daysOfWeek)
                break;
            
            JsonObject jo = jse.getAsJsonObject();
            
            String t = getFormatTemp(jo.get("main").getAsJsonObject().get("temp").getAsDouble());
            String w = jo.get("weather").getAsJsonArray().get(0).getAsJsonObject().get("main").getAsString();
            long dt = jo.get("dt").getAsLong();

            Date date = NormalizedDate.getNormalizedDate(dt, timezone);

            int dN = date.getDate();
            
            if (curDay != dN) {
                curDay = dN;
                ndays++;
                if (ndays > daysOfWeek)
                    break;
                
                sb.append("\n\t").append(formaterDay.format(date)).append("\n");
                    
            }
            
            sb.append(getIconTime(date.getHours())).append(' ')
              .append(formaterTime.format(date)).append(' ')
              .append(WeatherUtils.weatherIconsCodes.get("Temperature")).append(' ')
              .append(t).append(' ')
              .append(w).append(' ').append(WeatherUtils.weatherIconsCodes.get(w)).append("\n");
        }
        return sb.toString();
        
    }
    
    @Override
    public String get() {
        return get(6);
    }
    
}
