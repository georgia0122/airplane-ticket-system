import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AdminPanel extends JPanel {
    private JTabbedPane tabbedPane;
    public FlightPanel flightPanel;
    private JTable userTable;
    private JTable orderTable;
    private JButton btnExportOrders;
    private JCheckBox chkPendingRequests;

    public AdminPanel() {
        setLayout(new BorderLayout());
        tabbedPane = new JTabbedPane();
            // 航班管理
            flightPanel = new FlightPanel();
            JPanel flightPanelWrapper = new JPanel(new BorderLayout());
            flightPanelWrapper.add(flightPanel, BorderLayout.CENTER);
            JButton btnAddFlight = new JButton("新增航班");
            
            JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            addPanel.add(btnAddFlight);
            btnAddFlight.addActionListener(e -> showAddFlightDialog());
            flightPanelWrapper.add(addPanel, BorderLayout.SOUTH);
            tabbedPane.addTab("航班管理", flightPanelWrapper);
    
    
        // 用户管理
        userTable = new JTable();
        JScrollPane userScroll = new JScrollPane(userTable);
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.add(userScroll, BorderLayout.CENTER);
        JPanel userBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnDisable = new JButton("禁用/解禁");
        JButton btnResetPwd = new JButton("重置密码");
        JButton btnEditUser = new JButton("修改信息");
        userBtnPanel.add(btnDisable);
        userBtnPanel.add(btnResetPwd);
        userBtnPanel.add(btnEditUser);
        userPanel.add(userBtnPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("用户管理", userPanel);

        // 禁用/解禁（改为明确提示并显示管理员操作结果）
        btnDisable.addActionListener(e -> {
            int r = userTable.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(this, "请选择用户"); return; }
            int id = Integer.parseInt(userTable.getValueAt(r, 0).toString());
            UserDao dao = new UserDao();
            User u = dao.findAll().stream().filter(x -> x.getId() == id).findFirst().orElse(null);
            if (u == null) { JOptionPane.showMessageDialog(this, "未找到用户"); return; }

            boolean isDisabled = u.isDisabled();
            String actionLabel = isDisabled ? "解禁" : "禁用";
            String confirmMsg = String.format("确认%s用户 %s ?", actionLabel, u.getEmail());
            int confirm = JOptionPane.showConfirmDialog(this, confirmMsg, actionLabel + "用户", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            // 仅当账号以 "admin" 结尾时禁止被禁用（例如：email 或 昵称 以 admin 结尾）
            String emailLower = u.getEmail() == null ? "" : u.getEmail().toLowerCase();
            String nameLower = u.getFullName() == null ? "" : u.getFullName().toLowerCase();
            if (!isDisabled && (emailLower.endsWith("admin") || nameLower.endsWith("admin"))) {
                JOptionPane.showMessageDialog(this, "不能禁用以 'admin' 结尾的账户");
                return;
            }

            // 切换 disabled 状态
            u.setDisabled(!isDisabled);
            boolean ok = dao.update(u);
            if (ok) {
                String resultMsg = String.format("管理员已%s %s", (isDisabled ? "解禁" : "禁用"), u.getEmail());
                JOptionPane.showMessageDialog(this, resultMsg);
                loadUsers();
            } else {
                JOptionPane.showMessageDialog(this, "操作失败");
            }
        });
        // 重置密码
        btnResetPwd.addActionListener(e -> {
            int r = userTable.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(this, "请选择用户"); return; }
            int id = Integer.parseInt(userTable.getValueAt(r, 0).toString());
            String newPwd = JOptionPane.showInputDialog(this, "输入新密码:");
            if (newPwd == null || newPwd.isEmpty()) return;
            UserDao dao = new UserDao();
            User u = dao.findAll().stream().filter(x->x.getId()==id).findFirst().orElse(null);
            if (u == null) { JOptionPane.showMessageDialog(this, "未找到用户"); return; }
            u.setPasswordHash(org.mindrot.jbcrypt.BCrypt.hashpw(newPwd, org.mindrot.jbcrypt.BCrypt.gensalt()));
            if (dao.update(u)) { JOptionPane.showMessageDialog(this, "密码已重置"); }
            else JOptionPane.showMessageDialog(this, "操作失败");
        });
        // 修改信息
        btnEditUser.addActionListener(e -> {
            int r = userTable.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(this, "请选择用户"); return; }
            int id = Integer.parseInt(userTable.getValueAt(r, 0).toString());
            UserDao dao = new UserDao();
            User u = dao.findAll().stream().filter(x->x.getId()==id).findFirst().orElse(null);
            if (u == null) { JOptionPane.showMessageDialog(this, "未找到用户"); return; }
            JTextField nameField = new JTextField(u.getFullName());
            JTextField emailField = new JTextField(u.getEmail());
            JPanel panel = new JPanel(new GridLayout(2,2));
            panel.add(new JLabel("昵称:")); panel.add(nameField);
            panel.add(new JLabel("邮箱:")); panel.add(emailField);
            int res = JOptionPane.showConfirmDialog(this, panel, "修改信息", JOptionPane.OK_CANCEL_OPTION);
            if (res == JOptionPane.OK_OPTION) {
                u.setFullName(nameField.getText().trim());
                u.setEmail(emailField.getText().trim());
                if (dao.update(u)) { JOptionPane.showMessageDialog(this, "修改成功"); loadUsers(); }
                else JOptionPane.showMessageDialog(this, "操作失败");
            }
        });
        // ...existing code...
        // 订单管理
        orderTable = new JTable();
        JScrollPane orderScroll = new JScrollPane(orderTable);
        btnExportOrders = new JButton("导出订单报表");
        JButton btnRefund = new JButton("处理退票请求");
        JButton btnReschedule = new JButton("处理改签请求");
        JPanel orderPanel = new JPanel(new BorderLayout());
        // 顶部：仅显示待审核复选框
        JPanel orderTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        chkPendingRequests = new JCheckBox("仅显示待审核");
        orderTop.add(chkPendingRequests);
        // 当复选框切换时，重新加载订单以实时应用过滤
        chkPendingRequests.addActionListener(e -> loadOrders());
        orderPanel.add(orderTop, BorderLayout.NORTH);
        orderPanel.add(orderScroll, BorderLayout.CENTER);
        JPanel orderBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        orderBtnPanel.add(btnExportOrders);
        orderBtnPanel.add(btnRefund);
        orderBtnPanel.add(btnReschedule);
        orderPanel.add(orderBtnPanel, BorderLayout.SOUTH);
        tabbedPane.addTab("订单管理", orderPanel);

        // 处理退票请求（仅当用户提交退票申请时允许处理）
        btnRefund.addActionListener(e -> {
            int r = orderTable.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(this, "请选择订单"); return; }
            int id = Integer.parseInt(orderTable.getValueAt(r, 0).toString());
            OrderDao dao = new OrderDao();
            Order o = dao.findAll().stream().filter(x->x.getId()==id).findFirst().orElse(null);
            if (o == null) { JOptionPane.showMessageDialog(this, "未找到订单"); return; }
            if (!o.isRefundRequest()) { JOptionPane.showMessageDialog(this, "该订单没有退票申请"); return; }
            if (o.getStatus() == Order.Status.REFUNDED) { JOptionPane.showMessageDialog(this, "订单已退票"); return; }
            int res = JOptionPane.showConfirmDialog(this, "确认退票？", "退票审核", JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                o.setStatus(Order.Status.REFUNDED);
                o.setRefundRequest(false);
                List<Order> list = dao.findAll();
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getId() == o.getId()) { list.set(i, o); break; }
                }
                dao.insertAll(list);
                JOptionPane.showMessageDialog(this, "退票成功");
                loadOrders();
            }
        });
        // 处理改签请求（仅当用户提交改签申请时允许处理）
        btnReschedule.addActionListener(e -> {
            int r = orderTable.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(this, "请选择订单"); return; }
            int id = Integer.parseInt(orderTable.getValueAt(r, 0).toString());
            OrderDao dao = new OrderDao();
            Order o = dao.findAll().stream().filter(x->x.getId()==id).findFirst().orElse(null);
            if (o == null) { JOptionPane.showMessageDialog(this, "未找到订单"); return; }
            if (!o.isRescheduleRequest()) { JOptionPane.showMessageDialog(this, "该订单没有改签申请"); return; }
            if (o.getStatus() == Order.Status.CANCELLED) { JOptionPane.showMessageDialog(this, "订单已取消"); return; }
            // 如果用户已建议目标航班/座位，则预填这些值以便管理员一键接受
            String preflight = o.getRequestedFlightId() > 0 ? String.valueOf(o.getRequestedFlightId()) : String.valueOf(o.getFlightId());
            String preSeat = o.getRequestedSeatNumber() != null && !o.getRequestedSeatNumber().isEmpty() ? o.getRequestedSeatNumber() : (o.getSeatNumber() == null ? "" : o.getSeatNumber());
            JTextField flightIdField = new JTextField(preflight);
            JTextField seatField = new JTextField(preSeat);
            JPanel panel = new JPanel(new GridLayout(2,2));
            panel.add(new JLabel("新航班ID:")); panel.add(flightIdField);
            panel.add(new JLabel("新座位号:")); panel.add(seatField);
            int res = JOptionPane.showConfirmDialog(this, panel, "改签审核", JOptionPane.OK_CANCEL_OPTION);
            if (res == JOptionPane.OK_OPTION) {
                int newFlightId = Integer.parseInt(flightIdField.getText().trim());
                String newSeat = seatField.getText().trim();
                // 调整航班座位：返还旧航班、扣减新航班
                FlightDao fdao = new FlightDao();
                Flight oldF = fdao.findById(o.getFlightId()).orElse(null);
                Flight newF = fdao.findById(newFlightId).orElse(null);
                if (oldF != null) {
                    oldF.setSeatsLeft(oldF.getSeatsLeft() + o.getSeatCount());
                    fdao.update(oldF);
                }
                if (newF != null) {
                    newF.setSeatsLeft(newF.getSeatsLeft() - o.getSeatCount());
                    fdao.update(newF);
                }
                // 更新订单为新航班/座位并标记为已处理
                o.setFlightId(newFlightId);
                o.setSeatNumber(newSeat);
                o.setStatus(Order.Status.PAID); // 改签后仍为已支付
                o.setRescheduleRequest(false);
                // 清除用户建议字段
                o.setRequestedFlightId(-1);
                o.setRequestedSeatNumber(null);
                o.setRequestReason(null);
                List<Order> list = dao.findAll();
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getId() == o.getId()) { list.set(i, o); break; }
                }
                dao.insertAll(list);
                JOptionPane.showMessageDialog(this, "改签成功");
                loadOrders();
            }
        });
        add(tabbedPane, BorderLayout.CENTER);

        // 加载数据
        loadUsers();
        loadOrders();
        // 导出订单报表
        btnExportOrders.addActionListener(e -> exportOrders());
    }
    // 新增航班弹窗（手动添加/批量导入）
    private void showAddFlightDialog() {
        Window window = SwingUtilities.getWindowAncestor(this);
        AddFlightDialog dialog = new AddFlightDialog(window, flightPanel);
        dialog.setVisible(true);
    }

    private void loadUsers() {
        try {
            UserDao userDao = new UserDao();
            List<User> users = userDao.findAll();
            String[] cols = {"ID", "邮箱", "昵称", "角色"};
            String[][] data = new String[users.size()][4];
            for (int i = 0; i < users.size(); i++) {
                User u = users.get(i);
                data[i][0] = String.valueOf(u.getId());
                data[i][1] = u.getEmail();
                data[i][2] = u.getFullName();
                data[i][3] = u.getRole();
            }
            userTable.setModel(new javax.swing.table.DefaultTableModel(data, cols));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "加载用户失败: " + ex.getMessage());
        }
    }

    private void loadOrders() {
        try {
            OrderDao orderDao = new OrderDao();
            FlightDao flightDao = new FlightDao();
            List<Order> orders = orderDao.findAll();
            boolean filterPending = chkPendingRequests != null && chkPendingRequests.isSelected();
            // 申请状态列。将用户ID替换为显示用户名
            UserDao userDao = new UserDao();
            String[] cols = {"ID", "用户名", "航班号", "起点", "终点", "出发时间", "票价", "座位数", "座位号", "下单时间", "申请状态"};
            java.util.List<String[]> rows = new java.util.ArrayList<>();
            for (Order o : orders) {
                if (filterPending && !(o.isRefundRequest() || o.isRescheduleRequest())) continue;
                Flight f = flightDao.findById(o.getFlightId()).orElse(null);
                String[] row = new String[11];
                row[0] = String.valueOf(o.getId());
                // 显示用户名（优先 fullName，其次 email），若找不到则回退显示 numeric id
                User u = userDao.findAll().stream().filter(x -> x.getId() == o.getUserId()).findFirst().orElse(null);
                if (u != null) {
                    String display = (u.getFullName() != null && !u.getFullName().trim().isEmpty()) ? u.getFullName() : u.getEmail();
                    row[1] = display != null ? display : String.valueOf(o.getUserId());
                } else {
                    row[1] = String.valueOf(o.getUserId());
                }
                if (f != null) {
                    row[2] = f.getCode();
                    row[3] = f.getOrigin();
                    row[4] = f.getDestination();
                    row[5] = f.getDepartTime()!=null?f.getDepartTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")):"";
                    row[6] = String.valueOf(f.getPrice());
                } else {
                    row[2] = row[3] = row[4] = row[5] = row[6] = "-";
                }
                row[7] = String.valueOf(o.getSeatCount());
                row[8] = o.getSeatNumber();
                row[9] = o.getOrderTime()!=null ? o.getOrderTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
                // 申请状态
                boolean rReq = o.isRefundRequest();
                boolean sReq = o.isRescheduleRequest();
                if (rReq && sReq) row[10] = "退票&改签申请(待审核)";
                else if (rReq) row[10] = "退票申请(待审核)";
                else if (sReq) row[10] = "改签申请(待审核)";
                else row[10] = "-";
                rows.add(row);
            }
            String[][] data = new String[rows.size()][];
            data = rows.toArray(data);
            orderTable.setModel(new javax.swing.table.DefaultTableModel(data, cols));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "加载订单失败: " + ex.getMessage());
        }
    }

    private void exportOrders() {
        try {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("选择导出位置");
            int r = chooser.showSaveDialog(this);
            if (r == JFileChooser.APPROVE_OPTION) {
                java.io.File file = chooser.getSelectedFile();
                OrderDao orderDao = new OrderDao();
                List<Order> orders = orderDao.findAll();
                try (java.io.PrintWriter pw = new java.io.PrintWriter(file)) {
                    pw.println("id,userId,flightId,seatCount,orderTime");
                    for (Order o : orders) {
                        pw.printf("%d,%d,%d,%d,%s\n", o.getId(), o.getUserId(), o.getFlightId(), o.getSeatCount(), o.getOrderTime().toString());
                    }
                }
                JOptionPane.showMessageDialog(this, "导出成功！");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "导出失败: " + ex.getMessage());
        }
    }
}
