
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author anvis
 */
public class receipt extends javax.swing.JFrame {

    Connection conn;
    JFrame main_menu_frame;
    JLabel month_label, year_label;
    JComboBox month_input;
    JSpinner year_input;
    String month_str[] = {"January","February","March","April","May","June","July","August","September","October","November","December"}; 
    String column_name[] = {"Receipt Number", "Date of Purchase", "Employee ID", "Cartridge Type", "Quantity Purchased"};
    JTable jt_stock;
    JPanel pane;
    /**
     * Creates new form receipt
     */
    public receipt() {
        initComponents();
    }

    public void getMonthTransaction() {
        
        String year_str = year_input.getValue().toString();
        String month_string = month_input.getSelectedItem().toString();
        int month = Arrays.asList(month_str).indexOf(month_string);
        String[][] result_data = new String[0][0];
        // Perform Select on users table
        String query_transactions = "select RECEIPT_NUMBER, DATE_OF_PURCHASE, EMPLOYEE_ID, CARTRIDGE_TYPE, QUANTITY_PURCHASED from TRANSACTIONS WHERE DATE_OF_PURCHASE > ? AND DATE_OF_PURCHASE < ?";
        try {
            PreparedStatement statement = conn.prepareStatement(query_transactions,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE
            );

            Date dt = new Date();
            Calendar c = Calendar.getInstance();
            c.set(Integer.parseInt(year_str),month,1,0,0);
            //c.set(Calendar.DAY_OF_MONTH,1);
            dt = c.getTime();
            
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");  
            String strDateFrom = formatter.format(dt);
            c.add(Calendar.MONTH, 1);
            c.add(Calendar.DATE,-1);
            dt= c.getTime();
            String strDateTo = formatter.format(dt);
            
            System.out.println(strDateFrom+","+strDateTo);
            statement.setString(1, strDateFrom);
            statement.setString(2, strDateTo);
            
            
            ResultSet rs = statement.executeQuery();
            int size = 0;
            if (rs != null) {
                rs.last();    // moves cursor to the last row
                size = rs.getRow(); // get row id 
                rs.beforeFirst();
            }
            
            result_data = new String[size][5];

            while(rs!=null && rs.next()) {
                result_data[rs.getRow() - 1][0] = rs.getString(1);
                result_data[rs.getRow() - 1][1] = rs.getString(2);
                result_data[rs.getRow() - 1][2] = rs.getString(3);
                result_data[rs.getRow() - 1][3] = rs.getString(4);
                result_data[rs.getRow() - 1][4] = rs.getString(5);
                
            }
        } catch (SQLException e) {
            System.out.println("Query not executed" + e.toString());
        }
        
        // Set JTable
        DefaultTableModel current_table = (DefaultTableModel) jt_stock.getModel();
        for(String[] individual_result:result_data)
            current_table.addRow(individual_result);
        
        DefaultTableModel new_table = new DefaultTableModel(result_data,column_name);
        jt_stock.setModel(new_table);
        current_table.setNumRows(result_data.length);
        jt_stock.repaint() ;
        current_table.fireTableDataChanged();
    }

    public receipt(Connection conn, JFrame main_menu_frame) {
        // Get Connection
        this.conn = conn;
        this.main_menu_frame = main_menu_frame;

        // Get Stock from query
        String stock_data[][] = new String[0][5];

        // Create and Populate JTable
        
        jt_stock = new JTable(new DefaultTableModel(stock_data, column_name));
        jt_stock.setFillsViewportHeight(true);
        JScrollPane sp = new JScrollPane(jt_stock);

        pane = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        year_label = new JLabel("Enter Year");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy = 0;
        pane.add(year_label, c);
        
        
        SpinnerModel yearSpinnerModel = new SpinnerNumberModel(2010, //initial value
                2010, //min
                2021, //max
                1);//step;
        year_input = new JSpinner(yearSpinnerModel);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 0;
        pane.add(year_input, c);
        
        month_label = new JLabel("Enter Month");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
        c.gridx = 2;
        c.gridy = 0;
        pane.add(month_label, c);
         
        month_input = new JComboBox(month_str);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
        c.gridx = 3;
        c.gridy = 0;
        pane.add(month_input, c);
        
        
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 3;
        c.gridheight = 4;
        c.gridx = 0;
        c.gridy = 1;
        pane.add(sp, c);

        JButton view_button = new JButton("View Transactions");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
        c.gridx = 1;
        c.gridy = 7;
        pane.add(view_button, c);
        
        JButton back_button = new JButton("BACK");
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
        c.gridx = 2;
        c.gridy = 7;
        pane.add(back_button, c);

        back_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                back_to_menu();
            }
        });
        
        view_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getMonthTransaction();
            }
        });

        pane.setSize(800, 800);
        add(pane, BorderLayout.CENTER);
    }


    public void back_to_menu() {
        this.setVisible(false);
        main_menu_frame.setVisible(true);
        this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
        this.dispose();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jButton1.setText("ok");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(122, 122, 122)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(163, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(169, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addGap(106, 106, 106))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        mainMenu jfrm2 = new mainMenu();
        jfrm2.setSize(500, 500);
        jfrm2.setVisible(true);
        this.setVisible(false);
        this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

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
            java.util.logging.Logger.getLogger(receipt.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(receipt.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(receipt.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(receipt.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new receipt().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    // End of variables declaration//GEN-END:variables
}
