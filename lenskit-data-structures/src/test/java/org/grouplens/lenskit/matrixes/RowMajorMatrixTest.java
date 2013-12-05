package org.grouplens.lenskit.matrixes;

import org.grouplens.lenskit.vectors.Vec;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class RowMajorMatrixTest {
    @Test
    public void testEmptyMatrix() {
        Matrix m = Matrixes.create(0, 0);
        assertThat(m.getRowCount(), equalTo(0));
        assertThat(m.getColumnCount(), equalTo(0));
    }

    @Test
    public void testRowMatrix() {
        MutableMatrix m = Matrixes.create(1, 3);
        assertThat(m.getRowCount(), equalTo(1));
        assertThat(m.getColumnCount(), equalTo(3));
        assertThat(m.row(0).size(), equalTo(3));
        assertThat(m.row(0).sum(), equalTo(0.0));
        assertThat(m.column(0).size(), equalTo(1));
        assertThat(m.column(1).size(), equalTo(1));
        assertThat(m.column(0).sum(), equalTo(0.0));
    }

    @Test
    public void testColumnMatrix() {
        MutableMatrix m = Matrixes.create(3, 1);
        assertThat(m.getRowCount(), equalTo(3));
        assertThat(m.getColumnCount(), equalTo(1));
        assertThat(m.row(0).size(), equalTo(1));
        assertThat(m.row(1).size(), equalTo(1));
        assertThat(m.row(0).sum(), equalTo(0.0));
        assertThat(m.column(0).size(), equalTo(3));
        assertThat(m.column(0).sum(), equalTo(0.0));
    }

    @Test
    public void testSetData() {
        MutableMatrix m = Matrixes.create(2, 3);
        m.set(0, 0, 1);
        m.set(0, 1, 2);
        m.set(0, 2, 3);
        m.set(1, 0, -1);
        m.set(1, 1, -2);
        m.set(1, 2, -3);
        assertThat(m.getRowCount(), equalTo(2));
        assertThat(m.getColumnCount(), equalTo(3));
        assertThat(m.row(0).size(), equalTo(3));
        assertThat(m.row(1).size(), equalTo(3));
        assertThat(m.row(0).sum(), equalTo(6.0));
        assertThat(m.row(1).sum(), equalTo(-6.0));
        assertThat(m.column(0).size(), equalTo(2));
        assertThat(m.column(1).size(), equalTo(2));
        assertThat(m.column(2).size(), equalTo(2));
        assertThat(m.column(0).sum(), equalTo(0.0));
        assertThat(m.column(1).sum(), equalTo(0.0));
        assertThat(m.column(2).sum(), equalTo(0.0));

        assertThat(m.get(0, 1), equalTo(2.0));
        assertThat(m.row(0).get(1), equalTo(2.0));
        assertThat(m.column(1).get(0), equalTo(2.0));
    }

    @Test
    public void testRowsAndColumns() {
        MutableMatrix m = Matrixes.create(2, 3);
        m.set(0, 0, 1);
        m.set(0, 1, 2);
        m.set(0, 2, 3);
        m.set(1, 0, -1);
        m.set(1, 1, -2);
        m.set(1, 2, -3);
        m.row(1).scale(Math.PI);
        assertThat(m.rows(),
                   contains((Vec) m.row(0), m.row(1)));
        assertThat(m.columns(),
                   contains((Vec) m.column(0), m.column(1), m.column(2)));
    }

    @Test
    public void testImmutable() {
        MutableMatrix m = Matrixes.create(2, 3);
        m.set(0, 0, 1);
        m.set(0, 1, 2);
        m.set(0, 2, 3);
        m.set(1, 0, -1);
        m.set(1, 1, -2);
        m.set(1, 2, -3);
        ImmutableMatrix imm = m.immutable();
        assertThat(imm.rows(), equalTo(m.rows()));
        assertThat(imm.columns(), equalTo(m.columns()));
        m.set(1, 1, -2.5);
        assertThat(imm.get(1,1), equalTo(-2.0));
        assertThat(imm.column(1), not(equalTo((Vec) m.column(1))));
    }
}
