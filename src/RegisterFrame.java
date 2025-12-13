
import javax.swing.*;
import java.awt.*;

public class RegisterFrame extends JFrame {
    private JTextField tfEmail, tfFullName;
    private JPasswordField pfPassword, pfConfirm;
    private UserDao userDao = new UserDao();

    public RegisterFrame() {
        setTitle("注册新用户");
        setSize(520, 380);
        setLocationRelativeTo(null);
        init();
    }

    private void init() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.gridx=0; c.gridy=0;
        panel.add(new JLabel("全名:"), c);
        c.gridx=1;
        tfFullName = new JTextField(18);
        panel.add(tfFullName, c);

        c.gridx=0; c.gridy++;
        panel.add(new JLabel("邮箱:"), c);
        c.gridx=1;
        tfEmail = new JTextField(18);
        panel.add(tfEmail, c);

        c.gridx=0; c.gridy++;
        panel.add(new JLabel("密码:"), c);
        c.gridx=1;
        pfPassword = new JPasswordField(18);
        panel.add(pfPassword, c);

        c.gridx=0; c.gridy++;
        panel.add(new JLabel("确认密码:"), c);
        c.gridx=1;
        pfConfirm = new JPasswordField(18);
        panel.add(pfConfirm, c);

        c.gridx=0; c.gridy++;
        JButton btnRegister = new JButton("注册");
        panel.add(btnRegister, c);
        c.gridx=1;
        JButton btnCancel = new JButton("取消");
        panel.add(btnCancel, c);

        add(panel);

        btnRegister.addActionListener(e -> doRegister());
        btnCancel.addActionListener(e -> this.dispose());
    }

    private void doRegister() {
        String name = tfFullName.getText().trim();
        String email = tfEmail.getText().trim();
        String p1 = new String(pfPassword.getPassword()).trim();
        String p2 = new String(pfConfirm.getPassword()).trim();
        // 校验完整性
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "姓名不能为空，请输入您的真实姓名");
            return;
        }
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "邮箱不能为空，请输入邮箱（如 user@example.com）");
            return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            JOptionPane.showMessageDialog(this, "邮箱格式错误，请输入标准邮箱，如 user@example.com");
            return;
        }
        if (p1.isEmpty()) {
            JOptionPane.showMessageDialog(this, "密码不能为空，请输入6-20位密码");
            return;
        }
        if (p1.length() < 6 || p1.length() > 20) {
            JOptionPane.showMessageDialog(this, "密码长度应为6-20位");
            return;
        }
        if (!p1.equals(p2)) {
            JOptionPane.showMessageDialog(this, "两次密码不一致，请重新输入");
            return;
        }
        boolean ok = userDao.register(email, name, p1);
        if (ok) {
            JOptionPane.showMessageDialog(this, "注册成功，请登录");
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "注册失败：该邮箱已被注册，请更换邮箱");
        }
    }
}