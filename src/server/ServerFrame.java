package server;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ServerFrame extends JFrame {
    private JTextArea txtLog;
    private JList<String> clientList;
    private DefaultListModel<String> clientListModel;

    private JLabel lblStatus;
    private JButton btnToggleServer, btnBroadcast;
    private JTextField txtBroadcast;

    private boolean isRunning = false;

    public ServerFrame() {
        setTitle("Quản Lý Máy Chủ - Chat Server Management");
        setSize(850, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // --- TOP: THANH TRẠNG THÁI & ĐIỀU KHIỂN ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 246, 247));
        topPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        statusPanel.setBackground(new Color(245, 246, 247));

        JLabel lblTitle = new JLabel("Trạng thái Server:");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));

        lblStatus = new JLabel("Đang dừng (STOPPED)");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblStatus.setForeground(new Color(255, 59, 48));

        statusPanel.add(lblTitle);
        statusPanel.add(lblStatus);

        btnToggleServer = new JButton("Khởi động Server");
        styleButton(btnToggleServer, new Color(49, 162, 76), Color.WHITE); // Xanh lá

        topPanel.add(statusPanel, BorderLayout.WEST);
        topPanel.add(btnToggleServer, BorderLayout.EAST);

        // --- CENTER: CHIA 2 CỘT (LOG HOẠT ĐỘNG & DANH SÁCH CLIENT) ---
        // Cột trái: Log hoạt động (Chiếm phần lớn)
        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtLog.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane scrollLog = new JScrollPane(txtLog);
        scrollLog.setBorder(BorderFactory.createTitledBorder(" Nhật ký hoạt động (System Logs) "));

        // Cột phải: Danh sách client đang online
        clientListModel = new DefaultListModel<>();
        clientList = new JList<>(clientListModel);
        clientList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        clientList.setBackground(new Color(250, 250, 250));
        JScrollPane scrollClients = new JScrollPane(clientList);
        scrollClients.setPreferredSize(new Dimension(220, 0));
        scrollClients.setBorder(BorderFactory.createTitledBorder(" Client đang kết nối "));

        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollLog, scrollClients);
        centerSplit.setDividerLocation(580);
        centerSplit.setResizeWeight(0.8);
        centerSplit.setBorder(new EmptyBorder(10, 20, 10, 20));

        // --- BOTTOM: GỬI THÔNG BÁO HỆ THỐNG ---
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.setBackground(new Color(245, 246, 247));
        bottomPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        txtBroadcast = new JTextField();
        txtBroadcast.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtBroadcast.setPreferredSize(new Dimension(0, 38));
        txtBroadcast.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 222, 225), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        btnBroadcast = new JButton("Gửi thông báo toàn hệ thống");
        styleButton(btnBroadcast, new Color(10, 124, 255), Color.WHITE); // Xanh Messenger

        bottomPanel.add(new JLabel("Thông báo: "), BorderLayout.WEST);
        bottomPanel.add(txtBroadcast, BorderLayout.CENTER);
        bottomPanel.add(btnBroadcast, BorderLayout.EAST);

        // --- GÉP VÀO FRAME CHÍNH ---
        add(topPanel, BorderLayout.NORTH);
        add(centerSplit, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setPreferredSize(new Dimension(180, 38));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // --- CÁC HÀM ĐỂ CONTROLLER / SERVER GỌI CẬP NHẬT GIAO DIỆN ---
    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            txtLog.append("[" + java.time.LocalTime.now().toString().substring(0, 8) + "] " + message + "\n");
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });
    }

    public void updateClientList(String[] clients) {
        SwingUtilities.invokeLater(() -> {
            clientListModel.clear();
            for (String c : clients) {
                clientListModel.addElement(c);
            }
        });
    }

    public void setServerRunningState(boolean running) {
        this.isRunning = running;
        if (running) {
            lblStatus.setText("Đang chạy (RUNNING - Port 5000)");
            lblStatus.setForeground(new Color(49, 162, 76));
            btnToggleServer.setText("Dừng Server");
            btnToggleServer.setBackground(new Color(255, 59, 48));
        } else {
            lblStatus.setText("Đang dừng (STOPPED)");
            lblStatus.setForeground(new Color(255, 59, 48));
            btnToggleServer.setText("Khởi động Server");
            btnToggleServer.setBackground(new Color(49, 162, 76));
        }
    }

    public String getBroadcastText() {
        return txtBroadcast.getText().trim();
    }

    public void clearBroadcastText() {
        txtBroadcast.setText("");
    }

    // Lắng nghe sự kiện từ Controller
    public void setToggleServerListener(java.awt.event.ActionListener l) {
        btnToggleServer.addActionListener(l);
    }

    public void setBroadcastListener(java.awt.event.ActionListener l) {
        btnBroadcast.addActionListener(l);
        txtBroadcast.addActionListener(l);
    }
}