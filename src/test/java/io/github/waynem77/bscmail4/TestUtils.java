package io.github.waynem77.bscmail4;

import com.jayway.jsonpath.TypeRef;
import lombok.NonNull;
import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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
     * Returns a randomly-generated string ending with the given suffix.
     *
     * @param suffix the suffix; may not be null
     * @return a randomly-generated string ending with the given suffix
     * @throws NullPointerException if suffix is null
     */
    public static String randomStringWithSuffix(@NonNull String suffix)
    {
        return randomString() + "-" + suffix;
    }

    /**
     * Returns a randomly-generated string containing the contents within it.
     * @param contents the contents; may not be null
     * @return a randomly-generated string containing the contents
     * @throws NullPointerException if contents is null
     */
    public static String randomStringContaining(@NonNull String contents)
    {
        String randomString = randomString();
        int insertionPoint = random.nextInt(randomString.length());
        return String.format("%s-%s-%s", randomString.substring(0, insertionPoint), contents, randomString.substring(insertionPoint));
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

    /**
     * Returns a randomly-generated boolean.
     *
     * @return a randomly-generated boolean
     */
    public static boolean randomBoolean()
    {
        return random.nextBoolean();
    }

    public static Matcher<Collection<?>> equalToUnordered(Collection<?> expected)
    {
        return new BaseMatcher<>()
        {
            @Override
            public boolean matches(Object actual)
            {
                if (expected == null)
                {
                    return actual == null;
                }

                if (!(actual instanceof Collection))
                {
                    return false;
                }

                Collection<Object> actualCollection = (Collection)actual;

                Map<?, Long> expectedFrequencyMap = expected.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
                Map<?, Long> actualFrequencyMap = actualCollection.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
                return expectedFrequencyMap.equals(actualFrequencyMap);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("equal, in some order, to").appendValue(expected);
            }
        };
    }
}
