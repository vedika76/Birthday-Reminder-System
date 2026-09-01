import java.sql.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;


// ============================================================
// PLACEHOLDER TEXT FIELD
// ============================================================

class PlaceholderTextField extends JTextField {

    private String placeholder;

    public PlaceholderTextField(String placeholder) {

        this.placeholder = placeholder;

        setForeground(Color.BLACK);

        addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        if (getText().isEmpty() && !isFocusOwner()) {

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setColor(new Color(170, 170, 170));

            g2.setFont(getFont());

            FontMetrics fm = g2.getFontMetrics();

            int x = getInsets().left + 4;

            int y = (getHeight() - fm.getHeight()) / 2
                    + fm.getAscent();

            g2.drawString(
                    placeholder,
                    x,
                    y
            );

            g2.dispose();
        }
    }
}


// ============================================================
// CONTACT MODEL
// ============================================================

class Contact {

    private int id;
    private String name;
    private String email;
    private String phone;
    private String relationship;
    private String notes;

    private LocalDate birthDate;
    private int reminderDays;


    public Contact() {

        reminderDays = 7;
    }


    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
    }


    public String getPhone() {
        return phone;
    }


    public void setPhone(String phone) {
        this.phone = phone;
    }


    public LocalDate getBirthDate() {
        return birthDate;
    }


    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }


    public String getRelationship() {
        return relationship;
    }


    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }


    public int getReminderDays() {
        return reminderDays;
    }


    public void setReminderDays(int reminderDays) {
        this.reminderDays = reminderDays;
    }


    public String getNotes() {
        return notes;
    }


    public void setNotes(String notes) {
        this.notes = notes;
    }


    // ========================================================
    // CALCULATE AGE
    // ========================================================

    public int getAge() {

        if (birthDate == null) {
            return 0;
        }

        LocalDate today = LocalDate.now();

        return Period.between(
                birthDate,
                today
        ).getYears();
    }


    // ========================================================
    // CALCULATE DAYS UNTIL NEXT BIRTHDAY
    // ========================================================

    public long getDaysUntilBirthday() {

        if (birthDate == null) {
            return -1;
        }

        LocalDate today = LocalDate.now();

        LocalDate nextBirthday =
                birthDate.withYear(today.getYear());


        // If birthday already passed this year,
        // calculate for next year

        if (nextBirthday.isBefore(today)) {

            nextBirthday =
                    nextBirthday.plusYears(1);
        }


        return ChronoUnit.DAYS.between(
                today,
                nextBirthday
        );
    }


    // ========================================================
    // CHECK IF BIRTHDAY IS TODAY
    // ========================================================

    public boolean isBirthdayToday() {

        if (birthDate == null) {
            return false;
        }

        LocalDate today = LocalDate.now();

        return birthDate.getMonthValue()
                == today.getMonthValue()

                &&

                birthDate.getDayOfMonth()
                        == today.getDayOfMonth();
    }
}


// ============================================================
// DATE UTILITIES
// ============================================================

class DateUtils {

    private static final DateTimeFormatter DB_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");


    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy");


    public static LocalDate parseDate(String date) {

        return LocalDate.parse(
                date,
                DB_FORMAT
        );
    }


    public static String formatDate(LocalDate date) {

        if (date == null) {
            return "";
        }

        return date.format(
                DISPLAY_FORMAT
        );
    }


    public static String formatForDB(LocalDate date) {

        if (date == null) {
            return "";
        }

        return date.format(
                DB_FORMAT
        );
    }


    public static boolean isValidDate(String date) {

        try {

            parseDate(date);

            return true;

        } catch (Exception e) {

            return false;
        }
    }
}


// ============================================================
// DATABASE MANAGER
// ============================================================

class DatabaseManager {

    private static final String URL =
            "jdbc:mysql://localhost:3306/birthday_db?useSSL=false&serverTimezone=UTC";


    private static final String USER =
            "root";


    // CHANGE PASSWORD IF YOUR MYSQL PASSWORD IS DIFFERENT

    private static final String PASSWORD =
            "1234";


    private static Connection connection = null;


    public static Connection getConnection() {

        if (connection == null) {

            try {

                Class.forName(
                        "com.mysql.cj.jdbc.Driver"
                );


                connection =
                        DriverManager.getConnection(
                                URL,
                                USER,
                                PASSWORD
                        );


                System.out.println(
                        "Database connected successfully!"
                );


            } catch (Exception e) {

                System.out.println(
                        "Database connection failed!"
                );

                e.printStackTrace();
            }
        }


        return connection;
    }


    // ========================================================
    // SETUP DATABASE
    // ========================================================

    public static void setupDatabase() {

        try {

            Connection tempConnection =

                    DriverManager.getConnection(

                            "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC",

                            USER,

                            PASSWORD
                    );


            Statement statement =
                    tempConnection.createStatement();


            statement.executeUpdate(

                    "CREATE DATABASE IF NOT EXISTS birthday_db"
            );


            statement.executeUpdate(

                    "USE birthday_db"
            );


            statement.executeUpdate(

                    "CREATE TABLE IF NOT EXISTS contacts (" +

                            "id INT AUTO_INCREMENT PRIMARY KEY," +

                            "name VARCHAR(100) NOT NULL," +

                            "email VARCHAR(100)," +

                            "phone VARCHAR(20)," +

                            "birth_date DATE NOT NULL," +

                            "relationship VARCHAR(50)," +

                            "reminder_days INT DEFAULT 7," +

                            "notes TEXT)"
            );


            statement.close();

            tempConnection.close();


            System.out.println(
                    "Database setup complete!"
            );


        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}


// ============================================================
// DAO INTERFACE
// ============================================================

interface ContactDAO {

    boolean add(Contact contact);

    boolean update(Contact contact);

    boolean delete(int id);

    Contact getById(int id);

    List<Contact> getAll();

    List<Contact> getTodayBirthdays();

    List<Contact> getUpcoming(int days);

    List<Contact> search(String keyword);
}


// ============================================================
// DAO IMPLEMENTATION
// ============================================================

class ContactDAOImpl implements ContactDAO {


    @Override
    public boolean add(Contact contact) {

        String sql =

                "INSERT INTO contacts " +

                        "(name, email, phone, birth_date, relationship, reminder_days, notes) " +

                        "VALUES (?, ?, ?, ?, ?, ?, ?)";


        try (

                PreparedStatement preparedStatement =

                        DatabaseManager.getConnection()
                                .prepareStatement(
                                        sql,
                                        Statement.RETURN_GENERATED_KEYS
                                )
        ) {

            preparedStatement.setString(
                    1,
                    contact.getName()
            );


            preparedStatement.setString(
                    2,
                    ""
            );


            preparedStatement.setString(
                    3,
                    ""
            );


            preparedStatement.setDate(

                    4,

                    java.sql.Date.valueOf(
                            contact.getBirthDate()
                    )
            );


            preparedStatement.setString(
                    5,
                    contact.getRelationship()
            );


            preparedStatement.setInt(
                    6,
                    contact.getReminderDays()
            );


            preparedStatement.setString(
                    7,
                    contact.getNotes()
            );


            int rowsAffected =
                    preparedStatement.executeUpdate();


            if (rowsAffected > 0) {

                ResultSet generatedKeys =
                        preparedStatement.getGeneratedKeys();


                if (generatedKeys.next()) {

                    contact.setId(
                            generatedKeys.getInt(1)
                    );
                }


                generatedKeys.close();

                return true;
            }


            return false;


        } catch (SQLException e) {

            e.printStackTrace();

            return false;
        }
    }


    // ========================================================
    // UPDATE CONTACT
    // ========================================================

    @Override
    public boolean update(Contact contact) {

        String sql =

                "UPDATE contacts SET " +

                        "name=?, " +

                        "birth_date=?, " +

                        "relationship=?, " +

                        "reminder_days=?, " +

                        "notes=? " +

                        "WHERE id=?";


        try (

                PreparedStatement preparedStatement =

                        DatabaseManager.getConnection()
                                .prepareStatement(sql)
        ) {

            preparedStatement.setString(
                    1,
                    contact.getName()
            );


            preparedStatement.setDate(

                    2,

                    java.sql.Date.valueOf(
                            contact.getBirthDate()
                    )
            );


            preparedStatement.setString(
                    3,
                    contact.getRelationship()
            );


            preparedStatement.setInt(
                    4,
                    contact.getReminderDays()
            );


            preparedStatement.setString(
                    5,
                    contact.getNotes()
            );


            preparedStatement.setInt(
                    6,
                    contact.getId()
            );


            return preparedStatement.executeUpdate() > 0;


        } catch (SQLException e) {

            e.printStackTrace();

            return false;
        }
    }


    // ========================================================
    // DELETE CONTACT
    // ========================================================

    @Override
    public boolean delete(int id) {

        String sql =
                "DELETE FROM contacts WHERE id=?";


        try (

                PreparedStatement preparedStatement =

                        DatabaseManager.getConnection()
                                .prepareStatement(sql)
        ) {

            preparedStatement.setInt(
                    1,
                    id
            );


            return preparedStatement.executeUpdate() > 0;


        } catch (SQLException e) {

            e.printStackTrace();

            return false;
        }
    }


    // ========================================================
    // GET CONTACT BY ID
    // ========================================================

    @Override
    public Contact getById(int id) {

        String sql =
                "SELECT * FROM contacts WHERE id=?";


        try (

                PreparedStatement preparedStatement =

                        DatabaseManager.getConnection()
                                .prepareStatement(sql)
        ) {

            preparedStatement.setInt(
                    1,
                    id
            );


            ResultSet resultSet =
                    preparedStatement.executeQuery();


            if (resultSet.next()) {

                return extractContact(
                        resultSet
                );
            }


        } catch (SQLException e) {

            e.printStackTrace();
        }


        return null;
    }


    // ========================================================
    // GET ALL CONTACTS
    // ========================================================

    @Override
    public List<Contact> getAll() {

        List<Contact> contacts =
                new ArrayList<>();


        String sql =
                "SELECT * FROM contacts ORDER BY name";


        try (

                Statement statement =

                        DatabaseManager.getConnection()
                                .createStatement();

                ResultSet resultSet =

                        statement.executeQuery(sql)
        ) {

            while (resultSet.next()) {

                contacts.add(
                        extractContact(resultSet)
                );
            }


        } catch (SQLException e) {

            e.printStackTrace();
        }


        return contacts;
    }


    // ========================================================
    // GET TODAY BIRTHDAYS
    // ========================================================

    @Override
    public List<Contact> getTodayBirthdays() {

        List<Contact> allContacts =
                getAll();


        List<Contact> todayBirthdays =
                new ArrayList<>();


        for (Contact contact : allContacts) {

            if (contact.isBirthdayToday()) {

                todayBirthdays.add(contact);
            }
        }


        return todayBirthdays;
    }


    // ========================================================
    // GET UPCOMING BIRTHDAYS
    // ========================================================

    @Override
    public List<Contact> getUpcoming(int days) {

        List<Contact> allContacts =
                getAll();


        List<Contact> upcoming =
                new ArrayList<>();


        for (Contact contact : allContacts) {

            long daysLeft =
                    contact.getDaysUntilBirthday();


            if (daysLeft >= 0 && daysLeft <= days) {

                upcoming.add(contact);
            }
        }


        upcoming.sort(

                Comparator.comparingLong(
                        Contact::getDaysUntilBirthday
                )
        );


        return upcoming;
    }


    // ========================================================
    // SEARCH CONTACT
    // ========================================================

    @Override
    public List<Contact> search(String keyword) {

        List<Contact> contacts =
                new ArrayList<>();


        String sql =

                "SELECT * FROM contacts " +

                        "WHERE name LIKE ? " +

                        "OR relationship LIKE ? " +

                        "ORDER BY name";


        try (

                PreparedStatement preparedStatement =

                        DatabaseManager.getConnection()
                                .prepareStatement(sql)
        ) {

            String pattern =
                    "%" + keyword + "%";


            preparedStatement.setString(
                    1,
                    pattern
            );


            preparedStatement.setString(
                    2,
                    pattern
            );


            ResultSet resultSet =
                    preparedStatement.executeQuery();


            while (resultSet.next()) {

                contacts.add(
                        extractContact(resultSet)
                );
            }


        } catch (SQLException e) {

            e.printStackTrace();
        }


        return contacts;
    }


    // ========================================================
    // EXTRACT CONTACT
    // ========================================================

    private Contact extractContact(
            ResultSet resultSet
    ) throws SQLException {

        Contact contact =
                new Contact();


        contact.setId(
                resultSet.getInt("id")
        );


        contact.setName(
                resultSet.getString("name")
        );


        java.sql.Date sqlDate =
                resultSet.getDate("birth_date");


        if (sqlDate != null) {

            contact.setBirthDate(
                    sqlDate.toLocalDate()
            );
        }


        contact.setRelationship(
                resultSet.getString("relationship")
        );


        contact.setReminderDays(
                resultSet.getInt("reminder_days")
        );


        contact.setNotes(
                resultSet.getString("notes")
        );


        return contact;
    }
}


// ============================================================
// CONTROLLER
// ============================================================

class ContactController {

    private ContactDAO dao =
            new ContactDAOImpl();


    public boolean addContact(Contact contact) {

        if (contact == null ||
                contact.getName() == null ||
                contact.getName().trim().isEmpty()) {

            return false;
        }


        return dao.add(contact);
    }


    public boolean updateContact(Contact contact) {

        if (contact == null ||
                contact.getId() <= 0) {

            return false;
        }


        return dao.update(contact);
    }


    public boolean deleteContact(int id) {

        return dao.delete(id);
    }


    public Contact getContactById(int id) {

        return dao.getById(id);
    }


    public List<Contact> getAllContacts() {

        return dao.getAll();
    }


    public List<Contact> getTodayBirthdays() {

        return dao.getTodayBirthdays();
    }


    public List<Contact> getUpcomingBirthdays(
            int days
    ) {

        return dao.getUpcoming(days);
    }


    public List<Contact> searchContacts(
            String keyword
    ) {

        if (keyword == null ||
                keyword.trim().isEmpty()) {

            return dao.getAll();
        }


        return dao.search(
                keyword.trim()
        );
    }
}


// ============================================================
// ADD / EDIT BIRTHDAY DIALOG
// ============================================================

class AddEditContactDialog extends JDialog {

    private ContactController controller;
    private Contact contact;
    private boolean saved = false;

    private JTextField nameField;
    private JTextField dateField;
    private JTextField notesField;

    private JComboBox<String> relationshipCombo;
    private JSpinner reminderSpinner;


    public AddEditContactDialog(

            JFrame parent,
            ContactController controller,
            Contact contact
    ) {

        super(

                parent,

                contact == null
                        ? "Add Birthday"
                        : "Edit Birthday",

                true
        );


        this.controller = controller;
        this.contact = contact;


        initializeUI();


        if (contact != null) {
            loadContactData();
        }


        setSize(470, 500);

        setLocationRelativeTo(parent);

        setResizable(false);
    }


    // ========================================================
    // INITIALIZE UI
    // ========================================================

    private void initializeUI() {

        Color backgroundColor =
                new Color(245, 242, 250);

        Color cardColor =
                Color.WHITE;

        Color textColor =
                new Color(70, 60, 85);

        Color accentColor =
                new Color(190, 170, 215);


        JPanel mainPanel =
                new JPanel(
                        new BorderLayout(10, 10)
                );


        mainPanel.setBackground(
                backgroundColor
        );


        mainPanel.setBorder(

                BorderFactory.createEmptyBorder(
                        25,
                        30,
                        20,
                        30
                )
        );


        // HEADER

        JPanel headerPanel =
                new JPanel();


        headerPanel.setLayout(

                new BoxLayout(
                        headerPanel,
                        BoxLayout.Y_AXIS
                )
        );


        headerPanel.setBackground(
                backgroundColor
        );


        JLabel heading =
                new JLabel(

                        contact == null
                                ? "Add New Birthday"
                                : "Edit Birthday"
                );


        heading.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );


        heading.setFont(

                new Font(
                        "SansSerif",
                        Font.BOLD,
                        24
                )
        );


        heading.setForeground(
                textColor
        );


        JLabel subtitle =
                new JLabel(
                        "Keep track of every special day"
                );


        subtitle.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );


        subtitle.setFont(

                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        13
                )
        );


        subtitle.setForeground(
                new Color(140, 135, 150)
        );


        headerPanel.add(heading);

        headerPanel.add(
                Box.createVerticalStrut(6)
        );

        headerPanel.add(subtitle);


        // FORM CARD

        JPanel cardPanel =
                new JPanel(
                        new GridBagLayout()
                );


        cardPanel.setBackground(
                cardColor
        );


        cardPanel.setBorder(

                BorderFactory.createCompoundBorder(

                        BorderFactory.createLineBorder(
                                new Color(225, 220, 235),
                                1,
                                true
                        ),

                        BorderFactory.createEmptyBorder(
                                18,
                                20,
                                18,
                                20
                        )
                )
        );


        GridBagConstraints gbc =
                new GridBagConstraints();


        gbc.insets =
                new Insets(8, 5, 8, 5);


        gbc.fill =
                GridBagConstraints.HORIZONTAL;


        int row = 0;


        // NAME

        addLabel(
                cardPanel,
                "Name *",
                gbc,
                row
        );


        nameField =
                createTextField();


        addField(
                cardPanel,
                nameField,
                gbc,
                row
        );


        row++;


        // BIRTHDAY

        addLabel(
                cardPanel,
                "Birthday *",
                gbc,
                row
        );


        // Placeholder inside text field

        dateField =
                new PlaceholderTextField(
                        "YYYY-MM-DD"
                );


        dateField.setPreferredSize(
                new Dimension(190, 30)
        );


        dateField.setFont(

                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        13
                )
        );


        addField(
                cardPanel,
                dateField,
                gbc,
                row
        );


        row++;


        // RELATIONSHIP

        addLabel(
                cardPanel,
                "Relationship",
                gbc,
                row
        );


        String[] relationships = {

                "Friend",
                "Family",
                "Classmate",
                "Colleague",
                "Other"
        };


        relationshipCombo =
                new JComboBox<>(
                        relationships
                );


        relationshipCombo.setFont(

                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        13
                )
        );


        addField(
                cardPanel,
                relationshipCombo,
                gbc,
                row
        );


        row++;


        // REMINDER

        addLabel(
                cardPanel,
                "Remind Before",
                gbc,
                row
        );


        reminderSpinner =
                new JSpinner(

                        new SpinnerNumberModel(
                                7,
                                1,
                                30,
                                1
                        )
                );


        reminderSpinner.setFont(

                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        13
                )
        );


        addField(
                cardPanel,
                reminderSpinner,
                gbc,
                row
        );


        row++;


        // NOTES

        addLabel(
                cardPanel,
                "Notes",
                gbc,
                row
        );


        notesField =
                createTextField();


        addField(
                cardPanel,
                notesField,
                gbc,
                row
        );


        // BUTTONS

        JPanel buttonPanel =
                new JPanel(

                        new FlowLayout(
                                FlowLayout.CENTER,
                                15,
                                5
                        )
                );


        buttonPanel.setBackground(
                backgroundColor
        );


        JButton cancelButton =
                new JButton("Cancel");


        JButton saveButton =
                new JButton(

                        contact == null
                                ? "Save Birthday"
                                : "Update"
                );


        cancelButton.setPreferredSize(
                new Dimension(110, 36)
        );


        saveButton.setPreferredSize(
                new Dimension(130, 36)
        );


        cancelButton.setFocusPainted(false);
        saveButton.setFocusPainted(false);


        cancelButton.setFont(

                new Font(
                        "SansSerif",
                        Font.BOLD,
                        13
                )
        );


        saveButton.setFont(

                new Font(
                        "SansSerif",
                        Font.BOLD,
                        13
                )
        );


        cancelButton.setBackground(
                new Color(235, 232, 240)
        );


        saveButton.setBackground(
                accentColor
        );


        cancelButton.addActionListener(
                e -> dispose()
        );


        saveButton.addActionListener(
                e -> saveContact()
        );


        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);


        JPanel centerWrapper =
                new JPanel(
                        new BorderLayout()
                );


        centerWrapper.setBackground(
                backgroundColor
        );


        centerWrapper.setBorder(

                BorderFactory.createEmptyBorder(
                        18,
                        0,
                        5,
                        0
                )
        );


        centerWrapper.add(
                cardPanel,
                BorderLayout.CENTER
        );


        mainPanel.add(
                headerPanel,
                BorderLayout.NORTH
        );


        mainPanel.add(
                centerWrapper,
                BorderLayout.CENTER
        );


        mainPanel.add(
                buttonPanel,
                BorderLayout.SOUTH
        );


        add(mainPanel);


        getRootPane().setDefaultButton(
                saveButton
        );
    }


    private void addLabel(

            JPanel panel,
            String text,
            GridBagConstraints gbc,
            int row
    ) {

        gbc.gridx = 0;

        gbc.gridy = row;

        gbc.weightx = 0.35;


        JLabel label =
                new JLabel(text);


        label.setFont(

                new Font(
                        "SansSerif",
                        Font.BOLD,
                        13
                )
        );


        label.setForeground(
                new Color(80, 75, 95)
        );


        panel.add(label, gbc);
    }


    private void addField(

            JPanel panel,
            Component component,
            GridBagConstraints gbc,
            int row
    ) {

        gbc.gridx = 1;

        gbc.gridy = row;

        gbc.weightx = 0.65;


        panel.add(component, gbc);
    }


    private JTextField createTextField() {

        JTextField field =
                new JTextField();


        field.setPreferredSize(
                new Dimension(190, 30)
        );


        field.setFont(

                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        13
                )
        );


        return field;
    }


    // ========================================================
    // LOAD DATA
    // ========================================================

    private void loadContactData() {

        nameField.setText(
                contact.getName()
        );


        if (contact.getBirthDate() != null) {

            dateField.setText(

                    DateUtils.formatForDB(
                            contact.getBirthDate()
                    )
            );
        }


        if (contact.getRelationship() != null) {

            relationshipCombo.setSelectedItem(
                    contact.getRelationship()
            );
        }


        reminderSpinner.setValue(
                contact.getReminderDays()
        );


        notesField.setText(
                contact.getNotes()
        );
    }


    // ========================================================
    // SAVE CONTACT
    // ========================================================

    private void saveContact() {

        String name =
                nameField.getText().trim();


        if (name.isEmpty()) {

            JOptionPane.showMessageDialog(

                    this,

                    "Please enter a name.",

                    "Validation Error",

                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }


        String dateString =
                dateField.getText().trim();


        if (dateString.isEmpty()) {

            JOptionPane.showMessageDialog(

                    this,

                    "Please enter a birthday.",

                    "Validation Error",

                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }


        if (!DateUtils.isValidDate(dateString)) {

            JOptionPane.showMessageDialog(

                    this,

                    "Invalid date format.\nPlease use YYYY-MM-DD",

                    "Validation Error",

                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }


        try {

            LocalDate birthDate =
                    DateUtils.parseDate(
                            dateString
                    );


            if (contact == null) {

                contact =
                        new Contact();
            }


            contact.setName(name);


            contact.setBirthDate(
                    birthDate
            );


            contact.setRelationship(

                    (String)
                            relationshipCombo.getSelectedItem()
            );


            contact.setReminderDays(

                    (Integer)
                            reminderSpinner.getValue()
            );


            contact.setNotes(

                    notesField.getText().trim()
            );


            boolean success;


            if (contact.getId() == 0) {

                success =
                        controller.addContact(contact);

            } else {

                success =
                        controller.updateContact(contact);
            }


            if (success) {

                saved = true;

                dispose();

            } else {

                JOptionPane.showMessageDialog(

                        this,

                        "Unable to save birthday.",

                        "Error",

                        JOptionPane.ERROR_MESSAGE
                );
            }


        } catch (Exception e) {

            JOptionPane.showMessageDialog(

                    this,

                    "Error: " + e.getMessage(),

                    "Error",

                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    public boolean isSaved() {

        return saved;
    }
}


// ============================================================
// MAIN FRAME
// ============================================================

public class MainFrame extends JFrame {

    private ContactController controller;

    private JTable contactTable;

    private DefaultTableModel tableModel;

    private TableRowSorter<DefaultTableModel> rowSorter;

    private JTextField searchField;

    private JLabel statusLabel;

    private JLabel birthdayCountLabel;

    private JTabbedPane tabbedPane;

    private JList<String> birthdayList;

    private DefaultListModel<String> birthdayListModel;


    private static final DateTimeFormatter DISPLAY_FORMAT =

            DateTimeFormatter.ofPattern(
                    "dd MMM yyyy"
            );


    public MainFrame() {

        controller =
                new ContactController();


        initializeUI();

        loadData();

        checkBirthdays();
    }


    // ========================================================
    // INITIALIZE MAIN UI
    // ========================================================

    private void initializeUI() {

        setTitle(
                "Birthday Reminder System"
        );


        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );


        setSize(
                950,
                620
        );


        setMinimumSize(
                new Dimension(
                        800,
                        500
                )
        );


        setLocationRelativeTo(null);


        setLayout(
                new BorderLayout()
        );


        createMenuBar();

        createToolbar();

        createTabbedPane();

        createStatusBar();
    }


    // ========================================================
    // MENU BAR
    // ========================================================

    private void createMenuBar() {

        JMenuBar menuBar =
                new JMenuBar();


        JMenu fileMenu =
                new JMenu("File");


        JMenuItem addItem =
                new JMenuItem("Add Birthday");


        addItem.addActionListener(

                e ->
                        showAddEditDialog(null)
        );


        JMenuItem exitItem =
                new JMenuItem("Exit");


        exitItem.addActionListener(
                e -> System.exit(0)
        );


        fileMenu.add(addItem);

        fileMenu.addSeparator();

        fileMenu.add(exitItem);


        JMenu viewMenu =
                new JMenu("View");


        JMenuItem refreshItem =
                new JMenuItem("Refresh");


        refreshItem.addActionListener(
                e -> loadData()
        );


        JMenuItem todayItem =
                new JMenuItem("Today's Birthdays");


        todayItem.addActionListener(
                e -> showTodayBirthdays()
        );


        viewMenu.add(refreshItem);

        viewMenu.add(todayItem);


        JMenu helpMenu =
                new JMenu("Help");


        JMenuItem aboutItem =
                new JMenuItem("About");


        aboutItem.addActionListener(
                e -> showAboutDialog()
        );


        helpMenu.add(aboutItem);


        menuBar.add(fileMenu);

        menuBar.add(viewMenu);

        menuBar.add(helpMenu);


        setJMenuBar(menuBar);
    }


    // ========================================================
    // TOOLBAR
    // ========================================================

    private void createToolbar() {

        Color toolbarColor =
                new Color(245, 242, 250);


        JPanel toolbar =
                new JPanel(
                        new BorderLayout(15, 10)
                );


        toolbar.setBackground(
                toolbarColor
        );


        toolbar.setBorder(

                BorderFactory.createEmptyBorder(
                        12,
                        18,
                        12,
                        18
                )
        );


        JPanel buttonPanel =
                new JPanel(

                        new FlowLayout(
                                FlowLayout.LEFT,
                                8,
                                0
                        )
                );


        buttonPanel.setOpaque(false);


        JButton addButton =
                new JButton("Add Birthday");

        JButton editButton =
                new JButton("Edit");

        JButton deleteButton =
                new JButton("Delete");

        JButton refreshButton =
                new JButton("Refresh");


        styleToolbarButton(addButton);
        styleToolbarButton(editButton);
        styleToolbarButton(deleteButton);
        styleToolbarButton(refreshButton);


        addButton.addActionListener(
                e -> showAddEditDialog(null)
        );


        editButton.addActionListener(
                e -> editSelectedContact()
        );


        deleteButton.addActionListener(
                e -> deleteSelectedContact()
        );


        refreshButton.addActionListener(
                e -> loadData()
        );


        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);


        // SEARCH

        JPanel searchPanel =
                new JPanel(

                        new FlowLayout(
                                FlowLayout.RIGHT,
                                8,
                                0
                        )
                );


        searchPanel.setOpaque(false);


        JLabel searchLabel =
                new JLabel("Search");


        searchLabel.setFont(

                new Font(
                        "SansSerif",
                        Font.BOLD,
                        13
                )
        );


        searchField =
                new JTextField(15);


        searchField.setPreferredSize(
                new Dimension(170, 30)
        );


        JButton searchButton =
                new JButton("Find");


        styleToolbarButton(searchButton);


        searchButton.addActionListener(
                e -> searchContacts()
        );


        searchField.addActionListener(
                e -> searchContacts()
        );


        searchPanel.add(searchLabel);

        searchPanel.add(searchField);

        searchPanel.add(searchButton);


        toolbar.add(
                buttonPanel,
                BorderLayout.WEST
        );


        toolbar.add(
                searchPanel,
                BorderLayout.EAST
        );


        add(
                toolbar,
                BorderLayout.NORTH
        );
    }


    private void styleToolbarButton(
            JButton button
    ) {

        button.setFocusPainted(false);


        button.setFont(

                new Font(
                        "SansSerif",
                        Font.BOLD,
                        12
                )
        );


        button.setBackground(
                Color.WHITE
        );


        button.setPreferredSize(
                new Dimension(105, 30)
        );
    }


    // ========================================================
    // TABBED PANE
    // ========================================================

    private void createTabbedPane() {

        tabbedPane =
                new JTabbedPane();


        tabbedPane.setFont(

                new Font(
                        "SansSerif",
                        Font.BOLD,
                        13
                )
        );


        tabbedPane.addTab(
                "All Birthdays",
                createContactsPanel()
        );


        tabbedPane.addTab(
                "Birthday Reminders",
                createBirthdayReminderPanel()
        );


        add(
                tabbedPane,
                BorderLayout.CENTER
        );
    }


    // ========================================================
    // CONTACT TABLE
    // ========================================================

    private JPanel createContactsPanel() {

        JPanel panel =
                new JPanel(
                        new BorderLayout()
                );


        panel.setBackground(
                Color.WHITE
        );


        panel.setBorder(

                BorderFactory.createEmptyBorder(
                        18,
                        20,
                        18,
                        20
                )
        );


        String[] columns = {

                "ID",
                "Name",
                "Birthday",
                "Age",
                "Days Left",
                "Relationship"
        };


        tableModel =
                new DefaultTableModel(
                        columns,
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {

                        return false;
                    }
                };


        contactTable =
                new JTable(tableModel);


        contactTable.setRowHeight(38);


        contactTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );


        contactTable.setFont(

                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        14
                )
        );


        contactTable.setShowGrid(false);


        contactTable.setIntercellSpacing(
                new Dimension(0, 0)
        );


        contactTable.setSelectionBackground(
                new Color(225, 215, 238)
        );


        contactTable.getTableHeader().setFont(

                new Font(
                        "SansSerif",
                        Font.BOLD,
                        13
                )
        );


        contactTable.getTableHeader().setPreferredSize(
                new Dimension(0, 40)
        );


        contactTable.getTableHeader().setBackground(
                new Color(235, 230, 242)
        );


        rowSorter =
                new TableRowSorter<>(
                        tableModel
                );


        contactTable.setRowSorter(
                rowSorter
        );


        DefaultTableCellRenderer centerRenderer =
                new DefaultTableCellRenderer();


        centerRenderer.setHorizontalAlignment(
                SwingConstants.CENTER
        );


        for (int i = 0; i < columns.length; i++) {

            if (i != 1) {

                contactTable
                        .getColumnModel()
                        .getColumn(i)
                        .setCellRenderer(
                                centerRenderer
                        );
            }
        }


        contactTable
                .getColumnModel()
                .getColumn(0)
                .setPreferredWidth(45);


        contactTable
                .getColumnModel()
                .getColumn(1)
                .setPreferredWidth(200);


        contactTable
                .getColumnModel()
                .getColumn(2)
                .setPreferredWidth(140);


        contactTable
                .getColumnModel()
                .getColumn(3)
                .setPreferredWidth(70);


        contactTable
                .getColumnModel()
                .getColumn(4)
                .setPreferredWidth(100);


        contactTable
                .getColumnModel()
                .getColumn(5)
                .setPreferredWidth(140);


        JScrollPane scrollPane =
                new JScrollPane(contactTable);


        scrollPane.setBorder(

                BorderFactory.createLineBorder(
                        new Color(225, 220, 235)
                )
        );


        panel.add(
                scrollPane,
                BorderLayout.CENTER
        );


        contactTable.addMouseListener(

                new MouseAdapter() {

                    @Override
                    public void mouseClicked(
                            MouseEvent event
                    ) {

                        if (event.getClickCount() == 2) {

                            editSelectedContact();
                        }
                    }
                }
        );


        return panel;
    }


    // ========================================================
    // BIRTHDAY REMINDER PANEL
    // ========================================================

    private JPanel createBirthdayReminderPanel() {

        JPanel panel =
                new JPanel(
                        new BorderLayout(15, 15)
                );


        panel.setBackground(
                new Color(248, 247, 251)
        );


        panel.setBorder(

                BorderFactory.createEmptyBorder(
                        30,
                        35,
                        25,
                        35
                )
        );


        JPanel headerPanel =
                new JPanel();


        headerPanel.setLayout(

                new BoxLayout(
                        headerPanel,
                        BoxLayout.Y_AXIS
                )
        );


        headerPanel.setOpaque(false);


        JLabel titleLabel =
                new JLabel(
                        "Upcoming Birthday Reminders"
                );


        titleLabel.setFont(

                new Font(
                        "SansSerif",
                        Font.BOLD,
                        23
                )
        );


        titleLabel.setForeground(
                new Color(70, 60, 90)
        );


        titleLabel.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );


        JLabel subtitle =
                new JLabel(
                        "Birthdays coming up in the next 7 days"
                );


        subtitle.setFont(

                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        13
                )
        );


        subtitle.setForeground(
                new Color(130, 125, 145)
        );


        subtitle.setAlignmentX(
                Component.CENTER_ALIGNMENT
        );


        headerPanel.add(titleLabel);

        headerPanel.add(
                Box.createVerticalStrut(7)
        );

        headerPanel.add(subtitle);


        panel.add(
                headerPanel,
                BorderLayout.NORTH
        );


        birthdayListModel =
                new DefaultListModel<>();


        birthdayList =
                new JList<>(
                        birthdayListModel
                );


        birthdayList.setFont(

                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        15
                )
        );


        birthdayList.setFixedCellHeight(42);

        birthdayList.setBackground(Color.WHITE);


        birthdayList.setBorder(

                BorderFactory.createEmptyBorder(
                        10,
                        15,
                        10,
                        15
                )
        );


        JScrollPane scrollPane =
                new JScrollPane(
                        birthdayList
                );


        scrollPane.setBorder(

                BorderFactory.createLineBorder(
                        new Color(225, 220, 235)
                )
        );


        panel.add(
                scrollPane,
                BorderLayout.CENTER
        );


        JPanel buttonPanel =
                new JPanel(

                        new FlowLayout(
                                FlowLayout.CENTER,
                                12,
                                5
                        )
                );


        buttonPanel.setOpaque(false);


        JButton refreshButton =
                new JButton("Refresh");


        JButton viewAllButton =
                new JButton("View All");


        refreshButton.setFocusPainted(false);
        viewAllButton.setFocusPainted(false);


        refreshButton.setPreferredSize(
                new Dimension(110, 34)
        );


        viewAllButton.setPreferredSize(
                new Dimension(110, 34)
        );


        refreshButton.addActionListener(
                e -> updateBirthdayList()
        );


        viewAllButton.addActionListener(

                e ->
                        tabbedPane.setSelectedIndex(0)
        );


        buttonPanel.add(refreshButton);
        buttonPanel.add(viewAllButton);


        panel.add(
                buttonPanel,
                BorderLayout.SOUTH
        );


        return panel;
    }


    // ========================================================
    // STATUS BAR
    // ========================================================

    private void createStatusBar() {

        JPanel statusBar =
                new JPanel(
                        new BorderLayout()
                );


        statusBar.setBackground(
                new Color(245, 242, 250)
        );


        statusBar.setBorder(

                BorderFactory.createEmptyBorder(
                        8,
                        15,
                        8,
                        15
                )
        );


        statusLabel =
                new JLabel("Ready");


        birthdayCountLabel =
                new JLabel(
                        "Today's Birthdays: 0"
                );


        statusLabel.setFont(

                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        12
                )
        );


        birthdayCountLabel.setFont(

                new Font(
                        "SansSerif",
                        Font.BOLD,
                        12
                )
        );


        statusBar.add(
                statusLabel,
                BorderLayout.WEST
        );


        statusBar.add(
                birthdayCountLabel,
                BorderLayout.EAST
        );


        add(
                statusBar,
                BorderLayout.SOUTH
        );
    }


    // ========================================================
    // LOAD DATA
    // ========================================================

    private void loadData() {

        tableModel.setRowCount(0);


        List<Contact> contacts =
                controller.getAllContacts();


        for (Contact contact : contacts) {

            Object[] row = {

                    contact.getId(),

                    contact.getName(),

                    contact.getBirthDate() != null

                            ? contact.getBirthDate()
                            .format(DISPLAY_FORMAT)

                            : "",

                    contact.getAge(),

                    contact.getDaysUntilBirthday(),

                    contact.getRelationship() != null

                            ? contact.getRelationship()

                            : ""
            };


            tableModel.addRow(row);
        }


        statusLabel.setText(
                "Total Birthdays: " + contacts.size()
        );


        updateBirthdayList();

        updateBirthdayCount();
    }


    // ========================================================
    // SEARCH
    // ========================================================

    private void searchContacts() {

        String keyword =
                searchField.getText().trim();


        tableModel.setRowCount(0);


        List<Contact> contacts =
                controller.searchContacts(keyword);


        for (Contact contact : contacts) {

            Object[] row = {

                    contact.getId(),

                    contact.getName(),

                    contact.getBirthDate() != null

                            ? contact.getBirthDate()
                            .format(DISPLAY_FORMAT)

                            : "",

                    contact.getAge(),

                    contact.getDaysUntilBirthday(),

                    contact.getRelationship() != null

                            ? contact.getRelationship()

                            : ""
            };


            tableModel.addRow(row);
        }


        statusLabel.setText(
                "Found " + contacts.size() + " result(s)"
        );
    }


    // ========================================================
    // UPDATE BIRTHDAY LIST
    // ========================================================

    private void updateBirthdayList() {

        birthdayListModel.clear();


        List<Contact> upcomingBirthdays =

                controller.getUpcomingBirthdays(7);


        if (upcomingBirthdays.isEmpty()) {

            birthdayListModel.addElement(

                    "No upcoming birthdays in the next 7 days"
            );

            return;
        }


        for (Contact contact : upcomingBirthdays) {

            long daysLeft =
                    contact.getDaysUntilBirthday();


            String entry;


            if (daysLeft == 0) {

                entry =
                        "TODAY - "
                                + contact.getName()
                                + "'s Birthday";

            } else if (daysLeft == 1) {

                entry =
                        "TOMORROW - "
                                + contact.getName()
                                + "'s Birthday";

            } else {

                entry =
                        contact.getName()
                                + " - "
                                + daysLeft
                                + " days left";
            }


            birthdayListModel.addElement(entry);
        }
    }


    // ========================================================
    // UPDATE TODAY COUNT
    // ========================================================

    private void updateBirthdayCount() {

        List<Contact> todayBirthdays =
                controller.getTodayBirthdays();


        birthdayCountLabel.setText(

                "Today's Birthdays: "
                        + todayBirthdays.size()
        );
    }


    // ========================================================
    // CHECK BIRTHDAYS
    // ========================================================

    private void checkBirthdays() {

        List<Contact> todayBirthdays =
                controller.getTodayBirthdays();


        if (!todayBirthdays.isEmpty()) {

            StringBuilder message =
                    new StringBuilder(
                            "Today's Birthdays:\n\n"
                    );


            for (Contact contact : todayBirthdays) {

                message.append(
                        contact.getName()
                );

                message.append("\n");
            }


            JOptionPane.showMessageDialog(

                    this,

                    message.toString(),

                    "Birthday Reminder",

                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }


    // ========================================================
    // SHOW TODAY BIRTHDAYS
    // ========================================================

    private void showTodayBirthdays() {

        List<Contact> todayBirthdays =
                controller.getTodayBirthdays();


        if (todayBirthdays.isEmpty()) {

            JOptionPane.showMessageDialog(

                    this,

                    "No birthdays today.",

                    "Today's Birthdays",

                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }


        StringBuilder message =
                new StringBuilder(
                        "Today's Birthdays:\n\n"
                );


        for (Contact contact : todayBirthdays) {

            message.append(
                    contact.getName()
            );

            message.append("\n");
        }


        JOptionPane.showMessageDialog(

                this,

                message.toString(),

                "Today's Birthdays",

                JOptionPane.INFORMATION_MESSAGE
        );
    }


    // ========================================================
    // SHOW ADD / EDIT DIALOG
    // ========================================================

    private void showAddEditDialog(
            Contact contact
    ) {

        AddEditContactDialog dialog =

                new AddEditContactDialog(

                        this,

                        controller,

                        contact
                );


        dialog.setVisible(true);


        if (dialog.isSaved()) {

            loadData();
        }
    }


    // ========================================================
    // EDIT
    // ========================================================

    private void editSelectedContact() {

        int selectedRow =
                contactTable.getSelectedRow();


        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(

                    this,

                    "Please select a birthday to edit."
            );

            return;
        }


        int modelRow =

                contactTable.convertRowIndexToModel(
                        selectedRow
                );


        int id =

                (int) tableModel.getValueAt(
                        modelRow,
                        0
                );


        Contact contact =
                controller.getContactById(id);


        if (contact != null) {

            showAddEditDialog(contact);
        }
    }


    // ========================================================
    // DELETE
    // ========================================================

    private void deleteSelectedContact() {

        int selectedRow =
                contactTable.getSelectedRow();


        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(

                    this,

                    "Please select a birthday to delete."
            );

            return;
        }


        int modelRow =

                contactTable.convertRowIndexToModel(
                        selectedRow
                );


        int id =

                (int) tableModel.getValueAt(
                        modelRow,
                        0
                );


        String name =

                (String) tableModel.getValueAt(
                        modelRow,
                        1
                );


        int confirmation =
                JOptionPane.showConfirmDialog(

                        this,

                        "Are you sure you want to delete "
                                + name
                                + "'s birthday?",

                        "Confirm Delete",

                        JOptionPane.YES_NO_OPTION,

                        JOptionPane.WARNING_MESSAGE
                );


        if (confirmation == JOptionPane.YES_OPTION) {

            boolean deleted =
                    controller.deleteContact(id);


            if (deleted) {

                loadData();


                JOptionPane.showMessageDialog(

                        this,

                        "Birthday deleted successfully."
                );

            } else {

                JOptionPane.showMessageDialog(

                        this,

                        "Failed to delete birthday.",

                        "Error",

                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }


    // ========================================================
    // ABOUT
    // ========================================================

    private void showAboutDialog() {

        String message =

                "Birthday Reminder System\n\n"

                        + "Built using Java Swing, AWT and JDBC\n"

                        + "Database: MySQL\n\n"

                        + "Version 1.0";


        JOptionPane.showMessageDialog(

                this,

                message,

                "About",

                JOptionPane.INFORMATION_MESSAGE
        );
    }


    // ========================================================
    // MAIN
    // ========================================================

    public static void main(String[] args) {

        DatabaseManager.setupDatabase();


        SwingUtilities.invokeLater(

                () -> {

                    try {

                        UIManager.setLookAndFeel(

                                UIManager
                                        .getSystemLookAndFeelClassName()
                        );

                    } catch (Exception e) {

                        e.printStackTrace();
                    }


                    MainFrame frame =
                            new MainFrame();


                    frame.setVisible(true);
                }
        );
    }
}