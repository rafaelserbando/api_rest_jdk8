
// TransactionPOJO.java
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Calendar;

public class TransactionPOJO {

    @JsonDeserialize(using = CalendarDeserializer.class)
    @JsonSerialize(using = CalendarSerializer.class)
    private Calendar fechaTransaccion;

    public Calendar getFechaTransaccion() {
        return fechaTransaccion;
    }

    public void setFechaTransaccion(Calendar fechaTransaccion) {
        this.fechaTransaccion = fechaTransaccion;
    }

    // … otros campos, getters y setters …
}
