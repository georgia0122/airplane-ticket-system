
import javax.swing.*;
import java.awt.*;
public class LoginFrame extends JFrame {
    private JTextField tfEmail;
    private JPasswordField pfPassword;
    private UserDao userDao = new UserDao();

    public LoginFrame() {
        setTitle("飞机订票管理系统 — 登录");
        setSize(480, 300);
        setLocationRelativeTo(null);
        // 使用 DISPOSE_ON_CLOSE 避免通过关闭登录窗口导致整个应用退出
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        init();
    }

    private void init() {
        JLabel lblTitle = new JLabel("飞机订票管理系统", SwingConstants.CENTER);
        lblTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        panel.add(lblTitle, c);

        c.gridwidth = 1;
        c.gridy++;
        panel.add(new JLabel("邮箱:"), c);
        c.gridx = 1;
        tfEmail = new JTextField(18);
        panel.add(tfEmail, c);

        c.gridx = 0; c.gridy++;
        panel.add(new JLabel("密码:"), c);
        c.gridx = 1;
        pfPassword = new JPasswordField(18);
        panel.add(pfPassword, c);

        c.gridx = 0; c.gridy++;
        JButton btnLogin = new JButton("登录");
        panel.add(btnLogin, c);
        c.gridx = 1;
        JButton btnRegister = new JButton("注册新用户");
        panel.add(btnRegister, c);

        add(panel);

        btnLogin.addActionListener(e -> doLogin());
        btnRegister.addActionListener(e -> {
            RegisterFrame rf = new RegisterFrame();
            rf.setVisible(true);
        });

        // 回车触发登录
        pfPassword.addActionListener(e -> doLogin());
    }

    private void doLogin() {
        String email = tfEmail.getText().trim();
        String pwd = new String(pfPassword.getPassword()).trim();
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "邮箱不能为空，请输入邮箱（如 user@example.com）");
            return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            JOptionPane.showMessageDialog(this, "邮箱格式错误，请输入标准邮箱，如 user@example.com");
            return;
        }
        if (pwd.isEmpty()) {
            JOptionPane.showMessageDialog(this, "密码不能为空，请输入密码");
            return;
        }
            var opt = userDao.getUser(email);
            if (opt.isPresent() && opt.get().isDisabled()) {
                JOptionPane.showMessageDialog(this, "账号已被禁用，请联系管理员");
                return;
            }
            boolean ok = userDao.validateLogin(email, pwd);
        if (ok) {
            try {
                User user = userDao.findByEmail(email).get();
                MainFrame mf = new MainFrame(user);
                mf.setVisible(true);
                // 只有在主界面成功创建后才关闭登录窗口
                this.dispose();
            } catch (Exception ex) {
                // 如果创建主界面失败，保留登录窗口并提示错误，便于调试
                JOptionPane.showMessageDialog(this, "登录成功，但无法打开主界面：" + ex.getMessage());
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "登录失败：邮箱或密码错误。请检查邮箱拼写和密码，区分大小写。");
        }
    }
}