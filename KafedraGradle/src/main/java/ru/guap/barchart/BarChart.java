package ru.guap.barchart;

import javax.servlet.http.HttpServlet;

/**
 * Базовый класс для столбчатых диаграмм
 * @author Cr0s
 *
 */
public abstract class BarChart extends HttpServlet {
	private static final long serialVersionUID = -3430306187935238900L;

	/**
	 * Ширина и высота блока диаграммы в пикселях
	 */
	protected static final int BLOCK_WIDTH = 580;
	protected static final int BLOCK_HEIGHT = 270;
	
	/**
	 * Строковые константы
	 */
	protected static final String STR_CONTRACT = "Контракт";
	protected static final String STR_BUDGET = "Бюджет";
	protected static final String STR_PERCENT = "Проценты";
	
	public BarChart() {
		super();
	}
}
