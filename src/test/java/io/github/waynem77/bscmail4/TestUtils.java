package io.github.waynem77.bscmail4;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A collection of test utilities.
 */
public class TestUtils
{
    private static final Random random = new Random();

    /**
     * Returns a randomly-generated string. The string is generated from a random UUID, with mixed upper- and lowercase
     * letters.
     *
     * @return a randomly-generated string
     */
    public static String randomString()
    {
        return Arrays.stream(UUID.randomUUID().toString().split(""))
                .map(s -> random.nextBoolean() ? s.toUpperCase() : s.toLowerCase())
                .collect(Collectors.joining());
    }

    /**
     * Returns a randomly-generated string beginning with the given prefix. The remainder of the string is generated
     * from a random UUID, with mixed upper- and lowercase letters.
     *
     * @param prefix the prefix
     * @return a randomly-generated string beginning with the given prefix
     */
    public static String randomStringWithPrefix(String prefix)
    {
        return prefix + randomString();
    }

    /**
     * Returns a randomly-generated string ending with the given suffix. The remainder of the string is generated
     * from a random UUID, with mixed upper- and lowercase letters.
     *
     * @param suffix the suffix
     * @return a randomly-generated string ending with the given suffix
     */
    public static String randomStringWithSuffix(String suffix)
    {
        return randomString() + suffix;
    }

    /**
     * Returns a randomly-generated string containing the given text at a random index. The remainder of the string
     * is generated from a random UUID, with mixed upper- and lowercase letters.
     *
     * @param text the inserted text
     * @return a randomly-generated string containing the given text
     */
    public static String randomStringContaining(String text)
    {
        String baseString = randomString();
        int splitIndex = random.nextInt(baseString.length());
        return baseString.substring(0, splitIndex) + text + baseString.substring(0);
    }

    /**
     * Returns a randomly-generated boolean.
     *
     * @return a randomly-generated boolean
     */
    public static boolean randomBool()
    {
        return random.nextBoolean();
    }
}
