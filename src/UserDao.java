
import java.io.*;
import java.util.*;
import org.mindrot.jbcrypt.BCrypt;

public class UserDao {
    private static final String FILE = "users.csv";

    public boolean createUser(User user) {
        List<User> list = findAll();
        int maxId = list.stream().mapToInt(User::getId).max().orElse(0);
        user.setId(maxId + 1);
        if (user.getRole() == null) user.setRole("user");
        list.add(user);
        return saveAll(list);
    }

    /**
     * 迁移历史数据：把那些把管理员误设为role="disabled"的记录恢复为原始角色。
     * 启发式判断：如果 email 或 fullName 中包含 "admin" 或 id==1，则恢复为 admin，否则恢复为 user。
     * 同时把 disabled 字段设置为 true（表示账号仍处于禁用状态）。
     * 返回 [restoredAdmins, restoredUsers]
     */
    public int[] migrateDisabledRolesHeuristic() {
        List<User> list = findAll();
        int admins = 0, users = 0;
        boolean changed = false;
        for (User u : list) {
            if (u.getRole() != null && u.getRole().equals("disabled")) {
                // 标记为禁用状态
                u.setDisabled(true);
                String email = u.getEmail() == null ? "" : u.getEmail().toLowerCase();
                String name = u.getFullName() == null ? "" : u.getFullName().toLowerCase();
                if (email.contains("admin") || name.contains("admin") || u.getId() == 1) {
                    u.setRole("admin");
                    admins++;
                } else {
                    u.setRole("user");
                    users++;
                }
                changed = true;
            }
        }
        if (changed) saveAll(list);
        return new int[]{admins, users};
    }

    public Optional<User> findByEmail(String email) {
        return findAll().stream().filter(u -> u.getEmail().equals(email)).findFirst();
    }

    // 注册：接收明文密码，进行 BCrypt 哈希，并保存
    public boolean register(String email, String fullName, String plainPassword) {
        if (findByEmail(email).isPresent()) return false;
        String hashed = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        return createUser(new User(email, fullName, hashed, "user"));
    }

    // 登录验证（email + plain password）
    public boolean validateLogin(String email, String plainPassword) {
        var opt = findByEmail(email);
        if (opt.isEmpty()) return false;
        String hash = opt.get().getPasswordHash();
        return BCrypt.checkpw(plainPassword, hash);
    }

    // 获取用户完整信息
    public Optional<User> getUser(String email) {
        return findByEmail(email);
    }

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
                if (arr.length > 4) u.setRole(arr[4]);
                else u.setRole("user");
                // backward compatible: if CSV has disabled column
                if (arr.length > 5) {
                    try { u.setDisabled(Integer.parseInt(arr[5]) == 1); } catch (Exception ex) { u.setDisabled(false); }
                } else {
                    u.setDisabled(false);
                }
                // 启发式识别：如果邮箱或昵称包含 admin 或 '管理员'，在运行时默认识别为管理员
                try {
                    String email = u.getEmail() == null ? "" : u.getEmail().toLowerCase();
                    String name = u.getFullName() == null ? "" : u.getFullName().toLowerCase();
                    if (!"admin".equals(u.getRole()) && (email.contains("admin") || name.contains("admin") || name.contains("管理员") || u.getId() == 1)) {
                        u.setRole("admin");
                    }
                } catch (Exception ignore) {}
                list.add(u);
            }
        } catch (IOException e) {
            // ignore if file not exist
        }
        return list;
    }

    /**
     * 持久化将启发式识别出的 admin 账号恢复为 role=admin（会修改 CSV）。
     * 返回被设置为 admin 的数量。
     */
    public int promoteAdminsByHeuristic() {
        List<User> list = findAll();
        int promoted = 0;
        boolean changed = false;
        for (User u : list) {
            if (!"admin".equals(u.getRole())) {
                String email = u.getEmail() == null ? "" : u.getEmail().toLowerCase();
                String name = u.getFullName() == null ? "" : u.getFullName().toLowerCase();
                if (email.contains("admin") || name.contains("admin") || name.contains("管理员") || u.getId() == 1) {
                    u.setRole("admin");
                    promoted++;
                    changed = true;
                }
            }
        }
        if (changed) saveAll(list);
        return promoted;
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

    private boolean saveAll(List<User> list) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            pw.println("id,email,fullName,passwordHash,role,disabled");
            for (User u : list) {
                pw.printf("%d,%s,%s,%s,%s,%d\n", u.getId(), u.getEmail(), u.getFullName(), u.getPasswordHash(), u.getRole(), u.isDisabled() ? 1 : 0);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}