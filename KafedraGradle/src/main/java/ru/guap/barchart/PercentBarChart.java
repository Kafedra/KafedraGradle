package ru.guap.barchart;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import ru.guap.dao.DBManager;
import ru.guap.treeview.BurdenManager;

/**
 * Генератор столбчатой диаграммы распределения нагрузки среди преподавателей в процентном отношении от их ставок
 * @author Cr0s
 *
 */
@WebServlet("/PercentBarChart")
public class PercentBarChart extends BarChart {

	/**
	 * Стандартное количество часов (ставка 1.0 или 100%)
	 */
	private static final int ANNUAL_STATE = 800;

	public PercentBarChart() {
		super();
	}

	/**
	 * Отвечает на GET-запрос картинкой с диаграммой
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		OutputStream out = response.getOutputStream();
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		Connection cnn = null;
		ResultSet tData = null;
		PreparedStatement countValues = null, teacherData = null;
		
		int teachId; 
		int teachRateG; // Ставка преподавателя за бюджет
		int teachRateC; // Ставка преподавателя за контракт
		int aVgO; // Кол-во бюджетных часов для группы
		int aVcO; //Кол-во контрактных часов для группы
		String teachName;		
		
		try {
			// Соединяемся с БД и выбираем данные
			cnn = DBManager.getInstance().getConnection();
		
			countValues = cnn.prepareStatement("SELECT sum(ValueG), sum(ValueCO) FROM kafedra.kaf43 WHERE load_id = ? AND teachers_id = ?");
			teacherData = cnn.prepareStatement("SELECT * FROM kafedra.teachers");  
			
			float percent = 0.0f;
			countValues.setInt(1, BurdenManager.LOAD_VERSION);
			tData = teacherData.executeQuery();

			// Для каждого преподавателя считаем часы и создаём столбик
			while(tData.next()) {
				teachId = tData.getInt(1);
				teachName = tData.getString(2);
				teachRateG = tData.getInt(3);
				teachRateC = tData.getInt(4);

				countValues.setInt(2, teachId);
				ResultSet allValues = countValues.executeQuery();

				if(allValues.next()) {
					aVgO = allValues.getInt(2);
					aVcO = allValues.getInt(1); 

					int rateSumRatio = (teachRateG + teachRateC) / 100;
					if (ANNUAL_STATE * rateSumRatio != 0) {
						percent = (float) ((aVgO + aVcO) / (ANNUAL_STATE * rateSumRatio * 1.0f));
					}
					
					dataset.addValue(percent * 100, STR_PERCENT,teachName);					
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

			// Отсылаем картинку
			response.setContentType("image/png");
			ChartUtilities.writeChartAsPNG(out, chart, BLOCK_WIDTH, BLOCK_HEIGHT);

		}
		catch (Exception e) {
			e.printStackTrace();
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
