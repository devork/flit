package com.flit.protoc;

import com.flit.protoc.gen.GeneratorException;
import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@ToString
public class Parameter {

    private String key;
    private String value;

    public Parameter(String[] strings) {
        this.key = strings[0];
        this.value = strings[1];
    }

    public static Map<String, Parameter> of(String value) {
        if (value == null || (value = value.trim()).isEmpty()) {
            throw new GeneratorException("Empty value passed to parameter builder");
        }

        return Arrays.stream(value.split(","))
            .map(p -> p.split("=", 2))
            .map(Parameter::new)
            .collect(Collectors.toMap(Parameter::getKey, Function.identity()));
    }
}
