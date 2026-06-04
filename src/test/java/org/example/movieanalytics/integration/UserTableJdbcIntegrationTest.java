package org.example.movieanalytics.integration;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserTableJdbcIntegrationTest {

    @Test
    void shouldCreateUserTableSaveAndReadUser() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:user_test;DB_CLOSE_DELAY=-1")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                        create table app_users (
                            id bigint auto_increment primary key,
                            username varchar(100) not null,
                            email varchar(255) not null,
                            password_hash varchar(255) not null
                        )
                        """);

                statement.executeUpdate("""
                        insert into app_users(username, email, password_hash)
                        values ('marina', 'marina@example.com', 'hash')
                        """);

                ResultSet resultSet = statement.executeQuery("select username, email from app_users where username = 'marina'");
                resultSet.next();

                assertEquals("marina", resultSet.getString("username"));
                assertEquals("marina@example.com", resultSet.getString("email"));
            }
        }
    }
}
