/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.lenskit.eval.graph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Provider;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.grouplens.grapht.graph.Edge;
import org.grouplens.grapht.graph.Graph;
import org.grouplens.grapht.graph.Node;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.spi.reflect.InstanceSatisfaction;
import org.grouplens.grapht.spi.reflect.ProviderClassSatisfaction;
import org.grouplens.grapht.spi.reflect.ProviderInstanceSatisfaction;
import org.grouplens.lenskit.core.Parameter;

public class GraphVizWriter implements GraphWriter {

    private File outputFile;
    private int numGraphs;
    private int numNodes;
    private HashMap<Node, String> nodeNames;
    private HashMap<Node, String> nodeLabels;
    private ArrayList<Node> singlePortNodes;
    private BufferedWriter writer;

    public GraphVizWriter(File output) {
        outputFile = output;
        numGraphs = 0;
        numNodes = 0;
        nodeNames = new HashMap<Node, String>();
        nodeLabels = new HashMap<Node, String>();
        singlePortNodes = new ArrayList<Node>();
    }

    @Override
    public void start() {
        try {
            writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write("digraph G {");
            writer.newLine();
            writer.write("\trankdir = LR");
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Unable to open file " + outputFile, e);
        }
    }

    @Override
    public void addGraph(String label, Graph g, Node root) {
        HashSet<Node> graphNodes = new HashSet<Node>();
        HashSet<Edge> graphEdges = new HashSet<Edge>();
        visitNode(g, root, graphNodes, graphEdges);
        try {
            writeSubgraph(label, root, g.getNodes(), graphEdges);
            numGraphs++;
        } catch (IOException e) {
            throw new RuntimeException("Unable to write graph to file", e);
        }
    }

    private void visitNode(Graph g, Node n, Set<Node> nodes, Set<Edge> edges) {
        nodes.add(n);
        nodeNames.put(n, "n" + numNodes);
        numNodes++;
        Set<Edge> incomingEdges = g.getIncomingEdges(n);
        if (incomingEdges.isEmpty()) {
            nodeLabels.put(n, "Root");
        } else {
            Desire d = incomingEdges.iterator().next().getDesire();
            Annotation qualifier = d.getInjectionPoint().getAttributes().getQualifier();
            String desireDescription;
            if (qualifier != null) {
                if (qualifier.annotationType().getAnnotation(Parameter.class) != null) {
                    desireDescription = shortenClassName(qualifier.annotationType().getName()) +
                            ": " + ClassUtils.wrapperToPrimitive(d.getDesiredType()).toString();
                } else {
                    desireDescription = shortenClassName(qualifier.annotationType().getName()) +
                            ": " + shortenClassName(d.getDesiredType().toString());
                }
            } else {
                desireDescription = shortenClassName(d.getDesiredType().toString());
            }

            Satisfaction s = n.getLabel().getSatisfaction();
            String satisfactionDescription;
            if (s instanceof ProviderInstanceSatisfaction) {
                Provider<?> providerInstance = ((ProviderInstanceSatisfaction) s).getProvider();
                satisfactionDescription = "Provider: " + providerInstance.toString();
            } else if (s instanceof ProviderClassSatisfaction) {
                Class<? extends Provider<?>> providerType = ((ProviderClassSatisfaction) s).getProviderType();
                satisfactionDescription = "Provider Class: " + shortenClassName(providerType.getName());
            } else if (s instanceof InstanceSatisfaction) {
                String fullDescription = s.toString();
                satisfactionDescription = fullDescription.substring(fullDescription.indexOf('(') + 1,
                                                                    fullDescription.indexOf(')'));
            } else {
                satisfactionDescription = shortenClassName(s.getType().toString());
            }

            if (desireDescription.equals(satisfactionDescription)) {
                nodeLabels.put(n, desireDescription);
                singlePortNodes.add(n);
            } else {
                nodeLabels.put(n, String.format("<desire>%s|<satisfaction>%s", desireDescription,
                                                satisfactionDescription));
            }
        }

        for (Edge e : g.getOutgoingEdges(n)) {
            edges.add(e);
            if (!nodes.contains(e.getTail())) {
                visitNode(g, e.getTail(), nodes, edges);
            }
        }
    }

    private void writeSubgraph(String label, Node root, Set<Node> nodes, Set<Edge> edges) throws IOException {
        writer.write(String.format("\tsubgraph cluster%d {", numGraphs));
        writer.newLine();
        for (Node n : nodes) {
            if (n.equals(root)) {
                writer.write(String.format("\t\t%s [shape=diamond, label=\"Root\"];", nodeNames.get(n)));
                singlePortNodes.add(n);
            } else {
                writer.write(String.format("\t\t%s [shape=record, label=\"%s\"];", nodeNames.get(n),
                                           nodeLabels.get(n)));
            }
            writer.newLine();
        }

        for (Edge e : edges) {
            String edgeSource;
            if (singlePortNodes.contains(e.getHead())) {
                edgeSource = nodeNames.get(e.getHead());
            } else {
                edgeSource = nodeNames.get(e.getHead()) + ":satisfaction";
            }
            String edgeDestination;
            if (singlePortNodes.contains(e.getTail())) {
                edgeDestination = nodeNames.get(e.getTail());
            } else {
                edgeDestination = nodeNames.get(e.getTail()) + ":desire";
            }

            writer.write(String.format("\t\t%s -> %s;", edgeSource, edgeDestination));
            writer.newLine();
        }
        writer.write(String.format("\t\tlabel = \"%s\";", label));
        writer.newLine();
        writer.write("\t}");
        writer.newLine();
        writer.newLine();
    }

    @Override
    public void finish() {
        try {
            writer.write("}");
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to write to GraphViz file", e);
        }
    }

    private static String shortenClassName(String name) {
        String[] words = name.split(" ");
        String fullClassName = words[words.length - 1];
        String[] path = fullClassName.split("\\.");
        int i = 0;
        while (!Character.isUpperCase(path[i + 1].charAt(0))) {
            path[i] = path[i].substring(0, 1);
            i++;
        }
        return StringUtils.join(path, ".");
    }
}
