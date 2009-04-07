/**
 * @(#) Condition.java;
 * <p/>
 * Created on Apr 3, 2009
 * AUTHOR    ** Danil Glinenko
 * EMAIL     ** dglinenko@gmail.com
 * <p/>
 */

package net.codemate;

import java.util.ArrayList;
import java.util.List;

class Condition {
    private String tableName;
    private List<String> whereCond = new ArrayList<String>();

    public Condition() {
    }

    public Condition table(String tableName) {
//        if (tableName == null || tableName.isEmpty()){throw new IllegalArgumentException("\"tableName\" param can not be null or empty.");}
        resetConditions();
        this.tableName = tableName;
        return this;
    }

    public Condition table() {
        return table(null);
    }

    public String getTableName() {
        return tableName;
    }

    public Condition where(String clause) {
        resetConditions();
        if (clause != null) {
            whereCond.add(clause);
        }
        return this;
    }

    public Condition where() {
        return where(null);
    }


    public Condition where(String colName, Object value) {
        final String clause = buildClause(colName, value);
        return where(clause);
    }


    public Condition and(String andClause) {
        whereCond.add(andClause);
        return this;
    }

    public Condition and(String colName, Object value) {
        final String clause = buildClause(colName, value);
        return and(clause);
    }

    public String getCondtionClause() {
        final StringBuilder sb = new StringBuilder("1 = 1 ");
        for (final String clause : whereCond) {
            sb.append(" AND ").append(clause);
        }
        return sb.toString();
    }

    private void resetConditions() {
//        this.table(null);
        whereCond = new ArrayList<String>();
    }

    private String buildClause(String colName, Object value) {
        final StringBuilder sb = new StringBuilder();
        Object val;
        if (value instanceof Integer || value instanceof Long) {
            val = value;
        } else {
           val ="'" + value.toString() + "'";
        }
        sb.append(colName).append("=").append(val);
        return sb.toString();
    }

}
