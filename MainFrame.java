import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;

class Contact {
    private int id;
    private String name, relationship, notes;
    private LocalDate birthDate;
    private int reminderDays = 7;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate d) { this.birthDate = d; }
    public String getRelationship() { return relationship; }
    public void setRelationship(String r) { this.relationship = r; }
    public String getNotes() { return notes; }
    public void setNotes(String n) { this.notes = n; }
    public int getReminderDays() { return reminderDays; }
    public void setReminderDays(int r) { this.reminderDays = r; }

    public int getAge() {
        return birthDate == null ? 0 : Period.between(birthDate, LocalDate.now()).getYears();
    }

    public long getDaysUntilBirthday() {
        if (birthDate == null) return -1;
        LocalDate today = LocalDate.now();
        LocalDate next = birthDate.withYear(today.getYear());
        if (next.isBefore(today)) next = next.plusYears(1);
        return ChronoUnit.DAYS.between(today, next);
    }

    public boolean isBirthdayToday() {
        LocalDate today = LocalDate.now();
        return birthDate.getMonthValue() == today.getMonthValue()
                && birthDate.getDayOfMonth() == today.getDayOfMonth();
    }
}

// ===================== DATE UTILITIES =====================
class DateUtils {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public static LocalDate parse(String date) { return LocalDate.parse(date, FORMAT); }

    public static boolean isValid(String date) {
        try { parse(date); return true; } catch (Exception e) { return false; }
    }

    public static String display(LocalDate date) { return date == null ? "" : date.format(DISPLAY); }
}

// ===================== DATABASE MANAGER =====================
class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/birthday_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";
    private static Connection connection;

    public static void setupDatabase() {
        try {
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC", USER, PASSWORD);
            Statement st = con.createStatement();
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS birthday_db");
            st.executeUpdate("USE birthday_db");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS contacts (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) NOT NULL, " +
                    "birth_date DATE NOT NULL, relationship VARCHAR(50), " +
                    "reminder_days INT DEFAULT 7, notes TEXT)");
            st.close();
            con.close();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return connection;
    }
}


class ContactDAO {

    public boolean add(Contact c) {
        String sql = "INSERT INTO contacts (name,birth_date,relationship,reminder_days,notes) VALUES (?,?,?,?,?)";
        try {
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, c.getName());
            ps.setDate(2, java.sql.Date.valueOf(c.getBirthDate()));
            ps.setString(3, c.getRelationship());
            ps.setInt(4, c.getReminderDays());
            ps.setString(5, c.getNotes());
            boolean result = ps.executeUpdate() > 0;
            if (result) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) c.setId(rs.getInt(1));
                rs.close();
            }
            ps.close();
            return result;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(Contact c) {
        String sql = "UPDATE contacts SET name=?,birth_date=?,relationship=?,reminder_days=?,notes=? WHERE id=?";
        try {
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql);
            ps.setString(1, c.getName());
            ps.setDate(2, java.sql.Date.valueOf(c.getBirthDate()));
            ps.setString(3, c.getRelationship());
            ps.setInt(4, c.getReminderDays());
            ps.setString(5, c.getNotes());
            ps.setInt(6, c.getId());
            boolean result = ps.executeUpdate() > 0;
            ps.close();
            return result;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(int id) {
        try {
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement("DELETE FROM contacts WHERE id=?");
            ps.setInt(1, id);
            boolean result = ps.executeUpdate() > 0;
            ps.close();
            return result;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Contact> getAll() {
        List<Contact> list = new ArrayList<>();
        try {
            Statement st = DatabaseManager.getConnection().createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM contacts ORDER BY name");
            while (rs.next()) list.add(mapRow(rs));
            rs.close();
            st.close();
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Contact getById(int id) {
        try {
            PreparedStatement ps = DatabaseManager.getConnection().prepareStatement("SELECT * FROM contacts WHERE id=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Contact c = mapRow(rs);
                rs.close();
                ps.close();
                return c;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private Contact mapRow(ResultSet rs) throws SQLException {
        Contact c = new Contact();
        c.setId(rs.getInt("id"));
        c.setName(rs.getString("name"));
        c.setBirthDate(rs.getDate("birth_date").toLocalDate());
        c.setRelationship(rs.getString("relationship"));
        c.setReminderDays(rs.getInt("reminder_days"));
        c.setNotes(rs.getString("notes"));
        return c;
    }
}

public class MainFrame extends JFrame {
    private ContactDAO dao;
    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public MainFrame() {
        dao = new ContactDAO();
        setTitle("Birthday Reminder System");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        createUI();
        loadData();
        checkTodayBirthdays();
    }

    private void createUI() {
        getContentPane().setLayout(new BorderLayout(0, 10));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton add = new JButton("Add Birthday");
        JButton edit = new JButton("Edit");
        JButton delete = new JButton("Delete");
        JButton refresh = new JButton("Refresh");
        searchField = new JTextField(15);
        JButton search = new JButton("Find");

        add.addActionListener(e -> addBirthday());
        edit.addActionListener(e -> editBirthday());
        delete.addActionListener(e -> deleteBirthday());
        refresh.addActionListener(e -> loadData());
        search.addActionListener(e -> search());
        searchField.addActionListener(e -> search());

        top.add(add); top.add(edit); top.add(delete); top.add(refresh);
        top.add(new JLabel("   Search:")); top.add(searchField); top.add(search);
        add(top, BorderLayout.NORTH);

        String[] columns = {"ID", "Name", "Birthday", "Age", "Days Left", "Relationship"};
        model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);
        table.setGridColor(new Color(230, 230, 230));

        table.getColumnModel().getColumn(0).setPreferredWidth(40);   // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(160);  // Name
        table.getColumnModel().getColumn(2).setPreferredWidth(120);  // Birthday
        table.getColumnModel().getColumn(3).setPreferredWidth(60);   // Age
        table.getColumnModel().getColumn(4).setPreferredWidth(90);   // Days Left
        table.getColumnModel().getColumn(5).setPreferredWidth(120);  // Relationship

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadData() {
        model.setRowCount(0);
        for (Contact c : dao.getAll()) addRow(c);
    }

    private void addRow(Contact c) {
        model.addRow(new Object[]{c.getId(), c.getName(), c.getBirthDate().format(DISPLAY),
                c.getAge(), c.getDaysUntilBirthday(), c.getRelationship()});
    }

    private JPanel buildFormPanel(JTextField name, JTextField date, JComboBox<String> relation, JTextField notes) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("Name:")); panel.add(name);
        panel.add(new JLabel("Birthday (yyyy-MM-dd):")); panel.add(date);
        panel.add(new JLabel("Relationship:")); panel.add(relation);
        panel.add(new JLabel("Notes:")); panel.add(notes);
        return panel;
    }

    private JComboBox<String> relationBox() {
        return new JComboBox<>(new String[]{"Friend", "Family", "Classmate", "Colleague", "Other"});
    }

    private void addBirthday() {
        JTextField name = new JTextField(), date = new JTextField(), notes = new JTextField();
        JComboBox<String> relation = relationBox();
        JPanel panel = buildFormPanel(name, date, relation, notes);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Birthday", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String nameText = name.getText().trim(), dateText = date.getText().trim();
        if (nameText.isEmpty()) { showError("Please enter a name."); return; }
        if (dateText.isEmpty()) { showError("Please enter a birthday."); return; }
        if (!DateUtils.isValid(dateText)) { showError("Invalid date! Use YYYY-MM-DD, e.g. 2006-12-07"); return; }

        LocalDate birthDate = DateUtils.parse(dateText);
        if (birthDate.isAfter(LocalDate.now())) { showError("Birthday cannot be a future date."); return; }

        Contact c = new Contact();
        c.setName(nameText);
        c.setBirthDate(birthDate);
        c.setRelationship((String) relation.getSelectedItem());
        c.setNotes(notes.getText().trim());

        if (dao.add(c)) { JOptionPane.showMessageDialog(this, "Birthday added successfully."); loadData(); }
        else showError("Unable to save birthday.");
    }

    private void editBirthday() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Please select a birthday to edit."); return; }

        int id = (int) model.getValueAt(row, 0);
        Contact c = dao.getById(id);
        if (c == null) return;

        JTextField name = new JTextField(c.getName());
        JTextField date = new JTextField(c.getBirthDate().toString());
        JTextField notes = new JTextField(c.getNotes());
        JComboBox<String> relation = relationBox();
        relation.setSelectedItem(c.getRelationship());
        JPanel panel = buildFormPanel(name, date, relation, notes);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Birthday", JOptionPane.OK_CANCEL_OPTION);
        if (result != JOptionPane.OK_OPTION) return;

        String dateText = date.getText().trim();
        if (!DateUtils.isValid(dateText)) { showError("Invalid date! Use YYYY-MM-DD."); return; }

        LocalDate birthDate = DateUtils.parse(dateText);
        if (birthDate.isAfter(LocalDate.now())) { showError("Birthday cannot be a future date."); return; }

        c.setName(name.getText().trim());
        c.setBirthDate(birthDate);
        c.setRelationship((String) relation.getSelectedItem());
        c.setNotes(notes.getText().trim());

        if (dao.update(c)) { JOptionPane.showMessageDialog(this, "Birthday updated successfully."); loadData(); }
    }

    private void deleteBirthday() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Please select a birthday to delete."); return; }

        int id = (int) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete " + name + "'s birthday?", "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION && dao.delete(id)) {
            loadData();
            JOptionPane.showMessageDialog(this, "Birthday deleted successfully.");
        }
    }

    private void search() {
        String keyword = searchField.getText().trim().toLowerCase();
        model.setRowCount(0);
        for (Contact c : dao.getAll()) {
            if (c.getName().toLowerCase().contains(keyword) || c.getRelationship().toLowerCase().contains(keyword))
                addRow(c);
        }
    }

    private void checkTodayBirthdays() {
        StringBuilder message = new StringBuilder();
        for (Contact c : dao.getAll()) {
            if (c.isBirthdayToday()) message.append(c.getName()).append("\n");
        }
        if (message.length() > 0) {
            JOptionPane.showMessageDialog(this, "Today's Birthdays:\n\n" + message,
                    "Birthday Reminder", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        DatabaseManager.setupDatabase();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
