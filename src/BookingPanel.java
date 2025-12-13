import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BookingPanel extends JPanel {
    private FlightDao flightDao = new FlightDao();
    private OrderDao orderDao = new OrderDao();
    private User user;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfFrom, tfTo, tfDate, tfCode;
    private JSpinner dateSpinner;
    private JComboBox<String> cbSort;
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public BookingPanel(User user) {
        this.user = user;
        setLayout(new BorderLayout());
        init();
        doSearch(); // 自动刷新航班列表
    }

    private void init() {
    JPanel searchPanel = new JPanel(new BorderLayout());
    JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
    leftPanel.setBackground(new Color(245,250,255));
    Font labelFont = new Font("微软雅黑", Font.BOLD, 15);
    Font fieldFont = new Font("微软雅黑", Font.PLAIN, 15);
    JLabel lblFrom = new JLabel("出发地:"); lblFrom.setFont(labelFont); leftPanel.add(lblFrom);
    tfFrom = new JTextField(8); tfFrom.setFont(fieldFont); leftPanel.add(tfFrom);
    JLabel lblTo = new JLabel("目的地:"); lblTo.setFont(labelFont); leftPanel.add(lblTo);
    tfTo = new JTextField(8); tfTo.setFont(fieldFont); leftPanel.add(tfTo);
    JLabel lblDate = new JLabel("出发日期:"); lblDate.setFont(labelFont); leftPanel.add(lblDate);
    tfDate = new JTextField(10); tfDate.setFont(fieldFont); leftPanel.add(tfDate);
    SpinnerDateModel dateModel = new SpinnerDateModel();
    dateSpinner = new JSpinner(dateModel);
    dateSpinner.setFont(fieldFont);
    dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
    leftPanel.add(dateSpinner);
    // 日历选择器和文本框联动
    dateSpinner.addChangeListener(e -> {
        java.util.Date d = (java.util.Date)dateSpinner.getValue();
        tfDate.setText(new java.text.SimpleDateFormat("yyyy-MM-dd").format(d));
        doSearch();
    });
    tfDate.addActionListener(e -> doSearch());
    JLabel lblCode = new JLabel("航班号:"); lblCode.setFont(labelFont); leftPanel.add(lblCode);
    tfCode = new JTextField(8); tfCode.setFont(fieldFont); leftPanel.add(tfCode);

    JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
    rightPanel.setBackground(new Color(245,250,255));
    JLabel lblSort = new JLabel("排序:"); lblSort.setFont(labelFont); rightPanel.add(lblSort);
    cbSort = new JComboBox<>(new String[]{"默认","票价升序","票价降序","起飞时间升序","起飞时间降序","剩余座位升序","剩余座位降序"});
    cbSort.setFont(fieldFont); rightPanel.add(cbSort);
    JButton btnSearch = new JButton("搜索"); btnSearch.setFont(labelFont);
    JButton btnRefresh = new JButton("刷新"); btnRefresh.setFont(labelFont);
    JButton btnFindAll = new JButton("查找全部"); btnFindAll.setFont(labelFont);
    rightPanel.add(btnSearch); rightPanel.add(btnRefresh); rightPanel.add(btnFindAll);

    searchPanel.add(leftPanel, BorderLayout.CENTER);
    searchPanel.add(rightPanel, BorderLayout.EAST);

        String[] cols = {"ID","航班号","起点","终点","出发时间","到达时间","票价","剩余座位"};
        tableModel = new DefaultTableModel(cols,0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        JScrollPane sp = new JScrollPane(table);

        // 使用垂直分割面板使搜索区域可调整大小，避免被航班列表覆盖
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, searchPanel, sp);
        split.setOneTouchExpandable(true);
        split.setResizeWeight(0);
        split.setDividerLocation(140); // 初始高度，可根据需要调整
        add(split, BorderLayout.CENTER);

        JButton btnBook = new JButton("订票");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnBook);
        add(bottom, BorderLayout.SOUTH);

        btnSearch.addActionListener(e -> doSearch());
        btnBook.addActionListener(e -> doBook());
        btnRefresh.addActionListener(e -> doSearch());
        // 查找全部：清空所有过滤条件并展示所有航班
        btnFindAll.addActionListener(e -> {
            tfFrom.setText("");
            tfTo.setText("");
            tfDate.setText("");
            tfCode.setText("");
            cbSort.setSelectedIndex(0);
            doSearch();
        });
        cbSort.addActionListener(e -> doSearch());

        // 输入框回车自动搜索
        java.awt.event.ActionListener enterSearch = e -> doSearch();
        tfFrom.addActionListener(enterSearch);
        tfTo.addActionListener(enterSearch);
        tfDate.addActionListener(enterSearch);
        tfCode.addActionListener(enterSearch);
    }

    public void doSearch() {
        String from = tfFrom.getText().trim().toLowerCase();
        String to = tfTo.getText().trim().toLowerCase();
        String date = tfDate.getText().trim();
        String code = tfCode.getText().trim().toLowerCase();
        String sort = (String)cbSort.getSelectedItem();
        tableModel.setRowCount(0);
        List<Flight> list = flightDao.findAll();
        // 多条件模糊过滤（不区分大小写，支持部分关键字/拼音首字母）
        List<Flight> filtered = new java.util.ArrayList<>();
        for (Flight f : list) {
            boolean match = true;
            if (!from.isEmpty() && !f.getOrigin().toLowerCase().matches(".*"+from+".*")) match = false;
            if (!to.isEmpty() && !f.getDestination().toLowerCase().matches(".*"+to+".*")) match = false;
            if (!date.isEmpty()) {
                String d = f.getDepartTime()!=null ? f.getDepartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";
                if (!d.equals(date)) match = false;
            }
            if (!code.isEmpty() && !f.getCode().toLowerCase().matches(".*"+code+".*")) match = false;
            if (match) filtered.add(f);
        }
        // 排序
        filtered.sort((a, b) -> {
            switch (sort) {
                case "票价升序": return Double.compare(a.getPrice(), b.getPrice());
                case "票价降序": return Double.compare(b.getPrice(), a.getPrice());
                case "起飞时间升序": return a.getDepartTime().compareTo(b.getDepartTime());
                case "起飞时间降序": return b.getDepartTime().compareTo(a.getDepartTime());
                case "剩余座位升序": return Integer.compare(a.getSeatsLeft(), b.getSeatsLeft());
                case "剩余座位降序": return Integer.compare(b.getSeatsLeft(), a.getSeatsLeft());
                default: return 0;
            }
        });
        for (Flight f : filtered) {
            tableModel.addRow(new Object[]{
                    f.getId(), f.getCode(), f.getOrigin(), f.getDestination(),
                    f.getDepartTime()!=null?f.getDepartTime().format(dtf):"",
                    f.getArriveTime()!=null?f.getArriveTime().format(dtf):"",
                    f.getPrice(), f.getSeatsLeft()
            });
        }
        // 如果本次搜索使用了出发地或出发日期过滤，则在搜索完成后重置到初始状态（清空这些输入）
        if (!from.isEmpty() || !date.isEmpty()) {
            SwingUtilities.invokeLater(() -> {
                tfFrom.setText("");
                tfDate.setText("");
                // 不强制修改 dateSpinner 避免触发额外事件
            });
        }
    }

    protected void onOrderSuccess() {
        // 可被MainFrame重写，订票成功后回调
    }

    private void doBook() {
        int r = table.getSelectedRow();
        if (r == -1) {
            JOptionPane.showMessageDialog(this, "请选择要订票的航班");
            return;
        }
        int flightId = (int) tableModel.getValueAt(r, 0);
        Flight f = flightDao.findById(flightId).orElse(null);
        if (f == null) {
            JOptionPane.showMessageDialog(this, "未找到航班");
            return;
        }
        if (f.getSeatsLeft() <= 0) {
            JOptionPane.showMessageDialog(this, "该航班已无剩余座位");
            return;
        }
        // 舱位选择弹窗已移除，直接进入图形化选座界面

        // 订票张数
        String seatStr = JOptionPane.showInputDialog(this, "请输入订票张数（1~"+f.getSeatsLeft()+"）:");
        if (seatStr == null) return;
        int seatCount;
        try {
            seatCount = Integer.parseInt(seatStr.trim());
            if (seatCount <= 0 || seatCount > f.getSeatsLeft()) throw new NumberFormatException();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "请输入有效的订票张数（1~"+f.getSeatsLeft()+"），只能输入数字");
            return;
        }


        // 获取已售座位列表（所有已出票订单座位号）
        java.util.Set<String> soldSeats = new java.util.HashSet<>();
        for (Order o : orderDao.findAll()) {
            if (o.getFlightId() == f.getId() && o.getStatus() == Order.Status.PAID && o.getSeatNumber() != null) {
                for (String s : o.getSeatNumber().split(",")) {
                    soldSeats.add(s.trim());
                }
            }
        }
        // 订票前弹窗显示三种舱位票价和选择按钮
        double priceEconomy = f.getPrice();
        double priceBusiness = Math.round(f.getPrice() * 1.5 * 100.0) / 100.0;
        double priceFirst = Math.round(f.getPrice() * 2.2 * 100.0) / 100.0;
        String[] cabinNames = {"经济舱", "商务舱", "头等舱"};
        double[] cabinPrices = {priceEconomy, priceBusiness, priceFirst};
        JPanel pricePanel = new JPanel(new GridLayout(4,2,8,8));
        pricePanel.add(new JLabel("经济舱: " + priceEconomy + " 元"));
        JRadioButton rbEconomy = new JRadioButton("选择经济舱");
        pricePanel.add(rbEconomy);
        pricePanel.add(new JLabel("商务舱: " + priceBusiness + " 元"));
        JRadioButton rbBusiness = new JRadioButton("选择商务舱");
        pricePanel.add(rbBusiness);
        pricePanel.add(new JLabel("头等舱: " + priceFirst + " 元"));
        JRadioButton rbFirst = new JRadioButton("选择头等舱");
        pricePanel.add(rbFirst);
        ButtonGroup group = new ButtonGroup();
        group.add(rbEconomy); group.add(rbBusiness); group.add(rbFirst);
        rbEconomy.setSelected(true);
        int result = JOptionPane.showConfirmDialog(this, pricePanel, "请选择舱位", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;
        int selectedCabin = 0;
        if (rbFirst.isSelected()) selectedCabin = 2;
        else if (rbBusiness.isSelected()) selectedCabin = 1;
        else selectedCabin = 0;
        // 图形化选座界面，只允许用户选择对应舱位座位
        SeatSelectionDialog seatDialog = new SeatSelectionDialog((Frame)SwingUtilities.getWindowAncestor(this), seatCount, soldSeats, selectedCabin);
        seatDialog.setVisible(true);
        if (!seatDialog.isConfirmed()) return;
        String seatNumber = seatDialog.getSelectedSeatsStr();

        // 订单详情弹窗，支付前确认
        // 订单详情弹窗，支付前确认，严格随用户选择的舱位和价格变动
        String cabinStr = cabinNames[selectedCabin];
        double cabinPrice = cabinPrices[selectedCabin];
        StringBuilder detail = new StringBuilder();
        detail.append("请确认订单信息：\n\n");
        detail.append("航班号: ").append(f.getCode()).append("\n");
        detail.append("路线: ").append(f.getOrigin()).append(" → ").append(f.getDestination()).append("\n");
        detail.append("出发时间: ").append(f.getDepartTime()!=null?f.getDepartTime().format(dtf):"").append("\n");
        detail.append("到达时间: ").append(f.getArriveTime()!=null?f.getArriveTime().format(dtf):"").append("\n");
        detail.append("航程: ").append(f.getDuration()).append("分钟\n");
        detail.append("舱位: ").append(cabinStr).append("\n");
        detail.append("票价: ").append(cabinPrice).append(" 元\n");
        detail.append("订票张数: ").append(seatCount).append("\n");
        detail.append("座位号: ").append(seatNumber).append("\n");

        int confirm = JOptionPane.showConfirmDialog(this, detail.toString(), "订单确认", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (confirm != JOptionPane.OK_OPTION) {
            JOptionPane.showMessageDialog(this, "已取消订票。");
            return;
        }

        // 支付方式选择
        Object[] payOptions = {"微信支付", "支付宝", "银联", "取消"};
        int paySel = JOptionPane.showOptionDialog(this, "请选择支付方式：", "支付", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, payOptions, payOptions[0]);
        if (paySel == 3 || paySel == JOptionPane.CLOSED_OPTION) {
            JOptionPane.showMessageDialog(this, "订单未支付，已取消。");
            return;
        }
        Order.PayType payType = Order.PayType.NONE;
        if (paySel == 0) payType = Order.PayType.WECHAT;
        else if (paySel == 1) payType = Order.PayType.ALIPAY;
        else if (paySel == 2) payType = Order.PayType.UNIONPAY;

        // 创建订单，初始状态为待支付
        Order order = new Order(user.getId(), f.getId(), seatCount, seatNumber, LocalDateTime.now());
        order.setStatus(Order.Status.PENDING);
        order.setPayType(payType);
        orderDao.insert(order);

        // 支付二维码或链接弹窗
        String payMsg = "请使用" + payOptions[paySel] + "扫码或点击链接完成支付。\n";
        if (paySel == 0) {
            payMsg += "[微信支付二维码] (模拟)\n";
            payMsg += "https://pay.wechat.com/mock?id=" + order.getId();
        } else if (paySel == 1) {
            payMsg += "[支付宝二维码] (模拟)\n";
            payMsg += "https://www.alipay.com/mock?id=" + order.getId();
        } else if (paySel == 2) {
            payMsg += "[银联支付二维码] (模拟)\n";
            payMsg += "https://www.unionpay.com/mock?id=" + order.getId();
        }
        JOptionPane.showMessageDialog(this, payMsg, "支付二维码/链接", JOptionPane.INFORMATION_MESSAGE);

        // 模拟支付成功
        boolean paySuccess = true; // 可扩展为实际支付接口
        if (paySuccess) {
            f.setSeatsLeft(f.getSeatsLeft() - seatCount);
            flightDao.update(f);
            order.setStatus(Order.Status.PAID);
            // 更新订单状态
            List<Order> all = orderDao.findAll();
            for (Order o : all) {
                if (o.getId() == order.getId()) {
                    o.setStatus(Order.Status.PAID);
                    o.setPayType(payType);
                    break;
                }
            }
            orderDao.insertAll(all);
            JOptionPane.showMessageDialog(this, payOptions[paySel] + "成功，订票完成！\n座位号: " + seatNumber);
        } else {
            JOptionPane.showMessageDialog(this, "支付失败，订单仍为待支付状态。");
        }
        doSearch();
        onOrderSuccess();

    }

}
