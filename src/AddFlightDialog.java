import javax.swing.*;
import java.awt.*;
import java.io.*;

public class AddFlightDialog extends JDialog {
        // 支持Window参数的构造方法，兼容AdminPanel调用
        public AddFlightDialog(Window owner, FlightPanel flightPanel) {
            this(owner instanceof Frame ? (Frame)owner : null, flightPanel);
        }
    private JTextField codeField, originField, destField, departField, arriveField, priceField, seatsTotalField, seatsLeftField, durationField;
    private JButton btnSave, btnImport;
    private FlightPanel flightPanel;

    public AddFlightDialog(Frame owner, FlightPanel flightPanel) {
        super(owner, "新增航班", true);
        this.flightPanel = flightPanel;
        setLayout(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();
        // 手动添加
        JPanel manualPanel = new JPanel(new GridLayout(10,2,5,5));
        manualPanel.add(new JLabel("航班号:"));
        codeField = new JTextField(); manualPanel.add(codeField);
        manualPanel.add(new JLabel("起点:"));
        originField = new JTextField(); manualPanel.add(originField);
        manualPanel.add(new JLabel("终点:"));
        destField = new JTextField(); manualPanel.add(destField);
        manualPanel.add(new JLabel("出发时间(yyyy-MM-dd HH:mm):"));
        departField = new JTextField(); manualPanel.add(departField);
        manualPanel.add(new JLabel("到达时间(yyyy-MM-dd HH:mm):"));
        arriveField = new JTextField(); manualPanel.add(arriveField);
        manualPanel.add(new JLabel("票价:"));
        priceField = new JTextField(); manualPanel.add(priceField);
        manualPanel.add(new JLabel("座位总数:"));
        seatsTotalField = new JTextField(); manualPanel.add(seatsTotalField);
        manualPanel.add(new JLabel("剩余座位:"));
        seatsLeftField = new JTextField(); manualPanel.add(seatsLeftField);
        manualPanel.add(new JLabel("时长(分钟):"));
        durationField = new JTextField(); manualPanel.add(durationField);
        btnSave = new JButton("保存");
        manualPanel.add(btnSave);
        manualPanel.add(new JLabel());
        tabbedPane.addTab("手动添加", manualPanel);
        // 批量导入
        JPanel importPanel = new JPanel(new BorderLayout());
        btnImport = new JButton("批量导入CSV");
        importPanel.add(btnImport, BorderLayout.CENTER);
        tabbedPane.addTab("批量导入", importPanel);
        add(tabbedPane, BorderLayout.CENTER);
        setSize(700, 520);
        setLocationRelativeTo(owner);
        // 保存按钮事件
        btnSave.addActionListener(e -> saveFlight());
        // 批量导入事件
        btnImport.addActionListener(e -> importFlights());
    }

    private void saveFlight() {
        try {
            Flight f = new Flight();
            f.setCode(codeField.getText().trim());
            f.setOrigin(originField.getText().trim());
            f.setDestination(destField.getText().trim());
            f.setDepartTime(java.time.LocalDateTime.parse(departField.getText().trim(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            f.setArriveTime(java.time.LocalDateTime.parse(arriveField.getText().trim(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            f.setPrice(Double.parseDouble(priceField.getText().trim()));
            f.setSeatsTotal(Integer.parseInt(seatsTotalField.getText().trim()));
            f.setSeatsLeft(Integer.parseInt(seatsLeftField.getText().trim()));
            f.setDuration(Integer.parseInt(durationField.getText().trim()));
            java.util.List<Flight> flights = new java.util.ArrayList<>(new FlightDao().findAll());
            int maxId = flights.stream().mapToInt(Flight::getId).max().orElse(0);
            f.setId(maxId+1);
            flights.add(f);
            new FlightDao().saveAll(flights);
            flightPanel.loadData();
            JOptionPane.showMessageDialog(this, "添加成功！");
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "添加失败: " + ex.getMessage());
        }
    }

    private void importFlights() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("选择航班CSV文件");
        int r = chooser.showOpenDialog(this);
        if (r != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        int imported = 0, failed = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            java.util.List<Flight> flights = new java.util.ArrayList<>(new FlightDao().findAll());
            int maxId = flights.stream().mapToInt(Flight::getId).max().orElse(0);
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("id,")) continue;
                try {
                    String[] arr = line.split(",");
                    if (arr.length < 9) throw new Exception("字段不足");
                    Flight f = new Flight();
                    f.setId(++maxId);
                    f.setCode(arr[1]);
                    f.setOrigin(arr[2]);
                    f.setDestination(arr[3]);
                    f.setDepartTime(java.time.LocalDateTime.parse(arr[4], java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                    f.setArriveTime(java.time.LocalDateTime.parse(arr[5], java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                    f.setPrice(Double.parseDouble(arr[6]));
                    f.setSeatsTotal(Integer.parseInt(arr[7]));
                    f.setSeatsLeft(Integer.parseInt(arr[8]));
                    if (arr.length >= 10) f.setDuration(Integer.parseInt(arr[9]));
                    else f.setDuration(0);
                    flights.add(f);
                    imported++;
                } catch (Exception ex) {
                    failed++;
                }
            }
            new FlightDao().saveAll(flights);
            flightPanel.loadData();
            JOptionPane.showMessageDialog(this, "导入完成：成功"+imported+"条，失败"+failed+"条");
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "导入失败："+ex.getMessage());
        }
    }
}
