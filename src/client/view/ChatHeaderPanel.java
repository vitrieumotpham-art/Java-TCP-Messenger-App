package client.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ChatHeaderPanel extends JPanel {
    private JLabel lblHeaderName;
    private JLabel lblHeaderStatus;
    private JPanel headerAvatarPanel;
    private boolean isOnline = false;
    private String currentInitial = "?";
    private final Color MESSENGER_BLUE = new Color(10, 124, 255);

    public ChatHeaderPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 222, 225)),
                new EmptyBorder(10, 20, 10, 20)
        ));

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        infoPanel.setBackground(Color.WHITE);

        // Vẽ Avatar nhỏ trên Header kèm chấm trạng thái
        headerAvatarPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(228, 230, 235));
                g2.fillOval(0, 0, 46, 46);
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(currentInitial, (46 - fm.stringWidth(currentInitial)) / 2, (46 - fm.getHeight()) / 2 + fm.getAscent());

                g2.setColor(Color.WHITE);
                g2.fillOval(32, 32, 14, 14);
                if (isOnline) g2.setColor(new Color(49, 162, 76));
                else g2.setColor(new Color(160, 160, 160));
                g2.fillOval(34, 34, 10, 10);
                g2.dispose();
            }
        };
        headerAvatarPanel.setPreferredSize(new Dimension(46, 46));
        headerAvatarPanel.setOpaque(false);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);

        lblHeaderName = new JLabel("Mọi người");
        lblHeaderName.setFont(new Font("Segoe UI", Font.BOLD, 17));

        lblHeaderStatus = new JLabel("Phòng chat chung");
        lblHeaderStatus.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblHeaderStatus.setForeground(new Color(138, 141, 145));

        textPanel.add(lblHeaderName);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(lblHeaderStatus);

        infoPanel.add(headerAvatarPanel);
        infoPanel.add(textPanel);

        // Các nút icon gọi thoại/video vector bên phải header
        JPanel toolsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 8));
        toolsPanel.setBackground(Color.WHITE);

        JPanel btnCall = createVectorCallIcon();
        JPanel btnVideo = createVectorVideoIcon();

        toolsPanel.add(btnCall);
        toolsPanel.add(btnVideo);

        add(infoPanel, BorderLayout.WEST);
        add(toolsPanel, BorderLayout.EAST);
    }

    public void updateHeaderInfo(String realName, boolean online) {
        this.currentInitial = realName.isEmpty() ? "?" : realName.substring(0, 1).toUpperCase();
        this.isOnline = online;

        lblHeaderName.setText(realName);
        if (realName.equals("Mọi người")) {
            lblHeaderStatus.setText("Phòng chat chung");
            lblHeaderStatus.setForeground(new Color(138, 141, 145));
        } else if (online) {
            lblHeaderStatus.setText("Đang hoạt động");
            lblHeaderStatus.setForeground(new Color(49, 162, 76));
        } else {
            lblHeaderStatus.setText("Ngoại tuyến");
            lblHeaderStatus.setForeground(new Color(138, 141, 145));
        }
        headerAvatarPanel.repaint();
    }

    private JPanel createVectorCallIcon() {
        JPanel btn = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MESSENGER_BLUE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawArc(4, 4, 16, 16, 45, 180);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(24, 24));
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createVectorVideoIcon() {
        JPanel btn = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(MESSENGER_BLUE);
                g2.drawRect(2, 6, 14, 12);
                int[] xPoints = {18, 22, 22, 18};
                int[] yPoints = {9, 6, 18, 15};
                g2.fillPolygon(xPoints, yPoints, 4);
                g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(26, 24));
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}