package ru.guap.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import ru.guap.dao.DBManager;

/**
 * Менеджер для работы с СУБД
 * @author Cr0s
 *
 */
public class DBManager {
	/**
	 * Название схемы (базы данных) в СУБД
	 */
	public static final String DB_NAME = "kafedra";
	
	/**
	 * JDBC-адрес для подключения
	 */
	public static final String URL = "jdbc:mysql://localhost:3306/" + DB_NAME;

	/**
	 * Имя главной таблицы с нагрузкой
	 */
	public static final String MAIN_TABLE_NAME = "kaf43";
	
	/**
	 * Имя таблицы с журналом версий нагрузки
	 */
	public static final String LOADS_TABLE_NAME = "load";

	/**
	 * Логин к БД
	 */
	public static final String DB_LOGIN = "kafedra";
	
	/**
	 * Пароль к БД
	 */
	public static final String DB_PASS = "123456";

	/**
	 * Ссылка на экземпляр класса (паттерн "Одиночка"
	 */
	private static DBManager instance = null;
	private PreparedStatement psTeacher;
	
	private DBManager() {

	}

	public static DBManager getInstance() {
		if (instance == null) {
			instance = new DBManager();
		}

		return instance;
	}

	public boolean executeSql(String sql) {
		Connection con = null; 
		PreparedStatement ps = null;
		
		try {
			con = getConnection();
			ps = con.prepareStatement(sql);

			ps.executeUpdate();

		} catch (Exception ex) {
			Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null,
					ex);
			return false;
		} finally {
			closeResource(con, null, ps);
		}

		return true;
	}

	public ResultSet getSqlResult(String sql) {
		ResultSet res = null;
		Connection con = null;
		PreparedStatement ps = null;

		try {
			con = getConnection();	
			ps = con.prepareStatement(sql);

			res = ps.executeQuery();

		} catch (Exception ex) {
			Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null,
					ex);
		} finally {
			closeResource(con, res, ps);
		}

		return res;
	}

	public int getLastInsertIdAfterSQL(String sql, String table) {
		int id = 0;

		Connection con = null;
		Statement ps = null;
		ResultSet res = null;
		
		try {
			con = getConnection();
			ps = con.createStatement();

			System.out.println(ps.toString());
			ps.execute(sql);
			ps.execute(String.format("SELECT LAST_INSERT_ID() FROM `%s`.`%s`;", DBManager.DB_NAME, table));

			res = ps.getResultSet();
			if (res != null && res.next()) {
				id = res.getInt(1);
			}

		} catch (Exception ex) {
			Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null,
					ex);
			id = 0;
		} finally {
			closeResource(con, res, ps);
		}

		return id;	
	}

	public Connection getConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			return DriverManager.getConnection(URL, DB_LOGIN, DB_PASS);
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public void closeResource(Connection connection, ResultSet rs, Statement stmt) {
	    try {
	        if (rs != null) {
	            rs.close();
	        }
	    } catch (Exception ex) {
	    } finally {
	        try {
	            if (stmt != null) {
	                stmt.close();
	            }
	        } catch (Exception ex) {
	        } finally {
	            try {
	                if (connection != null) {
	                    connection.close();
	                }
	            } catch (Exception ex) {
	            }
	        }
	    }
	}	
	
	public String getTeacherById(int teacherID) throws SQLException {
		Connection con = null;
		PreparedStatement psTeacher = null;
		ResultSet res = null;
		
		try {
			con = getConnection();
			psTeacher = con.prepareStatement("SELECT fio FROM kafedra.teachers WHERE id = ?;");		
			
			psTeacher.setInt(1, teacherID);
			res = psTeacher.executeQuery();
	
			while (res.next()) {
				String teacherName = res.getString(1);
				
				if (teacherName.trim().isEmpty()) {
					teacherName = "";
				}
				
				return teacherName;
			}    
			
		} finally {
			closeResource(con, res, psTeacher);
		}

		return "";
	}
}
