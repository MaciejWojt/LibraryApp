import java.sql.*;

public class libraryProject {
	public static void main(String[] args) {
		Connection conn = null;
		String url = "jdbc:mysql://127.0.0.1:3306/library";
		String user = "root";
		String passwd = "";
		try {
			conn = DriverManager.getConnection(url,user,passwd);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(conn!=null) {
			System.out.println("you are connected!");
		}
		
	}
}