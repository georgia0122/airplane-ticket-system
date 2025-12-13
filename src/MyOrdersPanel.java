import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MyOrdersPanel extends JPanel {
    private OrderDao orderDao = new OrderDao();
    private FlightDao flightDao = new FlightDao();
    private User user;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnRefund, btnChange;
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public MyOrdersPanel(User user) {
        this.user = user;
        setLayout(new BorderLayout());
        init();
        loadData(); // 自动刷新订单列表
    }

    private void init() {
        String[] cols = {"订单ID","航班号","起点","终点","出发时间","到达时间","票价","订票张数","座位号","下单时间","状态","支付方式"};
        tableModel = new DefaultTableModel(cols,0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        JScrollPane sp = new JScrollPane(table);
        add(sp, BorderLayout.CENTER);

        // 右键菜单
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem miRefund = new JMenuItem("退票");
        JMenuItem miChange = new JMenuItem("改签");
        popupMenu.add(miRefund); popupMenu.add(miChange);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) showMenu(e);
            }
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) showMenu(e);
            }
            private void showMenu(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    table.setRowSelectionInterval(row, row);
                    popupMenu.show(table, e.getX(), e.getY());
                }
            }
        });
        miRefund.addActionListener(e -> doRefund());
        miChange.addActionListener(e -> doChange());

        // 保留底部按钮（可选）
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRefresh = new JButton("刷新");
        btnRefund = new JButton("退票");
        btnChange = new JButton("改签");
        btnPanel.add(btnRefresh); btnPanel.add(btnRefund); btnPanel.add(btnChange);
        add(btnPanel, BorderLayout.SOUTH);
        btnRefresh.addActionListener(e -> loadData());
        btnRefund.addActionListener(e -> doRefund());
        btnChange.addActionListener(e -> doChange());
        loadData();
    }

    // 退票功能
    private void doRefund() {
        int r = table.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "请选择要退票的订单");
            return;
        }
        int orderId = (int) tableModel.getValueAt(r, 0);
        Order order = orderDao.findAll().stream().filter(o -> o.getId() == orderId).findFirst().orElse(null);
        if (order == null) {
            JOptionPane.showMessageDialog(this, "订单不存在");
            return;
        }
        if (order.getStatus() != Order.Status.PAID) {
            JOptionPane.showMessageDialog(this, "仅已出票订单可退票");
            return;
        }
        // 提交退票申请，而不是直接退款。管理员需在后台审核后执行实际退款。
        int confirm = JOptionPane.showConfirmDialog(this, "确定要提交退票申请吗？管理员审核通过后才会退款。", "提交退票申请", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        List<Order> all = orderDao.findAll();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId() == orderId) {
                all.get(i).setRefundRequest(true);
                break;
            }
        }
        boolean ok = orderDao.insertAll(all);
        if (ok) {
            loadData();
            JOptionPane.showMessageDialog(this, "退票申请已提交，等待管理员审核。");
        } else {
            JOptionPane.showMessageDialog(this, "提交失败，请稍后重试。");
        }
    }

    // 改签功能
    private void doChange() {
        int r = table.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "请选择要改签的订单");
            return;
        }
        int orderId = (int) tableModel.getValueAt(r, 0);
        Order order = orderDao.findAll().stream().filter(o -> o.getId() == orderId).findFirst().orElse(null);
        if (order == null) {
            JOptionPane.showMessageDialog(this, "订单不存在");
            return;
        }
        if (order.getStatus() != Order.Status.PAID) {
            JOptionPane.showMessageDialog(this, "仅已出票订单可改签");
            return;
        }
        // 让用户选择或建议目标航班和期望座位（可选），然后提交改签申请
        List<Flight> flights = flightDao.findAll();
        String[] options = flights.stream().map(f -> f.getId() + ": " + f.getCode() + " " + f.getOrigin() + "->" + f.getDestination() + " " + (f.getDepartTime()!=null?f.getDepartTime().format(dtf):""))
                .toArray(String[]::new);
        JPanel panel = new JPanel(new BorderLayout(6,6));
        JPanel top = new JPanel(new GridLayout(3,2,6,6));
        top.add(new JLabel("建议改签到的航班（可选）："));
        JComboBox<String> cbFlights = new JComboBox<>();
        cbFlights.addItem("");
        for (String s : options) cbFlights.addItem(s);
        top.add(cbFlights);
        top.add(new JLabel("建议座位号（逗号分隔，可留空）："));
        JTextField tfSeatSuggest = new JTextField();
        top.add(tfSeatSuggest);
        top.add(new JLabel("备注（可选）："));
        JTextField tfReason = new JTextField();
        top.add(tfReason);
        panel.add(top, BorderLayout.CENTER);
        int res = JOptionPane.showConfirmDialog(this, panel, "提交改签申请 - 建议目标航班与座位", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        // 解析用户选择
        String sel = (String) cbFlights.getSelectedItem();
        int suggestedFlightId = -1;
        if (sel != null && !sel.trim().isEmpty()) {
            try {
                suggestedFlightId = Integer.parseInt(sel.split(":")[0].trim());
            } catch (Exception ignore) { suggestedFlightId = -1; }
        }
        String suggestedSeats = tfSeatSuggest.getText().trim();
        String reason = tfReason.getText().trim();

        int confirm = JOptionPane.showConfirmDialog(this, "确定要提交改签申请吗？管理员审核通过后将为您处理改签。", "提交改签申请", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        List<Order> all = orderDao.findAll();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId() == orderId) {
                all.get(i).setRescheduleRequest(true);
                all.get(i).setRequestedFlightId(suggestedFlightId);
                all.get(i).setRequestedSeatNumber(suggestedSeats == null ? "" : suggestedSeats);
                all.get(i).setRequestReason(reason == null ? "" : reason);
                break;
            }
        }
        boolean ok = orderDao.insertAll(all);
        if (ok) {
            loadData();
            JOptionPane.showMessageDialog(this, "改签申请已提交，等待管理员审核。");
        } else {
            JOptionPane.showMessageDialog(this, "提交失败，请稍后重试。");
        }
    }

    public void loadData() {
        tableModel.setRowCount(0);
        try {
            List<Order> orders = orderDao.findByUserId(user.getId());
            for (Order o : orders) {
                Flight f = flightDao.findById(o.getFlightId()).orElse(null);
                if (f != null) {
                    String statusWithReq = getStatusText(o.getStatus());
                    if (o.isRefundRequest()) statusWithReq += " | 退票申请(待审核)";
                    if (o.isRescheduleRequest()) statusWithReq += " | 改签申请(待审核)";
                    // 若用户已建议目标航班/座位，显示建议摘要
                    if (o.getRequestedFlightId() > 0) {
                        Flight suggested = flightDao.findById(o.getRequestedFlightId()).orElse(null);
                        if (suggested != null) statusWithReq += " | 建议改签到: " + suggested.getCode() + " (座位: " + (o.getRequestedSeatNumber() == null ? "" : o.getRequestedSeatNumber()) + ")";
                        else statusWithReq += " | 建议改签到航班ID:" + o.getRequestedFlightId();
                    }
                    tableModel.addRow(new Object[]{
                        o.getId(), f.getCode(), f.getOrigin(), f.getDestination(),
                        f.getDepartTime()!=null?f.getDepartTime().format(dtf):"",
                        f.getArriveTime()!=null?f.getArriveTime().format(dtf):"",
                        f.getPrice(), o.getSeatCount(), o.getSeatNumber(), o.getOrderTime().format(dtf),
                        statusWithReq, o.getPayType().name()
                    });
                } else {
                    String statusWithReq = getStatusText(o.getStatus());
                    if (o.isRefundRequest()) statusWithReq += " | 退票申请(待审核)";
                    if (o.isRescheduleRequest()) statusWithReq += " | 改签申请(待审核)";
                    tableModel.addRow(new Object[]{
                        o.getId(), "(航班已删除)", "-", "-", "-", "-", "-", o.getSeatCount(), o.getSeatNumber(), o.getOrderTime().format(dtf),
                        statusWithReq, o.getPayType().name()
                    });
                }
            }
        } catch (Exception ex) {
            // 保护性捕获，避免因数据异常导致整个面板崩溃
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载订单时出现错误：" + ex.getMessage());
        }
        table.clearSelection();
    }
        // 订单状态中文显示
        private String getStatusText(Order.Status status) {
            switch (status) {
                case PENDING: return "待支付";
                case PAID: return "已出票";
                case CANCELLED: return "已取消";
                case REFUNDED: return "已退款";
                default: return status.name();
            }
        }
    }
