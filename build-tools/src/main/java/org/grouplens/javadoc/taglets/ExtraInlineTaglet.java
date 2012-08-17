package org.grouplens.javadoc.taglets;

import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;

import java.util.Map;

/**
 * @author Michael Ekstrand
 */
public class ExtraInlineTaglet implements Taglet {

    private String tagName;

    public ExtraInlineTaglet(String name) {
        tagName = name;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void register(Map map) {
        map.put("var", new ExtraInlineTaglet("var"));
    }

    @Override
    public boolean inField() {
        return true;
    }

    @Override
    public boolean inConstructor() {
        return true;
    }

    @Override
    public boolean inMethod() {
        return true;
    }

    @Override
    public boolean inOverview() {
        return true;
    }

    @Override
    public boolean inPackage() {
        return true;
    }

    @Override
    public boolean inType() {
        return true;
    }

    @Override
    public boolean isInlineTag() {
        return true;
    }

    @Override
    public String getName() {
        return tagName;
    }

    @Override
    public String toString(Tag tag) {
        StringBuilder bld = new StringBuilder();
        bld.append('<').append(tagName).append('>');
        bld.append(tag.text());
        bld.append("</").append(tagName).append('>');
        return bld.toString();
    }

    @Override
    public String toString(Tag[] tags) {
        StringBuilder bld = new StringBuilder();
        for (Tag tag: tags) {
            bld.append(toString(tag));
        }
        return bld.toString();
    }
}
