import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.*;

/**
 * Simple CSV -> MySQL mirror helper.
 *
 * Behavior:
 * - Reads DB configuration from `db.properties` at project root or from
 *   environment variables `DB_URL`, `DB_USER`, `DB_PASS`.
 * - If JDBC driver or connection is not available, mirroring is silently disabled.
 * - When enabled, `replaceTable` will create the table if missing (all columns TEXT)
 *   truncate it and bulk-insert the provided rows.
 */
public class DBMirror {
    private static boolean initialized = false;
    private static boolean enabled = false;
    private static String url;
    private static String user;
    private static String pass;

    private static synchronized void init() {
        if (initialized) return;
        initialized = true;
        // load properties file if exists
        Properties p = new Properties();
        try (InputStream in = new FileInputStream("db.properties")) {
            p.load(in);
            url = p.getProperty("db.url", System.getenv("DB_URL"));
            user = p.getProperty("db.user", System.getenv("DB_USER"));
            pass = p.getProperty("db.pass", System.getenv("DB_PASS"));
        } catch (Exception e) {
            // fallback to environment variables
            url = System.getenv("DB_URL");
            user = System.getenv("DB_USER");
            pass = System.getenv("DB_PASS");
        }

        if (url == null || url.trim().isEmpty()) {
            // no DB config provided
            enabled = false;
            return;
        }

        try {
            // try to load MySQL driver
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException ex) {
                // try older driver class
                try { Class.forName("com.mysql.jdbc.Driver"); } catch (ClassNotFoundException ex2) { throw ex; }
            }
            // test connection
            try (Connection c = DriverManager.getConnection(url, user, pass)) {
                enabled = c != null && !c.isClosed();
            }
        } catch (Throwable ex) {
            enabled = false;
        }
    }

    public static boolean isEnabled() {
        init();
        return enabled;
    }

    private static String safeName(String name) {
        if (name == null) return "table";
        return name.replaceAll("[^A-Za-z0-9_]", "_");
    }

    public static void replaceTable(String tableName, String[] columns, List<String[]> rows) {
        init();
        if (!enabled) return;
        String t = safeName(tableName);
        String createSql = buildCreateSql(t, columns);
        String insertSql = buildInsertSql(t, columns);
        try (Connection c = DriverManager.getConnection(url, user, pass)) {
            c.setAutoCommit(false);
            try (Statement s = c.createStatement()) {
                s.execute(createSql);
                s.execute("TRUNCATE TABLE `" + t + "`");
            }
            try (PreparedStatement ps = c.prepareStatement(insertSql)) {
                for (String[] row : rows) {
                    for (int i = 0; i < columns.length; i++) {
                        String v = "";
                        if (row != null && i < row.length && row[i] != null) v = row[i];
                        ps.setString(i + 1, v);
                    }
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            c.commit();
        } catch (Throwable ex) {
            // fail silently but print stack for debugging
            System.err.println("DBMirror: failed to mirror to MySQL: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static String buildCreateSql(String t, String[] columns) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS `").append(t).append("` (");
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) sb.append(",");
            sb.append("`").append(safeName(columns[i])).append("` TEXT");
        }
        sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        return sb.toString();
    }

    private static String buildInsertSql(String t, String[] columns) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO `").append(t).append("` (");
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) sb.append(",");
            sb.append("`").append(safeName(columns[i])).append("`");
        }
        sb.append(") VALUES (");
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) sb.append(",");
            sb.append("?");
        }
        sb.append(")");
        // Note: insert columns use header-derived safe names
        return sb.toString();
    }
}
