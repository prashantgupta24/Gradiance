package com.DB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import com.backup.PasswordHash;

public class SuperUser extends Login {

	private Connection conn;
	private Statement stmt;
	private Scanner reader;
	private ResultSet rs;

	public SuperUser(Connection conn, Statement stmt, Scanner reader) {
		this.conn = conn;
		this.stmt = stmt;
		this.reader = reader;
	}

	public void mainScreen() throws SQLException {

		boolean isFirstTime = true;

		do {
			String name = "";
			String lastLogin = "";

			String sql = "select name, lastlogin from users where userid = '"
					+ UserId + "'";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				name = rs.getString("Name");
				lastLogin = rs.getString("lastlogin");
			}
			System.out.println("\n\nHello " + name);

			if (isFirstTime) {
				if (!(lastLogin == null))
					System.out
							.println("\nYou last logged in at : " + lastLogin);
				Utility.saveLastLogin(UserId, stmt);
				isFirstTime = false;
			}
			int userOption;
			System.out.println("\nWhat would you like to do today?");
			System.out
					.println("\n 1. Add Professor \n 2. Delete User \n 3. Logout");
			do {
				try {
					userOption = Integer.parseInt(reader.nextLine());
				} catch (Exception e) {
					continue;
				}
				if (userOption >= 1 && userOption <= 4)
					break;
				else
					System.out.println("Please enter correct option!");
			} while (true);
			switch (userOption) {
			case 1:
				AddProfessor();
				break;
			case 2:
				DeleteUser();
				break;
			case 3:
				System.out.println("You have successfully logged out!");
				Utility.cleanupNotifs(stmt);
				return;
			}
		} while (true);

	}

	private void DeleteUser() {
		// TODO Auto-generated method stub
		System.out.println("Enter the USERID to delete");
		String userid = reader.nextLine();
		String query = "DELETE FROM USERS WHERE USERID = '" + userid + "'";
		try {
			int res = stmt.executeUpdate(query);
			if (res != 1)
				System.out.println("User not deleted!!!");
			System.out.println("\nUser deleted!");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("SQL statement is not valid or not allowed");
		}

	}

	private void AddProfessor() {
		// TODO Auto-generated method stub
		try {
			System.out.println("Enter the Professor's name:");
			String name = reader.nextLine();
			System.out.println("Enter the Professor's id");
			String id = reader.nextLine();
			System.out.println("Enter the Professor's initial passowrd");
			String password = reader.nextLine();
			System.out
					.println("Professor will be asked to changed the passowrd");
			String pass = PasswordHash.createHash(password);
			String query = "INSERT INTO USERS VALUES('" + name + "', '" + id
					+ "', '" + pass + "', 'Faculty', 4, null)";

			int res = stmt.executeUpdate(query);
			if (res != 1)
				System.out.println("Professor was not added");
			System.out.println("Press ENTER to continue");
			reader.nextLine();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception while adding Professor");
		}
	}
}
