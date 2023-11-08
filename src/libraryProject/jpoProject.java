package libraryProject;

import java.awt.EventQueue;
import java.sql.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import net.proteanit.sql.DbUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import com.opencsv.CSVReader;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

public class jpoProject {

	private JFrame frame;
	private JTable table;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					jpoProject window = new jpoProject();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public jpoProject() {
		initialize();
		Connect();
		table_load();
		checkBorrows();
		checkZeroAmount();
	}
	
	public Connection con;
	public PreparedStatement query;
	public ResultSet rs;
	private JTextField Title;
	private JTextField Author;
	private JTextField Type;
	private JTextField Amount;
	private JTextField ID;
	private JTextField Mail;
	
	public void Connect() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			con = DriverManager.getConnection("jdbc:mysql://localhost/library","root","");
		}catch(ClassNotFoundException e){
			
		}catch(SQLException e) {
			
		}
	}
	
	public void table_load() {
		try {
			query = con.prepareStatement("SELECT * FROM books");
			rs = query.executeQuery();
			table.setModel(DbUtils.resultSetToTableModel(rs));
			
		}
		catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	public void clients_load() {
		try {
			query = con.prepareStatement("SELECT * FROM booked");
			rs = query.executeQuery();
			table.setModel(DbUtils.resultSetToTableModel(rs));
			
		}
		catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	public void checkZeroAmount() {
		try {
			query = con.prepareStatement("SELECT amount,title FROM books");
			rs = query.executeQuery();
			while(rs.next()) {
				if(rs.getInt(1)==0) {
					JOptionPane.showMessageDialog(null, rs.getString(2) + " has no copy in library!");
				}
			}
		}
		catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	public void checkBorrows() {
		try {
			query = con.prepareStatement("SELECT date,mail FROM booked");
			rs = query.executeQuery();
			long millis = System.currentTimeMillis();  
		    Date date_sql = new java.sql.Date(millis);  
		    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		    String today = df.format(date_sql);
			while(rs.next()) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

				String dateInString = rs.getString(1);
				LocalDate date = LocalDate.parse(dateInString);
				LocalDate now = LocalDate.parse(today);
				long monthsDistance = ChronoUnit.MONTHS.between(date,now);
				if(monthsDistance>=1) {
					JOptionPane.showMessageDialog(null, "late client: " + rs.getString(2),"book not received back in time",JOptionPane.INFORMATION_MESSAGE);
				}
				
			}
			
		}
		catch(SQLException e){
			e.printStackTrace();
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 891, 423);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JLabel lblLibraryManager = new JLabel("Library Manager");
		lblLibraryManager.setFont(new Font("Times New Roman", Font.BOLD, 30));
		lblLibraryManager.setBounds(196, 0, 230, 53);
		frame.getContentPane().add(lblLibraryManager);
		
		JButton btnDelete = new JButton("d_book");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String id;
				id = ID.getText();
				try {
					query = con.prepareStatement("DELETE FROM books WHERE book_id=?");
					query.setString(1, id);
					query.executeUpdate();
					JOptionPane.showMessageDialog(null, "book deleted");
					table_load();
					Title.setText("");
				}
				catch(SQLException e1) {
					e1.printStackTrace();
				}
			}
		});
		btnDelete.setBounds(253, 306, 88, 25);
		frame.getContentPane().add(btnDelete);
		
		JButton btnRanking = new JButton("ranking");
		btnRanking.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				try {
					String choice = ID.getText() + ".csv";
					System.out.println(choice);
				    BufferedReader reader = new BufferedReader(new FileReader("(...)/libraryProject/src/libraryProject/"+choice)); //(...) means absolute path to csv files
				    
				    List<String> lines = new ArrayList<>();
				    String line;
				    while ((line = reader.readLine()) != null) {
				      lines.add(line);
				    }
				    reader.close();

				    int numberOfRows = lines.size() - 1;
				    LocalDate[] dates = new LocalDate[numberOfRows];
				    double[] opinions = new double[numberOfRows];

				    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				    for (int i = 1; i < lines.size(); i++) {
				      String[] values = lines.get(i).split(",");
				      dates[i - 1] = LocalDate.parse(values[0], formatter);
				      opinions[i - 1] = Double.parseDouble(values[1]);
				    }

				    int endIndex = numberOfRows - 1;
				    int startIndex = endIndex - 29;
				    LocalDate endDate = dates[endIndex];
				    LocalDate startDate = endDate.minusDays(29);

				    OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
				    double[][] x = new double[endIndex - startIndex + 1][1];
				    double[] y = new double[endIndex - startIndex + 1];
				    for (int i = startIndex; i <= endIndex; i++) {
				      x[i - startIndex][0] = i - startIndex;
				      y[i - startIndex] = opinions[i];
				    }
				    regression.newSampleData(y, x);
				    double[] beta = regression.estimateRegressionParameters();

				    double predictedOpinion = beta[0] + beta[1] * (endIndex - startIndex + 1 + 10);
				    JOptionPane.showMessageDialog(null, "Average opinion about book with ID: " + ID.getText() + " after next 10 days equals: " + (int)(Math.round(predictedOpinion * 100))/100.0,"book's average opinion prediction",JOptionPane.INFORMATION_MESSAGE);

				  } catch (IOException e3) {
				    e3.printStackTrace();
				  } catch (MathIllegalArgumentException | MathIllegalStateException e4) {
				    e4.printStackTrace();
				  }
			}	
		});
		btnRanking.setBounds(667, 306, 100, 25);
		frame.getContentPane().add(btnRanking);
		
		JScrollPane table_1 = new JScrollPane();
		table_1.setBounds(305, 54, 574, 239);
		frame.getContentPane().add(table_1);
		
		table = new JTable();
		table_1.setViewportView(table);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "add book", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
		panel.setBounds(45, 46, 248, 247);
		frame.getContentPane().add(panel);
		panel.setLayout(null);
		
		JLabel lblBookName = new JLabel("Title");
		lblBookName.setFont(new Font("Dialog", Font.BOLD, 12));
		lblBookName.setBounds(12, 23, 108, 24);
		panel.add(lblBookName);
		
		JLabel lblAuthor = new JLabel("Author");
		lblAuthor.setBounds(12, 59, 108, 24);
		panel.add(lblAuthor);
		
		JLabel lblType = new JLabel("Type");
		lblType.setBounds(12, 93, 108, 24);
		panel.add(lblType);
		
		JLabel lblAmount = new JLabel("amount");
		lblAmount.setBounds(12, 123, 66, 24);
		panel.add(lblAmount);
		
		Title = new JTextField();
		Title.setBounds(81, 26, 114, 19);
		panel.add(Title);
		Title.setColumns(10);
		
		Author = new JTextField();
		Author.setColumns(10);
		Author.setBounds(81, 62, 114, 19);
		panel.add(Author);
		
		Type = new JTextField();
		Type.setColumns(10);
		Type.setBounds(81, 95, 114, 19);
		panel.add(Type);
		
		Amount = new JTextField();
		Amount.setColumns(10);
		Amount.setBounds(81, 126, 114, 19);
		panel.add(Amount);
		
		JButton btnAdd = new JButton("add");
		btnAdd.setBounds(81, 157, 114, 25);
		panel.add(btnAdd);
		
		JLabel lblBookId = new JLabel("Book/Customer ID");
		lblBookId.setBounds(65, 306, 133, 24);
		frame.getContentPane().add(lblBookId);
		
		ID = new JTextField();
		ID.setColumns(10);
		ID.setBounds(191, 309, 50, 19);
		frame.getContentPane().add(ID);
		
		JLabel lblDate = new JLabel("Mail");
		lblDate.setBounds(65, 334, 108, 24);
		frame.getContentPane().add(lblDate);
		
		Mail = new JTextField();
		Mail.setColumns(10);
		Mail.setBounds(127, 337, 114, 19);
		frame.getContentPane().add(Mail);
		
		JButton btnDelete_1 = new JButton("1_view");
		btnDelete_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				table_load();
			}
		});
		btnDelete_1.setBounds(454, 306, 88, 25);
		frame.getContentPane().add(btnDelete_1);
		
		JButton btnBorrow_1 = new JButton("2_view");
		btnBorrow_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clients_load();
			}
		});
		btnBorrow_1.setBounds(454, 334, 88, 25);
		frame.getContentPane().add(btnBorrow_1);
		
		JButton btnBorrow_2 = new JButton("borrow");
		btnBorrow_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String id,mail,date;
				long millis = System.currentTimeMillis();  
			    Date date_sql = new java.sql.Date(millis);  
			    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				id = ID.getText();
				mail = Mail.getText();
				date = df.format(date_sql);
				
				Pattern pattern = Pattern.compile("^(.+)@(.+)$", Pattern.CASE_INSENSITIVE);
				
				if(!pattern.matcher(mail).matches()) {
					JOptionPane.showMessageDialog(null, "invalid mail!");
					return;
				}
				
				try {
					query = con.prepareStatement("INSERT INTO booked(book_id,mail,date)values(?,?,?)");
					query.setString(1, id);
					query.setString(2, mail);
					query.setString(3, date);
					query.executeUpdate();
					query = con.prepareStatement("UPDATE books SET amount = amount - 1 WHERE book_id = ? AND amount > 0");
					query.setString(1, id);					
					query.executeUpdate();
					
					JOptionPane.showMessageDialog(null, "borrow added");
					clients_load();
					ID.setText("");
					Mail.setText("");
					
				}
				catch(SQLException e1) {
					e1.printStackTrace();
				}
			}
		});
		btnBorrow_2.setBounds(253, 334, 88, 25);
		frame.getContentPane().add(btnBorrow_2);
		
		JButton btnDelborrow = new JButton("d_bor");
		btnDelborrow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String id,book_d = null;
				id = ID.getText();
				try {
					query = con.prepareStatement("SELECT book_id FROM booked WHERE customer_id=?");
					query.setString(1, id);
					rs = query.executeQuery();
					while(rs.next()) {
						book_d = rs.getString(1);
					}
					
					query = con.prepareStatement("DELETE FROM booked WHERE customer_id=?");
					query.setString(1, id);
					query.executeUpdate();
					
					query = con.prepareStatement("UPDATE books SET amount = amount + 1 WHERE book_id = ?");
					
					query.setString(1, book_d);	
					query.executeUpdate();					
					JOptionPane.showMessageDialog(null, "borrow deleted");
					clients_load();
					Title.setText("");
				}
				catch(SQLException e1) {
					e1.printStackTrace();
				}
			}
		});
		btnDelborrow.setFont(new Font("Dialog", Font.BOLD, 12));
		btnDelborrow.setBounds(353, 306, 88, 25);
		frame.getContentPane().add(btnDelborrow);
		
		JButton btnBorrow_2_1 = new JButton("check");
		btnBorrow_2_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				checkBorrows();
			}
		});
		btnBorrow_2_1.setBounds(353, 334, 88, 25);
		frame.getContentPane().add(btnBorrow_2_1);
		
		JButton btnDelete_1_1 = new JButton("update\n");
		btnDelete_1_1.setBounds(555, 306, 100, 25);
		frame.getContentPane().add(btnDelete_1_1);
		btnDelete_1_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String title,author,type,amount,id;
				
				title = Title.getText();
				author = Author.getText();
				type = Type.getText();
				amount = Amount.getText();
				id = ID.getText();
				try {
					query = con.prepareStatement("UPDATE books set title=?, author=?, type=?, amount=? WHERE book_id=?");
					query.setString(1, title);
					query.setString(2, author);
					query.setString(3, type);
					query.setString(4, amount);
					query.setString(5, id);
					
					query.executeUpdate();
					JOptionPane.showMessageDialog(null, "book updated");
					table_load();
					Title.setText("");
					Author.setText("");
					Type.setText("");
					Amount.setText("");
					
				}
				catch(SQLException e1) {
					e1.printStackTrace();
				}
			}
		});
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String title,author,type,amount;
				
				title = Title.getText();
				author = Author.getText();
				type = Type.getText();
				amount = Amount.getText();
				try {
					query = con.prepareStatement("INSERT INTO books(title,author,type,amount)values(?,?,?,?)");
					query.setString(1, title);
					query.setString(2, author);
					query.setString(3, type);
					query.setString(4, amount);
					query.executeUpdate();
					JOptionPane.showMessageDialog(null, "book added");
					table_load();
					Title.setText("");
					Author.setText("");
					Type.setText("");
					Amount.setText("");
					
				}
				catch(SQLException e1) {
					e1.printStackTrace();
				}
				
			}
		});
	}
}
