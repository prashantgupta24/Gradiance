package com.DB;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;


public class TA extends Login {

	// private Connection conn;
	private Statement stmt;
	private Scanner reader;

	// private ResultSet rs;

	public TA(Connection conn, Statement stmt, Scanner reader)
			throws SQLException {
		// this.conn = conn;
		this.stmt = stmt;
		this.reader = reader;
	}

	void selectedCourseScreen(String courseChoosen) throws SQLException {

		int userOption;

		do {
			System.out.println("For " + courseChoosen + " : ");
			System.out
					.println("\n 1. View homework \n 2. Reports \n 3. Go to previous menu");

			do {
				try {
					userOption = Integer.parseInt(reader.nextLine());
				} catch (NumberFormatException e) {
					continue;
				}
				if (userOption >= 1 && userOption <= 3)
					break;
				else
					System.out.println("Please enter correct option!");
			} while (true);
			switch (userOption) {
			case 1:
				viewHomework(courseChoosen);
				break;
			case 2:
				reports(courseChoosen);
				break;
			case 3:
				return;
			}

		} while (true);
	}

	/*
	 * private void selectCourse() throws SQLException {
	 * 
	 * do { int crs; System.out.println("Select course :"); HashMap<Integer,
	 * String> courses = new HashMap<>(); String courseChosen = ""; int i = 1;
	 * 
	 * String sql = "select course_id from TA where TA_ID = '" + UserId + "'";
	 * rs = stmt.executeQuery(sql);
	 * 
	 * while (rs.next()) { String courseID = rs.getString("course_id");
	 * System.out.println("\n" + i + ". " + courseID); courses.put(i++,
	 * courseID); } System.out.println("\n" + i + ". Go to previous screen"); do
	 * { try { crs = Integer.parseInt(reader.nextLine()); } catch (Exception e)
	 * { continue; } if (crs == i) return; courseChosen = courses.get(crs); if
	 * (courseChosen != null) break; else
	 * System.out.println("Invalid choice! Enter again"); } while (true);
	 * 
	 * selectedCourseScreen(courseChosen);
	 * 
	 * } while (true);
	 * 
	 * }
	 */

	private void reports(String courseid) {

		String inchoice;
		int choice;
		// Utility.ClearScreen();
		String query;
		ResultSet rs;
		System.out
				.println("======================================================================");
		System.out.println("1. Find students who did not take homework 1.");
		System.out
				.println("2. Find students who scored the maximum score on the first attempt for homework 1.");
		System.out
				.println("3. Find students who scored the maximum score on the first attempt for each homework.");
		System.out
				.println("4. For each student, show total score for each homework and average score across all homeworks.");
		System.out
				.println("5. For each homework, show average number of attempts");

		System.out.println("6. Back");
		System.out
				.println("======================================================================");
		System.out.println("Enter your choice:");
		try {
			inchoice = reader.nextLine();
			choice = Integer.parseInt(inchoice);
			while (choice <= 0 || choice > 6) {
				System.out.println("Enter a valid choice");
				inchoice = reader.nextLine();
				choice = Integer.parseInt(inchoice);
			}
		} catch (NumberFormatException e) {
			System.out.println("You entered an input which is not a number.");
			System.out
					.println("Please select this menu again from the previous menu");
			return;
		}
		String hw_id;
		try {
			switch (choice) {
			case 1:
				System.out.println("Enter the ID of homework-1:");
				hw_id = reader.nextLine();
				query = "SELECT NAME, USERID FROM USERS WHERE USERID IN (SELECT USERID FROM USERS WHERE USERLEVEL = 'Undergrad' or USERLEVEL = 'Grad' MINUS"
						+ "(SELECT A.USERID FROM ATTEMPT A WHERE A.EXERCISE_ID = '"
						+ hw_id + "' AND A.COURSE_ID = '" + courseid + "'))";
				rs = stmt.executeQuery(query);
				System.out
						.println("======================================================================");
				while (rs.next()) {
					System.out.println(rs.getString("NAME"));
					// System.out.println(rs.getString("USERID"));
				}
				System.out
						.println("======================================================================");
				break;

			case 2:
				System.out.println("Enter the ID of homework-1:");
				hw_id = reader.nextLine();
				query = "SELECT NAME, USERID FROM USERS WHERE USERID IN"
						+ "(SELECT USERID FROM ATTEMPT WHERE COURSE_ID = '"
						+ courseid + "' AND ATTEMPT_NO = 1 AND EXERCISE_ID = '"
						+ hw_id + "' AND MARKS = "
						+ "(SELECT MAX(MARKS) FROM ATTEMPT WHERE COURSE_ID = '"
						+ courseid + "' AND ATTEMPT_NO = 1 AND EXERCISE_ID = '"
						+ hw_id + "'))";
				rs = stmt.executeQuery(query);
				if (Utility.GetResultSetNumRows(rs) == 0) {
					System.out.println("Empty resultset");
					break;
				}
				System.out
						.println("======================================================================");
				while (rs.next()) {
					System.out.println(rs.getString("NAME"));
					// System.out.println(rs.getString("USERID"));
				}
				System.out
						.println("======================================================================");
				break;

			case 3:
				query = "SELECT EXID, U.NAME "
						+ "FROM (SELECT A.EXERCISE_ID AS EXID, A.USERID AS USID "
						+ "FROM ATTEMPT A WHERE A.COURSE_ID = '"
						+ courseid
						+ "' AND A.ATTEMPT_NO = 1 AND A.MARKS = "
						+ "(SELECT MAX(MARKS) FROM ATTEMPT B WHERE B.COURSE_ID = '"
						+ courseid
						+ "' AND B.ATTEMPT_NO = 1 AND B.EXERCISE_ID = A.EXERCISE_ID)), "
						+ "USERS U " + "WHERE USID = U.USERID";
				rs = stmt.executeQuery(query);
				if (Utility.GetResultSetNumRows(rs) == 0) {
					System.out.println("Empty resultset");
					break;
				}
				System.out
						.println("======================================================================");
				while (rs.next()) {
					System.out.println(rs.getString("EXID") + "\t"
							+ rs.getString("NAME"));
					// System.out.println(rs.getString("USERID"));
				}
				System.out
						.println("======================================================================");
				break;

			case 4:
				query = "SELECT USERID, SUM(MARKS) AS TOTAL, AVG(MARKS) AS AVG "
						+ "FROM ATTEMPT "
						+ "WHERE COURSE_ID = '"
						+ courseid
						+ "' " + "GROUP BY USERID";
				rs = stmt.executeQuery(query);
				System.out
						.println("=================================================");
				System.out.println("USERID\tTOTAL\tAVERAGE");
				System.out
						.println("=================================================");
				while (rs.next()) {
					System.out.println(rs.getString("USERID") + "\t "
							+ rs.getInt("TOTAL") + "\t" + rs.getFloat("AVG"));
				}
				break;

			case 5:
				query = "SELECT EXERCISE_ID, COUNT(*)/COUNT(DISTINCT USERID) AS AVERAGE "
						+ "FROM ATTEMPT "
						+ "WHERE COURSE_ID = '"
						+ courseid
						+ "' " + "GROUP BY EXERCISE_ID";
				rs = stmt.executeQuery(query);
				System.out
						.println("=================================================");
				System.out.println("EXERCISE_ID\tAVERAGE ATTEMPTS");
				System.out
						.println("=================================================");
				while (rs.next()) {
					System.out.println(rs.getString("EXERCISE_ID") + "\t\t\t"
							+ rs.getFloat("AVERAGE"));
				}
				break;

			/*
			 * case 6: System.out
			 * .println("Enter your query. You can only execute SELECT queries."
			 * ); query = reader.nextLine(); String query_lower =
			 * query.toLowerCase(); while (query_lower.startsWith("insert") ||
			 * query_lower.startsWith("update") ||
			 * query_lower.startsWith("delete") ||
			 * query_lower.startsWith("drop")) { System.out .println(
			 * "Query not allowed!!\nEnter again. Press ENTER if you do not want to enter query."
			 * ); query = reader.nextLine(); if (query.equals("")) return;
			 * query_lower = query.toLowerCase(); } rs =
			 * stmt.executeQuery(query_lower); ResultSetMetaData rsmd =
			 * rs.getMetaData(); System.out.println("Columns = " +
			 * rsmd.getColumnCount()); System.out .println(
			 * "=================================================================================================="
			 * ); for (int i = 1; i <= rsmd.getColumnCount(); i++) {
			 * System.out.print(rsmd.getColumnLabel(i));
			 * System.out.print(" | "); } System.out .println(
			 * "\n=================================================================================================="
			 * ); while (rs.next()) { for (int i = 1; i <=
			 * rsmd.getColumnCount(); i++)
			 * System.out.print(rs.getString(rsmd.getColumnLabel(i)) + " | ");
			 * System.out.println("\n"); } System.out .println(
			 * "=================================================================================================="
			 * );
			 */
			case 6:
				return;

			default:
				System.out.println("Wrong choice");
				return;
			}
		} catch (SQLException e) {
			// e.printStackTrace();
			System.out
					.println("Invalid SQL statement or SQL statement not allowed");
			return;
		}
		System.out.println("Press Enter to continue");
		reader.nextLine();
	}

	private void viewHomework(String courseChoosen) throws SQLException {
		int rows = 0;
		int option = 0;
		int counter = 0;
		String sql;
		try {
			ResultSet rs = null;

			do {
				sql = "SELECT * FROM ASSESSMENT WHERE COURSE_ID = '"
						+ courseChoosen + "'";
				rs = stmt.executeQuery(sql);
				rows = Utility.GetResultSetNumRows(rs);
				if (rows == 0) {
					System.out.println("No questions found for this criteria");
					return;
				}
				counter = 1;
				option = DisplayHomeworkMenu(rs, rows);
				if (option <= rows) {
					counter = 1;
					rs.beforeFirst();
					while (rs.next()) {
						if (counter == option) {
							// Modify homework parameteres here
							// Utility.ClearScreen();
							System.out
									.println("======================================================================");
							System.out
									.println("Here are the details of the homework "
											+ rs.getString("EXERCISE_ID"));
							System.out
									.println("======================================================================");
							System.out.println("Start date = "
									+ Utility.GetFormatDate(rs
											.getDate("START_DATE")));
							System.out.println("End date = "
									+ Utility.GetFormatDate(rs
											.getDate("END_DATE")));
							System.out.println("Topic = "
									+ rs.getString("TOPIC"));
							System.out.println("Score Selection Method = "
									+ rs.getString("SCORE_SELECTION_METHOD"));
							int retries = rs.getInt("RETRIES");
							if (retries > 0)
								System.out
										.println("Number of attempts allowed = "
												+ rs.getInt("RETRIES"));
							else
								System.out
										.println("Number of attempts allowed = infinite");
							System.out.println("Difficulty = "
									+ rs.getString("DIFFICULTY"));
							System.out.println("Points per correct answer = "
									+ rs.getInt("POINTS_CORRECT"));
							System.out.println("Points per incorect answer = "
									+ rs.getInt("POINTS_WRONG"));
							int question_count = 1;
							String[] questions = rs.getString("QUESTIONS_LIST")
									.split(",");
							System.out.println("\nHere are the questions:");
							for (String q : questions) {
								sql = "SELECT TEXT FROM QUESTIONS WHERE QUESTION_ID = '"
										+ q + "'";
								ResultSet temp = stmt.executeQuery(sql);
								if (temp.next()) {
									System.out.println(question_count + ") "
											+ temp.getString("TEXT"));
									temp.close();
								}
							}
							System.out
									.println("======================================================================");
							System.out.println("Press Enter to continue");
							reader.nextLine();
							break;
						}
						counter++;
					}
				}
			} while (option != (rows + 1));
		} catch (SQLException e) {
			System.out.println("Something bad happened here!!:(");
			e.printStackTrace();
		}
	}

	private int DisplayHomeworkMenu(ResultSet rs, int rows) {
		int counter = 1;
		int option = 0;
		// Utility.ClearScreen();
		System.out
				.println("======================================================================");
		try {
			while (rs.next()) {
				String exercise_id = rs.getString("EXERCISE_ID");
				System.out.println(counter + ". " + exercise_id);
				counter++;
			}

			System.out.println(counter + ". Go back to previous menu");
			System.out
					.println("======================================================================");
			System.out.println("Enter your choice:");
			option = reader.nextInt();
			reader.nextLine();
			while (option > rows + 1 || option == 0) {
				System.out.println("Enter valid choice:");
				option = reader.nextInt();
				reader.nextLine();
			}
		} catch (SQLException e) {
			System.out.println("Something bad happened here!!:(");
			e.printStackTrace();
			return (rows + 1);
		}
		return option;
	}

}
