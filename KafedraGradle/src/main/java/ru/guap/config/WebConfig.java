package ru.guap.config;

public class WebConfig {
	/**
	 * Путь к директории для хранения временных DBF-файлов при их загрузке
	 */
    public static String DBF_UPLOAD_PATH = System.getProperty("user.dir") + System.getProperty("file.separator") + "uploads";
    
    /**
     * Префикс проекта в адресной строке
     */
    public static String JSP_PREFIX = "/KafedraGradle";
    
    /**
     * Названия страниц
     * @deprecated
     */
    public static String PAGE_NAME_MAIN = JSP_PREFIX + "/Main.jsp";
    
    /**
     * @deprecated
     */
    public static String PAGE_NAME_ERROR = JSP_PREFIX + "/Error.jsp";
    
    /**
     * @deprecated
     */
    public static String PAGE_NAME_UPLOAD = JSP_PREFIX + "/Upload.jsp";
    
    /**
     * @deprecated
     */
    public static String PAGE_NAME_VIEW = JSP_PREFIX + "/View.jsp";
    
    /**
     * @deprecated
     *
     */
    public static enum Error { ERROR_DEFAULT, DBF_ERROR };
    
    /**
     * @deprecated
     * @param e
     * @return
     */
    public static String getErrorPage(Error e) {
	switch (e) {
		case DBF_ERROR:
		    return PAGE_NAME_ERROR + "?m=1";
		    
		case ERROR_DEFAULT:
		default:
		    return PAGE_NAME_ERROR + "?m=0";	
	}
    }
}
