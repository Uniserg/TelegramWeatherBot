package telegramweatherbot.parsers;

import java.util.Date;
import java.util.TimeZone;

public class NormalizedDate {
    public static Date getNormalizedDate(long dt, long timezone) {
        long offset = TimeZone.getDefault().getOffset(0L);
        long normalized = (dt + timezone) * 1000 - offset;

        return new Date(normalized);
    }
}
