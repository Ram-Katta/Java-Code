import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.StringTokenizer;

import mammoth.services.HyphenCodes;
import shared.viewbean.SiteAccessRestrictionViewBean;
import shared.viewbean.UserAccessRestrictionDTO;

import com.ibm.as400.access.AS400JDBCDataSource;

/**
 * CSV File Lay out
 * 
 * Customer,External User Reference,Same/Range,IP,ISP,Num Logins with IP
 * 
 * @author pieter
 * 
 *         Rules 
 *         Find the user in the db, determine whether IP or MAC
 *         restrictions are in place for either the site or the user, if this is
 *         in place, leave as is, if not determine whether time MUA is in place.
 *         If so and MUA is currently set to all users same settings do the
 *         following:
 * 
 *         Set MUA to all users different settings Make the time settings for
 *         the site individual time settings for each user Set all users to the
 *         IP address appropriate. Where a particular user is not in the
 *         identified list, set to allow from any IP address.
 * 
 */
public class ManageUserAccessDDLCreator {
	
	private static String UPDATE_CDET = "UPDATE IBCDETPF SET CDRSTTYPE="+HyphenCodes.IBCODESPF324_SELECTIVE_USERS + " WHERE CDCURFN=";

	public ManageUserAccessDDLCreator() {
		super();
	}

	private boolean checkCust(Connection conn, int curfn) throws SQLException, IOException {

		String sqlSelectCUST = "select cuenableua from ibcustpf where curfn = ? and custatus = 1";
		PreparedStatement retrieveCust = null;
		ResultSet rsCust = null;
		try {

			retrieveCust = conn.prepareStatement(sqlSelectCUST);
			retrieveCust.setInt(1, curfn);
			rsCust = retrieveCust.executeQuery();
			if (rsCust.next())
				return rsCust.getBoolean("cuenableua");
			else
				return false;
		} catch (Exception e) {
			System.err.println("Error with checking cust : " + curfn);
			e.printStackTrace();
			return false;
		} finally {
			if (rsCust != null) {
				try {
					rsCust.close();
				} catch (Exception e) {
				}
			}
			if (retrieveCust != null) {
				try {
					retrieveCust.close();
				} catch (Exception e) {
				}
			}
		}
	}

	private SiteAccessRestrictionViewBean retrieveCdet(Connection conn, int curfn) throws Exception {

		String sqlSelectCDET = "select CDRSTTYPE,CDRSTBY,CDRSTPUBH,CDRSTDAYS,CDRSTSTIME,CDRSTETIME,CDRSTIP,CDRSTMAC " + 
		"FROM ibcdetpf where cdcurfn = ? and cdstatus = 1";

		PreparedStatement retrieveCdet = null;

		ResultSet rsCdet = null;
		try {

			retrieveCdet = conn.prepareStatement(sqlSelectCDET);
			retrieveCdet.setInt(1, curfn);
			rsCdet = retrieveCdet.executeQuery();

			SiteAccessRestrictionViewBean viewBean = null;

			if (rsCdet.next()) {
				viewBean = new SiteAccessRestrictionViewBean();
				viewBean.setTypeOfRestriction(rsCdet.getInt("CDRSTTYPE"));
				viewBean.setRestrictedBy(rsCdet.getInt("CDRSTBY"));
				viewBean.setAllowPublicHolidays(rsCdet.getBoolean("CDRSTPUBH"));
				viewBean.setAllowDays(rsCdet.getInt("CDRSTDAYS"));
				viewBean.setStartTime(rsCdet.getInt("CDRSTSTIME"));
				viewBean.setEndTime(rsCdet.getInt("CDRSTETIME"));
				viewBean.setIpAddress(rsCdet.getString("CDRSTIP"));
//				viewBean.setMacAddress(rsCdet.getString("CDRSTMAC"));
			}else{
				throw new Exception("No active customer found! curfn = " + curfn);
			}
			return viewBean;
		
		} finally {
			if (rsCdet != null) {
				try {
					rsCdet.close();
				} catch (Exception e) {
				}
			}
			if (retrieveCdet != null) {
				try {
					retrieveCdet.close();
				} catch (Exception e) {
				}
			}
		}
	}

	private UserAccessRestrictionDTO retrieveUdet(Connection conn, int usrfn) throws Exception {

		String sqlSelectUDET = "select UDRSTBY,UDRSTPUBH,UDRSTDAYS,UDRSTSTIME,UDRSTETIME,UDRSTIP,UDRSTMAC " + 
		"FROM ibudetpf where udusrfn = ? and udstatus = 1";

		PreparedStatement retrieveUdet = null;

		ResultSet rsUdet = null;
		try {

			retrieveUdet = conn.prepareStatement(sqlSelectUDET);
			retrieveUdet.setInt(1, usrfn);
			rsUdet = retrieveUdet.executeQuery();
			UserAccessRestrictionDTO viewBean = null;

			if (rsUdet.next()) {
				viewBean = new UserAccessRestrictionDTO();
				viewBean.setRestrictedBy(rsUdet.getInt("UDRSTBY"));
				viewBean.setAllowPublicHolidays(rsUdet.getBoolean("UDRSTPUBH"));
				viewBean.setAllowDays(rsUdet.getInt("UDRSTDAYS"));
				viewBean.setStartTime(rsUdet.getInt("UDRSTSTIME"));
				viewBean.setEndTime(rsUdet.getInt("UDRSTETIME"));
				viewBean.setIpAddress(rsUdet.getString("UDRSTIP"));
//				viewBean.setMacAddress(rsUdet.getString("UDRSTMAC"));
			}else{
				throw new Exception("No active user found! usrfn = " + usrfn);
			}
			return viewBean;
		} finally {
			if (rsUdet != null) {
				try {
					rsUdet.close();
				} catch (Exception e) {
				}
			}
			if (retrieveUdet != null) {
				try {
					retrieveUdet.close();
				} catch (Exception e) {
				}
			}
		}
	}

	private String processLine(Connection conn, FileWriter ddlWriter, int curfn, int usrfn, String ip) {
		try {
			StringBuffer report = new StringBuffer();
			StringBuffer ddl = new StringBuffer();
			

			/*
			 * Rules 
			 * Find the user in the db, determine whether IP or MAC
			 * restrictions are in place for either the site or the user, if
			 * this is in place, leave as is, if not determine whether time MUA
			 * is in place. If so and MUA is currently set to all users same
			 * settings do the following:
			 * 
			 * Set MUA to all users different settings Make the time settings
			 * for the site individual time settings for each user Set all users
			 * to the IP address appropriate. Where a particular user is not in
			 * the identified list, set to allow from any IP address.
			 */
			SiteAccessRestrictionViewBean siteBean = retrieveCdet(conn, curfn);
			if(siteBean == null){
				return "CDET entry not found";
			}
			ddl.append("--Old site settings," + siteBean.getTypeOfRestriction() + "," );
			ddl.append(siteBean.getRestrictedBy() + "," );
			ddl.append("'" + siteBean.getIpAddress() + "'," );
			ddl.append((siteBean.allowPublicHolidays()?"'1'":"'0'") + "," );
			ddl.append(siteBean.getAllowDays() + "," );
			ddl.append(siteBean.getStartTime() + "," );
			ddl.append(siteBean.getEndTime()  + "\n" );
			
			UserAccessRestrictionDTO userBean = retrieveUdet(conn, usrfn);
			ddl.append("--Old user settings,'" + userBean.getIpAddress() + "'," );
			ddl.append((userBean.allowPublicHolidays()?"'1'":"'0'") + "," );
			ddl.append(userBean.getAllowDays() + "," );
			ddl.append(userBean.getStartTime() + "," );
			ddl.append(userBean.getEndTime() + "\n" );
			
			if (!checkCust(conn, curfn)) {
				report.append("Enable - ");
				ddl.append("UPDATE IBCUSTPF SET CUENABLEUA=1 WHERE CURFN=" + curfn + ";\n");
			}
			
			switch (siteBean.getTypeOfRestriction()) {
				case HyphenCodes.IBCODESPF324_NO_RESTRICTION:
					// There are no restrictions for user access
					report.append("Update To SELECTIVE_USERS from NO_RESTRICTION");
					ddl.append(UPDATE_CDET + curfn + ";\n");
					userBean.setRestrictedByIP(true);
					userBean.setIpAddress(ip);
					break;
				case HyphenCodes.IBCODESPF324_ALL_USERS_SAME_PROFILE:
					// The restriction applied by site and all users will be
					// restricted by site settings
					if (siteBean.isRestrictedByIP()) {
						report.append("No ChangeSite - restricted by IP " + siteBean.getIpAddress());
						return report.toString();
					}
//					if (siteBean.isRestrictedByMAC()) {
//						report.append("No Change - Site restricted by MAC " + siteBean.getMacAddress());
//						return report.toString();
//					}
					if (siteBean.isRestrictedByTime()) {
						report.append("Update To ALL_USERS_DIFF_PROFILE from ALL_USERS_SAME_PROFILE and Copy Site Time Settings");
						ddl.append("UPDATE IBCDETPF SET CDRSTTYPE="+HyphenCodes.IBCODESPF324_ALL_USERS_DIFF_PROFILE + " WHERE CDCURFN=" + curfn + ";\n");
						ddl.append("UPDATE IBUDETPF SET \n");
						ddl.append("UDRSTBY=" + siteBean.getRestrictedBy());
						ddl.append(",\nUDRSTPUBH=" + (siteBean.allowPublicHolidays()?"'1'":"'0'"));
						ddl.append(",\nUDRSTDAYS=" + siteBean.getAllowDays());
						ddl.append(",\nUDRSTSTIME=" + siteBean.getStartTime());
						ddl.append(",\nUDRSTETIME=" + siteBean.getEndTime());
						ddl.append("\nWHERE UDUSRFN in (");
						ddl.append("\nSELECT USRFN from IBUSERPF");
						ddl.append("\nWHERE USCURFN = " + curfn+ ");\n");
						
						userBean.setRestrictedByTime(true);
						userBean.setStartTime(siteBean.getStartTime());
						userBean.setEndTime(siteBean.getEndTime());
						userBean.setAllowDays(siteBean.getAllowDays());
						userBean.setAllowPublicHolidays(siteBean.allowPublicHolidays());
					}else{
						report.append("Update To SELECTIVE_USERS from ALL_USERS_SAME_PROFILE");
						ddl.append(UPDATE_CDET + curfn + ";\n");
					}
					userBean.setRestrictedByIP(true);
					userBean.setIpAddress(ip);
					break;
				case HyphenCodes.IBCODESPF324_ALL_USERS_DIFF_PROFILE:
					// The restriction applied by user and all users will be
					// restricted by their own settings
					
					if (userBean.isRestrictedByIP()) {
						report.append("No Change - All User restricted by IP " + userBean.getIpAddress());
						return report.toString();
					}
//					if (userBean.isRestrictedByMAC()) {
//						report.append("No Change - All User restricted by MAC " + userBean.getMacAddress());
//						return report.toString();
//					}
					report.append("Left as ALL_USERS_DIFF_PROFILE and Updated user IP");
					userBean.setRestrictedByIP(true);
					userBean.setIpAddress(ip);
					break;
				case HyphenCodes.IBCODESPF324_SELECTIVE_USERS:
					// The restriction applied by user and ONLY users that are
					// configured will be restricted
					
					if (userBean.isRestrictedByIP()) {
						report.append("No Change - Selective User restricted by IP " + userBean.getIpAddress());
						return report.toString();
					}
//					if (userBean.isRestrictedByMAC()) {
//						report.append("No Change - Selective User restricted by MAC " + userBean.getMacAddress());
//						return report.toString();
//					}
					report.append("Left as SELECTIVE_USERS and Updated user IP");
					userBean.setRestrictedByIP(true);
					userBean.setIpAddress(ip);
					break;
				default:
					// There are no restrictions for user access
					report.append("Update To SELECTIVE_USERS");
					ddl.append(UPDATE_CDET + curfn + ";\n");
					userBean.setRestrictedByIP(true);
					userBean.setIpAddress(ip);
					break;
				}
			
			if(userBean != null){
				ddl.append("UPDATE IBUDETPF SET \n");
				ddl.append("UDRSTBY=" + userBean.getRestrictedBy());
				ddl.append(",\nUDRSTIP='" + userBean.getIpAddress()+"'");
				if(userBean.isRestrictedByTime()){
					ddl.append(",\nUDRSTPUBH=" + (userBean.allowPublicHolidays()?"'1'":"'0'"));
					ddl.append(",\nUDRSTDAYS=" + userBean.getAllowDays());
					ddl.append(",\nUDRSTSTIME=" + userBean.getStartTime());
					ddl.append(",\nUDRSTETIME=" + userBean.getEndTime());
				}
				ddl.append("\nWHERE UDUSRFN=" + usrfn +";\n");
			}else{
				report.append("Should not get THIS!!!!\n");
			}

			ddlWriter.append(ddl.toString());
			return report.toString();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	private void processFile(Connection conn, String fileName) {
		try {
			String reportFileName = fileName.replaceFirst(".csv", "Report.csv");
			FileWriter reportWriter = new FileWriter(reportFileName, true);
			String ddlFileName = fileName.replaceFirst(".csv", ".ddl");
			FileWriter ddlWriter = new FileWriter(ddlFileName, true);
			// create BufferedReader to read csv file
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String strLine = "";
			StringTokenizer st = null;
			int lineNumber = 0, tokenNumber = 0;
			// read comma separated file line by line
			while ((strLine = br.readLine()) != null) {
				reportWriter.append(strLine);
				lineNumber++;
				// skip first line
				if (lineNumber == 1){
					reportWriter.append("\n");
					continue;
				}
				ddlWriter.append("--" + strLine + "\n");
				reportWriter.append(",");
				// break comma separated line using ","
				/*
				 * Tokens 1 -> Customer 2 -> External User Reference 3 ->
				 * Same/Range 4 -> IP 5 -> ISP 6 -> Num Logins with IP
				 */
				int curfn = -1;
				int usrfn = -1;
				String ip = "x.x.x.x";
				st = new StringTokenizer(strLine, ",");
				while (st.hasMoreTokens()) {
					tokenNumber++;
					if (tokenNumber > 4)
						break;

					switch (tokenNumber) {
					case 1:
						curfn = Integer.valueOf(st.nextToken());
						break;
					case 2:
						usrfn = Integer.valueOf(st.nextToken());
						break;
					case 3:
						//not used
						st.nextToken();
						break;
					case 4:
						ip = st.nextToken().replace('*', 'x');
						break;
					}
				}
				
				reportWriter.append(processLine(conn, ddlWriter, curfn, usrfn, ip));
				// reset token number
				tokenNumber = 0;
				reportWriter.append("\n");
				ddlWriter.append("\n");
			}
			ddlWriter.flush();
			ddlWriter.close();

			reportWriter.flush();
			reportWriter.close();

		} catch (Exception e) {
			System.err.println("Exception while reading csv file: " + fileName);
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Connection ha01Conn = null;
		try {
			if (args.length != 5) {
				System.err.println("Required Parameters :");
				System.err.println("AS400 Database Library");
				System.err.println("Database IP");
				System.err.println("Database User");
				System.err.println("Database Password");
				System.err.println("Upload filename");
				System.exit(0);
			}
			ManageUserAccessDDLCreator creator = new ManageUserAccessDDLCreator();

			AS400JDBCDataSource mammothDataSource = new AS400JDBCDataSource();
			mammothDataSource.setLibraries(args[0]);
			mammothDataSource.setServerName(args[1]);
			mammothDataSource.setUser(args[2]);
			mammothDataSource.setPassword(args[3]);

			ha01Conn = mammothDataSource.getConnection();

			creator.processFile(ha01Conn, args[4]);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.err.println("Done");
			try {
				ha01Conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.exit(0);
	}

}