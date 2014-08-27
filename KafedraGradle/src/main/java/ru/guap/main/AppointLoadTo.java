package ru.guap.main;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import ru.guap.appointment.AppointmentsManager;
import ru.guap.dao.DBManager;

/**
 * Сервлет, принимающий запрос от фронт-энда на назначение нагрузки
 * Назначение пустого преподавателя снимает с него заданную нагрузку
 * @author user
 *
 */
@WebServlet(description = "Appoints specified load to specified teacher", urlPatterns = { "/AppointLoadTo" })
@MultipartConfig
public class AppointLoadTo extends HttpServlet {
	private static final long serialVersionUID = 2L;

	public AppointLoadTo() {
		super();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

	}

	/**
	 * Получаем POST-запрос
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Входные данные представлены в виде JSON
		String jsonData = request.getParameter("data");
		
		// Дополнительный параметр запроса - идентификатор преподавателя
		Integer teacherId = Integer.valueOf(request.getParameter("teacher_id"));
		
		// Отвечаем тоже в формате JSON
		response.setContentType("application/json; charset=UTF-8");
		PrintWriter out = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), "UTF8"), true);
		
		if (jsonData.isEmpty() || teacherId < 0) { // Сообщаем об ошибке
			out.println("{ \"success\": false, \"reason\": \"invalid json data\" }");
		} else {
			JSONParser parser = new JSONParser();
			
			// Массив идентификаторов назначенных элементов нагрузки
			JSONArray appointed = new JSONArray();
			
			try {
				JSONArray ja = (JSONArray) parser.parse(jsonData);
				
				for (Object object : ja) {
					JSONObject jo = (JSONObject) object;
					
					// Признак потокового элемента (несколько групп в одном)
					boolean isMulti = Boolean.valueOf((String) jo.get("isMulti"));
					
					if (isMulti) {
						// Назначаем целый поток
						int streamId = Integer.valueOf((String) jo.get("streamId"));
						int discId = Integer.valueOf((String) jo.get("discId"));
						int appointedId = AppointmentsManager.getInstance().appointTeacherToStream(streamId, discId, teacherId);
						
						if (appointedId != 0) {
							appointed.add(appointedId);
						}
					} else {
						// Назначаем одиночный элемент
						int itemId = Integer.valueOf((String) jo.get("id"));
						AppointmentsManager.getInstance().appointTeacherToItem(itemId, teacherId);
						
						appointed.add(itemId);
					}
				}				
			} catch (ParseException e) {
				out.println("{ \"success\": false, \"reason\": \"json data corrupted\" }");
				e.printStackTrace();
				
				return;
			} catch (SQLException e) {
				out.println("{ \"success\": false, \"reason\": \"SQL error\" }");
				e.printStackTrace();
			}
			
			// Завершаем формирование ответа и отсылаем
			JSONObject joo = new JSONObject();
			joo.put("success", "true");
			joo.put("appointed", appointed);
			
			try {
				joo.put("teacher", DBManager.getInstance().getTeacherById(teacherId));
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			out.println(joo.toJSONString());
		}		
	}
}
