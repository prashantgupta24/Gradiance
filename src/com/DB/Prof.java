package com.DB;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;

import com.backup.PasswordHash;

public class Prof extends Login {

	private Connection conn;
	private Statement stmt;
	private Scanner reader;
	private ResultSet rs;
	private Calendar currDtCal = Calendar.getInstance();
	private Date currentdate;

	public Prof(Connection conn, Statement stmt, Scanner reader) {
		this.conn = conn;
		this.stmt = stmt;
		this.reader = reader;
		currDtCal.set(Calendar.HOUR_OF_DAY, 0);
		currDtCal.set(Calendar.MINUTE, 0);
		currDtCal.set(Calendar.SECOND, 0);
		currDtCal.set(Calendar.MILLISECOND, 0);
		currentdate = currDtCal.getTime();

	}

	public void mainScreen() throws SQLException {

		boolean isFirstLogin = true;

		do {
			String name = "";
			String lastLogin = "";

			String sql = "select name, lastlogin from users where userid = '"
					+ UserId + "'";
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				name = rs.getString("Name");
				lastLogin = rs.getString("lastlogin");
			}

			System.out.println("\n\nHello " + name);

			if (isFirstLogin) {
				if (!(lastLogin == null))
					System.out
							.println("\nYou last logged in at : " + lastLogin);
				Utility.saveLastLogin(UserId, stmt);
				isFirstLogin = false;
			}
			int userOption;
			System.out.println("\nWhat would you like to do today?");
			System.out
					.println("\n 1. Update TA information \n 2. Select Course \n 3. Add Course \n 4. Change Password \n 5. Logout");
			do {
				try {
					userOption = Integer.parseInt(reader.nextLine());
				} catch (NumberFormatException e) {
					continue;
				}
				if (userOption >= 1 && userOption <= 5)
					break;
				else
					System.out.println("Please enter correct option!");
			} while (true);
			switch (userOption) {
			case 1:
				updateTA();
				break;
			case 2:
				selectCourse();
				break;
			case 3:
				addCourse();
				break;
			case 4:
				ChangePassword();
				break;
			case 5:
				System.out.println("You have successfully logged out!");
				Utility.cleanupNotifs(stmt);
				return;
			}
		} while (true);

	}

	private void ChangePassword() {
		System.out.println("\nEnter your new password");
		String newpassword = reader.nextLine();
		try {
			String pass = PasswordHash.createHash(newpassword);
			String query = "UPDATE USERS SET PASSWORD = '" + pass
					+ "' WHERE USERID = '" + UserId + "'";
			int res = stmt.executeUpdate(query);
			if (res != 1)
				System.out.println("Password not changed!!");
			System.out
					.println("\nPassword changed successfully! Press ENTER to continue");
			reader.nextLine();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			System.out.println("Exception: NoSuchAlgorithms");
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			System.out.println("Exception: InvalidKeySpecException");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Invalid SQL query");
		}

	}

	private void courseSelectedScreen(String courseChoosen) throws SQLException {

		int userOption;

		do {

			notifHandler(courseChoosen);

			System.out.println("\nFor " + courseChoosen + " : ");
			System.out
					.println("\n 1. Add homework \n 2. Add/Remove Questions to homework \n 3. Edit homework \n 4. View homework"
							+ "\n 5. View notifications \n 6. Post notifications \n 7. Reports \n 8. View Scores \n 9. Go to main menu");

			do {
				try {
					userOption = Integer.parseInt(reader.nextLine());
				} catch (NumberFormatException e) {
					continue;
				}
				if (userOption >= 1 && userOption <= 9)
					break;
				else
					System.out.println("Please enter correct option!");
			} while (true);
			switch (userOption) {
			case 1:
				addHW(courseChoosen);
				break;
			case 2:
				addRemQuesHW(courseChoosen);
				break;
			case 3:
				EditHomework(courseChoosen);
				break;
			case 4:
				viewHomework(courseChoosen);
				break;
			case 5:
				viewNotifs(courseChoosen);
				break;
			case 6:
				postNotifs(courseChoosen);
				break;
			case 7:
				reports(courseChoosen);
				break;
			case 8:
				viewScores(courseChoosen);
				break;
			case 9:
				return;
			}

		} while (true);
	}

	private void viewScores(String courseid) {

		System.out.println("Enter the Homework ID:");
		String hwid = reader.nextLine();
		String sql = "SELECT SCORE_SELECTION_METHOD FROM ASSESSMENT "
				+ "WHERE EXERCISE_ID = '" + hwid + "'";
		try {
			rs = stmt.executeQuery(sql);
			String policy = "";
			while (rs.next())
				policy = rs.getString("SCORE_SELECTION_METHOD");

			System.out.println("Displaying scores according to the " + policy
					+ " scoring policy");

			if (policy.equals("average score"))
				sql = "SELECT A.USERID, AVG(A.MARKS) AS SCORE "
						+ "FROM ATTEMPT A " + "WHERE A.EXERCISE_ID = '" + hwid
						+ "'" + "GROUP BY A.USERID";

			else if (policy.equals("latest attempt"))
				sql = "SELECT A.USERID, A.MARKS AS SCORE " + "FROM ATTEMPT A "
						+ "WHERE A.EXERCISE_ID = '" + hwid + "'"
						+ "GROUP BY A.USERID, A.MARKS, A.ATTEMPT_NO "
						+ "HAVING A.ATTEMPT_NO = (SELECT MAX(B.ATTEMPT_NO) "
						+ "FROM ATTEMPT B " + "WHERE B.EXERCISE_ID = '" + hwid
						+ "'" + "AND B.USERID = A.USERID)";

			else
				sql = "SELECT A.USERID, MAX(A.MARKS) AS SCORE "
						+ "FROM ATTEMPT A " + "WHERE A.EXERCISE_ID = '" + hwid
						+ "'" + "GROUP BY A.USERID";

			rs = stmt.executeQuery(sql);

			System.out
					.println("==================================================================================================");
			System.out.println("USERID\tSCORE");
			System.out
					.println("==================================================================================================");
			while (rs.next()) {
				System.out.println(rs.getString("USERID") + "\t"
						+ rs.getString("SCORE"));
			}
			System.out
					.println("==================================================================================================");
			System.out.println("Press ENTER to continue");
			reader.nextLine();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out
					.println("Something bad happened. Going to the previous menu.");
		}
	}

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
		System.out.println("6. Enter custom query");
		System.out.println("7. Back");
		System.out
				.println("======================================================================");
		System.out.println("Enter your choice:");
		try {
			inchoice = reader.nextLine();
			choice = Integer.parseInt(inchoice);
			while (choice <= 0 || choice > 7) {
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
				query = "SELECT U.NAME, U.USERID "
						+ "FROM USERS U "
						+ "WHERE U.USERID IN (SELECT B.STUDENT_ID " 
						                   + "FROM ENROLLMENT B "
						                   + "WHERE B.COURSE_ID = '" + courseid + "'"
						                   + "MINUS (SELECT A.USERID " 
						                           + "FROM ATTEMPT A " 
						                           + "WHERE A.EXERCISE_ID = '" + hw_id + "'" 
						                           + "AND A.COURSE_ID = '" + courseid + "'))";
				System.out.println(query);
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
				System.out.println(query);
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
				System.out.println(query);
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
				System.out.println(query);
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
				System.out.println(query);
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

			case 6:
				System.out
						.println("Enter your query. You can only execute SELECT queries.");
				query = reader.nextLine();
				String query_lower = query.toLowerCase();
				while (query_lower.startsWith("insert")
						|| query_lower.startsWith("update")
						|| query_lower.startsWith("delete")
						|| query_lower.startsWith("drop")) {
					System.out
							.println("Query not allowed!!\nEnter again. Press ENTER if you do not want to enter query.");
					query = reader.nextLine();
					if (query.equals(""))
						return;
					query_lower = query.toLowerCase();
				}
				rs = stmt.executeQuery(query_lower);
				ResultSetMetaData rsmd = rs.getMetaData();
				//System.out.println("Columns = " + rsmd.getColumnCount());
				System.out
						.println("==================================================================================================");
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					System.out.print(rsmd.getColumnLabel(i));
					System.out.print(" | ");
				}
				System.out
						.println("\n==================================================================================================");
				while (rs.next()) {
					for (int i = 1; i <= rsmd.getColumnCount(); i++)
						System.out.print(rs.getString(rsmd.getColumnLabel(i))
								+ " | ");
					System.out.println("\n");
				}
				System.out
						.println("==================================================================================================");
			case 7:
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

	private void addRemQuesHW(String courseid) {
		int rows = 0, option = 0, counter = 0;
		try {
			String current = Utility.GetFormatDate(currentdate);
			String sql = "SELECT EXERCISE_ID, TOPIC, DIFFICULTY, QUESTIONS_LIST FROM ASSESSMENT WHERE END_DATE > '"
					+ current + "' AND COURSE_ID = '" + courseid + "'";
			ResultSet rs = null;
			do {
				rs = stmt.executeQuery(sql);
				rows = Utility.GetResultSetNumRows(rs);

				if (rows == 0) {
					System.out.println("No homeworks found");
					return;
				}
				counter = 1;
				option = DisplayHomeworkMenu(rs, rows);
				if (option <= rows) {
					counter = 1;
					rs.beforeFirst();
					while (rs.next()) {
						if (counter == option) {
							String exercise = rs.getString("EXERCISE_ID");
							String topic = rs.getString("TOPIC");
							String difficulty = rs.getString("DIFFICULTY");
							String questions = rs.getString("QUESTIONS_LIST");
							HandleModifyHomework(exercise, topic, difficulty,
									questions, courseid);
							break;
						}
						counter++;
					}
				}
				rs.close();
			} while (option != (rows + 1));
		} catch (SQLException e) {
			System.out.println("Something bad happened here!!:(");
			e.printStackTrace();
		}

	}

	private void HandleModifyHomework(String exercise, String topic,
			String difficulty, String questions, String courseChosen) {
		String inchoice;
		int option = 0;
		do {
			// Utility.ClearScreen();
			System.out
					.println("======================================================================");
			System.out.println(exercise);
			System.out
					.println("======================================================================");
			System.out.println("1. Search and Add question");
			System.out.println("2. Remove question");
			System.out.println("3. Back");
			System.out
					.println("======================================================================");
			System.out.println("Enter your choice:");
			try {
				inchoice = reader.nextLine();
				option = Integer.parseInt(inchoice);
				while (option > 3) {
					System.out.println("Invalid choice.\nEnter your choice:");
					inchoice = reader.nextLine();
					option = Integer.parseInt(inchoice);
				}
			} catch (NumberFormatException e) {
				System.out
						.println("You entered an input which is not a number.");
				System.out
						.println("Please select this menu again from the previous menu");
				return;
			}
			if (option == 1)
				SearchAddQuestion(exercise, topic, difficulty, questions,
						courseChosen);
			else if (option == 2)
				RemoveQuestion(exercise, questions);
		} while (option != 3);
	}

	private void RemoveQuestion(String exercise, String questions) {
		int counter = 1;
		String inremove;
		int remove = 0;
		String sql = "";
		String[] list = questions.split(",");
		ResultSet rs = null;
		// Utility.ClearScreen();
		System.out
				.println("======================================================================");
		System.out.println("Here are the questions currently in the homework");
		System.out
				.println("======================================================================");
		try {
			for (String question : list) {
				sql = "SELECT TEXT FROM QUESTIONS WHERE QUESTION_ID = '"
						+ question + "'";
				rs = stmt.executeQuery(sql);
				while (rs.next())
					System.out.println(counter + ". " + rs.getString("TEXT"));
				counter++;
			}
			rs.close();
			System.out
					.println("======================================================================");
			System.out
					.println("Enter the question number you want to delete. Press 0 to go back:");
			try {
				inremove = reader.nextLine();
				remove = Integer.parseInt(inremove);
				while (remove > list.length) {
					System.out.println("Enter a valid question number");
					inremove = reader.nextLine();
					remove = Integer.parseInt(inremove);
				}
			} catch (NumberFormatException e) {
				System.out
						.println("You entered an input which is not a number.");
				System.out
						.println("Please select this menu again from the previous menu");
				return;
			}
			if (remove == 0)
				return;
			String remove_id = list[remove - 1];

			// Form a new list of question IDs
			String new_list = "";
			for (String question : list) {
				if (question.equals(remove_id))
					continue;
				else
					new_list = new_list + question + ",";
			}

			new_list = new_list.substring(0, new_list.length() - 1);
			sql = "UPDATE ASSESSMENT SET QUESTIONS_LIST = '" + new_list
					+ "' WHERE EXERCISE_ID = '" + exercise + "'";
			int rows = stmt.executeUpdate(sql);
			if (rows != 1) {
				System.out.println("Homework not modified");
			} else {
				System.out
						.println("\n*******************\nHomework modified!!\n*******************\n");
				conn.commit();
			}
			System.out.println("Press Enter to continue");
			reader.nextLine();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void SearchAddQuestion(String exercise, String topic,
			String difficulty, String questions, String courseChosen) {
		int levelLow = Integer.parseInt(difficulty.split("-")[0]);
		int levelHigh = Integer.parseInt(difficulty.split("-")[1]);
		try {
			String sql = "SELECT Q1.QUESTION_ID, Q1.TEXT FROM QUESTIONS Q1 WHERE Q1.DIFFICULTY >= "
					+ levelLow
					+ " AND Q1.DIFFICULTY <= "
					+ levelHigh
					+ " AND Q1.CHAPTER_ID IN ("
					+ "SELECT C1.CHAPTER_ID FROM CHAPTERS C1, COURSETEXTBOOK T1"
					+ " WHERE T1.COURSE_ID = '"
					+ courseChosen
					+ "'"
					+ " AND T1.ISBN = C1.ISBN"
					+ " AND C1.CHAPTER_NAME = '"
					+ topic + "')";

			ResultSet rs = null;
			rs = stmt.executeQuery(sql);
			int rows = Utility.GetResultSetNumRows(rs);

			if (rows == 0) {
				System.out.println("No questions found for this criteria");
				return;
			}

			int question_num = 1;
			// Utility.ClearScreen();
			System.out
					.println("======================================================================");
			while (rs.next()) {
				String text = rs.getString("TEXT");
				System.out.print(question_num + ": ");
				System.out.println(", Question: " + text);
				question_num++;
			}
			System.out
					.println("======================================================================");

			String[] new_questions_str;
			String[] old_questions_id_str;
			System.out
					.println("Select the questions you want to include in the exercise.");
			System.out
					.println("Type the question numbers and separate them by a comma. E.g. 1,2 if you want to select questions 1 and 2.");
			String question_string = reader.nextLine();
			new_questions_str = question_string.split(",");
			old_questions_id_str = questions.split(",");
			ArrayList<Integer> new_questions = new ArrayList<Integer>();
			for (String ques : new_questions_str)
				new_questions.add(Integer.parseInt(ques));
			Collections.sort(new_questions);

			HashSet<Integer> new_questions_set = new HashSet<Integer>();
			for (String ques : old_questions_id_str)
				new_questions_set.add(Integer.parseInt(ques));

			question_num = 1;
			rs.beforeFirst();
			while (rs.next()) {
				if (new_questions.contains(question_num))
					new_questions_set.add(rs.getInt("QUESTION_ID"));
				question_num++;
			}
			String question_list = "";
			for (Integer id : new_questions_set) {
				question_list = question_list + id + ",";
			}
			question_list = question_list.substring(0,
					question_list.length() - 1);

			sql = "UPDATE ASSESSMENT SET QUESTIONS_LIST = '" + question_list
					+ "' WHERE EXERCISE_ID = '" + exercise + "'";
			int row = stmt.executeUpdate(sql);
			if (row == 1) {
				System.out
						.println("\n*******************\nHomework modified!!\n*******************\n");
				conn.commit();
			} else {
				System.out.println("Homework not modified!!");
			}
			System.out.println("Press Enter to continue");
			reader.nextLine();
			rs.close();

		} catch (SQLException e) {
			System.out.println("Something bad happened here!!:(");
			e.printStackTrace();
		}

	}

	private void addHW(String courseid) {
		// Utility.ClearScreen();
		String inchoice, difficulty, topic, exercise_id, scoring_scheme;
		int retries, num_questions, points_per_correct, points_per_incorrect, seed;
		int level = 0, levelLow = 0, levelHigh = 0;
		System.out
				.println("======================================================================");
		System.out
				.println("Enter start date in (dd-Month-YY) format. E.g 20-AUG-14");
		String start_date = reader.nextLine();
		while (!Utility.isThisDateValid(start_date, "dd-MMM-yy")
				|| !Utility.CompareTodayDate(currentdate, start_date)) {
			System.out.println("Enter valid start date:");
			start_date = reader.nextLine();
		}
		System.out
				.println("Enter end date in (dd-Month-YY) format. E.g 20-AUG-14");
		String end_date = reader.nextLine();
		while (!Utility.isThisDateValid(end_date, "dd-MMM-yy")
				|| !Utility.CompareDates(start_date, end_date)
				|| !Utility.CompareTodayDate(currentdate, end_date)) {
			System.out.println("Enter valid end date:");
			end_date = reader.nextLine();
		}

		try {
			System.out
					.println("Enter number of attempts. Enter 0 for infinite attempts:");
			inchoice = reader.nextLine();
			retries = Integer.parseInt(inchoice);

			System.out.println("Enter topic:");
			topic = reader.nextLine();
			System.out
					.println("Enter difficulty range. Valid range is 1-5. (Example: 1-3, 1, 5, 1-5... Invalid: 0, 1-8, 6):");
			difficulty = reader.nextLine();
			Boolean check = true;
			do {
				check = true;
				if (!difficulty.contains("-") || difficulty.startsWith("-")) {
					level = Integer.parseInt(difficulty);
					levelLow = 1;
					levelHigh = 5;
				} else {
					levelLow = Integer.parseInt(difficulty.split("-")[0]);
					levelHigh = Integer.parseInt(difficulty.split("-")[1]);
					level = 1;
				}
				if (level <= 0 || level > 5 || levelLow <= 0 || levelLow > 5
						|| levelHigh <= 0 || levelHigh > 5) {
					System.out.println("Enter a valid difficulty range");
					difficulty = reader.nextLine();
					check = false;
				}
			} while (!check);
			System.out
					.println("Enter the score selection scheme: (latest attempt, maximum score or average score)");
			scoring_scheme = reader.nextLine();
			while (!(scoring_scheme.equals("latest attempt")
					|| scoring_scheme.equals("maximum score") || scoring_scheme
						.equals("average score"))) {
				System.out.println("Invalid scoring scheme.\nEnter again:");
				scoring_scheme = reader.nextLine();
			}
			System.out.println("Enter number of questions:");
			inchoice = reader.nextLine();
			num_questions = Integer.parseInt(inchoice);
			System.out.println("Enter the points per correct answer value");
			inchoice = reader.nextLine();
			points_per_correct = Integer.parseInt(inchoice);
			System.out.println("Enter the points per incorrect answer value");
			inchoice = reader.nextLine();
			points_per_incorrect = Integer.parseInt(inchoice);
			System.out
					.println("Give a name to the exercise (ID should be unique)");
			exercise_id = reader.nextLine();
			Random randomGenerator = new Random();
			seed = randomGenerator.nextInt(100);
			System.out
					.println("======================================================================");
		} catch (NumberFormatException e) {
			System.out.println("You entered string where number was expected.");
			System.out
					.println("Please select this menu again from the previous menu");
			return;
		}
		try {
			String sql;
			if (difficulty.contains("-"))
				sql = "SELECT Q1.QUESTION_ID, Q1.TEXT FROM QUESTIONS Q1 WHERE Q1.DIFFICULTY >= "
						+ levelLow
						+ " AND Q1.DIFFICULTY <= "
						+ levelHigh
						+ " AND Q1.CHAPTER_ID IN ("
						+ "SELECT C1.CHAPTER_ID FROM CHAPTERS C1, COURSETEXTBOOK T1"
						+ " WHERE T1.COURSE_ID = '"
						+ courseid
						+ "'"
						+ " AND T1.ISBN = C1.ISBN"
						+ " AND C1.CHAPTER_NAME = '"
						+ topic + "')";
			else
				sql = "SELECT Q1.QUESTION_ID, Q1.TEXT FROM QUESTIONS Q1 WHERE Q1.DIFFICULTY = "
						+ level
						+ " AND Q1.CHAPTER_ID IN ("
						+ "SELECT C1.CHAPTER_ID FROM CHAPTERS C1, COURSETEXTBOOK T1"
						+ " WHERE T1.COURSE_ID = '"
						+ courseid
						+ "'"
						+ " AND T1.ISBN = C1.ISBN"
						+ " AND C1.CHAPTER_NAME = '"
						+ topic + "')";

			// System.out.println(sql);

			ResultSet rs = null;
			rs = stmt.executeQuery(sql);
			int rows = Utility.GetResultSetNumRows(rs);

			if (rows == 0) {
				System.out.println("\nNo questions found for this criteria!");
				return;
			}

			int question_num = 1;
			while (rs.next()) {
				String text = rs.getString("TEXT");
				System.out.print(question_num + ": ");
				System.out.println(", Question: " + text);
				question_num++;
			}

			String[] questions_str;
			do {
				System.out
						.println("Select the questions you want to include in the exercise.");
				System.out
						.println("Type the question numbers and separate them by a comma. E.g. 1,2 if you want to select questions 1 and 2.");
				String question_string = reader.nextLine();
				questions_str = question_string.split(",");
			} while (questions_str.length != num_questions
					&& num_questions <= rows);

			ArrayList<Integer> questions = new ArrayList<Integer>();
			for (String ques : questions_str)
				questions.add(Integer.parseInt(ques));

			question_num = 1;
			Collections.sort(questions);
			String question_list = "";
			rs.beforeFirst();
			while (rs.next()) {
				if (questions.contains(question_num))
					question_list = question_list + rs.getInt("QUESTION_ID")
							+ ",";
				question_num++;
			}
			question_list = question_list.substring(0,
					question_list.length() - 1);

			sql = "INSERT INTO ASSESSMENT VALUES ('" + exercise_id + "', '"
					+ question_list + "', " + points_per_correct + ", "
					+ points_per_incorrect + ", '" + start_date + "', '"
					+ end_date + "', " + retries + ", " + seed + ", '"
					+ scoring_scheme + "', '" + courseid + "', '" + topic
					+ "', '" + difficulty + "', " + num_questions + ")";
			rs = stmt.executeQuery(sql);
			try {
				conn.commit();
			} catch (Exception e) {

			}
			System.out
					.println("\n*******************\nHomework created!!\n*******************\n");
			/*
			 * rs.close(); stmt.close(); conn.close();
			 */

		} catch (SQLException e) {
			System.out.println("Something bad happened here!!:(");
			e.printStackTrace();
		}
	}

	private void postNotifs(String courseChoosen) throws SQLException {

		Statement stmt1 = conn.createStatement(
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

		do {
			String notifMsg = "";
			System.out.println("\nEnter the notification to be broadcasted :");
			notifMsg = reader.nextLine();
			String sql = "select student_id from enrollment where course_id = '"
					+ courseChoosen + "'";

			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				String stuID = rs.getString("student_id");

				String insertSQL = "INSERT INTO notification VALUES ('" + stuID
						+ "', '" + courseChoosen + "', '" + notifMsg
						+ "','N', CURRENT_DATE, 'Y')";
				stmt1.executeQuery(insertSQL);
			}
			
			try {
				conn.commit();
			} catch (Exception e) {

			}

			System.out
					.println("\n\nNotification broadcasted! Do you want to send another? (y/n)");
			char ip = reader.nextLine().charAt(0);

			if (ip == 'n')
				return;

		} while (true);

	}

	/*
	 * private void viewHomework(String courseChoosen) throws SQLException {
	 * 
	 * do { int crs; System.out.println("Select homework :"); HashMap<Integer,
	 * String> courses = new HashMap<>(); String homeworkChosen = ""; int i = 1;
	 * 
	 * String sql = "select exercise_id from ASSESSMENT where course_id = '" +
	 * courseChoosen + "'";
	 * 
	 * rs = stmt.executeQuery(sql);
	 * 
	 * while (rs.next()) { String ex_ID = rs.getString("exercise_id");
	 * System.out.println("\n" + i + ". " + ex_ID); courses.put(i++, ex_ID); }
	 * System.out.println("\n" + i + ". Go to previous screen"); do { crs =
	 * Integer.parseInt(reader.nextLine()); if (crs == i) return; homeworkChosen
	 * = courses.get(crs); if (homeworkChosen != null) break; else
	 * System.out.println("Invalid choice! Enter again"); } while (true);
	 * 
	 * hwDetailScreen(homeworkChosen);
	 * 
	 * } while (true);
	 * 
	 * }
	 */

	private void viewHomework(String courseChosen) {
		int rows = 0;
		int option = 0;
		int counter = 0;
		String sql;
		try {
			ResultSet rs = null;

			do {
				sql = "SELECT * FROM ASSESSMENT WHERE COURSE_ID = '"
						+ courseChosen + "'";
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
		String inchoice;
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
			inchoice = reader.nextLine();
			option = Integer.parseInt(inchoice);
			while (option > rows + 1 || option == 0) {
				System.out.println("Enter valid choice:");
				inchoice = reader.nextLine();
				option = Integer.parseInt(inchoice);
			}
		} catch (SQLException e) {
			System.out.println("Something bad happened here!!:(");
			e.printStackTrace();
			return (rows + 1);
		} catch (NumberFormatException e) {
			System.out.println("You entered an input which is not a number.");
			System.out
					.println("Please select this menu again from the previous menu");
			return (rows + 1);
		}
		return option;
	}

	private void addCourse() throws SQLException {

		boolean loop;
		String course_id = "";

		do {
			loop = false;
			System.out.println("\nPlease enter the following information : ");
			System.out.println("\nPlease enter course name :");
			String courseName = reader.nextLine();
			System.out.println("\nPlease enter unique token for course :");
			String token = reader.nextLine();
			System.out.println("\nPlease enter course_id for course :");
			course_id = reader.nextLine();
			System.out.println("\nPlease enter max enrollment for course :");
			Integer max_enroll = null;
			Boolean isException = true;

			while (isException) {
				try {
					max_enroll = Integer.parseInt(reader.nextLine());
					isException = false;
				} catch (Exception e1) {
					isException = true;
				}
			}
			System.out.println("\nPlease enter course level for course :");
			String courseLevel = reader.nextLine();
			System.out
					.println("\nPlease enter start date (dd-mmm-yy) for course :");
			String startDate = reader.nextLine();
			System.out
					.println("\nPlease enter end date (dd-mmm-yy) for course :");
			String endDate = reader.nextLine();
			String sql = "insert into COURSES values ('" + token + "', '"
					+ course_id + "', '" + UserId + "', '0', '" + max_enroll
					+ "', '" + courseLevel + "', '" + startDate + "', '"
					+ endDate + "', '" + courseName + "')";

			// System.out.println(sql);
			try {
				stmt.executeQuery(sql);
			} catch (SQLException e) {
				e.printStackTrace();
				System.out
						.println("\nCannot insert row! Do you want to enter values again? (y/n)");

				char ip = reader.nextLine().charAt(0);

				if (ip == 'n')
					return;

				loop = true;
			}
		} while (loop);

		try {
			conn.commit();
		} catch (SQLException e) {

		}
		System.out.println("\n\nCourse successfully added!\n");
		courseSelectedScreen(course_id);

	}

	private void EditHomework(String courseChosen) {
		int rows = 0;
		int option = 0;
		int counter = 0;
		try {
			String current = Utility.GetFormatDate(currentdate);
			String sql = "SELECT EXERCISE_ID, START_DATE FROM ASSESSMENT WHERE END_DATE > '"
					+ current + "' AND COURSE_ID = '" + courseChosen + "'";
			ResultSet rs = null;

			do {
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
							EditHomeworkParameters(rs.getString("EXERCISE_ID"),
									courseChosen);
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

	private void EditHomeworkParameters(String exercise, String courseid) {
		int choice, rows = 0;
		String sql, inchoice;
		ResultSet rs = null;

		// Utility.ClearScreen();
		System.out
				.println("======================================================================");
		System.out.println("Edit " + exercise);
		System.out.println("Choose what to update:");
		System.out
				.println("======================================================================");
		System.out.println("1. Start date");
		System.out.println("2. End date");
		System.out.println("3. Number of attempts");
		System.out.println("4. Topics");
		System.out.println("5. Difficulty level");
		System.out
				.println("6. Score selection (latest attempt, maximum score or average score)");
		System.out.println("7. Number of questions");
		System.out.println("8. Correct answer points");
		System.out.println("9. Incorrect answer points");
		System.out.println("10. Back");
		System.out
				.println("======================================================================");
		System.out.println("Enter your choice:");

		try {
			inchoice = reader.nextLine();
			choice = Integer.parseInt(inchoice);
			while (choice <= 0 || choice > 10) {
				System.out.println("Enter valid option");
				inchoice = reader.nextLine();
				choice = Integer.parseInt(inchoice);
			}
		} catch (NumberFormatException e) {
			System.out.println("You entered an input which is not a number.");
			System.out
					.println("Please select this menu again from the previous menu");
			return;
		}

		try {
			sql = "SELECT * FROM ASSESSMENT WHERE EXERCISE_ID = '" + exercise
					+ "'";
			rs = stmt.executeQuery(sql);
			if (rs.next()) {
				switch (choice) {
				case 1:
					System.out
							.println("Enter the new start date.\n Current start date is "
									+ Utility.GetFormatDate(rs
											.getDate("START_DATE")));
					String start_date = reader.nextLine();
					while (!Utility.isThisDateValid(start_date, "dd-MMM-yy")
							|| !Utility.CompareDates(start_date, Utility
									.GetFormatDate(rs.getDate("END_DATE")))
							|| !Utility.CompareTodayDate(currentdate,
									start_date)) {
						System.out.println("Enter valid start date:");
						start_date = reader.nextLine();
					}
					sql = "UPDATE ASSESSMENT SET START_DATE = '" + start_date
							+ "' WHERE EXERCISE_ID = '" + exercise
							+ "' AND COURSE_ID = '" + courseid + "'";
					rows = stmt.executeUpdate(sql);
					break;

				case 2:
					System.out
							.println("Enter the new end date.\n Current end date is "
									+ Utility.GetFormatDate(rs
											.getDate("END_DATE")));
					String end_date = reader.nextLine();
					while (!Utility.isThisDateValid(end_date, "dd-MMM-yy")
							|| !Utility.CompareDates(Utility.GetFormatDate(rs
									.getDate("START_DATE")), end_date)
							|| !Utility.CompareTodayDate(currentdate, end_date)) {
						System.out.println("Enter valid end date:");
						start_date = reader.nextLine();
					}
					sql = "UPDATE ASSESSMENT SET END_DATE = '" + end_date
							+ "' WHERE EXERCISE_ID = '" + exercise
							+ "' AND COURSE_ID = '" + courseid + "'";
					rows = stmt.executeUpdate(sql);
					break;

				case 3:
					System.out
							.println("Enter the new value of number of attempts:");
					System.out.println("Current value is "
							+ rs.getInt("RETRIES"));
					int numattempt;
					String invalue;
					try {
						invalue = reader.nextLine();
						numattempt = Integer.parseInt(invalue);
					} catch (NumberFormatException e) {
						System.out
								.println("You entered an input which is not a number.");
						System.out
								.println("Please select this menu again from the previous menu");
						return;
					}
					sql = "UPDATE ASSESSMENT SET RETRIES = " + numattempt
							+ " WHERE EXERCISE_ID = '" + exercise
							+ "' AND COURSE_ID = '" + courseid + "'";
					rows = stmt.executeUpdate(sql);
					break;

				case 6:
					System.out
							.println("Enter the new score selection (latest attempt, maximum score or average score).");
					System.out.println("Current value is "
							+ rs.getString("SCORE_SELECTION_METHOD"));
					String scoring_policy = reader.nextLine();
					reader.nextLine();
					sql = "UPDATE ASSESSMENT SET SCORE_SELECTION_METHOD = '"
							+ scoring_policy + "' WHERE EXERCISE_ID = '"
							+ exercise + "' AND COURSE_ID = '" + courseid + "'";
					rows = stmt.executeUpdate(sql);
					break;

				case 8:
					System.out
							.println("Enter the new value of correct answer points:");
					System.out.println("Current value is "
							+ rs.getInt("POINTS_CORRECT"));
					int correct_points;
					try {
						invalue = reader.nextLine();
						correct_points = Integer.parseInt(invalue);
					} catch (NumberFormatException e) {
						System.out
								.println("You entered an input which is not a number.");
						System.out
								.println("Please select this menu again from the previous menu");
						return;
					}
					sql = "UPDATE ASSESSMENT SET POINTS_CORRECT = "
							+ correct_points + " WHERE EXERCISE_ID = '"
							+ exercise + "' AND COURSE_ID = '" + courseid + "'";
					rows = stmt.executeUpdate(sql);
					break;

				case 9:
					System.out
							.println("Enter the new value of wrong answer points:");
					System.out.println("Current value is "
							+ rs.getInt("POINTS_WRONG"));
					int incorrect_points;
					try {
						invalue = reader.nextLine();
						incorrect_points = Integer.parseInt(invalue);
					} catch (NumberFormatException e) {
						System.out
								.println("You entered an input which is not a number.");
						System.out
								.println("Please select this menu again from the previous menu");
						return;
					}
					reader.nextLine();
					sql = "UPDATE ASSESSMENT SET POINTS_WRONG = "
							+ incorrect_points + " WHERE EXERCISE_ID = '"
							+ exercise + "' AND COURSE_ID = '" + courseid + "'";
					rows = stmt.executeUpdate(sql);
					break;

				case 10:
					return;

				default:
					System.out.println("Choice not applicable");
					return;
				}
			}
			if (rows != 1)
				System.out.println("Homework not modified");
			else {
				System.out
						.println("\n*******************\nHomework modified!!\n*******************\n");
				System.out.println("Press ENTER to continue");
				reader.nextLine();
				conn.commit();
			}
		} catch (SQLException e) {
			System.out.println("Something bad happened here!!:(");
			e.printStackTrace();
		}
	}

	private void selectCourse() throws SQLException {

		do {
			int crs;
			System.out.println("Select course :");
			HashMap<Integer, String> courses = new HashMap<>();
			String courseChosen = "";
			int i = 1;

			String sql = "select course_id from courses where professor_id = '"
					+ UserId + "'";
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				String courseID = rs.getString("course_id");
				System.out.println("\n" + i + ". " + courseID);
				courses.put(i++, courseID);
			}
			System.out.println("\n" + i + ". Go to previous screen");
			do {
				try {
					crs = Integer.parseInt(reader.nextLine());
				} catch (NumberFormatException e) {
					continue;
				}
				if (crs == i)
					return;
				courseChosen = courses.get(crs);
				if (courseChosen != null)
					break;
				else
					System.out.println("Invalid choice! Enter again");
			} while (true);

			courseSelectedScreen(courseChosen);

		} while (true);

	}

	private void updateTA() throws SQLException {

		String sql = "";
		ResultSet rs;
		String Id_TA;

		do {
			System.out
					.println("\nEnter the User ID of the student you want to upgrade to TA:");
			Id_TA = reader.nextLine();
			sql = "select * from users where userid = '" + Id_TA
					+ "' and USERLEVEL = 'Grad' ";
			rs = stmt.executeQuery(sql);
			if (!rs.next()) {
				System.out
						.println("User Id does not exist or user is not a Grad student! Try again? (y/n) : ");
				char ip = reader.nextLine().charAt(0);

				if (ip == 'n')
					return;
			} else
				break;

		} while (true);

		sql = "select course_id from courses where professor_id = '" + UserId
				+ "'";
		rs = stmt.executeQuery(sql);

		int crs;
		System.out
				.println("Enter the Course for which the TA is to be assigned :");

		HashMap<Integer, String> courses = new HashMap<>();
		String courseChosen = "";
		int i = 1;
		while (rs.next()) {
			String courseID = rs.getString("course_id");
			System.out.println("\n" + i + ". " + courseID);
			courses.put(i++, courseID);
		}

		System.out.println("\n" + i + ". Go to previous screen");

		do {
			try {
				crs = Integer.parseInt(reader.nextLine());
			} catch (Exception e) {
				continue;
			}
			if (crs == i)
				return;
			courseChosen = courses.get(crs);
			if (courseChosen != null)
				break;
			else
				System.out.println("Invalid choice! Enter again");
		} while (true);

		try {
			sql = "update users set accesslevel = '2' where userid = '" + Id_TA
					+ "'";
			stmt.executeQuery(sql);
		} catch (Exception e1) {
			// e1.printStackTrace();
		}

		try {
			sql = "INSERT INTO TA VALUES ('" + courseChosen + "', '" + Id_TA
					+ "')";
			stmt.executeQuery(sql);
		} catch (java.sql.SQLIntegrityConstraintViolationException e) {
			System.out.println("\n" + Id_TA + " is already a TA!!");
			return;
		}

		try {
			sql = "delete from enrollment e where e.student_id = '" + Id_TA
					+ "' and course_id = '" + courseChosen + "'";
			stmt.executeQuery(sql);
		} catch (Exception e) {
		}

		try {
			conn.commit();
		} catch (Exception e) {

		}

		System.out.println("\n" + Id_TA
				+ " was successfully made TA for course " + courseChosen);
	}

	private void notifHandler(String courseChoosen) throws SQLException {

		String sql = "select * from notification where course_id = '"
				+ courseChoosen + "' and user_id = '" + UserId
				+ "' and is_from_prof = 'N' and is_Read = 'N'";
		// System.out.println(sql);

		rs = stmt.executeQuery(sql);
		// System.out.println(rs.getRow());

		if (rs.next()) {
			System.out.println("\nYou have unread notification(s)");
		}

	}

	private void viewNotifs(String courseChoosen) throws SQLException {
		String sql = "";
		int i = 0;

		Statement stmt1 = conn.createStatement(
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		sql = "select notification_msg from notification where user_id = '"
				+ UserId + "' and course_id = '" + courseChoosen
				+ "' and is_Read = 'N'";
		ResultSet rs = stmt.executeQuery(sql);
		if (!rs.next())
			System.out.println("\nYou have no unread notification!");
		rs.beforeFirst();
		while (rs.next()) {
			String notificationMsg = rs.getString("notification_msg");
			System.out.println("\n" + ++i + ". " + notificationMsg);
			sql = "update notification set is_Read = 'Y' where user_id = '"
					+ UserId + "' and course_id = '" + courseChoosen + "'";
			stmt1.executeQuery(sql);
		}

		System.out.println("\n Press enter to go back");
		reader.nextLine();
		return;

	}

}
