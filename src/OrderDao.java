
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class OrderDao {
    private static final String FILE = "orders.csv";
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // 批量写入订单（用于退票/改签后重写文件）
    public boolean insertAll(List<Order> list) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            pw.println("id,userId,flightId,seatCount,seatNumber,orderTime,status,payType,refund_request,reschedule_request,requested_flight_id,requested_seat_number,request_reason");
            for (Order o : list) {
                pw.printf("%d,%d,%d,%d,%s,%s,%s,%s,%d,%d,%d,%s,%s\n",
                    o.getId(), o.getUserId(), o.getFlightId(), o.getSeatCount(),
                    o.getSeatNumber() == null ? "" : o.getSeatNumber(),
                    o.getOrderTime() == null ? "" : o.getOrderTime().format(dtf),
                    o.getStatus() == null ? "PENDING" : o.getStatus().name(),
                    o.getPayType() == null ? "NONE" : o.getPayType().name(),
                    o.isRefundRequest() ? 1 : 0,
                    o.isRescheduleRequest() ? 1 : 0,
                    o.getRequestedFlightId(),
                    o.getRequestedSeatNumber() == null ? "" : o.getRequestedSeatNumber(),
                    o.getRequestReason() == null ? "" : o.getRequestReason()
                );
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insert(Order o) {
        List<Order> list = findAll();
        int maxId = list.stream().mapToInt(Order::getId).max().orElse(0);
        o.setId(maxId + 1);
        list.add(o);
        return saveAll(list);
    }

    public List<Order> findAll() {
        List<Order> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("id,")) continue;
                try {
                    String[] arr = line.split(",");
                    if (arr.length < 8) continue;
                    Order o = new Order();
                    o.setId(Integer.parseInt(arr[0]));
                    o.setUserId(Integer.parseInt(arr[1]));
                    o.setFlightId(Integer.parseInt(arr[2]));
                    o.setSeatCount(Integer.parseInt(arr[3]));
                    o.setSeatNumber(arr[4]);
                    o.setOrderTime(LocalDateTime.parse(arr[5], dtf));
                    o.setStatus(Order.Status.valueOf(arr[6]));
                    o.setPayType(Order.PayType.valueOf(arr[7]));
                    // optional request flags
                    if (arr.length > 8) {
                        try { o.setRefundRequest(Integer.parseInt(arr[8]) == 1); } catch (Exception ex) { o.setRefundRequest(false); }
                    }
                    if (arr.length > 9) {
                        try { o.setRescheduleRequest(Integer.parseInt(arr[9]) == 1); } catch (Exception ex) { o.setRescheduleRequest(false); }
                    }
                    // optional suggested target fields
                    if (arr.length > 10) {
                        try { o.setRequestedFlightId(Integer.parseInt(arr[10])); } catch (Exception ex) { o.setRequestedFlightId(-1); }
                    }
                    if (arr.length > 11) {
                        try { o.setRequestedSeatNumber(arr[11]); } catch (Exception ex) { o.setRequestedSeatNumber(null); }
                    }
                    if (arr.length > 12) {
                        try { o.setRequestReason(arr[12]); } catch (Exception ex) { o.setRequestReason(null); }
                    }
                    list.add(o);
                } catch (Exception ex) {
                    // 忽略单行解析错误，继续读取下一行
                    ex.printStackTrace();
                }
            }
        } catch (IOException e) {
            // ignore if file not exist
        }
        return list;
    }

    public List<Order> findByUserId(int userId) {
        List<Order> all = findAll();
        List<Order> result = new ArrayList<>();
        for (Order o : all) {
            if (o.getUserId() == userId) result.add(o);
        }
        return result;
    }

    private boolean saveAll(List<Order> list) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            // include request flags and suggested target fields to keep CSV schema consistent with insertAll
            pw.println("id,userId,flightId,seatCount,seatNumber,orderTime,status,payType,refund_request,reschedule_request,requested_flight_id,requested_seat_number,request_reason");
            for (Order o : list) {
                pw.printf("%d,%d,%d,%d,%s,%s,%s,%s,%d,%d,%d,%s,%s\n",
                    o.getId(), o.getUserId(), o.getFlightId(), o.getSeatCount(),
                    o.getSeatNumber() == null ? "" : o.getSeatNumber(),
                    o.getOrderTime() == null ? "" : o.getOrderTime().format(dtf),
                    o.getStatus() == null ? "PENDING" : o.getStatus().name(),
                    o.getPayType() == null ? "NONE" : o.getPayType().name(),
                    o.isRefundRequest() ? 1 : 0,
                    o.isRescheduleRequest() ? 1 : 0,
                    o.getRequestedFlightId(),
                    o.getRequestedSeatNumber() == null ? "" : o.getRequestedSeatNumber(),
                    o.getRequestReason() == null ? "" : o.getRequestReason()
                );
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
