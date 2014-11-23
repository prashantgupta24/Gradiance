package com.DB;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import com.backup.PasswordHash;


public class Login {
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
	static final String DB_URL = "jdbc:oracle:thin:@localhost:1521:xe";

	// Database credentials
	static String USER = "prashant";
	static String PASS = "1234";

	public static String UserAccessLevel = "";
	public static String UserId = "";

	public static void main(String[] args) throws Exception {

		Connection conn = null;
		Statement stmt = null;
		Scanner reader = new Scanner(System.in);

		System.out.println("\nEnter the username for connecting to database:");
		USER = reader.nextLine();
		System.out.println("Enter the password:");
		PASS = reader.nextLine();

		try {
			// STEP 2: Register JDBC driver
			Class.forName("oracle.jdbc.driver.OracleDriver");

			// STEP 3: Open a connection
			System.out.println("Connecting to database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			// STEP 4: Execute a query
			System.out.println("Creating statement...");
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		boolean loop = true;
		do {
			clearSavedValues();
			loop = loginScreen(conn, stmt, reader);
			if (!UserAccessLevel.equals("")) {

				switch (UserAccessLevel) {
				case "1":
					Student s = new Student(conn, stmt, reader);
					s.mainScreen();
					break;
				case "2":
					Student s1 = new Student(conn, stmt, reader);
					s1.mainScreen();
					break;
				case "3":
					Prof p = new Prof(conn, stmt, reader);
					p.mainScreen();
					break;
					
				case "4":
					SuperUser su = new SuperUser(conn, stmt, reader);
					su.mainScreen();
					break;
				}
			}
		} while (loop);
		System.out.println("\n\nGoodbye!");
		stmt.close();
		conn.close();
	}// end main

	private static void clearSavedValues() {
		UserAccessLevel = "";
		UserId = "";
	}

	/**
	 * @param conn
	 * @param stmt
	 * @param reader
	 * @throws SQLException
	 */
	private static boolean loginScreen(Connection conn, Statement stmt,
			Scanner reader) throws SQLException {

		do {
			System.out
					.println("\nWelcome to the Awesome Gradiance Management system!\n\n 1. Login \n 2. Create User \n 3. Exit");
			int userOption;
			try {
				userOption = Integer.parseInt(reader.nextLine());
			} catch (NumberFormatException e) {
				continue;
			}

			switch (userOption) {
			case 1:
				login(stmt, reader);
				return true;
			case 2:
				createUser(conn, stmt, reader);
				break;
			case 3:
				return false;
			}
			/*
			 * if (userOption == 1) { login(stmt, reader); return true; } else
			 * if (userOption == 2) { createUser(conn, stmt, reader); } else
			 * if(userOption == 3) return false;
			 */
		} while (true);
	}

	/**
	 * @param stmt
	 * @param reader
	 * @throws SQLException
	 */
	private static void login(Statement stmt, Scanner reader)
			throws SQLException {

		String sql;
		ResultSet rs;
		String pw;
		String hash = "";

		do {
			System.out.println("Enter the User id:");
			UserId = reader.nextLine();
			System.out.println("Enter the password:");
			pw = reader.nextLine();

			sql = "SELECT USERID, PASSWORD, USERLEVEL, ACCESSLEVEL FROM USERS WHERE USERID ='"
					+ UserId + "'";
			// System.out.println(sql);
			rs = stmt.executeQuery(sql);
			while (rs.next())
				hash = rs.getString("password");

			try {
				if (hash.equals("") || !PasswordHash.validatePassword(pw, hash))
					System.out
							.println("Incorrect credentials! Try again? (y/n) : ");
				else {
					rs.beforeFirst();
					while (rs.next())
						UserAccessLevel = rs.getString("accesslevel");
					return;
				}
			} catch (NoSuchAlgorithmException | InvalidKeySpecException e1) {

				System.out.println("Error validating username/password!");
				return;
			}

			char ip;
			try {
				ip = reader.nextLine().charAt(0);
			} catch (Exception e) {

				// e.printStackTrace();
				continue;
			}

			if (ip == 'n')
				return;

		} while (true);

	}

	/**
	 * @param conn
	 * @param stmt
	 * @param reader
	 * @throws SQLException
	 */
	private static void createUser(Connection conn, Statement stmt,
			Scanner reader) throws SQLException {

		String sql = "";
		String name = "";
		String uId;
		String pwd;
		int ul;

		System.out.println("Enter your Name:");
		name = reader.nextLine();
		System.out.println("Enter your User Id:");
		uId = reader.nextLine();
		System.out.println("Enter the password:");
		pwd = reader.nextLine();
		String pwdHash = null;
		try {
			pwdHash = PasswordHash.createHash(pwd);
			// System.out.println(pwdHash);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e1) {
			System.out.println("Error creating hash values for password!");
			return;
		}

		do {
			System.out
					.println("Enter your study level: 1 (Undergrad) , 2 (Grad)");
			try {
				ul = Integer.parseInt(reader.nextLine());
			} catch (Exception e) {
				// e.printStackTrace();
				continue;
			}

			if (ul == 1 || ul == 2)
				break;
			else
				System.out.println("Please enter correct value! \n");

		} while (true);

		if (ul == 1)
			sql = "INSERT INTO USERS VALUES ('" + name + "', '" + uId + "', '"
					+ pwdHash + "','Undergrad', '1', NULL)";
		else if (ul == 2)
			sql = "INSERT INTO USERS VALUES ('" + name + "', '" + uId + "', '"
					+ pwdHash + "', 'Grad' , '1', NULL)";

		// System.out.println(sql);

		try {
			stmt.executeQuery(sql);
		} catch (java.sql.SQLIntegrityConstraintViolationException e) {
			// e.printStackTrace();
			System.out
					.println("\n\nUsername already exists, enter different value! \n");
			return;
		}

		try {
			conn.commit();
		} catch (Exception e) {

		}

		System.out.println("User created successfully!");
	}
}
