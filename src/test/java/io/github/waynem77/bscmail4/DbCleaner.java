package io.github.waynem77.bscmail4;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A test utility that removes test data from the database.
 * <p>
 * Best practices:
 * <ul>
 *     <li>Initialize the DbCleaner in a <code>@BeforeEach</code> method.</li>
 *     <li>Run the {@link #clean()} method in a <code>@AfterEach</code> method. This method will always be invoked,
 *     regardless of how the test method exits.</li>
 *     <li>In each test, add a call to {@link #addCleanup(String, Object)} as soon as an object is created in the
 *     database.</li>
 *     <li>If multiple objects are created by a command, make multiple calls to {@link #addCleanup(String, Object)}
 *     in the order the objects are created.</li>
 * </ul>
 * <p>
 * The DbCleaner will execute the delete commands in LIFO order.
 * <p>
 * Example
 * <pre>
 * @BeforeEach
 * {
 *     dbCleaner = new DbCleaner();
 * }
 *
 * @AfterEach
 * {
 *     dbCleaner.clean();
 * }
 *
 * @Test
 * public void test()
 * {
 *     FooEntity foo1 = createFooInDb();
 *     dbCleaner.addCleanup("foo", foo1.getId());
 *
 *     FooEntity foo2 = createFooInDb();
 *     dbCleaner.addCleanup("foo", foo2.getId());
 *
 *     BarEntity bar = createBarInDb();    // Creates a linked "baz" object.
 *     dbCleaner.addCleanup("bar", bar.getId());
 *     dbCleaner.addCleanup("baz", bar.getBaz().getId());
 * }
 * </pre>
 */
@Slf4j
public class DbCleaner
{
    @Getter
    @Setter
    @AllArgsConstructor
    private class Query
    {
        private String statement;
        private Object argument;
    }

    private final JdbcTemplate jdbcTemplate;
    private final Deque<Query> actions;

    /**
     * Creates a new DbCleaner from the given JdbcTemplate.
     *
     * @param jdbcTemplate the JdbcTemplate; may not be null
     * @throws NullPointerException if jdbcTemplate is null
     */
    public DbCleaner(@NonNull JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
        actions = new ArrayDeque<>();
    }

    /**
     * Adds a delete command to the DbCleaner. The command is of the form "DELETE FROM &lt;table&gt; WHERE ID =
     * &lt;id&gt;".
     *
     * @param table the database table for the command
     * @param id    the id of the object to remove
     * @param <T>   the type of id
     */
    public <T> void addCleanup(String table, T id)
    {
        String statement = String.format("delete from %s where id = ?", table);
        actions.push(new Query(statement, id));
    }

    /**
     * Performs all the delete commands stored in the DbCleaner, in LIFO order.
     */
    public void clean()
    {
        while (!actions.isEmpty())
        {
            Query query = actions.pop();
            log.info("Deleting from database. statement={}, id={}", query.getStatement(), query.getArgument());
            jdbcTemplate.update(query.getStatement(), query.getArgument());
        }
    }
}
