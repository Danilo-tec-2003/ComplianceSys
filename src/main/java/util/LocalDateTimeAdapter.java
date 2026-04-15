package util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

    // Define o formato esperado no JSON (ISO 8601)
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void write(JsonWriter out, LocalDateTime value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            // Serializa (Java -> JSON String)
            out.value(value.format(formatter));
        }
    }

    @Override
    public LocalDateTime read(JsonReader in) throws IOException {
        // Desserializa (JSON String -> Java LocalDateTime)
        if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
            in.nextNull();
            return null;
        } else {
            return LocalDateTime.parse(in.nextString(), formatter);
        }
    }
}
