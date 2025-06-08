// 2. CalendarSerializer.java
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class CalendarSerializer extends JsonSerializer<Calendar> {
    
    private static final SimpleDateFormat ISO_FORMAT = 
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    
    static {
        ISO_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    @Override
    public void serialize(Calendar value, JsonGenerator gen, SerializerProvider serializers) 
            throws IOException {
        
        if (value == null) {
            gen.writeNull();
            return;
        }
        
        // Convertir a UTC si no lo está
        Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utcCalendar.setTimeInMillis(value.getTimeInMillis());
        
        String formattedDate = ISO_FORMAT.format(utcCalendar.getTime());
        gen.writeString(formattedDate);
    }
}
