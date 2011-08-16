package gov.usgs.cida.ncetl.servlet;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import gov.usgs.cida.ncetl.utils.FileHelper;
import gov.usgs.cida.ncetl.utils.NcMLUtil;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import ucar.nc2.Attribute;
import ucar.nc2.Group;
import ucar.nc2.WrapperNetcdfFile;

/**
 *
 * @author Jordan Walker <jiwalker@usgs.gov>
 */
public class GlobalAttributeDeaggregationServlet extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request,
                                  HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/xml;charset=UTF-8");
        OutputStream out = response.getOutputStream();

        String directory = request.getParameter("directory");
        boolean recurse = ("true".equalsIgnoreCase(request.getParameter(
                "recurse")));
        final String regex = request.getParameter("regex");

        FileFilter filter = new FileFilter() {

            private Pattern pattern = null;

            @Override
            public boolean accept(File pathname) {
                pattern = (null != regex) ? Pattern.compile(regex) : Pattern.compile(
                        ".*");
                Matcher matcher = pattern.matcher(pathname.getName());
                return matcher.matches();
            }
        };

        WrapperNetcdfFile globals = new WrapperNetcdfFile();
        Map<String, Set<String>> attrMap = Maps.newHashMap();
        Group rootGroup = new Group(globals, null,
                                    "Aggregated global attributes");
        globals.addGroup(null, rootGroup);
        try {

            File file = new File(directory);
            addDirectoryContents(file, globals, rootGroup, recurse, filter, attrMap);
            for (String name : attrMap.keySet()) {
                Joiner joiner = Joiner.on(";" + IOUtils.LINE_SEPARATOR).skipNulls();
                String joinedStr = joiner.join(attrMap.get(name));
                Attribute attr = new Attribute(name, joinedStr);
                rootGroup.addAttribute(attr);
            }
            globals.writeNcML(out, null);
        }
        catch (Exception ex) {
            printError(out, "Error getting global attributes from files, try using a regex to refine your search");
        }
        finally {
            IOUtils.closeQuietly(out);
        }
    }

    private void addDirectoryContents(File fileOrDir, WrapperNetcdfFile ncml,
                                      Group metaGroup, boolean recurse,
                                      FileFilter filter, Map<String, Set<String>> map) throws
            IOException {
        if (fileOrDir.isDirectory()) {
            for (File file : fileOrDir.listFiles()) {

                if (file.isDirectory() && recurse) {
                    File recurseDir = new File(FileHelper.dirAppend(fileOrDir.getPath(), file.getName()));
                    addDirectoryContents(recurseDir, ncml, metaGroup, recurse,
                                         filter, map);
                }
                else {
                    //addFileContents(file, filter, ncml, metaGroup);
                    addFileContents(file, filter, map);
                }
            }
        }
        else {
            //addFileContents(fileOrDir, filter, ncml, metaGroup);
            addFileContents(fileOrDir, filter, map);
        }
    }

    private void addFileContents(File file, FileFilter filter, WrapperNetcdfFile globals,
                                 Group metaGroup) throws IOException {
        if (filter.accept(file)) {
            Group globalGroup = NcMLUtil.globalAttributesToMeta(file, globals);
            globals.addGroup(metaGroup, globalGroup);
        }
    }
    
    private void addFileContents(File file, FileFilter filter, Map<String,Set<String>> buffs) throws IOException {
        if (filter.accept(file)) {
            NcMLUtil.globalAttributesToMeta(file, buffs);
        }
    }
    private void printError(OutputStream out, String message) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
        writer.write("<?xml version='1.0' encoding='UTF-8'?>\n");
        writer.write("<error>" + message + "</error>");
        writer.flush();
        IOUtils.closeQuietly(writer);
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
