package demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

    @Autowired
    JdbcTemplate jdbcTemplate;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        log.info("Create a new Users table using the JDBC template");

        jdbcTemplate.execute("DROP TABLE users IF EXISTS");
        jdbcTemplate.execute("CREATE TABLE users(" +
                "id BIGINT PRIMARY KEY NOT NULL IDENTITY, " +
                "first_name VARCHAR(255), last_name VARCHAR(255), email VARCHAR(255))");

        // Split each supplied string into columns using the space symbol as a delimiter
        List<Object[]> splitUserRecords = Arrays.asList("Michael Hunger michael.hunger@gmail.com",
                "Bridget Kromhout bridget.kromhout@yahoo.com",
                "Kenny Bastani kbastani@gmail.com",
                "Josh Long jlong@gmail.com")
                .stream()
                .map(name -> name.split(" "))
                .collect(Collectors.toList());

        // Iterate through each user record and output their name
        splitUserRecords.forEach(user -> log.info(String.format("Inserting user record for %s %s",
                user[0], user[1])));

        // Use the JdbcTemplate's batchUpdate method to insert the new user records
        jdbcTemplate.batchUpdate("INSERT INTO users(first_name, last_name, email) VALUES (?,?,?)",
                splitUserRecords);

        log.info("Querying for customer records where first_name = 'Bridget':");

        // Use the JdbcTemplate query method to search for records with the first name Bridget
        jdbcTemplate.query("SELECT id, first_name, last_name, email FROM users WHERE first_name = ?",
                new Object[]{"Bridget"},
                (rs, rowNum) ->
                        new User(rs.getLong("id"),
                                rs.getString("first_name"),
                                rs.getString("last_name"),
                                rs.getString("email")))
                .forEach(customer -> log.info(customer.toString()));
    }
}
