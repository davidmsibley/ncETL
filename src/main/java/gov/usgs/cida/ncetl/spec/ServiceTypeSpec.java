package gov.usgs.cida.ncetl.spec;

import com.google.common.collect.Maps;
import gov.usgs.webservices.jdbc.spec.Spec;
import gov.usgs.webservices.jdbc.spec.mapping.ColumnMapping;
import gov.usgs.webservices.jdbc.spec.mapping.SearchMapping;
import gov.usgs.webservices.jdbc.spec.mapping.WhereClauseType;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import thredds.catalog.ServiceType;

/**
 *
 * @author Jordan Walker <jiwalker@usgs.gov>
 */
public class ServiceTypeSpec extends AbstractNcetlSpec {
    private static final long serialVersionUID = 1L;
    
    private static final String TABLE_NAME = "service_type";
    public static final String TYPE = "type";
    
    @Override
    public String setupTableName() {
        return TABLE_NAME;
    }
    
    @Override
    public boolean setupAccess_DELETE() {
        return false;
    }

    @Override
    public boolean setupAccess_INSERT() {
        return false;
    }

    @Override
    public boolean setupAccess_UPDATE() {
        return false;
    }

    @Override
    public ColumnMapping[] setupColumnMap() {
        return new ColumnMapping[] {
                    new ColumnMapping(ID, ID),
                    new ColumnMapping(TYPE, TYPE)
                };
    }

    @Override
    public SearchMapping[] setupSearchMap() {
        return new SearchMapping[] {
            new SearchMapping(ID, ID, null, WhereClauseType.equals, null, null, null),
            new SearchMapping("s_" + TYPE, TYPE, TYPE, WhereClauseType.equals, null, null, null)
        };
    }
    
    public static ServiceType lookup(int id, Connection con) throws SQLException {
        Spec spec = new ServiceTypeSpec();
        Map<String, String[]> params = Maps.newHashMap();
        params.put(ID, new String[] { "" + id });
        Spec.loadParameters(spec, params);
        ResultSet rs = Spec.getResultSet(spec, con);
        String type = null;
        if (rs.next()) {
            type = rs.getString(TYPE);
        }
        
        return ServiceType.getType(type);
    }
}
