package telegramweatherbot.parsers;

public abstract class Forecast {

    protected final static String API_KEY_TEMPLATE = "&units=metric&appid=";

    String city;
    String apiKey;

    public abstract String get();

    protected String getFormatTemp(double temp) {
        return String.format("%.0f C°", temp);
    }

    protected String getUtc(long timezone) {

        StringBuilder result = new StringBuilder();
        result.append("UTC");

        int utc = (int) (timezone / 3600);
        int utcP = (int) (timezone / 60) % 60;

        if (utc > 0) {
            result.append("+").append(utc);
        } else {
            result.append(utc);
        }

        if (utcP != 0) {
            result.append(":").append(utcP);
        }

        return result.toString();
    }

    protected String getIconTime(int hours) {
        String iconTime;
        if (5 <= hours && hours < 10) {
            iconTime = "Morning";
        } else if (10 <= hours && hours < 18) {
            iconTime = "Noon";
        } else if (18 <= hours && hours < 21) {
            iconTime = "Evening";
        } else {
            iconTime = "Night";
        }

        return WeatherUtils.weatherIconsCodes.get(iconTime);
    }

    public String getFlag(String country) {
        int flagOffset = 0x1F1E6;
        int asciiOffset = 0x41;

        int firstChar = Character.codePointAt(country, 0) - asciiOffset + flagOffset;
        int secondChar = Character.codePointAt(country, 1) - asciiOffset + flagOffset;

        String flag = new String(Character.toChars(firstChar))
                + new String(Character.toChars(secondChar));
        return flag;

    }

}
