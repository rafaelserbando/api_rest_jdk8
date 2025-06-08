// 1. CalendarDeserializer.java
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class CalendarDeserializer extends JsonDeserializer<Calendar> {
    
    private static final SimpleDateFormat ISO_FORMAT = 
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    
    // Formato alternativo sin milisegundos
    private static final SimpleDateFormat ISO_FORMAT_NO_MILLIS = 
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    
    // Formato solo fecha
    private static final SimpleDateFormat DATE_ONLY_FORMAT = 
        new SimpleDateFormat("yyyy-MM-dd");
    
    static {
        ISO_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        ISO_FORMAT_NO_MILLIS.setTimeZone(TimeZone.getTimeZone("UTC"));
        DATE_ONLY_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    @Override
    public Calendar deserialize(JsonParser p, DeserializationContext ctxt) 
            throws IOException {
        
        String dateStr = p.getValueAsString();
        
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        dateStr = dateStr.trim();
        
        try {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            
            // Intentar diferentes formatos
            if (dateStr.contains("T")) {
                if (dateStr.contains(".")) {
                    // Formato con milisegundos: 2024-06-07T15:30:00.000Z
                    cal.setTime(ISO_FORMAT.parse(dateStr));
                } else {
                    // Formato sin milisegundos: 2024-06-07T15:30:00Z
                    cal.setTime(ISO_FORMAT_NO_MILLIS.parse(dateStr));
                }
            } else {
                // Solo fecha: 2024-06-07
                cal.setTime(DATE_ONLY_FORMAT.parse(dateStr));
            }
            
            return cal;
            
        } catch (ParseException e) {
            throw new IOException("Error parsing date: " + dateStr + 
                ". Expected formats: yyyy-MM-dd'T'HH:mm:ss.SSS'Z', yyyy-MM-dd'T'HH:mm:ss'Z', or yyyy-MM-dd", e);
        }
    }
}
