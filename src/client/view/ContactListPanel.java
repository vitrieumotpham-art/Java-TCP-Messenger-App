package client.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

public class ContactListPanel extends JPanel {
    private JList<String> userList;
    private DefaultListModel<String> listModel;
    private JTextField txtSearch;

    public ContactListPanel(String ownerUsername) {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 246, 247));
        setPreferredSize(new Dimension(300, 0));

        // Top container (Profile header + Ô tìm kiếm)
        JPanel leftTopContainer = new JPanel();
        leftTopContainer.setLayout(new BoxLayout(leftTopContainer, BoxLayout.Y_AXIS));
        leftTopContainer.setBackground(new Color(245, 246, 247));

        // Ô tìm kiếm
        txtSearch = new JTextField(" Tìm kiếm...");
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.setBackground(new Color(228, 230, 235));
        txtSearch.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

        JPanel searchWrapper = new JPanel(new BorderLayout());
        searchWrapper.setBackground(new Color(245, 246, 247));
        searchWrapper.setBorder(new EmptyBorder(5, 15, 10, 15));
        searchWrapper.add(txtSearch, BorderLayout.CENTER);

        leftTopContainer.add(searchWrapper);

        // Danh sách user
        listModel = new DefaultListModel<>();
        userList = new JList<>(listModel);
        styleUserList(userList);
        JScrollPane scrollList = new JScrollPane(userList);
        scrollList.setBorder(null);

        add(leftTopContainer, BorderLayout.NORTH);
        add(scrollList, BorderLayout.CENTER);
    }

    private void styleUserList(JList<String> list) {
        list.setBackground(new Color(245, 246, 247));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(70);
        list.setCellRenderer((list1, value, index, isSelected, cellHasFocus) -> {
            boolean isOnline = value.contains("🟢");
            String realName = extractRealName(value);

            JPanel panel = new JPanel(new BorderLayout(15, 0));
            panel.setBorder(new EmptyBorder(10, 20, 10, 20));
            if (isSelected) panel.setBackground(new Color(235, 237, 240));
            else panel.setBackground(new Color(245, 246, 247));

            JPanel avatar = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(228, 230, 235));
                    g2.fillOval(0, 0, 50, 50);
                    g2.setColor(Color.BLACK);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
                    String initial = realName.isEmpty() ? "?" : realName.substring(0, 1).toUpperCase();
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(initial, (50 - fm.stringWidth(initial)) / 2, (50 - fm.getHeight()) / 2 + fm.getAscent());
                    g2.setColor(panel.getBackground());
                    g2.fillOval(34, 34, 16, 16);
                    if (isOnline) g2.setColor(new Color(49, 162, 76));
                    else g2.setColor(new Color(160, 160, 160));
                    g2.fillOval(36, 36, 12, 12);
                    g2.dispose();
                }
            };
            avatar.setPreferredSize(new Dimension(50, 50));
            avatar.setOpaque(false);

            JLabel lblName = new JLabel(realName);
            lblName.setFont(new Font("Segoe UI", isOnline ? Font.BOLD : Font.PLAIN, 16));
            lblName.setForeground(Color.BLACK);

            panel.add(avatar, BorderLayout.WEST);
            panel.add(lblName, BorderLayout.CENTER);
            return panel;
        });
    }

    private String extractRealName(String displayName) {
        if (displayName != null && displayName.contains(" (")) {
            return displayName.substring(0, displayName.indexOf(" ("));
        }
        return displayName;
    }

    public void updateOnlineUsers(String[] users) {
        String currentSelection = userList.getSelectedValue();
        listModel.clear();
        for (String u : users) {
            listModel.addElement(u);
        }
        if (currentSelection != null) {
            userList.setSelectedValue(currentSelection, true);
        } else if (listModel.getSize() > 0) {
            userList.setSelectedIndex(0);
        }
    }

    public String getSelectedUser() { return userList.getSelectedValue(); }
    public void setListSelectionListener(ListSelectionListener l) { userList.addListSelectionListener(l); }
}