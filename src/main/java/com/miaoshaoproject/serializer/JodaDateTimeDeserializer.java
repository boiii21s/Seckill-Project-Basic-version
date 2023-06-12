package com.miaoshaoproject.serializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.mysql.cj.x.protobuf.MysqlxDatatypes;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;

public class JodaDateTimeDeserializer extends JsonDeserializer<DateTime> {
    @Override
    public DateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        String dateString = p.readValueAs(String.class);
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        //反序列化时将string转成DateTime类型

        return DateTime.parse(dateString,dateTimeFormatter);
    }
}
