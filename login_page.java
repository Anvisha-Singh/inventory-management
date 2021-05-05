/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author kraja
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

class login_page extends JFrame implements ActionListener {

    JButton submit_button;
    JLabel username_label, password_label;
    final JTextField username_text, password_text;

    login_page() {

        JPanel pane = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        username_label = new JLabel("Username:");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 1;
        pane.add(username_label, c);

        username_text = new JTextField(15);
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 2;
        c.insets = new Insets(0,20,0,0);
        c.gridx = 3;
        c.gridy = 1;
        pane.add(username_text, c);

        password_label = new JLabel("Password");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.insets = new Insets(20,0,0,0);
        c.gridy = 4;
        pane.add(password_label, c);

        password_text = new JPasswordField(15);
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 2;
        c.insets = new Insets(20,20,0,0);
        c.gridx = 3;
        c.gridy = 4;
        pane.add(password_text, c);

        submit_button = new JButton("SUBMIT");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
        c.gridx = 3;
        c.gridy = 6;
        c.insets = new Insets(20,0,0,0);
        pane.add(submit_button, c);

        pane.setSize(400, 600);
        add(pane, BorderLayout.CENTER);

        submit_button.addActionListener(this);
        setTitle("LOGIN FORM");
    }

    public boolean authenticate_user(String username, String password) {
        // Get connection to SQL database
        JConnection conn_obj = LoginDemo.conn_obj;
        Connection con = conn_obj.getConnection();

        // Perform Select on users table
        String query_authentication = "select EMPLOYEE_ID from USERS where EMPLOYEE_ID = ? and PASSWORD = ?";
        try {
            PreparedStatement statement = con.prepareStatement(query_authentication);
            statement.setString(1,username);
            statement.setString(2,password);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                System.out.println("User Successfully Authenticated");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Query not executed" + e.toString());
        }
        return false;
    }

    public void actionPerformed(ActionEvent ae) {
        String uname = username_text.getText();
        String pwd = password_text.getText();

        // Perform Username and Password Authentication
        if (authenticate_user(uname, pwd)) {
            mainMenu jfrm2 = new mainMenu(LoginDemo.conn_obj.getConnection(),uname);
            jfrm2.setSize(500, 500);
            jfrm2.setVisible(true);
            this.setVisible(false);
            this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
            this.dispose();
        } else {
            System.out.println("enter the valid username and password");
            JOptionPane.showMessageDialog(this, "Incorrect login or password",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

class LoginDemo {

    public static JConnection conn_obj;

    public static void main(String arg[]) {
        // Set Login Page attributes
        try {
            login_page frame = new login_page();
            frame.setSize(500, 300);
            frame.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        // Set connection to database by creating object of Connection Class
        conn_obj = new JConnection();
        conn_obj.setConnection();

    }
}
