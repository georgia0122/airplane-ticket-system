import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProfilePanel extends JPanel {
    private User user;
    private JTextField txtName;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JPasswordField txtPassword2;
    private JButton btnSave;

    public ProfilePanel(User user) {
        this.user = user;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel lblName = new JLabel("昵称:");
        txtName = new JTextField(20);
        txtName.setText(user.getFullName());
        JLabel lblEmail = new JLabel("邮箱:");
        txtEmail = new JTextField(20);
        txtEmail.setText(user.getEmail());
        JLabel lblPassword = new JLabel("新密码:");
        txtPassword = new JPasswordField(20);
        JLabel lblPassword2 = new JLabel("确认密码:");
        txtPassword2 = new JPasswordField(20);
        btnSave = new JButton("保存修改");

        gbc.gridx = 0; gbc.gridy = 0;
        add(lblName, gbc);
        gbc.gridx = 1;
        add(txtName, gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        add(lblEmail, gbc);
        gbc.gridx = 1;
        add(txtEmail, gbc);
        gbc.gridx = 0; gbc.gridy = 2;
        add(lblPassword, gbc);
        gbc.gridx = 1;
        add(txtPassword, gbc);
        gbc.gridx = 0; gbc.gridy = 3;
        add(lblPassword2, gbc);
        gbc.gridx = 1;
        add(txtPassword2, gbc);
        gbc.gridx = 1; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        add(btnSave, gbc);

        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveProfile();
            }
        });
    }

    private void saveProfile() {
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String pwd = new String(txtPassword.getPassword());
        String pwd2 = new String(txtPassword2.getPassword());
        if (name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "昵称和邮箱不能为空！");
            return;
        }
        if (!pwd.isEmpty() && !pwd.equals(pwd2)) {
            JOptionPane.showMessageDialog(this, "两次输入的密码不一致！");
            return;
        }
        try {
            UserDao userDao = new UserDao();
            user.setFullName(name);
            user.setEmail(email);
            if (!pwd.isEmpty()) {
                String hash = org.mindrot.jbcrypt.BCrypt.hashpw(pwd, org.mindrot.jbcrypt.BCrypt.gensalt());
                user.setPasswordHash(hash);
            }
            boolean ok = userDao.update(user);
            if (ok) {
                JOptionPane.showMessageDialog(this, "修改成功！");
            } else {
                JOptionPane.showMessageDialog(this, "修改失败，请重试！");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "保存时发生错误：" + ex.getMessage());
        }
    }
}
