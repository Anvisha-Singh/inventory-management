
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author anvis
 */
public class purchase_order extends javax.swing.JFrame {

    Connection conn;
    JFrame main_menu_frame;
    String username, global_error_msg;
    JLabel department_label, cartridge_label, quantity_label;
    JComboBox department_input, cartridge_input;
    JSpinner quantity_spinner;
    JButton submit_button;

    /**
     * Creates new form purchase_order
     */
    public purchase_order() {
        initComponents();
        
    }

    public purchase_order(Connection conn, String username, JFrame main_menu_frame) {

        // Set global attributes
        this.conn = conn;
        this.main_menu_frame = main_menu_frame;
        this.username = username;

        JPanel pane = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        department_label = new JLabel("Department:");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 1;
        pane.add(department_label, c);

        department_input = new JComboBox(get_departments(conn));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 2;
        c.gridx = 3;
        c.gridy = 1;
        pane.add(department_input, c);

        cartridge_label = new JLabel("Cartridge");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 4;
        pane.add(cartridge_label, c);

        cartridge_input = new JComboBox(get_cartridge(conn));
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 2;
        c.gridx = 3;
        c.gridy = 4;
        pane.add(cartridge_input, c);

        quantity_label = new JLabel("Quantity");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 6;
        pane.add(quantity_label, c);

        SpinnerModel quantitySpinnerModel = new SpinnerNumberModel(0, //initial value
                0, //min
                100, //max
                1);//step;
        quantity_spinner = new JSpinner(quantitySpinnerModel);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 2;
        c.gridx = 3;
        c.gridy = 6;
        pane.add(quantity_spinner, c);

        submit_button = new JButton("SUBMIT");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
        c.gridx = 3;
        c.gridy = 8;
        pane.add(submit_button, c);

        pane.setSize(400, 600);
        add(pane, BorderLayout.CENTER);

        submit_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                perform_order_purchase(conn);
            }
        });
    }

    public boolean perform_order_validations(Connection con) {
        String department_name = department_input.getSelectedItem().toString();
        String cartridge_type = cartridge_input.getSelectedItem().toString();
        // Department Validity Check
        String query_department_validity = "select DEPARMENT_NAME from CARTRIDGE_USED_IN_DEPARMENT where DEPARMENT_NAME = ? and CARTRIDGE_TYPE = ?";
        try {
            PreparedStatement statement = con.prepareStatement(query_department_validity,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE
            );
            statement.setString(1, department_name);
            statement.setString(2, cartridge_type);
            ResultSet rs = statement.executeQuery();
            if (rs == null || !rs.next()) {
                global_error_msg = "Cartridge Type " + cartridge_type + " not valid for department " + department_name;
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Query not executed" + e.toString());
        }

        // Quantity Check
        
        String cartridge_quantity = quantity_spinner.getValue().toString();
        String query_cartridge = "select QUANTITY from CARTRIDGE where CARTRIDGE_TYPE = ?";
        try {
            PreparedStatement statement = con.prepareStatement(query_cartridge,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE
            );
            statement.setString(1, cartridge_type);
            ResultSet rs = statement.executeQuery();
            if (rs == null) {
                global_error_msg = "Cartridge Type " + cartridge_type + " not valid";
                return false;
            }
            if (rs.next()) {
                int quantity_in_stock = Integer.parseInt(rs.getString(1)), quantity_ordered = 0;
                try {
                    quantity_ordered = Integer.parseInt(cartridge_quantity);
                } catch (Exception e) {
                    global_error_msg = "Cartridge Quantity has to be integer";
                    return false;
                }
                if (quantity_ordered < 0) {
                    global_error_msg = "Cartridge Quantity cannot be negative";
                    return false;
                }
                if (quantity_in_stock - quantity_ordered < 0) {
                    global_error_msg = "Purchase Failed. Only " + quantity_in_stock + " cartridges in stock for Cartridge Type " + cartridge_type;
                    return false;
                }
            }
            else
            {
                global_error_msg = "Cartridge Type and Quantity not found";
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Validation Failed");
            System.out.println("Query not executed" + e.toString());
        }

        return true;

    }

    public void perform_purchase(Connection con) {
        // Get previous receipt number
        String query_prev_rcpt = "select MAX(RECEIPT_NUMBER) from TRANSACTIONS";
        String new_rcpt_num = "1";
        try {
            PreparedStatement statement = con.prepareStatement(query_prev_rcpt,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE
            );
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                new_rcpt_num = String.valueOf(Integer.parseInt(rs.getString(1))+1);
            }
        } catch (SQLException e) {
            System.out.println("Query not executed" + e.toString());
        }
        
        // Perform actual insert
        String query_purchase = "insert into TRANSACTIONS(RECEIPT_NUMBER,EMPLOYEE_ID,CARTRIDGE_TYPE,DEPARTMENT_NAME,QUANTITY_PURCHASED,DATE_OF_PURCHASE) "+"VALUES(?,?,?,?,?,?)";
        String cartridge_type = cartridge_input.getSelectedItem().toString();
        String department_name = department_input.getSelectedItem().toString();
        String quantity_purchased = quantity_spinner.getValue().toString();
        Date dt = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        String strDateCondition = formatter.format(dt);
        try {
            PreparedStatement statement = con.prepareStatement(query_purchase,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE
            );
            statement.setString(1, new_rcpt_num);
            statement.setString(2, username);
            statement.setString(3, cartridge_type);
            statement.setString(4, department_name);
            statement.setString(5, quantity_purchased);
            statement.setString(6, strDateCondition);
            int update_flag = statement.executeUpdate();
            if (update_flag != 1) {
                System.out.println("Insert Failed");
                JOptionPane.showMessageDialog(this, "Purchase Failed",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
            else
            {
                System.out.println("Insert Successful");
                JOptionPane.showMessageDialog(this, "Purchase Successful",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            System.out.println("Insert Failed");
            System.out.println("Query not executed" + e.toString());
        }
    }

    public void perform_order_purchase(Connection con) {
        if (perform_order_validations(con)) {
            perform_purchase(con);
        } else {
            JOptionPane.showMessageDialog(this, global_error_msg,
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public String[] get_cartridge(Connection con) {
        String valid_cartridge[] = new String[0];
        // Perform Select on users table
        String query_department = "select CARTRIDGE_TYPE from CARTRIDGE";
        try {
            PreparedStatement statement = con.prepareStatement(query_department,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE
            );
            ResultSet rs = statement.executeQuery();
            int size = 0;
            if (rs != null) {
                rs.last();    // moves cursor to the last row
                size = rs.getRow(); // get row id 
                rs.beforeFirst();
            }
            valid_cartridge = new String[size];

            while (rs.next()) {
                valid_cartridge[rs.getRow() - 1] = rs.getString(1);
            }
        } catch (SQLException e) {
            System.out.println("Query not executed" + e.toString());
        }
        return valid_cartridge;
    }

    public String[] get_departments(Connection con) {
        String valid_departments[] = new String[0];
        // Perform Select on users table
        String query_department = "select DEPARTMENT_NAME from DEPARTMENT";
        try {
            PreparedStatement statement = con.prepareStatement(query_department,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE
            );
            ResultSet rs = statement.executeQuery();
            int size = 0;
            if (rs != null) {
                rs.last();    // moves cursor to the last row
                size = rs.getRow(); // get row id 
                rs.beforeFirst();
            }
            valid_departments = new String[size];

            while (rs.next()) {
                valid_departments[rs.getRow() - 1] = rs.getString(1);
            }
        } catch (SQLException e) {
            System.out.println("Query not executed" + e.toString());
        }
        return valid_departments;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(purchase_order.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(purchase_order.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(purchase_order.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(purchase_order.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new purchase_order().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
