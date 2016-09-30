/*
 * AUT DMS S1 2016
 * Assignment : - Android Distributed Application
 *  Green, Terry (0829446)
 *  Prouting, Sez (0308852)
 *  Shaw, Aziel (14847095)
 *  
 *  
 */
package sezielioter.locator;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
//import jdk.internal.org.xml.sax.InputSource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import sezielioter.locator.beanInterface.DBReaderRemote;
import sezielioter.locator.data.TagData;


public class TagDataServlet extends HttpServlet {

    @EJB
    private DBReaderRemote dbReader;
    public static final String TAG_ID="04:C7:C3:E2:83:34:80";
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        //HttpSession session = request.getSession(true);
        String tagID = request.getParameter("tagID");
        System.out.println("TAG ID WAS COLLECTED AND = " + tagID);
        TagData destinationData = null;
        String tagDataString = "";
        
    // ***************************************************************
    //      GET DESTINATION DATA FROM DATABASE
    // ***************************************************************
        try {
            destinationData = (TagData)dbReader.getTagData(tagID);
            System.out.println(destinationData.getTagID() + "\n" +
                            destinationData.getTagLocation() + "\n" +
                            destinationData.getDestinationName() + "\n" +
                            destinationData.getDestinationLatitude() + "\n" +
                            destinationData.getDestinationLongitude() + "\n" +
                            destinationData.getCount());
        } catch (Exception e) {
            System.out.println("~~~~~~ could not access DB: " + e.getMessage());
        }
                
        
    // ***************************************************************
    //      CONVERT AND SEND DESTINATION DATA IN HTTP RESPONSE
    // ***************************************************************
        
        if(destinationData != null){
            
            String xmlString = (//"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                                destinationData.getTagID() + " " +
                                destinationData.getDestinationLatitude() + " " +
                                destinationData.getDestinationLongitude() + " " +
                                destinationData.getCount() + " " +
                                destinationData.getDestinationName() + " " +
                                destinationData.getTagLocation()
                    );
            
            response.setContentType("text/plain; charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print(xmlString);
            out.flush();
            out.close();
//            FileWriter fw = null;
//            try{
//                fw = new FileWriter("./tagData.xml");
//                fw.write(xmlString);
//                fw.close();
//                
//            } finally {
//                if(fw != null) fw.close();
//            }
//            
//            try{
//                File tagFile = new File("./tagData.xml");
//                response.setContentType("application/xml;charset=UTF-8");
//                response.setContentLength((int)tagFile.length());
//                response.setHeader("Content-Disposition","attachment;filename=\"tagData.xml\"");
//                response.setHeader("Content-Encoding", "xml");
//                
//                byte[] buffer = new byte[(int)tagFile.length()];   
//                FileInputStream fileIn = new FileInputStream(tagFile);   
//                fileIn.read(buffer);  
//                ServletOutputStream servOut = response.getOutputStream();
//                servOut.write(buffer);
//                servOut.flush();
//                servOut.close();
//                fileIn.close();
//            }
//            catch(IOException e){
//                System.out.println("-------> a problem pushing the output to response");
//            }
            
//            byte[] tagDataArray;
//            ByteArrayOutputStream baos = null;
//           // ObjectOutputStream objOut = null;
//           FileOutputStream fileOut = null;
//            OutputStream servOut = null;
//            try{
//                baos = new ByteArrayOutputStream();
//               // objOut = new ObjectOutputStream(baos);
//              //  objOut.writeObject(destinationData);
//                fileOut = new FileOutputStream("./tagData.xml");
//                tagDataArray = baos.toByteArray();
//                
//                servOut = response.getOutputStream();
//                servOut.write(tagDataArray);
//            }
//            finally{
//                if(baos != null) baos.close();
//                if(objOut != null) objOut.close();
//                if(servOut != null) servOut.close();
//            }
        }
    }
    

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
