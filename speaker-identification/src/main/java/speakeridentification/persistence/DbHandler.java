package speakeridentification.persistence;

import static java.sql.DriverManager.getConnection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AllArgsConstructor;
import speakeridentification.App;

@AllArgsConstructor
public class DbHandler {

    private static final Logger log = LoggerFactory.getLogger(DbHandler.class);
    private final String connectionString;

    public void initialize() {

        try (Connection conn = getConnection(connectionString);
            Statement statement = conn.createStatement()) {
            if (!tableExists("profile", conn)) {
                String create = "CREATE TABLE profile ("
                    + "id integer not null primary key GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                    + "name varchar(40) not null unique, "
                    + "type integer not null constraint type_const check (type in (0, 1, 2, 3)))";
                statement.execute(create);
                log.info("Created table 'profile'");
            } else {
                log.info("Table 'profile' already exists");
            }
        } catch (SQLException e) {
            log.error("Error creating table 'profile'", e);
            System.exit(1);
        }

        try (Connection conn = getConnection(connectionString);
            Statement statement = conn.createStatement()) {
            if (!tableExists("audio", conn)) {
                String create = "CREATE TABLE audio ("
                    + "id integer not null primary key GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                    + "profile_id integer not null references profile(id) ON DELETE CASCADE, "
                    + "training_data boolean, "
                    + "content blob not null)";
                statement.execute(create);
                log.info("Created table 'audio'");
            } else {
                log.info("Table 'audio' already exists");
            }
        } catch (SQLException e) {
            log.error("Error creating table 'audio'", e);
            System.exit(1);
        }
    }

    private static boolean tableExists(String tableName, Connection conn)
        throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        ResultSet result = meta.getTables(null, null, tableName.toUpperCase(), null);
        return result.next();
    }

    public static void shutdown() {
        String[] split = App.properties.getProperty("connectionString").split(";");
        String shutdownString = split[0] + ";shutdown=true";
        try {
            DriverManager.getConnection(shutdownString);
        } catch (SQLException e) {
            if (e.getSQLState().equals("XJ015") || e.getSQLState().equals("08006")) {
                log.info("Derby is shut down successfully");
            } else {
                log.error("Derby was not shut down correctly", e);
            }
        }
    }
}
