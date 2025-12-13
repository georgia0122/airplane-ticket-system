import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;


public class SeatSelectionDialog extends JDialog {
        // 新增：支持外部传入舱位
        public SeatSelectionDialog(Frame owner, int seatCount, Set<String> soldSeats, int selectedCabin) {
            this(owner, seatCount, soldSeats);
            this.selectedCabin = selectedCabin;
            updateSeatEnableByCabin();
            // 默认选中舱位按钮（外部传入时）
            if (cabinBtns != null && selectedCabin >= 0 && selectedCabin < cabinBtns.length) {
                cabinBtns[selectedCabin].setSelected(true);
            }
        }
    private int rows = 10; // 10排
    private int cols = 6;  // 6列（A~F）
    private JButton[][] seatButtons;
    private Set<String> selectedSeats = new HashSet<>();
    private boolean confirmed = false;
    private JLabel infoLabel;
    private Set<String> soldSeats;
    private int seatCount;
    private int selectedCabin = -1; // 0经济舱 1商务舱 2头等舱 (与 BookingPanel 对应)
    private JToggleButton[] cabinBtns;

    public SeatSelectionDialog(Frame owner, int seatCount, Set<String> soldSeats) {
        super(owner, "机舱选座", true);
        this.soldSeats = soldSeats != null ? soldSeats : new HashSet<>();
        this.seatCount = seatCount;
        setLayout(new BorderLayout(10,10));

        JPanel mainPanel = new JPanel(new BorderLayout());
        JLabel title = new JLabel("请选择座位（先选舱位，再选座位。浅蓝：已选，灰色：不可选，金色：头等舱，银色：商务舱，白色：经济舱，红色：已售）", JLabel.CENTER);
        title.setFont(new Font("微软雅黑", Font.BOLD, 16));
        title.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel cabinPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 6));
        String[] cabins = {"经济舱", "商务舱", "头等舱"};
        cabinBtns = new JToggleButton[3];
        ButtonGroup cabinGroup = new ButtonGroup();
        for (int i = 0; i < 3; i++) {
            cabinBtns[i] = new JToggleButton(cabins[i]);
            cabinBtns[i].setFont(new Font("微软雅黑", Font.BOLD, 15));
            cabinGroup.add(cabinBtns[i]);
            cabinPanel.add(cabinBtns[i]);
            final int cabinIdx = i;
            cabinBtns[i].addActionListener(e -> {
                selectedCabin = cabinIdx;
                updateSeatEnableByCabin();
            });
        }
        mainPanel.add(cabinPanel, BorderLayout.SOUTH);

        JPanel seatPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(220,220,220));
                g.fillRoundRect(20, 10, getWidth()-40, getHeight()-20, 40, 40); // 机舱轮廓
            }
        };
        seatPanel.setLayout(new GridBagLayout());
        seatPanel.setOpaque(false);
        seatButtons = new JButton[rows][cols];
        char[] colNames = {'A','B','C','D','E','F'};
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        for (int r = 0; r < rows; r++) {
            final int rowIdx = r;
            for (int c = 0; c < cols; c++) {
                final int colIdx = c;
                String seatNo = (rowIdx+1) + String.valueOf(colNames[colIdx]);
                JButton btn = new JButton(seatNo);
                btn.setFocusPainted(false);
                btn.setFont(new Font("微软雅黑", Font.BOLD, 13));
                btn.setPreferredSize(new Dimension(44,36));
                btn.setBorder(BorderFactory.createLineBorder(new Color(180,180,180), 1, true));
                // 舱位分区：1-2排头等舱，3-5排商务舱，其余经济舱
                if (rowIdx < 2) {
                    btn.setBackground(new Color(255,215,0)); // 头等舱金色
                } else if (rowIdx < 5) {
                    btn.setBackground(new Color(192,192,192)); // 商务舱银色
                } else {
                    btn.setBackground(Color.WHITE); // 经济舱
                }
                // 已售座位禁用
                if (soldSeats.contains(seatNo)) {
                    btn.setEnabled(false);
                    btn.setBackground(new Color(220,50,50)); // 红色
                    btn.setToolTipText("已售/不可选");
                }
                btn.addActionListener(e -> {
                    if (!btn.isEnabled()) return;
                    if (selectedCabin == -1) {
                        JOptionPane.showMessageDialog(this, "请先选择舱位等级");
                        return;
                    }
                    if (!isSeatInCabin(rowIdx, selectedCabin)) return;
                    if (selectedSeats.contains(seatNo)) {
                        selectedSeats.remove(seatNo);
                        // 恢复原色
                        if (rowIdx < 2) btn.setBackground(new Color(255,215,0));
                        else if (rowIdx < 5) btn.setBackground(new Color(192,192,192));
                        else btn.setBackground(Color.WHITE);
                    } else {
                        if (selectedSeats.size() >= seatCount) {
                            JOptionPane.showMessageDialog(this, "本次最多可选"+seatCount+"个座位");
                            return;
                        }
                        selectedSeats.add(seatNo);
                        btn.setBackground(new Color(135,206,250)); // 浅蓝高亮
                    }
                    infoLabel.setText("已选座位: " + selectedSeats);
                });
                seatButtons[rowIdx][colIdx] = btn;
                gbc.gridx = colIdx;
                gbc.gridy = rowIdx;
                seatPanel.add(btn, gbc);
            }
        }
        mainPanel.add(seatPanel, BorderLayout.CENTER);

        infoLabel = new JLabel("已选座位: ");
        infoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        infoLabel.setBorder(BorderFactory.createEmptyBorder(8,0,8,0));
        mainPanel.add(infoLabel, BorderLayout.NORTH);

        add(mainPanel, BorderLayout.CENTER);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = new JButton("确定");
        JButton btnCancel = new JButton("取消");
        btnOk.setFont(new Font("微软雅黑", Font.BOLD, 15));
        btnCancel.setFont(new Font("微软雅黑", Font.PLAIN, 15));
        btnPanel.add(btnOk); btnPanel.add(btnCancel);
        add(btnPanel, BorderLayout.SOUTH);
        btnOk.addActionListener(e -> {
            if (selectedSeats.size() != seatCount) {
                JOptionPane.showMessageDialog(this, "请选满"+seatCount+"个座位");
                return;
            }
            confirmed = true;
            setVisible(false);
        });
        btnCancel.addActionListener(e -> {
            confirmed = false;
            setVisible(false);
        });
        setSize(800, 600);
        setLocationRelativeTo(owner);
    }

    // 判断座位是否属于当前舱位
    private boolean isSeatInCabin(int rowIdx, int cabinIdx) {
        // Mapping: 0 = 经济舱 (rows >=5), 1 = 商务舱 (rows 2-4), 2 = 头等舱 (rows 0-1)
        if (cabinIdx == 0) return rowIdx >= 5;
        if (cabinIdx == 1) return rowIdx >= 2 && rowIdx < 5;
        if (cabinIdx == 2) return rowIdx < 2;
        return false;
    }

    // 根据选中舱位更新座位可选状态
    private void updateSeatEnableByCabin() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                JButton btn = seatButtons[r][c];
                String seatNo = (r+1) + String.valueOf((char)('A'+c));
                if (soldSeats.contains(seatNo)) continue;
                if (selectedCabin == -1) {
                    btn.setEnabled(true);
                    if (r < 2) btn.setBackground(new Color(255,215,0));
                    else if (r < 5) btn.setBackground(new Color(192,192,192));
                    else btn.setBackground(Color.WHITE);
                } else if (isSeatInCabin(r, selectedCabin)) {
                    btn.setEnabled(true);
                    if (r < 2) btn.setBackground(new Color(255,215,0));
                    else if (r < 5) btn.setBackground(new Color(192,192,192));
                    else btn.setBackground(Color.WHITE);
                } else {
                    btn.setEnabled(false);
                    btn.setBackground(new Color(200,200,200)); // 灰色禁用
                }
            }
        }
    }

    public boolean isConfirmed() { return confirmed; }
    public Set<String> getSelectedSeats() { return selectedSeats; }
    public String getSelectedSeatsStr() {
        return String.join(",", selectedSeats);
    }
}
