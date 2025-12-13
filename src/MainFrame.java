import javax.swing.*;
import java.awt.*;


public class MainFrame extends JFrame {
    private User user;

    public MainFrame(User user) {
        this.user = user;
        String roleText = "admin".equalsIgnoreCase(user.getRole()) ? "（管理员）" : "（用户）";
        setTitle("飞机订票管理系统 - 主界面" + roleText);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initUI();
    }

    private void initUI() {
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel lblTitle = new JLabel("飞机订票管理系统" + ("admin".equalsIgnoreCase(user.getRole()) ? "（管理员）" : "（用户）"), SwingConstants.CENTER);
        lblTitle.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        topPanel.add(lblTitle, BorderLayout.CENTER);
        JLabel lblWelcome = new JLabel("欢迎，" + user.getFullName() + "（" + user.getEmail() + "）", SwingConstants.RIGHT);
        topPanel.add(lblWelcome, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        if ("admin".equalsIgnoreCase(user.getRole())) {
            AdminPanel adminPanel = new AdminPanel();
            tabbedPane.addTab("后台管理", adminPanel);
            tabbedPane.addChangeListener(e -> {
                int idx = tabbedPane.getSelectedIndex();
                if (idx == 0 && adminPanel != null) adminPanel.flightPanel.loadData();
            });
        } else {
            MyOrdersPanel myOrdersPanel = new MyOrdersPanel(user);
            BookingPanel bookingPanel = new BookingPanel(user) {
                @Override
                protected void onOrderSuccess() {
                    myOrdersPanel.loadData();
                }
            };
            ProfilePanel profilePanel = new ProfilePanel(user);
            tabbedPane.addTab("航班查询/订票", bookingPanel);
            tabbedPane.addTab("我的订单", myOrdersPanel);
            tabbedPane.addTab("个人信息", profilePanel);
            // Tab切换时自动刷新
            tabbedPane.addChangeListener(e -> {
                int idx = tabbedPane.getSelectedIndex();
                if (idx == 0) bookingPanel.doSearch();
                if (idx == 1) myOrdersPanel.loadData();
            });
        }
        add(tabbedPane, BorderLayout.CENTER);

        JButton btnLogout = new JButton("退出登录");
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(btnLogout);
        add(bottomPanel, BorderLayout.SOUTH);

        btnLogout.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this, "确定要退出吗？", "确认", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                this.dispose();
                LoginFrame lf = new LoginFrame();
                lf.setVisible(true);
            }
        });
    }
}
