import java.sql.*;
import java.io.*;

public class CsvToMysqlImporter {
    private static final String URL = "jdbc:mysql://localhost:3306/flightdb?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "300241";

    public static void main(String[] args) throws Exception {
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        importUsers(conn, "users.csv");
        importFlights(conn, "flights.csv");
        importOrders(conn, "orders.csv");
        conn.close();
        System.out.println("导入完成");
    }

    private static void importUsers(Connection conn, String file) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // 跳过表头
            String sql = "INSERT INTO users (id, email, full_name, password_hash, role, disabled) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            while ((line = br.readLine()) != null) {
                String[] arr = line.split(",");
                ps.setInt(1, Integer.parseInt(arr[0]));
                ps.setString(2, arr[1]);
                ps.setString(3, arr[2]);
                ps.setString(4, arr[3]);
                ps.setString(5, arr.length > 4 ? arr[4] : "user");
                ps.setInt(6, arr.length > 5 ? Integer.parseInt(arr[5]) : 0);
                ps.executeUpdate();
            }
            ps.close();
        }
    }

    private static void importFlights(Connection conn, String file) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            String sql = "INSERT INTO flights (id, code, origin, destination, depart_time, arrive_time, price, seats_total, seats_left) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            while ((line = br.readLine()) != null) {
                String[] arr = line.split(",");
                ps.setInt(1, Integer.parseInt(arr[0]));
                ps.setString(2, arr[1]);
                ps.setString(3, arr[2]);
                ps.setString(4, arr[3]);
                ps.setString(5, arr[4]);
                ps.setString(6, arr[5]);
                ps.setDouble(7, Double.parseDouble(arr[6]));
                ps.setInt(8, Integer.parseInt(arr[7]));
                ps.setInt(9, Integer.parseInt(arr[8]));
                ps.executeUpdate();
            }
            ps.close();
        }
    }

    private static void importOrders(Connection conn, String file) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            String sql = "INSERT INTO orders (id, user_id, flight_id, seat_count, seat_number, order_time, status, pay_type, refund_request, reschedule_request) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            while ((line = br.readLine()) != null) {
                String[] arr = line.split(",");
                ps.setInt(1, Integer.parseInt(arr[0]));
                ps.setInt(2, Integer.parseInt(arr[1]));
                ps.setInt(3, Integer.parseInt(arr[2]));
                ps.setInt(4, Integer.parseInt(arr[3]));
                ps.setString(5, arr[4]);
                ps.setString(6, arr[5]);
                ps.setString(7, arr[6]);
                ps.setString(8, arr[7]);
                ps.setInt(9, arr.length > 8 ? Integer.parseInt(arr[8]) : 0);
                ps.setInt(10, arr.length > 9 ? Integer.parseInt(arr[9]) : 0);
                ps.executeUpdate();
            }
            ps.close();
        }
    }
}
