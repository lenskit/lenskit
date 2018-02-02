package org.lenskit.gradle;

import groovy.lang.MetaProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.commons.lang3.text.StrMatcher;
import org.apache.commons.lang3.text.StrTokenizer;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.joda.convert.StringConvert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Plugin for LensKit evaluations.  This sets up the basic infrastructure required for LensKit tasks to work.
 * To use, add the following to your `build.gradle`:
 *
 * ```groovy
 * apply plugin: 'lenskit'
 * ```
 *
 * This plugin only sets up infrastructure and configuration defaults. It does *not* create any tasks.
 */
public class LenskitPlugin implements Plugin<Project> {
    public void apply(Project project) {
        final LenskitExtension lenskit = project.getExtensions().create("lenskit", LenskitExtension.class, project);

        for (MetaProperty prop : DefaultGroovyMethods.getMetaClass(lenskit).getProperties()) {
            String prjProp = "lenskit." + prop.getName();
            if (project.hasProperty(prjProp)) {
                Object val = project.findProperty(prjProp);
                String vstr = val != null ? val.toString() : null;
                logger.info("setting property {} to {}", prjProp, val);
                Class type = prop.getType();
                Consumer<Object> set = (v) -> prop.setProperty(lenskit, v);
                if (type.equals(Property.class)) {
                    Method m = null;
                    try {
                        m = lenskit.getClass().getMethod("get" + StringUtils.capitalize(prop.getName()));
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                    Type t = m.getGenericReturnType();
                    Map<TypeVariable<?>, Type> args = TypeUtils.getTypeArguments(t, Property.class);
                    Type rt = args.get(Property.class.getTypeParameters()[0]);
                    type = TypeUtils.getRawType(rt, Object.class);
                    set = (v) -> ((Property) prop.getProperty(lenskit)).set(v);
                }

                if (type.equals(List.class)) {// if the type is list update the val using strtokenizer
                    StrTokenizer tok = new StrTokenizer(vstr, StrMatcher.splitMatcher(), StrMatcher.quoteMatcher());
                    val = DefaultGroovyMethods.toList(tok);
                } else if (type.equals(String.class)) {
                    val = vstr;
                } else {
                    val = StringConvert.INSTANCE.convertFromString(type, vstr);
                }

                set.accept(val);
            }

        }

    }

    private static final Logger logger = LoggerFactory.getLogger(LenskitPlugin.class);
}
