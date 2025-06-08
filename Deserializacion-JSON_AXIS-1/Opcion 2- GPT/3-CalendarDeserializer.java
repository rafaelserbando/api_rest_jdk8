// CalendarDeserializer.java
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class CalendarDeserializer extends JsonDeserializer<Calendar> {

    private static final DateTimeFormatter ISO_FORMATTER =
        DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneOffset.UTC);

    @Override
    public Calendar deserialize(JsonParser p,
                                DeserializationContext ctxt)
            throws IOException {
        String text = p.getText().trim();
        // Parseamos la cadena ISO a Instant
        Instant instant = Instant.from(ISO_FORMATTER.parse(text));
        // Convertimos a Calendar en UTC
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(Date.from(instant));
        return cal;
    }
}
