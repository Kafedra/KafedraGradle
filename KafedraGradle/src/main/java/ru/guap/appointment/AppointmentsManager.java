package ru.guap.appointment;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;

import ru.guap.dao.DBManager;
import ru.guap.treeview.BurdenManager;
import ru.guap.treeview.GroupLoadItem;
import ru.guap.treeview.GroupStream;

/**
 * Класс управления средствами назначения нагрузки
 * @author Cr0s
 *
 */
public class AppointmentsManager {
	/**
	 * Соединение с БД и подготовленое SQL-выражение для назначения преподавателя
	 */
	private Connection cnn;
	private PreparedStatement psAppointTeacher;
	
	/**
	 * Экземпляр класса (паттерн "Одиночка")
	 */
	private static AppointmentsManager instance;
	
	/**
	 * Закрытый конструктор класса, выполняющий инициализацию соединения с БД и формирующий SQL-выражение
	 * Паттерн "Одиночка"
	 */
	private AppointmentsManager() {
		cnn = DBManager.getInstance().getConnection();

		try {
			psAppointTeacher = cnn.prepareStatement("UPDATE kafedra.kaf43 SET teachers_id = ? WHERE id = ? AND load_id = " + BurdenManager.LOAD_VERSION);
		} catch (SQLException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Запрашивает экземпляр класса (паттерн "Одиночка")
	 * @return экземпляр класса
	 */
	public static AppointmentsManager getInstance() {
		if (instance == null) {
			instance = new AppointmentsManager();
		} 
		
		return instance;
	}
	
	/**
	 * Назначает поток преподавателю (или преподавателя потоку)
	 * @param streamId идентификатор потока
	 * @param discId идентификатор дисциплины
	 * @param teacherId идентификатор преподавателя
	 * @return идентификатор узла (хеш) для потока и дисциплины
	 * @throws SQLException
	 */
	public int appointTeacherToStream(int streamId, int discId, int teacherId) throws SQLException {
		GroupStream s = BurdenManager.getInstance().getStreamForDiscAndId(streamId, discId);
		
		if (s == null) {
			return 0;
		}
		
		// Перебираем всё, что есть в потоке
		for (GroupLoadItem item : s.getItems()) {
			appointTeacherToItem(item.getId(), teacherId);
			
			// Назначаем
			item.setAppointed((teacherId != 0));
			item.setTeacherId(teacherId);
		}
		
		return discId + 23 * streamId;
	}
	
	/**
	 * Назначает преподавателя отдельному элементу нагрузки
	 * @param itemId идентификатор элемента (номер строки в таблице)
	 * @param teacherId идентификатор преподавателя
	 * @throws SQLException
	 */
	public void appointTeacherToItem(int itemId, int teacherId) throws SQLException {
		if (teacherId == 0) {
			psAppointTeacher.setNull(1, Types.INTEGER);
		} else {
			psAppointTeacher.setInt(1, teacherId);
		}
		
		psAppointTeacher.setInt(2, itemId);
		
		psAppointTeacher.executeUpdate();		
	}
}
