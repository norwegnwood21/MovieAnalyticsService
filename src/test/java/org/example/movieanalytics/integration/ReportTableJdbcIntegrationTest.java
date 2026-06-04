package org.example.movieanalytics.integration;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReportTableJdbcIntegrationTest {

    @Test
    void shouldSaveReportAndDeleteItFromDatabase() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:report_test;DB_CLOSE_DELAY=-1")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("""
                        create table analysis_reports (
                            id bigint auto_increment primary key,
                            user_id bigint not null,
                            status varchar(50) not null,
                            genre_stats_json clob,
                            year_stats_json clob,
                            cast_stats_json clob
                        )
                        """);

                statement.executeUpdate("""
                        insert into analysis_reports(user_id, status, genre_stats_json, year_stats_json, cast_stats_json)
                        values (1, 'COMPLETED', '{"драма":2}', '{"1999":1}', '{"Actor":1}')
                        """);

                ResultSet beforeDelete = statement.executeQuery("select count(*) as cnt from analysis_reports");
                beforeDelete.next();
                assertEquals(1, beforeDelete.getInt("cnt"));

                statement.executeUpdate("delete from analysis_reports where user_id = 1");

                ResultSet afterDelete = statement.executeQuery("select count(*) as cnt from analysis_reports");
                afterDelete.next();
                assertEquals(0, afterDelete.getInt("cnt"));
            }
        }
    }
}
