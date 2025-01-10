package io.github.waynem77.bscmail4;

import lombok.NonNull;

import java.util.Random;
import java.util.UUID;

/**
 * A collection of test utilities.
 */
public class TestUtils
{
    private static final Random random = new Random();

    /**
     * Returns a randomly-generated string. The string is generated from a random UUID.
     *
     * @return a randomly-generated string
     */
    public static String randomString()
    {
        return UUID.randomUUID().toString();
    }

    /**
     * Returns a randomly-generated string beginning with the given prefix.
     *
     * @param prefix the prefix; may not be null
     * @return a randomly-generated string beginning with the given prefix
     * @throws NullPointerException if prefix is null
     */
    public static String randomStringWithPrefix(@NonNull String prefix)
    {
        return prefix + "-" + randomString();
    }

    /**
     * Returns a randomly-generated long integer.
     *
     * @return a randomly-generated long integer
     */
    public static Long randomLong()
    {
        return random.nextLong();
    }
}
