
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FlightPanel extends JPanel {
    private FlightDao dao = new FlightDao();
    private JTable table;
    private DefaultTableModel tableModel;
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private JButton btnAdd, btnEdit, btnDelete;

    public FlightPanel() {
        setLayout(new BorderLayout());
        init();
    }


    private void init() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnEdit = new JButton("修改航班");
        btnDelete = new JButton("删除航班");
        JButton btnRefresh = new JButton("刷新");
        top.add(btnEdit); top.add(btnDelete); top.add(btnRefresh);
        add(top, BorderLayout.NORTH);

        String[] cols = {"ID","航班号","起点","终点","出发时间","到达时间","票价","座位总数","剩余座位","飞行时长(分钟)"};
        tableModel = new DefaultTableModel(cols,0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        JScrollPane sp = new JScrollPane(table);
        add(sp, BorderLayout.CENTER);

        // 新增航班按钮已移至AdminPanel右下角弹窗入口
        btnEdit.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(this,"请选择一行"); return; }
            int id = (int) tableModel.getValueAt(r, 0);
            dao.findById(id).ifPresentOrElse(this::openEditDialog, ()-> JOptionPane.showMessageDialog(this,"未找到航班"));
        });
        btnDelete.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r == -1) { JOptionPane.showMessageDialog(this,"请选择一行"); return; }
            int id = (int) tableModel.getValueAt(r, 0);
            int confirm = JOptionPane.showConfirmDialog(this,"确认删除？","删除",JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean ok = dao.delete(id);
                if (ok) { JOptionPane.showMessageDialog(this,"删除成功"); loadData(); }
                else JOptionPane.showMessageDialog(this,"删除失败");
            }
        });
        btnRefresh.addActionListener(e -> loadData());
    }

    public void loadData() {
        tableModel.setRowCount(0);
        List<Flight> list = dao.findAll();
        for (Flight f : list) {
            Object[] row = new Object[]{
                f.getId(),
                f.getCode(),
                f.getOrigin(),
                f.getDestination(),
                f.getDepartTime() != null ? f.getDepartTime().format(dtf) : "",
                f.getArriveTime() != null ? f.getArriveTime().format(dtf) : "",
                f.getPrice(),
                f.getSeatsTotal(),
                f.getSeatsLeft(),
                f.getDuration()
            };
            tableModel.addRow(row);
        }
    }

    private void openEditDialog(Flight flight) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "航班信息", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.gridx=0;c.gridy=0; p.add(new JLabel("航班号:"), c); c.gridx=1; JTextField tfCode = new JTextField(12); p.add(tfCode,c);
        c.gridx=0;c.gridy++; p.add(new JLabel("起点:"), c); c.gridx=1; JTextField tfOrigin = new JTextField(8); p.add(tfOrigin,c);
        c.gridx=0;c.gridy++; p.add(new JLabel("终点:"), c); c.gridx=1; JTextField tfDest = new JTextField(8); p.add(tfDest,c);
        c.gridx=0;c.gridy++; p.add(new JLabel("出发 (yyyy-MM-dd HH:mm):"), c); c.gridx=1; JTextField tfDepart = new JTextField(16); p.add(tfDepart,c);
        c.gridx=0;c.gridy++; p.add(new JLabel("到达 (yyyy-MM-dd HH:mm):"), c); c.gridx=1; JTextField tfArrive = new JTextField(16); p.add(tfArrive,c);
        c.gridx=0;c.gridy++; p.add(new JLabel("票价:"), c); c.gridx=1; JTextField tfPrice = new JTextField(8); p.add(tfPrice,c);
        c.gridx=0;c.gridy++; p.add(new JLabel("座位总数:"), c); c.gridx=1; JTextField tfTotal = new JTextField(6); p.add(tfTotal,c);
        c.gridx=0;c.gridy++; p.add(new JLabel("飞行时长(分钟):"), c); c.gridx=1; JTextField tfDuration = new JTextField(6); p.add(tfDuration,c);

        if (flight != null) {
            tfCode.setText(flight.getCode());
            tfOrigin.setText(flight.getOrigin());
            tfDest.setText(flight.getDestination());
            tfDepart.setText(flight.getDepartTime()!=null?flight.getDepartTime().format(dtf):"");
            tfArrive.setText(flight.getArriveTime()!=null?flight.getArriveTime().format(dtf):"");
            tfPrice.setText(String.valueOf(flight.getPrice()));
            tfTotal.setText(String.valueOf(flight.getSeatsTotal()));
            tfDuration.setText(String.valueOf(flight.getDuration()));
        }

        c.gridx=0;c.gridy++; JButton btnSave = new JButton("保存"); p.add(btnSave,c);
        c.gridx=1; JButton btnCancel = new JButton("取消"); p.add(btnCancel,c);

        dlg.add(p);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            try {
                String code = tfCode.getText().trim();
                String origin = tfOrigin.getText().trim();
                String dest = tfDest.getText().trim();
                String departStr = tfDepart.getText().trim();
                String arriveStr = tfArrive.getText().trim();
                String priceStr = tfPrice.getText().trim();
                String totalStr = tfTotal.getText().trim();

                // 校验航班号
                if (code.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "航班号不能为空，请输入航班号（如 CA1234）");
                    return;
                }
                // 校验起点终点
                if (origin.isEmpty() || dest.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "起点和终点不能为空，请输入城市名称");
                    return;
                }
                // 校验时间
                LocalDateTime depart, arrive;
                try {
                    depart = parseDateTime(departStr);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "出发时间格式错误，应为 yyyy-MM-dd HH:mm 或 yyyy/M/d HH:mm，如 2025-11-10 08:00");
                    return;
                }
                try {
                    arrive = parseDateTime(arriveStr);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "到达时间格式错误，应为 yyyy-MM-dd HH:mm 或 yyyy/M/d HH:mm，如 2025-11-10 10:30");
                    return;
                }
                if (!arrive.isAfter(depart)) {
                    JOptionPane.showMessageDialog(this, "到达时间必须晚于出发时间");
                    return;
                }
                // 校验票价
                double price;
                try {
                    price = Double.parseDouble(priceStr);
                    if (price < 0) throw new NumberFormatException();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "票价格式错误，请输入非负数字，如 499.0");
                    return;
                }
                // 校验座位数
                int total;
                try {
                    total = Integer.parseInt(totalStr);
                    if (total <= 0) throw new NumberFormatException();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "座位总数格式错误，请输入正整数，如 180");
                    return;
                }

                int duration;
                try {
                    duration = Integer.parseInt(tfDuration.getText().trim());
                    if (duration <= 0) throw new NumberFormatException();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "飞行时长格式错误，请输入正整数（分钟）");
                    return;
                }
                Flight f = (flight == null) ? new Flight() : flight;
                f.setCode(code); f.setOrigin(origin); f.setDestination(dest);
                f.setDepartTime(depart); f.setArriveTime(arrive);
                f.setPrice(price); f.setSeatsTotal(total);
                f.setDuration(duration);
                if (flight == null) {
                    f.setSeatsLeft(total);
                    boolean ok = dao.insert(f);
                    if (ok) { JOptionPane.showMessageDialog(this,"新增成功"); dlg.dispose(); loadData(); }
                    else JOptionPane.showMessageDialog(this,"新增失败，可能航班号重复");
                } else {
                    // 若编辑时修改总座位，尝试保持剩余座位不为负
                    if (f.getSeatsLeft() > total) f.setSeatsLeft(total);
                    boolean ok = dao.update(f);
                    if (ok) { JOptionPane.showMessageDialog(this,"修改成功"); dlg.dispose(); loadData(); }
                    else JOptionPane.showMessageDialog(this,"修改失败");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "保存失败：" + ex.getMessage());
                ex.printStackTrace();
            }
        });
        dlg.setVisible(true);
    }

    // 权限控制方法
    public void setEditEnabled(boolean enabled) {
        if (btnAdd != null) btnAdd.setEnabled(enabled);
        if (btnEdit != null) btnEdit.setEnabled(enabled);
        if (btnDelete != null) btnDelete.setEnabled(enabled);
    }

    // 支持多种日期格式自动解析
    private LocalDateTime parseDateTime(String text) {
        String[] patterns = {"yyyy-MM-dd HH:mm", "yyyy-M-d HH:mm", "yyyy/MM/dd HH:mm", "yyyy/M/d HH:mm"};
        for (String p : patterns) {
            try {
                return LocalDateTime.parse(text, DateTimeFormatter.ofPattern(p));
            } catch (Exception ignore) {}
        }
        throw new RuntimeException("时间格式错误，应为 yyyy-MM-dd HH:mm 或 yyyy-M-d HH:mm");
    }
}
