import java.io.*;
import java.util.*;

public class UserCsvDao {
    private static final String FILE = "users.csv";

    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("id,")) continue;
                String[] arr = line.split(",");
                User u = new User();
                u.setId(Integer.parseInt(arr[0]));
                u.setEmail(arr[1]);
                u.setFullName(arr[2]);
                u.setPasswordHash(arr[3]);
                list.add(u);
            }
        } catch (IOException e) {
            // ignore if file not exist
        }
        return list;
    }

    public boolean insert(User u) {
        List<User> list = findAll();
        int maxId = list.stream().mapToInt(User::getId).max().orElse(0);
        u.setId(maxId + 1);
        list.add(u);
        return saveAll(list);
    }

    public boolean update(User u) {
        List<User> list = findAll();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == u.getId()) {
                list.set(i, u);
                return saveAll(list);
            }
        }
        return false;
    }

    public boolean delete(int id) {
        List<User> list = findAll();
        boolean removed = list.removeIf(u -> u.getId() == id);
        if (removed) return saveAll(list);
        return false;
    }

    public Optional<User> findByEmail(String email) {
        return findAll().stream().filter(u -> u.getEmail().equals(email)).findFirst();
    }

    private boolean saveAll(List<User> list) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            pw.println("id,email,fullName,passwordHash");
            for (User u : list) {
                pw.printf("%d,%s,%s,%s\n", u.getId(), u.getEmail(), u.getFullName(), u.getPasswordHash());
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
