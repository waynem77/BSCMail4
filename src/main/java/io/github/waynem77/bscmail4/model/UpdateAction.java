package io.github.waynem77.bscmail4.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum UpdateAction
{
    ADD("add"),
    REMOVE("remove");

    private final String value;

    public static UpdateAction fromValue(String value)
    {
        return Arrays.stream(values())
                .filter(updateAction -> Objects.equals(updateAction.getValue(), value))
                .findFirst()
                .orElse(null);
    }
}
