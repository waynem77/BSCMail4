package io.github.waynem77.bscmail4;

import java.util.UUID;

/**
 * A collection of test utilities.
 */
public class TestUtils
{
    /**
     * Returns a randomly-generated string. The string is generated from a random UUID.
     *
     * @return a randomly-generated string
     */
    public static String randomString()
    {
        return UUID.randomUUID().toString();
    }
}
