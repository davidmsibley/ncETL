package gov.usgs.cida.ncetl.utils;

import com.google.common.collect.Maps;
import gov.usgs.cida.ncetl.spec.IngestControlSpec;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.naming.NamingException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jwalker
 */
public class IngestController {
    
    private static final Logger LOG = LoggerFactory.getLogger(
            IngestController.class);
    private static final String TIMER_NAME = "ncETL Ingestor";
    private static Timer timer = new Timer(TIMER_NAME);
    private static Map<String, TimerTask> runningTasks = Maps.newTreeMap();
    private static final int TIMER_LENGTH = 60000;
    private static final long serialVersionUID = 1L;
    
    public static void setupIngestors() throws SQLException, NamingException,
                                               ClassNotFoundException,
                                               MalformedURLException {
        
        Connection con = DatabaseUtil.getConnection();
        try {
            List<FTPIngestTask> tasks = IngestControlSpec.unmarshalAllIngestors(
                    con);
            for (FTPIngestTask task : tasks) {
                startIngestTimer(task);
            }
        }
        finally {
            DatabaseUtil.closeConnection(con);
        }
    }
    
    protected static void startIngestTimer(FTPIngestTask task) {
        String name = task.getName();
        if (task != null) {
            task.cancel();
        }
        timer.purge();
        runningTasks.remove(name);
        timer.scheduleAtFixedRate(task, 0L, task.getRescanEvery());
        LOG.debug("timerTask started for ingest: " + name);
        runningTasks.put(name, task);
    }
    
    public static void restartIngestTimer(String taskName) {
        Connection con = null;
        try {
            con = DatabaseUtil.getConnection();
            FTPIngestTask task = IngestControlSpec.unmarshalSpecificIngestor(taskName, con);
            startIngestTimer(task);
        }
        catch (SQLException ex) {
            LOG.debug("problem with sql when restarting ingestor", ex);
        }
        catch (NamingException ex) {
            LOG.debug("naming problem in ingestor restart", ex);
        }
        catch (ClassNotFoundException ex) {
            LOG.debug("class not found when restarting ingest task", ex);
        }
        catch (MalformedURLException ex) {
            LOG.debug("ingest url is malformed", ex);
        }
        finally {
            DatabaseUtil.closeConnection(con);
        }
    }
    
    public static void shutdownTimer() {
        timer.cancel();
        timer = null;
    }
}
