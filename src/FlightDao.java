
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class FlightDao {
    private static final String FILE = "flights.csv";
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public boolean insert(Flight f) {
        List<Flight> list = findAll();
        int maxId = list.stream().mapToInt(Flight::getId).max().orElse(0);
        f.setId(maxId + 1);
        list.add(f);
        return saveAll(list);
    }

    public boolean update(Flight f) {
        List<Flight> list = findAll();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == f.getId()) {
                list.set(i, f);
                return saveAll(list);
            }
        }
        return false;
    }

    public boolean delete(int id) {
        List<Flight> list = findAll();
        boolean removed = list.removeIf(f -> f.getId() == id);
        if (removed) return saveAll(list);
        return false;
    }

    public List<Flight> findAll() {
        List<Flight> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("id,")) continue;
                try {
                    String[] arr = line.split(",");
                    if (arr.length < 9) continue;
                    Flight f = new Flight();
                    f.setId(Integer.parseInt(arr[0]));
                    f.setCode(arr[1]);
                    f.setOrigin(arr[2]);
                    f.setDestination(arr[3]);
                    f.setDepartTime(LocalDateTime.parse(arr[4], dtf));
                    f.setArriveTime(LocalDateTime.parse(arr[5], dtf));
                    f.setPrice(Double.parseDouble(arr[6]));
                    f.setSeatsTotal(Integer.parseInt(arr[7]));
                    f.setSeatsLeft(Integer.parseInt(arr[8]));
                    if (arr.length >= 10) {
                        f.setDuration(Integer.parseInt(arr[9]));
                    } else {
                        f.setDuration(0);
                    }
                    list.add(f);
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

    public Optional<Flight> findById(int id) {
        return findAll().stream().filter(f -> f.getId() == id).findFirst();
    }

    public List<Flight> search(String from, String to) {
        List<Flight> list = findAll();
        List<Flight> result = new ArrayList<>();
        for (Flight f : list) {
            if (f.getOrigin().contains(from) && f.getDestination().contains(to)) {
                result.add(f);
            }
        }
        return result;
    }

    public boolean saveAll(List<Flight> list) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            pw.println("id,code,origin,destination,depart_time,arrive_time,price,seats_total,seats_left,duration");
            for (Flight f : list) {
                pw.printf("%d,%s,%s,%s,%s,%s,%.2f,%d,%d,%d\n",
                        f.getId(), f.getCode(), f.getOrigin(), f.getDestination(),
                        f.getDepartTime().format(dtf), f.getArriveTime().format(dtf),
                        f.getPrice(), f.getSeatsTotal(), f.getSeatsLeft(), f.getDuration());
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
