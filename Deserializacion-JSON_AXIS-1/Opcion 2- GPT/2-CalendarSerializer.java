// CalendarSerializer.java
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;
import java.util.Calendar;

public class CalendarSerializer extends JsonSerializer<Calendar> {
    private static final DateTimeFormatter ISO_FORMATTER =
        DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneOffset.UTC);

    @Override
    public void serialize(Calendar value,
                          JsonGenerator gen,
                          SerializerProvider serializers)
            throws IOException {
        Instant instant = value.toInstant();
        gen.writeString(ISO_FORMATTER.format(instant));
    }
}
