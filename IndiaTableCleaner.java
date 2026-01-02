import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringTokenizer;

import com.ibm.as400.access.AS400JDBCDataSource;



public class IndiaTableCleaner {

	
	
	private static void processTable(String table, Connection db2Conn) throws SQLException, IOException {

		String sql = "delete from " + table + " where " + table.substring(2,4) + "rfn <> 0";
		System.err.println(sql);
		PreparedStatement retrieveStatement = null;
		try {
			retrieveStatement = db2Conn.prepareStatement(sql);
			System.err.println("Entries deleted : " +retrieveStatement.executeUpdate());
		}catch (SQLException e) {
			if(e.getErrorCode() == -206){
				sql = "  delete from " + table;
				System.err.println("trying >>>> " + sql);
				retrieveStatement = null;
				retrieveStatement = db2Conn.prepareStatement(sql);
				System.err.println("Entries Truncated : " +retrieveStatement.executeUpdate());
			}else{
				e.printStackTrace();
			}

		} finally {
			if (retrieveStatement != null) {
				try {
					retrieveStatement.close();
				} catch (Exception e) {
				}
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Connection connection = null;
		try{
			
			BufferedReader reader = null;
			if(args.length > 0 && !args[0].isEmpty()){
				reader = new BufferedReader(new FileReader(args[0]));
			}else{
				System.exit(0);
			}
			StringTokenizer stTables;
			String codes = "";
			
			AS400JDBCDataSource dataSource = new AS400JDBCDataSource();
			dataSource.setUser("f2832623");
			dataSource.setPassword("bhu8nji9");
			dataSource.setLibraries("INBINDBDB");
			dataSource.setServerName("172.20.53.90");
			
			connection = dataSource.getConnection();
			
			System.out.println("Processing...");
			int count = 0;
			while ((codes = reader.readLine()) != null) {
				stTables = new StringTokenizer(codes, ",");
				while(stTables.hasMoreTokens()){
					connection.setAutoCommit(true);
					processTable(stTables.nextToken().trim(), connection);
					//one table per row
					break;
				}
			}
			System.out.println("Done...");
			
		}catch(Exception e){
			e.printStackTrace();
		} finally {
			try {
				connection.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
