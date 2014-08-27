package ru.guap.barchart;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import ru.guap.dao.DBManager;
import ru.guap.treeview.BurdenManager;

/**
 * Сервлет, генерирующий изображение диаграммы почасового распределения нагрузки
 * @author Cr0s
 *
 */
@WebServlet("/HoursBarChart")
public class HoursBarChart extends BarChart {

	public HoursBarChart() {
		super();
	}

	/**
	 * Обрабатывает GET-запрос от браузера пользователя и отвечает на него PNG-изображением с диаграммой
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		int teachId;
		int aVgO; //Budget hours of current group
		int aVcO; //Contract hours of current group
		String teachName;		
		
		OutputStream out = response.getOutputStream();
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		
		Connection cnn = null;
		PreparedStatement countValues = null, teacherData = null;
		ResultSet tData = null;
		
		try {
			// Соединяемся и выбираем данные
			cnn = DBManager.getInstance().getConnection();

			// Аггрегирующие функции в SQL-запросе берут сумму часов за бюджет и контракт
			countValues = cnn.prepareStatement("SELECT sum(ValueG), sum(ValueCO) FROM kafedra.kaf43 WHERE load_id = ? AND teachers_id = ?");
			
			teacherData = cnn.prepareStatement("SELECT * FROM kafedra.teachers");  			
			
			countValues.setInt(1, BurdenManager.LOAD_VERSION);
			tData = teacherData.executeQuery();    			

			// Считаем для каждого преподавателя, сколько у него часов и добавляем столбик в диаграмму
			while(tData.next()) {
				teachId = tData.getInt(1);
				teachName = tData.getString(2);

				countValues.setInt(2, teachId);
				ResultSet allValues = countValues.executeQuery();

				// Преподаватель имеет часы, добавляем столбик
				if(allValues.next()) {
					aVgO = allValues.getInt(1);
					aVcO = allValues.getInt(2);
					dataset.addValue(aVcO, STR_CONTRACT,teachName );
					dataset.addValue(aVgO, STR_BUDGET, teachName);    			
				}
			}

			// Генерируем диаграмму
			JFreeChart chart = ChartFactory.createBarChart(
					"s1", // FIXME
					"s2", // FIXME
					"s3", // FIXME
					dataset,
					PlotOrientation.VERTICAL,
					true, true, false
					);

			CategoryAxis ca = new CategoryAxis();
			ca.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
			ca.setMaximumCategoryLabelWidthRatio(5f);

			ca.setLowerMargin(0);
			ca.setCategoryMargin(0);
			ca.setUpperMargin(0);      			

			chart.getCategoryPlot().setDomainAxis(ca);

			// Отсылаем изображение
			response.setContentType("image/png");
			ChartUtilities.writeChartAsPNG(out, chart, BLOCK_WIDTH, BLOCK_HEIGHT);
		}
		catch (Exception e) {
			System.err.println(e.toString());
		}
		finally {
			out.close();
			
			DBManager.getInstance().closeResource(cnn, null, null);
			DBManager.getInstance().closeResource(null, tData, null);
			DBManager.getInstance().closeResource(null, null, countValues);
			DBManager.getInstance().closeResource(null, null, teacherData);			
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
