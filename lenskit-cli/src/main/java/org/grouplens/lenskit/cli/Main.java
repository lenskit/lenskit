package org.grouplens.lenskit.cli;

/**
 * Deprecated compatibility alias for running LensKit commands.
 * @deprecated Use {@link org.lenskit.cli.Main}.
 */
@Deprecated
public class Main {
    public static void main(String[] args) {
        System.err.println("warning: org.grouplens.lenskit.cli.Main is deprecated; use o.l.cli.Main");
        org.lenskit.cli.Main.main(args);
    }
}
