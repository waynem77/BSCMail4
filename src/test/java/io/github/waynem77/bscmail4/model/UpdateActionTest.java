package io.github.waynem77.bscmail4.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static io.github.waynem77.bscmail4.TestUtils.randomString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class UpdateActionTest
{
    @ParameterizedTest
    @MethodSource("fromValueReturnsCorrectValueArguments")
    public void fromValueReturnsCorrectValue(String value, UpdateAction expected)
    {
        assertThat(UpdateAction.fromValue(value), equalTo(expected));
    }

    @Test
    public void fromValueReturnsNullIfArgumentIsInvalid()
    {
        assertThat(UpdateAction.fromValue(randomString()), nullValue());
    }

    static Stream<Arguments> fromValueReturnsCorrectValueArguments()
    {
        return Arrays.stream(UpdateAction.values())
                .map(updateAction -> arguments(updateAction.getValue(), updateAction));
    }
}