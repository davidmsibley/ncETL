package thredds.catalog;

import gov.usgs.cida.ncetl.utils.DatabaseUtil;
import gov.usgs.cida.ncetl.utils.FileHelper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Map;
import javax.naming.NamingException;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Jordan Walker <jiwalker@usgs.gov>
 */
public final class CatalogHelper {
    private static final String DEFAULT_CATALOG_FILENAME = "catalog.xml";
    private static final String DEFAULT_CATALOG_LOCATION = FileHelper.dirAppend(FileHelper.getDatasetsDirectory(), DEFAULT_CATALOG_FILENAME);
    private static final String DEFAULT_CATALOG_NAME = "ncETL generated THREDDS catalog";

    private CatalogHelper() {
    }

    /**
     * Sets up a location in the default location location.
     * 
     * @throws URISyntaxException
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public static void setupCatalog() throws URISyntaxException, FileNotFoundException, IOException {
        setupCatalog(new File(DEFAULT_CATALOG_LOCATION), DEFAULT_CATALOG_NAME);
    }
    
    public static void setupCatalog(File location) throws URISyntaxException, FileNotFoundException, IOException {
        setupCatalog(location, DEFAULT_CATALOG_NAME);
    }
    
    public static void setupCatalog(File location, String catalogName) throws URISyntaxException, FileNotFoundException, IOException {
        if (!location.exists()) {
            createNewCatalog(catalogName, location.getPath());
        }
    }
    
    public static void createNewCatalog(String name, String absolutePath) throws
        URISyntaxException, FileNotFoundException, IOException {
        URI uri = new URI("file://" + absolutePath);
        InvCatalogImpl impl = createCatalogImpl(name, uri);
        writeCatalog(impl);
    }
    
    public static InvCatalog readCatalog(URI absoluteURI) {
        InvCatalogFactory factory = new InvCatalogFactory("FileReadFactory", false);
        return factory.readXML(absoluteURI.toString());
    }
    
    public static void writeCatalog(InvCatalog cat) throws IOException {
        InvCatalogImpl impl = (InvCatalogImpl)cat;
        File file = new File(impl.getBaseURI());
        
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            impl.writeXML(fos);
        }
        finally {
            IOUtils.closeQuietly(fos);
        }
    }
    
    public static InvCatalog syncWithDatabase(URI location) throws SQLException, NamingException, ClassNotFoundException {
        InvCatalog editMe = readCatalog(location);
        Map<String, String> catalogInfo = DatabaseUtil.getCatalogInfo(location);
        
        // Gather all the needed information from the database
        //String id = catalogInfo.get("id");
        String name = catalogInfo.get("NAME"); 
        
        // Set the leaf nodes of catalog
        //List<Map<String,String>> datasets = DatabaseUtil.getDatasetInfos(id);
        // put the invCatalog into editable state, set name
        InvCatalogModifier setter = new InvCatalogModifier(editMe);
        setter.setName(name);
        
        // Search in database for children of this catalog
        // Recursively do syncWithDatabase for sub-catalogs, datasets, services
        // DatasetHelper.syncWithDatabase(datasetid)
        
        return editMe;
    }
    
    protected static InvCatalogImpl createCatalogImpl(String name, URI uri) {
        InvCatalogFactory factory = new InvCatalogFactory("CatalogFactory", true);
        InvCatalogImpl impl = new InvCatalogImpl(name, "1.0", uri);
        impl.setCatalogFactory(factory);
        impl.setCatalogConverterToVersion1();
        return impl;
    }
    
    public static void addDataset(InvCatalog cat, InvDataset dataset) {
        InvCatalogImpl concreteCat = (InvCatalogImpl)cat;
        InvDatasetImpl concreteDataset = (InvDatasetImpl)dataset;
        concreteCat.addDatasetByID(concreteDataset);
    }
    
    public static void removeDataset(InvCatalog cat, String datasetId) {
        InvCatalogImpl concreteCat = (InvCatalogImpl)cat;
        InvDatasetImpl ds = (InvDatasetImpl)new InvDatasetWrapper("blah", datasetId).build();
        concreteCat.removeDatasetByID(ds);
    }
    
    public static void addService(InvCatalog cat, InvService serv) {
        InvCatalogImpl concreteCat = (InvCatalogImpl)cat;
        concreteCat.addService(serv);
    }
    
    public static void removeService(InvCatalog cat, String serviceName) {
        InvCatalogImpl concreteCat = (InvCatalogImpl)cat;
        InvService serv = cat.serviceHash.remove(serviceName);
        concreteCat.services.remove(serv);
    }
    
    public static String getDefaultCatalogFilename() {
        return DEFAULT_CATALOG_FILENAME;
    }
    
    public static String getDefaultCatalogName() {
        return DEFAULT_CATALOG_NAME;
    }
}
