package com.DB;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

import com.backup.PasswordHash;


public class Student extends Login {

	private Connection conn;
	private Statement stmt;
	private Scanner reader;
	private ResultSet rs;
	private Calendar currDtCal = Calendar.getInstance();
	private Date currentDate;

	public Student(Connection conn, Statement stmt, Scanner reader) {
		this.conn = conn;
		this.stmt = stmt;
		this.reader = reader;
		currDtCal.set(Calendar.HOUR_OF_DAY, 0);
		currDtCal.set(Calendar.MINUTE, 0);
		currDtCal.set(Calendar.SECOND, 0);
		currDtCal.set(Calendar.MILLISECOND, 0);
		currentDate = currDtCal.getTime();
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
					.println("\n 1. Select Course \n 2. Add Course \n 3. Drop course \n 4. Change Password \n 5. Logout");
			do {
				try {
					userOption = Integer.parseInt(reader.nextLine());
				} catch (Exception e) {
					continue; 
				}
				if (userOption >= 1 && userOption <= 5)
					break;
				else
					System.out.println("Please enter correct option!");
			} while (true);
			switch (userOption) {
			case 1:
				selectCourse();
				break;
			case 2:
				addCourse();
				break;
			case 3:
				dropCourse();
				break;
			case 4 : 
				changePassword();
				break;
			case 5:
				System.out.println("You have successfully logged out!");
				Utility.cleanupNotifs(stmt);
				return;
			}
		} while (true);

	}

	private void changePassword() {

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

	private void selectCourse() throws SQLException {

		do {
			int crs;
			System.out.println("Select course :");
			HashMap<Integer, String> courses = new HashMap<>();
			String curDate = Utility.GetFormatDate(currentDate);
			String courseChosen = "";
			int i = 1;

			String sql = "select e.course_id from enrollment e, courses c where e.student_id = '"
					+ UserId + "' and e.course_id = c.course_id and c.end_date > '"+curDate+"'";
			//System.out.println(sql);
			rs = stmt.executeQuery(sql);

			while (rs.next()) {
				String courseID = rs.getString("course_id");
				System.out.println("\n" + i + ". " + courseID);
				courses.put(i++, courseID);
			}

			sql = "select course_id from TA where TA_ID = '" + UserId + "'";
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

			sql = "SELECT * FROM TA WHERE ta_id ='" + UserId
					+ "' AND course_id ='" + courseChosen + "'";
			// System.out.println(sql);
			rs = stmt.executeQuery(sql);

			if (!rs.next())
				selectedCourseScreen(courseChosen);
			else {
				TA ta = new TA(conn, stmt, reader);
				ta.selectedCourseScreen(courseChosen);
			}

		} while (true);

	}

	private void dropCourse() throws SQLException {

		do {
			int crs;
			System.out.println("Select course :");
			HashMap<Integer, String> courses = new HashMap<>();
			String courseChosen = "";
			int i = 1;

			String sql = "select course_id from enrollment where student_id = '"
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

			System.out.println("\nAre you sure you want to drop "
					+ courseChosen + "? This cannot be undone! (y/n)");

			char ip = 0;
			try {
				ip = reader.nextLine().charAt(0);
			} catch (Exception e) {
				System.out.println("\nIncorrect option! Course not dropped...");
				return;
			}

			if (ip == 'n')
				return;

			try {
				sql = "delete from enrollment e where e.student_id = '"
						+ UserId + "' and course_id = '" + courseChosen + "'";
				stmt.executeQuery(sql);
			} catch (Exception e) {
				System.out
						.println("\nProblem dropping course! Try again later...");
				return;
			}

			System.out.println("\nCourse successfully dropped!");
			return;

		} while (true);

	}

	private void addCourse() throws SQLException {
		String courseToken = "";
		String course_id = "";
		String sql = "";

		do {
			System.out.println("\nEnter the course token : ");
			courseToken = reader.nextLine();
			sql = "select * from courses where token = '" + courseToken + "'";
			rs = stmt.executeQuery(sql);
			if (!rs.next()) {
				System.out
						.println("Course does not exist! Enter another course? (y/n) : ");
				char ip;
				try {
					ip = reader.nextLine().charAt(0);
				} catch (Exception e) {
					continue;
				}

				if (ip == 'n')
					return;
			} else {
				course_id = rs.getString("course_id");

				if (UserAccessLevel.equalsIgnoreCase("2")) {
					sql = "SELECT * FROM TA WHERE ta_id ='" + UserId
							+ "' AND course_id ='" + course_id + "'";
					// System.out.println(sql);
					rs = stmt.executeQuery(sql);
					if (rs.next()) {
						System.out
								.println("\nYou cannot enroll in this course! You are already a TA in it!");
						return;
					}
				}

				sql = "SELECT * FROM ENROLLMENT WHERE STUDENT_ID ='" + UserId
						+ "' AND course_id ='" + course_id + "'";
				// System.out.println(sql);
				rs = stmt.executeQuery(sql);
				if (rs.next()) {
					System.out
							.println("\nYou are already enrolled in this course!");
					return;
				}

				sql = "select * from courses where token = '" + courseToken
						+ "' and end_Date > CURRENT_DATE";
				rs = stmt.executeQuery(sql);
				if (!rs.next()) {
					System.out
							.println("Course due date over! Enter another course? (y/n) : ");

					char ip;
					try {
						ip = reader.nextLine().charAt(0);
					} catch (Exception e) {
						continue;
					}

					if (ip == 'n')
						return;
				} else {
					sql = "select * from courses where token = '" + courseToken
							+ "' and num_students < max_enrollment";
					rs = stmt.executeQuery(sql);
					if (!rs.next()) {
						System.out
								.println("Course full! Cannot register! Enter another course? (y/n) : ");

						char ip;
						try {
							ip = reader.nextLine().charAt(0);
						} catch (Exception e) {
							continue;
						}

						if (ip == 'n')
							return;
					} else {
						break;
					}
				}
			}

		} while (true);

		try {

			sql = "insert into enrollment values('" + UserId + "', '"
					+ course_id + "')";
			stmt.executeQuery(sql);

			try {
				conn.commit();
			} catch (Exception e) {

			}
			// Handled by trigger
			/*
			 * sql =
			 * "update courses set num_students = num_students + 1 where courses.course_id = '"
			 * + course_id + "' "; stmt.executeQuery(sql);
			 */

			notifTAHandler(course_id);

		} catch (Exception e) {
			System.out.println("\nError adding the course. Try again later");
			return;
		}

		System.out.println("\nCourse successfully added!");
		selectedCourseScreen(course_id);
	}

	private void notifTAHandler(String course_id) throws SQLException {

		Statement stmt1 = conn.createStatement(
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs1 = null;
		Statement stmt2 = conn.createStatement(
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs2;
		Statement stmt3 = conn.createStatement(
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

		String sql;

		sql = "select course_id from ta where ta_id = '" + UserId
				+ "' and course_id <> '" + course_id + "'";
		try {
			rs = stmt.executeQuery(sql);
		} catch (Exception e1) {
			// e1.printStackTrace();
		}

		while (rs.next()) {
			String courseIdTa = rs.getString("course_id");
			// System.out.println("course id is " + courseIdTa);

			sql = "select c1.CHAPTER_NAME, c2.CHAPTER_NAME, c1.CHAPTER_ID, c2.CHAPTER_ID from chapters c1, chapters c2, coursetextbook cb1, "
					+ "coursetextbook cb2 where c1.chapter_name = c2.chapter_name AND c1.ISBN = cb1.ISBN AND c2.ISBN = cb2.ISBN "
					+ "AND cb1.COURSE_ID = '"
					+ course_id
					+ "' AND cb2.COURSE_ID = '" + courseIdTa + "'";
			// System.out.println(sql);

			try {
				rs1 = stmt1.executeQuery(sql);
			} catch (Exception e1) {
				// System.out.println("Hellooooooooo checking testing!!!!!!!!------------------------------------------");
				e1.printStackTrace();
			}
			if (rs1.next()) {
				sql = "select professor_id, course_id from courses where course_id = '"
						+ course_id + "' or course_id = '" + courseIdTa + "'";
				// System.out.println(sql);
				rs2 = stmt2.executeQuery(sql);

				while (rs2.next()) {
					String curDate = Utility.GetFormatDate(currentDate);
					String profId = rs2.getString("professor_id");
					String courseIdProf = rs2.getString("course_id");
					sql = "insert into notification values('" + profId + "', '"
							+ courseIdProf + "', 'Student with id:" + UserId
							+ " has enrolled for course " + course_id
							+ " and is a ta for course " + courseIdTa
							+ " which has overlapping topics', 'N', '"
							+ curDate + "', 'N')";
					try {
						// System.out.println(sql);
						stmt3.executeQuery(sql);
					} catch (Exception e) {
						System.out.println("Insert not successful");
					}
				}
				try {
					conn.commit();
				} catch (Exception e) {

				}

			}

		}
	}

	private void selectedCourseScreen(String courseChoosen) throws SQLException {

		/*
		 * Date endDate = null; long diffInMillisec = 0; long diffInHours = 0;
		 * String exerciseId = ""; String courseId = ""; String isRead = "";
		 * ResultSet rs1;
		 */

		int userOption;

		do {

			notifHandler(courseChoosen);

			System.out.println("\nFor " + courseChoosen + " : ");
			System.out
					.println("\n 1. View Scores \n 2. Attempt Homework \n 3. View past submissions"
							+ "\n 4. View notifications \n 5. View Statistics \n 6. Go to previous menu");

			do {
				try {
					userOption = Integer.parseInt(reader.nextLine());
				} catch (NumberFormatException e) {
					continue;
				}
				if (userOption >= 1 && userOption <= 6)
					break;
				else
					System.out.println("Please enter correct option!");
			} while (true);
			switch (userOption) {
			case 1:
				viewScores(courseChoosen);
				break;
			case 2:
				attemptHomework(courseChoosen);
				break;
			case 3:
				viewPastSubmissions(courseChoosen);
				break;
			case 4:
				viewNotifs(courseChoosen);
				break;
			case 5:
				viewStatistics(courseChoosen);
				break;
			case 6:
				return;
			}

		} while (true);
	}

	/**
	 * @param courseChoosen
	 * @param calendar
	 * @param today
	 * @param curDate
	 * @param stmt1
	 * @param stmt2
	 * @param stmt3
	 * @throws SQLException
	 * @author Divya
	 */
	private void notifHandler(String courseChoosen) throws SQLException {

		Calendar calendar = Calendar.getInstance();
		Calendar today = Calendar.getInstance();
		today.setTime(new Date());

		Statement stmt1 = conn.createStatement(
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		Statement stmt2 = conn.createStatement(
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		Statement stmt3 = conn.createStatement(
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

		String curDate = Utility.GetFormatDate(currentDate);

		Date endDate;
		long diffInMillisec;
		long diffInHours;
		String exerciseId;
		String courseId;
		String isRead;
		ResultSet rs1;
		ResultSet rs2;
		// System.out.println("\nFor notifications trying.... : ");
		String sql = "select * from notification where course_id = '"
				+ courseChoosen + "' and user_id = '" + UserId
				+ "' and is_from_prof = 'N' and date_gen = '" + curDate + "'";
		 //System.out.println(sql);

		rs = stmt.executeQuery(sql);
		// System.out.println(rs.getRow());

		if (!rs.next()) {
			sql = "select * from assessment where course_id = '"
					+ courseChoosen + "'";
			 //System.out.println(sql);
			rs1 = stmt1.executeQuery(sql);

			while (rs1.next()) {
				endDate = rs1.getDate("end_date");
				//System.out.println(endDate);
				// System.out.println("\n\nEnd date is  " + endDate + "\n");
				calendar.setTime(endDate);
				// System.out.println(calendar.getTime());

				// System.out.println("Today date is " + today.getTime());

				diffInMillisec = calendar.getTimeInMillis()
						- today.getTimeInMillis();
				diffInHours = diffInMillisec / (60 * 60 * 1000);
				 //System.out.println("\n\nDiference in hours is  " +
				 //diffInHours+ "\n");

				if (diffInHours <= 24 && diffInHours >= 0) {
					exerciseId = rs1.getString("exercise_id");
					courseId = rs1.getString("course_Id");
					sql = "select * from attempt where exercise_id = '"
							+ exerciseId + "' and userid = '" + UserId + "'";
					 //System.out.println(sql);
					rs2 = stmt2.executeQuery(sql);
					if (!rs2.next()) {
						sql = "insert into notification values('" + UserId
								+ "', '" + courseId
								+ "', 'you have due homework for exercise id "
								+ exerciseId + "', 'N', '" + curDate
								+ "', 'N')";
						try {
							// System.out.println(sql);
							stmt3.executeQuery(sql);
						} catch (Exception e) {
							System.out.println("Insert not successful");
						}
						try {
							conn.commit();
						} catch (Exception e) {

						}
					}
				}
			}
		}

		sql = "select * from notification where course_id = '" + courseChoosen
				+ "' and user_id = '" + UserId + "'";
		rs = stmt.executeQuery(sql);

		// rs.beforeFirst();
		while (rs.next()) {
			isRead = rs.getString("is_Read");
			if (isRead.equalsIgnoreCase("N")) {
				System.out.println("\nYou have unread notification(s)");
				break;
			}
		}
	}

	private void viewPastSubmissions(String course_id) {
		// String exerID="HW1";
		// String userID="jmick";
		// String couID="CSC540";
		// String exerID=exerID1;
		String userID = UserId;
		String couID = course_id;
		String[] exer = new String[20];
		String[] exer1 = new String[20];
		int exe = 0, exe1 = 0; // ex = 0;
		// String qlist = null;
		String exermain = null;
		String exermain1 = null;
		// Connection conn = null;
		Statement stmt = null;
		Statement stmt1 = null;
		try {
			// STEP 2: Register JDBC driver
			// Class.forName("oracle.jdbc.driver.OracleDriver");

			// STEP 3: Open a connection
			// System.out.println("Connecting to database...");
			// conn = DriverManager.getConnection(DB_URL, USER, PASS);

			// STEP 4: Execute a query
			// System.out.println("Creating statement...");
			stmt = conn.createStatement();
			stmt1 = conn.createStatement();
			String sql;

			sql = "SELECT * FROM ASSESSMENT WHERE COURSE_ID='" + couID + "'";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				Date d1 = rs.getDate("START_DATE");
				String d3 = new SimpleDateFormat("dd-MM-yy").format(d1);
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy");
				Date date3 = sdf.parse(d3);
				Date d4 = rs.getDate("END_DATE");
				String d5 = new SimpleDateFormat("dd-MM-yy").format(d4);
				sdf = new SimpleDateFormat("dd-MM-yy");
				Date date4 = sdf.parse(d5);
				// Date date1 = sdf.parse(d1);
				// - System.out.println(d3.toString());
				String d2 = new SimpleDateFormat("dd-MM-yy").format(new Date());
				// - System.out.println(d2.toString());
				Date date2 = sdf.parse(d2);

				if (((date2.after(date3)) && (date2.before(date4)))
						|| date2.equals(date3) || date2.equals(date4))

					exer[exe++] = rs.getString("EXERCISE_ID");
			}

			sql = "SELECT * FROM ASSESSMENT WHERE COURSE_ID='" + couID + "'";
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				Date d1 = rs.getDate("START_DATE");
				String d3 = new SimpleDateFormat("dd-MM-yy").format(d1);
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy");
				Date date3 = sdf.parse(d3);
				Date d4 = rs.getDate("END_DATE");
				String d5 = new SimpleDateFormat("dd-MM-yy").format(d4);
				sdf = new SimpleDateFormat("dd-MM-yy");
				Date date4 = sdf.parse(d5);
				// Date date1 = sdf.parse(d1);
				// - System.out.println(d3.toString());
				String d2 = new SimpleDateFormat("dd-MM-yy").format(new Date());
				// - System.out.println(d2.toString());
				Date date2 = sdf.parse(d2);

				if (!(((date2.after(date3)) && (date2.before(date4)))
						|| date2.equals(date3) || date2.equals(date4)))

					exer1[exe1++] = rs.getString("EXERCISE_ID");
			}

			int i = 0, dt = 0, dt1 = 0;
			;
			String[][] dt123 = new String[500][500];
			System.out.println("Open Homework:");
			while (i < exe) {
				String sql7 = "SELECT * FROM ATTEMPT WHERE USERID='" + userID
						+ "'" + "AND COURSE_ID='" + couID
						+ "' AND EXERCISE_ID='" + exer[i] + "'";
				rs = stmt.executeQuery(sql7);
				while (rs.next()) {
					System.out.print((dt + 1) + ": ");
					System.out.print(exer[i] + ", Attemp No. ");
					System.out.println(rs.getString("ATTEMPT_NO"));
					dt123[dt][0] = exer[i];
					dt123[dt][1] = rs.getString("ATTEMPT_NO");
					dt++;
				}
				i++;
			}
			dt1 = dt;
			System.out.println("Closed Homework:");
			int j = 0;
			while (i < exe + exe1) {
				String sql7 = "SELECT * FROM ATTEMPT WHERE USERID='" + userID
						+ "'" + "AND COURSE_ID='" + couID
						+ "' AND EXERCISE_ID='" + exer1[j++] + "'";
				rs = stmt.executeQuery(sql7);
				while (rs.next()) {
					System.out.print((dt + 1) + ": ");
					System.out.print(exer1[j - 1] + ", Attemp No. ");
					System.out.println(rs.getString("ATTEMPT_NO"));
					dt123[dt][0] = exer1[j - 1];
					dt123[dt][1] = rs.getString("ATTEMPT_NO");
					dt++;
				}
				i++;
			}
			int option;
			System.out.println((dt + 1) + " : Go Back");
			while (true) {

				System.out.println("Enter your option:");
				try {
					option = Integer.parseInt(reader.nextLine());
				} catch (Exception e) {
					continue;
				}
				if (option == (dt + 1))
					return;
				if (option < 1 || option > dt + 1)
					System.out.println("Incorrect option");
				else {
					exermain = dt123[option - 1][0];
					exermain1 = dt123[option - 1][1];
					break;
				}

			}
			/*
			 * sql= "SELECT * FROM ASSESSMENT where EXERCISE_ID='"+exermain+"'";
			 * rs = stmt.executeQuery(sql); while(rs.next()){
			 * qlist=rs.getString("QUESTIONS_LIST"); } int k=0; String
			 * where1=null, where2="WHERE "; String where="QUESTION_ID=";
			 * 
			 * String[] parts = qlist.split(","); while(k<parts.length){ where1
			 * = where + "'" +parts[k]+"' "; if(k != 0) where2 = where2 + "OR "
			 * + where1; else where2 = where2 + where1; k++; }
			 */
			// ResultSet rs ;
			String aString = null;
			String sql7 = "SELECT * FROM ATTEMPT WHERE USERID='" + userID + "'"
					+ "AND COURSE_ID='" + couID + "' AND ATTEMPT_NO="
					+ exermain1 + " AND EXERCISE_ID='" + exermain + "'";
			rs = stmt.executeQuery(sql7);
			while (rs.next()) {
				aString = rs.getString("ATTEMPT_STRING");
				break;
			}

			String[] parts = aString.split(";");
			String[] part = null;
			i = 0;
			while (i < parts.length) {
				// - System.out.println(parts[i]) ;
				part = parts[i].split(",");
				j = 0;
				while (j < part.length) {
					if (j == 0) {
						System.out.print("Question " + (i + 1) + ":");
						sql = "SELECT  * FROM QUESTIONS WHERE QUESTION_ID="
								+ part[j];
						rs = stmt.executeQuery(sql);
						while (rs.next()) {
							if (Integer.parseInt(part[7]) == 0)
								System.out.println(rs.getString("TEXT"));
							if (Integer.parseInt(part[7]) != 0) {
								String sval;
								sql = "SELECT * FROM QUESTIONS_PARAM WHERE QUESTION_ID="
										+ part[j];
								ResultSet rs1 = stmt1.executeQuery(sql);

								if (Integer.parseInt(part[7]) == 1) {
									sval = "VALUE1";
									// qlevel[index-1][1]=1;
								} else {
									sval = "VALUE2";
									// qlevel[index-1][1]=2;
								}

								String paramq = null;
								while (rs1.next()) {
									String[] par = rs1.getString(sval).split(
											",");

									int n = 0;

									while (n < par.length) {
										if (n == 0)
											paramq = rs.getString("TEXT")
													.replaceFirst(
															"\\<[^>]*\\>",
															par[n]);
										else
											paramq = paramq.replaceFirst(
													"\\<[^>]*\\>", par[n]);
										// question[index-1] =
										// rs.getString("TEXT");
										// System.out.println(
										// question[index-1].replaceFirst("\\<[^>]*\\>",
										// par[i]));
										n++;
									}
								}
								// question[index-1] = paramq;
								System.out.println(paramq);
							}
						}
					}

					else {
						if (j < 5) {
							sql = "SELECT  * FROM ANSWERS WHERE ANSWER_ID="
									+ part[j];
							rs = stmt.executeQuery(sql);
							System.out.print("Option " + j + ":");
							while (rs.next()) {
								System.out.println(rs.getString("ANSWER_TEXT"));
							}
						}

						if (j == 5) {
							sql = "SELECT  * FROM ANSWERS WHERE ANSWER_ID="
									+ part[j];
							rs = stmt.executeQuery(sql);
							System.out.print("Submitted Answer : ");
							while (rs.next()) {
								System.out.println(rs.getString("ANSWER_TEXT"));
							}
							// - System.out.println(part[j]);
							// - System.out.println(part[j+1]);
							if (part[j].equals(part[j + 1]))
								System.out
										.println("You answered the question correctly");
							else {
								System.out
										.println("You answered the question incorrectly");
								System.out.print("Explanation : ");
								sql = "SELECT  * FROM ANSWERS WHERE ANSWER_ID="
										+ part[j];
								rs = stmt.executeQuery(sql);
								while (rs.next()) {
									System.out.println(rs
											.getString("EXPLANATION"));
								}

							}
						}

					}

					// - System.out.println();

					j++;
				}

				if (option > dt1)
					System.out.print("Question Explanation : ");
				else
					System.out.print("Question Hint : ");
				sql = "SELECT  * FROM QUESTIONS WHERE QUESTION_ID=" + part[0];
				rs = stmt.executeQuery(sql);
				while (rs.next()) {
					if (option > dt1)
						System.out.println(rs.getString("EXPLANATION"));
					else
						System.out.println(rs.getString("HINT"));
				}
				i++;
				System.out.println();

			}
			// STEP 6: Clean-up environment
			/*
			 * rs.close(); stmt.close(); conn.close();
			 */
			while (true) {
				/*
				 * System.out.println("1. Back");
				 * System.out.println("Enter your option:"); option =
				 * Integer.parseInt(reader.nextLine()); if (option != 1)
				 * System.out.println("Incorrect option"); else { return; }
				 */

				System.out.println("\n Press enter to go back");
				reader.nextLine();
				return;

			}
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {/*
					 * // finally block used to close resources try { if (stmt
					 * != null) stmt.close(); } catch (SQLException se2) { }//
					 * nothing we can do try { if (conn != null) conn.close(); }
					 * catch (SQLException se) { se.printStackTrace(); }// end
					 * finally try
					 */
		}// end try
	}

	private void viewScores(String courseId) {
		// String exerID="HW1";
		// String userID="jmick";
		// String couID="CSC540";
		// String exerID=exerID1;
		String userID = UserId;
		String couID = courseId;
		// Connection conn = null;
		Statement stmt = null;
		try {
			// STEP 2: Register JDBC driver
			/*
			 * Class.forName("oracle.jdbc.driver.OracleDriver");
			 * 
			 * // STEP 3: Open a connection
			 * System.out.println("Connecting to database..."); conn =
			 * DriverManager.getConnection(DB_URL, USER, PASS);
			 * 
			 * // STEP 4: Execute a query
			 * System.out.println("Creating statement...");
			 */
			stmt = conn.createStatement();
			// String sql;

			String sql6 = "SELECT * FROM ATTEMPT WHERE USERID='" + userID + "'"
					+ "AND COURSE_ID='" + couID
					+ "' ORDER BY EXERCISE_ID, ATTEMPT_NO";
			ResultSet rs = stmt.executeQuery(sql6);
			while (rs.next()) {
				System.out.println("");
				System.out.print(rs.getString("EXERCISE_ID"));
				System.out.print("   Attempt ");
				System.out.print(rs.getString("ATTEMPT_NO"));
				System.out.print("   |   ");
				System.out.print(rs.getInt("MARKS"));
				System.out.print("/");
				System.out.println(rs.getInt("HIGHEST"));

			}
			// STEP 6: Clean-up environment
			/*
			 * rs.close(); stmt.close(); conn.close();
			 */

			while (true) {
				/*
				 * System.out.println("1. Back");
				 * System.out.println("Enter your option:"); int option =
				 * Integer.parseInt(reader.nextLine()); if (option != 1)
				 * System.out.println("Incorrect option"); else { return; }
				 */

				System.out.println("\n Press enter to go back");
				reader.nextLine();
				return;

			}
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {/*
					 * // finally block used to close resources try { if (stmt
					 * != null) stmt.close(); } catch (SQLException se2) { }//
					 * nothing we can do try { if (conn != null) conn.close(); }
					 * catch (SQLException se) { se.printStackTrace(); }// end
					 * finally try
					 */
		}// end try

	}

	private void attemptHomework(String courseId) {
		// String exerID="HW1";
		// String userID="jmick";
		// String couID="CSC540";

		// String exerID = "";
		String userID = UserId;
		String couID = courseId;

		int val = 0;
		int[][] qlevel = new int[20][2];

		String[] exer = new String[10];
		int exe = 0;
		String qlist = null;
		String exermain = null;
		int[][] assesQ = new int[20][8];
		int asses = 0;
		int[] array, array1 = new int[10];
		int[][] assesment = new int[10][2];
		int index = 0, index1 = 0; // m = 0;
		String[] question = new String[20];
		int[] qid = new int[10];
		String[][] answer = new String[20][20];
		int[][] aid = new int[20][20];
		// Connection conn = null;
		Statement stmt = null;
		Statement stmt1 = null;
		Statement stmt2 = null;
		try {

			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			stmt1 = conn.createStatement();
			String sql;
			// sql = "SELECT * FROM ASSESMENT";
			// ResultSet rs = stmt.executeQuery(sql);
			// int pc=rs.getInt("POINTS_CORRECT");
			// int pw=rs.getInt("POINTS_WRONG");
			// int ret=rs.getInt("RETRIES");

			sql = "SELECT * FROM ASSESSMENT WHERE COURSE_ID='" + couID + "'";
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				Date d1 = rs.getDate("START_DATE");
				String d3 = new SimpleDateFormat("dd-MM-yy").format(d1);
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy");
				Date date3 = sdf.parse(d3);
				Date d4 = rs.getDate("END_DATE");
				String d5 = new SimpleDateFormat("dd-MM-yy").format(d4);
				sdf = new SimpleDateFormat("dd-MM-yy");
				Date date4 = sdf.parse(d5);
				// Date date1 = sdf.parse(d1);
				// - System.out.println(d3.toString());
				String d2 = new SimpleDateFormat("dd-MM-yy").format(new Date());
				// - System.out.println(d2.toString());
				Date date2 = sdf.parse(d2);

				if (((date2.after(date3)) && (date2.before(date4)))
						|| date2.equals(date3) || date2.equals(date4))

					exer[exe++] = rs.getString("EXERCISE_ID");
			}
			int i = 0;
			System.out.println("Open Homework:");
			while (i < exe) {
				System.out.print((i + 1) + ": ");
				System.out.println(exer[i]);
				i++;
			}
			System.out.println((i + 1) + " : Go Back");
			while (true) {
				System.out.println("Enter your option:");
				int option;
				try {
					option = Integer.parseInt(reader.nextLine());
				} catch (Exception e) {
					continue;
				}
				if(option == (i+1))
					return;
				if (option < 1 || option > (i+1))
					System.out.println("Incorrect option");
				else {
					exermain = exer[option - 1];
					break;
				}

			}
			sql = "SELECT * FROM ATTEMPT where EXERCISE_ID='" + exermain + "' AND COURSE_ID='" + couID + "'" + "AND USERID='" + userID + "'"  ;
			rs = stmt.executeQuery(sql);
			rs.last();
			int size123 = rs.getRow();
			rs.beforeFirst();
		
			sql = "SELECT * FROM ASSESSMENT where EXERCISE_ID='" + exermain
					+ "'";
			rs = stmt.executeQuery(sql);
			int ret=0;
			while (rs.next()) {
				qlist = rs.getString("QUESTIONS_LIST");
			    ret=rs.getInt("RETRIES");
			}
			//System.out.println(ret + "," + size123);
            if(size123>=ret && (ret != 0) ){
            	System.out.println("Attempts Exhausted");
            	while (true) {
    				/*
    				 * System.out.println("1. Back");
    				 * System.out.println("Enter your option:"); int option =
    				 * Integer.parseInt(reader.nextLine()); if (option != 1)
    				 * System.out.println("Incorrect option"); else { return; }
    				 */

    				System.out.println("\n Press enter to go back");
    				reader.nextLine();
    				return;
    			}
            	
            }
			int k = 0;
			String where1 = null, where2 = "WHERE ";
			String where = "QUESTION_ID=";

			String[] parts = qlist.split(",");
			while (k < parts.length) {
				where1 = where + "'" + parts[k] + "' ";
				if (k != 0)
					where2 = where2 + "OR " + where1;
				else
					where2 = where2 + where1;
				k++;
			}
			// - System.out.println(where2);
			sql = "SELECT * FROM QUESTIONS " + where2;
			rs = stmt.executeQuery(sql);

			rs.last();
			int size = rs.getRow();
			rs.beforeFirst();
			// System.out.print(arra[1]);
			// STEP 5: Extract data from result set
			if (size > 0)
				array = RandomizeArray(0, size - 1);

			while (rs.next()) {
				// Retrieve by column name

				// m++;
				index1 = 0;
				qid[index] = rs.getInt("QUESTION_ID");
				String param;

				param = rs.getString("PARAMETERIZED");
				if (param.equals("Y")) {
					val = 2;
				}

				question[index++] = rs.getString("TEXT");
				qlevel[index - 1][0] = qid[index - 1];
				qlevel[index - 1][1] = 0;
				if (val != 0) {
					String sval;
					sql = "SELECT * FROM QUESTIONS_PARAM WHERE QUESTION_ID="
							+ qid[index - 1];
					ResultSet rs1 = stmt1.executeQuery(sql);
					if (val == 1) {
						sval = "VALUE1";
						qlevel[index - 1][1] = 1;
					} else {
						sval = "VALUE2";
						qlevel[index - 1][1] = 2;
					}

					String paramq = null;
					while (rs1.next()) {
						String[] par = rs1.getString(sval).split(",");

						i = 0;

						while (i < par.length) {
							if (i == 0)
								paramq = question[index - 1].replaceFirst(
										"\\<[^>]*\\>", par[i]);
							else
								paramq = paramq.replaceFirst("\\<[^>]*\\>",
										par[i]);
							// question[index-1] = rs.getString("TEXT");
							// System.out.println(
							// question[index-1].replaceFirst("\\<[^>]*\\>",
							// par[i]));
							i++;
						}
					}
					question[index - 1] = paramq;
				}
				/*
				 * String first = rs.getString("EXPLANATION"); int last =
				 * rs.getInt("CHAPTER_ID");
				 */
				if (index1 == 0)
					sql = "SELECT * FROM ANSWERS WHERE QUESTION_ID="
							+ qid[index - 1]
							+ "AND IS_CORRECT='Y' AND ROWNUM<=1 AND VALUE="
							+ val;
				else
					sql = "SELECT  * FROM ANSWERS WHERE QUESTION_ID="
							+ qid[index - 1]
							+ "AND IS_CORRECT='N' AND ROWNUM<=3 AND VALUE="
							+ val;
				ResultSet rs1 = stmt1.executeQuery(sql);
				// Display values
				// - System.out.print("QID: " + qid[index-1]);
				// - System.out.print(", Text: " + question[index-1]);
				// - System.out.print(", EXP: " + first);
				// - System.out.println(", Lev: " + last);
				i = 1;
				// - System.out.println("Options :");
				while (rs1.next()) {
					// - System.out.print(i++);
					// - System.out.print(" :");
					answer[index - 1][index1++] = rs1.getString("ANSWER_TEXT");
					aid[index - 1][index1 - 1] = rs1.getInt("ANSWER_ID");
					// - System.out.print("Answers: " +
					// answer[index-1][index1-1]);
					// - System.out.println("");
					if (index1 - 1 == 0) {
						sql = "SELECT  * FROM ANSWERS WHERE QUESTION_ID="
								+ qid[index - 1]
								+ "AND IS_CORRECT='N' AND ROWNUM<=3 AND VALUE="
								+ val;
						rs1 = stmt1.executeQuery(sql);
					}

				}

			}

			array = RandomizeArray(0, index - 1);

			i = 0;
			int j = 0;
			int option1;
			while (i < index) {
				System.out.println("Question " + (i + 1) + ": "
						+ question[array[i]]);
				array1 = RandomizeArray(0, 3);
				while (j < 4) {
					System.out.println("Option " + (j + 1) + ":"
							+ answer[array[i]][array1[j]]);
					assesQ[asses][j + 1] = aid[array[i]][array1[j]];
					j++;
				}
				assesment[i][0] = qid[array[i]];
				// - System.out.println("QID: "+ assesment[i][0] );
				assesQ[asses][0] = qid[array[i]];

				while (true) {
					System.out.println("Enter your answer number:");
					try {
						option1 = Integer.parseInt(reader.nextLine());
					} catch (Exception e) {
						continue;
					}
					if (option1 < 1 || option1 > 4)
						System.out.println("Incorrect number");
					else {
						// exermain=exer[option-1];
						break;
					}

				}
				// - System.out.println("Enter your answer number:");
				// - option1=in.nextInt();
				assesment[i][1] = aid[array[i]][array1[option1 - 1]];
				assesQ[asses][5] = assesment[i][1];
				// - System.out.println("AID: "+ assesment[i][1] );
				i++;
				j = 0;
				asses++;
			}
			i = 0;
			j = 0;
			// - System.out.println(asses);

			i = 0;
			stmt2 = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			int kml = 0;
			i = 0;
			val = 0;
			while (i < asses) {

				k = 0;
				while (k < asses) {
					if (qlevel[k][0] == assesQ[i][0]) {
						assesQ[i][7] = 0;
						if (qlevel[k][1] == 1)
							assesQ[i][7] = 1;
						else if (qlevel[k][1] == 2)
							assesQ[i][7] = 2;
					}
					k++;
				}
				/*
				 * String sql4 = "SELECT * FROM ASSESSMENT WHERE EXERCISE_ID=" +
				 * exerID;
				 */
				String sql3 = "SELECT * FROM ANSWERS WHERE QUESTION_ID='"
						+ assesQ[i][0]
						+ "' AND IS_CORRECT='Y' AND ROWNUM<=1 AND VALUE="
						+ assesQ[i][7];
				ResultSet rs3 = stmt2.executeQuery(sql3);
				// rs3.last();
				// System.out.println(rs3.getRow());
				rs3.beforeFirst();
				while (rs3.next()) {
					kml = rs3.getInt("ANSWER_ID");
					assesQ[i][6] = kml;

				}
				i++;
			}
			i = 0;
			j = 0;
			int correct = 0, co = 0, wrong = 0, wo, marks, hmarks;
			String assStr = null;
			String assSt = null;
			while (i < asses) {
				while (j < 8) {
					assSt = Integer.toString(assesQ[i][j]);
					if (assStr != null)
						assStr = assStr + assSt;
					else
						assStr = assSt;
					if (j != 7)
						assStr = assStr + ",";
					j++;
				}
				assStr = assStr + ";";
				j = 0;
				if (assesQ[i][5] == assesQ[i][6])
					correct++;
				else
					wrong++;

				i++;
			}
			// - System.out.println(assStr);
			// - System.out.println(correct);
			// - System.out.println(wrong);
			String sql5 = "SELECT * FROM ASSESSMENT WHERE EXERCISE_ID='"
					+ exermain + "'";
			ResultSet rs3 = stmt2.executeQuery(sql5);
			while (rs3.next()) {
				co = rs3.getInt("POINTS_CORRECT");
				correct = correct * co;
				wo = rs3.getInt("POINTS_WRONG");
				wrong = wrong * wo;
			}
			marks = correct + wrong;
			if (marks < 0)
				marks = 0;
			hmarks = (asses) * co;

			// - System.out.println(marks);
			// - System.out.println(hmarks);
			sql5 = "SELECT * FROM ATTEMPT WHERE EXERCISE_ID='" + exermain
					+ "' AND USERID='" + userID + "'";
			rs3 = stmt2.executeQuery(sql5);
			rs3.last();
			int att = rs3.getRow();
			att++;
			sql5 = "INSERT INTO ATTEMPT VALUES ( '" + exermain + "' , '"
					+ userID + "', '" + assStr + "', " + marks + ", " + hmarks
					+ ", " + att + ", '" + couID + "' )";
			stmt2.executeUpdate(sql5);
			try {
				conn.commit();
			} catch (Exception e) {

			}

			/*
			 * rs.close(); stmt.close(); conn.close();
			 */
			while (true) {
				/*
				 * System.out.println("1. Back");
				 * System.out.println("Enter your option:"); int option =
				 * Integer.parseInt(reader.nextLine()); if (option != 1)
				 * System.out.println("Incorrect option"); else { return; }
				 */

				System.out.println("\n Press enter to go back");
				reader.nextLine();
				return;
			}

			// STEP 6: Clean-up environment
			// - rs.close();
			// - stmt.close();
			// - conn.close();
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {/*
					 * // finally block used to close resources try { if (stmt
					 * != null) stmt.close(); } catch (SQLException se2) { }//
					 * nothing we can do try { if (conn != null) conn.close(); }
					 * catch (SQLException se) { se.printStackTrace(); }// end
					 * finally try
					 */
		}// end try
		System.out.println("Goodbye!");
		return;
	}

	public static int[] RandomizeArray(int a, int b) {
		Random rgen = new Random(); // Random number generator
		int size = b - a + 1;
		int[] array = new int[size];

		for (int i = 0; i < size; i++) {
			array[i] = a + i;
		}

		for (int i = 0; i < array.length; i++) {
			int randomPosition = rgen.nextInt(array.length);
			int temp = array[i];
			array[i] = array[randomPosition];
			array[randomPosition] = temp;
		}

		// for(int s: array)
		// System.out.println(s);

		return array;
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

	private void viewStatistics(String courseChoosen) throws SQLException {

		String sql;
		String sql1;
		String sql2;
		String[] exer = new String[20];
		int exe = 0;// ex = 0;
		String couID = courseChoosen;
		Statement stmt1 = conn.createStatement(
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs1 = null;
		Statement stmt2 = conn.createStatement(
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs2 = null;
		Statement stmt3 = conn.createStatement(
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		ResultSet rs3 = null;
		sql = "SELECT * FROM ASSESSMENT WHERE COURSE_ID='" + couID + "'";
		rs = stmt.executeQuery(sql);
		while (rs.next()) {
			try {
				Date d1 = rs.getDate("START_DATE");
				String d3 = new SimpleDateFormat("dd-MM-yy").format(d1);
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yy");
				Date date3 = sdf.parse(d3);
				Date d4 = rs.getDate("END_DATE");
				String d5 = new SimpleDateFormat("dd-MM-yy").format(d4);
				sdf = new SimpleDateFormat("dd-MM-yy");
				Date date4 = sdf.parse(d5);
				// Date date1 = sdf.parse(d1);
				// - System.out.println(d3.toString());
				String d2 = new SimpleDateFormat("dd-MM-yy").format(new Date());
				// - System.out.println(d2.toString());
				Date date2 = sdf.parse(d2);

				if (!(((date2.after(date3)) && (date2.before(date4)))
						|| date2.equals(date3) || date2.equals(date4))) {
					String exId = rs.getString("EXERCISE_ID");
					// System.out.println("exercide is " + exId);
					exer[exe++] = exId;
				}
			} catch (Exception e) {
			}
		}

		int i = 0, dt = 0, dt1 = 0;
		String exermain = null;
		String exermain1 = null;
		String[] dt123 = new String[500];
		dt1 = dt;
		System.out.println("Closed Homework:");
		int j = 0;
		while (i < exe) {
			sql = "SELECT DISTINCT EXERCISE_ID FROM ATTEMPT WHERE USERID='"
					+ UserId + "'" + "AND COURSE_ID='" + couID
					+ "' AND EXERCISE_ID='" + exer[j++] + "'";
			// System.out.println(sql7);

			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				System.out.print((dt + 1) + ": ");
				System.out.print(exer[j - 1] + "\n");
				// System.out.print(exer[j - 1] + ", Attemp No. ");
				// System.out.println(rs.getString("ATTEMPT_NO"));
				dt123[dt] = exer[j - 1];
				// dt123[dt][1] = rs.getString("ATTEMPT_NO");
				dt++;
			}
			i++;
		}

		int option;
		System.out.println((dt + 1) + " : Go Back");
		while (true) {
			System.out.println("Enter your option:");
			try {
				option = Integer.parseInt(reader.nextLine());
			} catch (Exception e) {
				continue;
			}
			if( option == (dt+1))
				return;
			if (option < 1 || option > dt+1)
				System.out.println("Incorrect option");
			else {
				exermain = dt123[option - 1];
				// System.out.println("checking :::::: " + exermain);
				// exermain1 = dt123[option - 1];
				// System.out.println("checking :::::: " + exermain1);
				break;
			}

		}

		int classAverage = 0;
		int rank = 0;
		String userId = "";
		int userMarks = 0;
		int userAttempts = 0;
		int scoredMarks = 0;
		int maxMarksScored = 0;
		String userWithMaxMarks = "";
		sql = "select AVG(MARKS) AS Average from ATTEMPT where EXERCISE_ID = '"
				+ exermain + "' AND COURSE_ID = '" + couID + "'";
		rs1 = stmt1.executeQuery(sql);
		while (rs1.next())
			classAverage = rs1.getInt("Average");
		System.out.println("Class average for exercise : " + exermain + " is "
				+ classAverage + "\n");

		sql = "select COUNT(*) AS Count from attempt where attempt_no = 1 and EXERCISE_ID = '"
				+ exermain
				+ "' AND COURSE_ID = '"
				+ couID
				+ "'and marks > (select marks from attempt where userid = '"
				+ UserId
				+ "' and attempt_no = 1 and EXERCISE_ID = '"
				+ exermain + "' AND COURSE_ID = '" + couID + "')";
		rs1 = stmt1.executeQuery(sql);
		while (rs1.next())
			rank = rs1.getInt("Count") + 1;
		System.out.println("Your rank for first attempt for exercise id "
				+ exermain + " is : " + rank + "\n");

		sql = "SELECT DISTINCT USERID FROM ATTEMPT " + "WHERE EXERCISE_ID = '"
				+ exermain + "' AND COURSE_ID = '" + couID + "'";
		// System.out.println(sql);
		rs1 = stmt1.executeQuery(sql);
		while (rs1.next()) {
			userId = rs1.getString("USERID");

			sql1 = "select MARKS from ATTEMPT WHERE MARKS = (select MAX(MARKS) "
					+ "from ATTEMPT WHERE EXERCISE_ID = '"
					+ exermain
					+ "' AND COURSE_ID = '"
					+ couID
					+ "' AND USERID = '"
					+ userId + "')";
			rs2 = stmt2.executeQuery(sql1);

			sql2 = "select ATTEMPT_NO from ATTEMPT WHERE ATTEMPT_NO = (select MAX(ATTEMPT_NO) "
					+ "from ATTEMPT WHERE EXERCISE_ID = '"
					+ exermain
					+ "' AND COURSE_ID = '"
					+ couID
					+ "' AND USERID = '"
					+ userId + "')";
			rs3 = stmt3.executeQuery(sql2);

			if (rs2.next() && rs3.next()) {
				userMarks = rs2.getInt("MARKS");
				userAttempts = rs3.getInt("ATTEMPT_NO");

				scoredMarks = userMarks / userAttempts;
				if (scoredMarks > maxMarksScored) {
					maxMarksScored = scoredMarks;
					userWithMaxMarks = userId;
				}

			}

		}

		sql = "select name from users where userid = '" + userWithMaxMarks
				+ "'";
		rs = stmt.executeQuery(sql);
		while (rs.next())
			System.out.println("Student who topped in exercise " + exermain
					+ " in class is : " + rs.getString("name"));
		while (true) {
			/*
			 * System.out.println("1. Back");
			 * System.out.println("Enter your option:"); option =
			 * Integer.parseInt(reader.nextLine()); if (option != 1)
			 * System.out.println("Incorrect option"); else { return; }
			 */

			System.out.println("\n Press enter to go back");
			reader.nextLine();
			return;
		}

	}

}
