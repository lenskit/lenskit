package org.grouplens.lenskit.data.text;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.text.StrTokenizer;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.data.event.EventBuilder;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A list of fields to parse.
 */
@Shareable
public class FieldList<B extends EventBuilder> extends AbstractList<Field<? super B>> implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Field<? super B>> fields;

    private FieldList(List<Field<? super B>> fl) {
        boolean haveOptional = false;
        for (Field<? super B> f: fl) {
            if (f.isOptional()) {
                haveOptional = true;
            } else if (haveOptional && !f.isOptional()) {
                throw new IllegalArgumentException("Optional field after non-optional field");
            }
        }
        fields = ImmutableList.copyOf(fl);
    }

    /**
     * Create a field list from a list of fields.
     * @param fields The list of fields.
     * @param <B> The type of builder used.
     * @return The field list.
     * @throws java.lang.IllegalArgumentException if the field list is unusable.
     */
    public static <B extends EventBuilder> FieldList<B> create(List<Field<? super B>> fields) {
        if (fields instanceof FieldList) {
            return (FieldList<B>) fields;
        } else {
            return new FieldList<B>(fields);
        }
    }

    /**
     * Create a field list from a list of fields.
     * @param fields The list of fields.
     * @param <B> The type of builder used.
     * @return The field list.
     * @throws java.lang.IllegalArgumentException if the field list is unusable.
     */
    public static <B extends EventBuilder> FieldList<B> create(Field<? super B>... fields) {
        return new FieldList<B>(ImmutableList.copyOf(fields));
    }

    /**
     * Parse the tokens (columns) and put their values into the builder.
     *
     * @param tokens The tokens to parse.
     * @param builder The builder into which to put the values.
     * @throws InvalidRowException if the row is not correctly formatted.
     */
    public void parse(StrTokenizer tokens, B builder) throws InvalidRowException {
        for (Field<? super B> field: fields) {
            String token = tokens.nextToken();
            if (token == null && field != null && !field.isOptional()) {
                // fixme make nulls optional if they are at the end
                throw new InvalidRowException("Non-optional field " + field.toString() + " missing");
            }
            if (field != null) {
                field.apply(token, builder);
            }
        }
    }

    @Override
    public Field<? super B> get(int index) {
        return fields.get(index);
    }

    @Override
    public int size() {
        return fields.size();
    }

    @Override
    public Iterator<Field<? super B>> iterator() {
        return fields.iterator();
    }

    @Override
    public ListIterator<Field<? super B>> listIterator(int index) {
        return fields.listIterator(index);
    }
}
