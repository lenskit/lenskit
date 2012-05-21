package org.grouplens.lenskit.eval.util.table;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * The implementation of the im memory table, which is the replica of the csv table output to the
 * file.
 *
 * @author Shuo Chang<schang@cs.umn.edu>
 */
public class TableImpl extends AbstractList<Row> implements Table {
    private List<Row> rows;
    private final HashMap<String, Integer> header;
    //To keep the order of the header
    private final List<String> headerList;

    public TableImpl(List<String> hdr){
        super();
        rows = new ArrayList<Row>();
        headerList = hdr;
        header = new HashMap<String, Integer>();
        for(int i = 0; i < hdr.size(); i++) {
            header.put(hdr.get(i), i);
        }
    }

    private TableImpl(List<String> hdr, Iterable<Row> rws){
        super();
        rows = Arrays.asList(Iterables.toArray(rws, Row.class));
        headerList = hdr;
        header = new HashMap<String, Integer>();
        for(int i = 0; i < hdr.size(); i++) {
            header.put(hdr.get(i), i);
        }
    }

    /**
     * Filter the table with the given matching data.
     * @param col The name of the column
     * @param data The data in the column to match
     * @return  A new table that has "data" in "col"
     */
    @Override
    public TableImpl filter(final String col, final Object data) {
        Predicate<Row> pred = new Predicate<Row>() {
            @Override
            public boolean apply(@Nonnull Row input) {
                return  data.equals(input.value(col));
            }
        };
        Iterable<Row> filtered = Iterables.filter(this.rows, pred);
        return new TableImpl(this.headerList, filtered);
    }

    /**
     * Put a new algorithm in the result.
     *
     * @param list the list of objects to insert to the result rows
     */
    public void addResultRow(Object[] list) {
        if (list.length > header.size()) {
            throw new IllegalArgumentException("row too long");
        }
        RowImpl row = new RowImpl(header, list);
        rows.add(row);
    }

    @Override
    public int size() {
        return rows.size();
    }

    @Override
    public Iterator<Row> iterator() {
        return rows.iterator();
    }


    @Override
    public Row get(int i) {
        return rows.get(i);
    }

    public ColumnImpl column(String col){
        return new ColumnImpl(col);
    }

    public List<String> getHeader() {
        return Collections.unmodifiableList(headerList);
    }

    public class ColumnImpl extends AbstractList<Object> implements Column{
        ArrayList<Object> column;

        ColumnImpl(String col) {
            super();
            column = new ArrayList<Object>();
            if(header.get(col)!=null){
                for(Row row : rows) {
                    column.add(row.value(header.get(col)));
                }
            }
        }

        public Double sum() {
            double sum = 0;
            if(column.size() == 0 ||
                    !Number.class.isAssignableFrom(column.get(0).getClass())) {
                return Double.NaN;
            }
            else {
                for(Object v : column) {
                    sum += ((Number)v).doubleValue();
                }
                return sum;
            }
        }

        public Double average() {
            if(column.size()==0) {
                return Double.NaN;
            }
            return sum()/column.size();
        }

        @Override
        public int size() {
            return column.size();  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Object get(int i) {
            return column.get(i);  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
