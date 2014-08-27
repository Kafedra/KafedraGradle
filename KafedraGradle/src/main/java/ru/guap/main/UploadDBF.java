package ru.guap.main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import ru.guap.config.WebConfig;
import ru.guap.dao.dbf.DBFConverter;

/**
 * Сервлет, принимающий от пользователя DBF-файл
 * @author Cr0s
 *
 */
@WebServlet(description = "A DBF file uploader", urlPatterns = { "/dbfupload" })
@MultipartConfig
public class UploadDBF extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public UploadDBF() {
        super();
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect(WebConfig.getErrorPage(WebConfig.Error.ERROR_DEFAULT));
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            PrintWriter out = response.getWriter();
            HttpSession httpSession = request.getSession();
            String filePathUpload = (String) httpSession.getAttribute("path")!=null ? httpSession.getAttribute("path").toString() : WebConfig.DBF_UPLOAD_PATH ;

            System.out.println("Path: " + filePathUpload);
            
            String path1 =  filePathUpload;
            String filename = null;
            File path = null;
            FileItem item=null;


            boolean isMultipart = ServletFileUpload.isMultipartContent(request);

            if (isMultipart) {
                FileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);
                String FieldName = "";
                try {
                    FileItemIterator iterator = upload.getItemIterator(request);
                    
                    while (iterator.hasNext()) {
                        item = (FileItem) iterator.next();

                        // Skip other files
                        if (!item.getFieldName().equals("dbf")) {
                        	continue;
                        }
                        
                        if (!item.isFormField()) {
                            filename = System.currentTimeMillis() + "_" + item.getName();
                            path = new File(path1 + File.separator);
                            
                            if (!path.exists()) {
                                boolean status = path.mkdirs();
                            }
                            
                            File uploadedFile = new File(path + System.getProperty("file.separator") + filename);  // for copy file
                            item.write(uploadedFile);

                            boolean result = DBFConverter.insertDBFContentToDB(uploadedFile);
                            if (!result) {
                        	response.sendRedirect(WebConfig.getErrorPage(WebConfig.Error.DBF_ERROR));
                        	return;
                            }
                            
                            response.sendRedirect(WebConfig.PAGE_NAME_MAIN);
                            return;
                        }

                    } // END OF WHILE 
                    
                    response.sendRedirect(WebConfig.PAGE_NAME_MAIN);
                } catch (FileUploadException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } 
            }   
    }
}
