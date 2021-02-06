package telegramweatherbot.parsers;

public class HourlyForecast3Days extends HourlyForecastWeek {

    public HourlyForecast3Days(String city, String apiKey) {
        super(city, apiKey);
    }

    @Override
    public String get() {
        return super.get(3);
    }

}
