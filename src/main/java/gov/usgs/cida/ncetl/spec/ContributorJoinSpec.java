package gov.usgs.cida.ncetl.spec;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gov.usgs.webservices.jdbc.spec.Spec;
import gov.usgs.webservices.jdbc.spec.mapping.ColumnMapping;
import gov.usgs.webservices.jdbc.spec.mapping.SearchMapping;
import gov.usgs.webservices.jdbc.spec.mapping.WhereClauseType;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import thredds.catalog.ThreddsMetadata.Contributor;

/**
 *
 * @author Jordan Walker <jiwalker@usgs.gov>
 */
public class ContributorJoinSpec  extends AbstractNcetlSpec {
    private static final long serialVersionUID = 1L;
    private static final String TABLE_NAME = "contributor";
    public static final String DATASET_ID = "dataset_id";
    public static final String CONTRIBUTOR_ID = "contributor_id";


    @Override
    public String setupTableName() {
        return TABLE_NAME;
    }
    
    @Override
    public ColumnMapping[] setupColumnMap() {
        return new ColumnMapping[] {
                    new ColumnMapping(ID, ID),
                    new ColumnMapping(DATASET_ID, DATASET_ID),
                    new ColumnMapping(CONTRIBUTOR_ID, CONTRIBUTOR_ID),
                    new ColumnMapping(INSERTED, null),
                    new ColumnMapping(UPDATED, null)
                };
    }

    @Override
    public SearchMapping[] setupSearchMap() {
        return new SearchMapping[] {
            new SearchMapping(ID, ID, null, WhereClauseType.equals, null, null, null),
            new SearchMapping("s_" + DATASET_ID, DATASET_ID, DATASET_ID, WhereClauseType.equals, null, null, null),
            new SearchMapping("s_" + CONTRIBUTOR_ID, CONTRIBUTOR_ID, CONTRIBUTOR_ID, WhereClauseType.equals, null, null, null),
            new SearchMapping("s_" + INSERTED, INSERTED, INSERTED, WhereClauseType.equals, null, null, null),
            new SearchMapping("s_" + UPDATED, UPDATED, UPDATED, WhereClauseType.equals, null, null, null)
        };
    }
    
    public static List<Contributor> unmarshal(int datasetId, Connection con) throws SQLException {
        List<Contributor> result = Lists.newLinkedList();
        ContributorJoinSpec spec = new ContributorJoinSpec();
        Map<String, String[]> params = Maps.newHashMap();
        params.put("s_" + DATASET_ID, new String[] { "" + datasetId });
        Spec.loadParameters(spec, params);
        ResultSet rs = Spec.getResultSet(spec, con);

        while (rs.next()) {
            int contrib_id = rs.getInt(CONTRIBUTOR_ID);
            result.add(ContributorSpec.lookup(contrib_id, con));
        }
        return result;
    }

}
