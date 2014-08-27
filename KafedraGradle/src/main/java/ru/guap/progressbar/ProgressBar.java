package ru.guap.progressbar;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.guap.dao.DBManager;
import ru.guap.treeview.BurdenManager;

@WebServlet(description = "Get count of all records and appointed records", urlPatterns = { "/GetProgress" })
@MultipartConfig

/**
 * Сервлет, отправляющий фронт-энду данные для ползунка уровня заполненности
 * @author Cr0s
 *
 */
public class ProgressBar extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Connection cnn;
	private static PreparedStatement psAll,psApp;
	
	public ProgressBar(){
		super();
		cnn = DBManager.getInstance().getConnection();
	
		try {
			psAll = cnn.prepareStatement("SELECT COUNT(id) FROM kafedra.kaf43 WHERE load_id = ?;");
			psApp = cnn.prepareStatement("SELECT COUNT(teachers_id) FROM kafedra.kaf43 WHERE load_id=?;");
			
			psAll.setInt(1, BurdenManager.LOAD_VERSION);
			psApp.setInt(1, BurdenManager.LOAD_VERSION);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/plain; charset=UTF-8");
		response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Expires", "0");
		
		try{
			PrintWriter out = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF8"), true);
			int all=2;
			int app=2;
			ResultSet allRes = psAll.executeQuery();
			ResultSet appRes = psApp.executeQuery();
						
			while(allRes.next())
				 all=allRes.getInt(1);
			
			while(appRes.next())
				app=appRes.getInt(1);
			
			String output = String.format("{ \"error\": false,"
					+ " \"appointed\": %s,"
					+ " \"allRecords\": %s"
					+ "}",app ,all );
			
			out.println(output);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		}
}
	
	

