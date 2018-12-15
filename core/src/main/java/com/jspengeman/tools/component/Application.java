package com.jspengeman.tools.component;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;
import com.jspengeman.tools.component.util.ObjectCreator;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * An {@link Application} is set of {@link Component}s whose
 * life cycle is managed based on the components dependencies.
 *
 *
 * @param <T>
 *      The type of configuration to use for components.
 */
public final class Application<T extends Configuration> {

    private final Configuration config;

    private final MutableGraph<String> dependencyGraph =
        GraphBuilder
            .directed()
            .allowsSelfLoops(false)
            .build();

    private final Map<String, Class> componentClassByName = new HashMap<>();

    public Application(Configuration config) {
        this.config = Verify.verifyNotNull(config, "config cannot be null.");
    }

    public com.jspengeman.tools.component.Application<T> component(String name, Class componentType) {
        Verify.verifyNotNull(name, "name cannot be null.");
        Verify.verifyNotNull(componentType, "componentType cannot be null.");

        componentClassByName.put(name, componentType);
        dependencyGraph.addNode(name);
        return this;
    }

    public com.jspengeman.tools.component.Application<T> component(String name,
                                                                   Class componentType,
                                                                   ImmutableList<String> dependencyNames) {
        Verify.verifyNotNull(name, "name cannot be null.");
        Verify.verifyNotNull(componentType, "componentType cannot be null.");
        Verify.verifyNotNull(dependencyNames, "dependencyNames cannot be null.");

        componentClassByName.put(name, componentType);
        for (String dependencyName : dependencyNames) {
            try {
                dependencyGraph.putEdge(dependencyName, name);
            } catch (UnsupportedOperationException e) {
                throw new IllegalArgumentException("Dependency Loop Detected.");
            }
        }
        return this;
    }

    /**
     *
     * @return
     */
    public com.jspengeman.tools.component.Application<T> start() {
        ImmutableList<String> dependencyOrder =
            topologicalSort(dependencyGraph);
        Map<String, Component> constructedComponents = new HashMap<>();

        for (String componentName : dependencyOrder) {
            Class componentType = componentClassByName.get(componentName);

            ImmutableList<Component> dependencies =
                dependencyGraph.predecessors(componentName)
                    .stream()
                    .map(constructedComponents::get)
                    .collect(ImmutableList.toImmutableList());

            Component component =
                (Component) ObjectCreator.newInstance(
                    componentType,
                    ImmutableList
                        .builder()
                        .add(config)
                        .addAll(dependencies)
                        .build());

            constructedComponents.put(componentName, component);
        }

        for (String componentName : dependencyOrder) {
            if (constructedComponents.containsKey(componentName)) {
                constructedComponents
                    .get(componentName)
                    .start();
            } else {
                // throw error if it can't be started.
            }
        }

        return this;
    }

    /**
     *
     * @return
     */
    public com.jspengeman.tools.component.Application<T> stop() {
        return this;
    }

    public static <T> ImmutableList<T> topologicalSort(Graph<T> input) {
        MutableGraph<T> graph = Graphs.copyOf(input);
        ImmutableList.Builder<T> sorted = ImmutableList.builder();
        Queue<T> queue = new ArrayDeque<>();
        graph.nodes()
            .stream()
            .filter(node -> graph.inDegree(node) == 0)
            .forEach(queue::offer);

        while (!queue.isEmpty()) {
            T dependency = queue.poll();
            sorted.add(dependency);

            for (T dependent : graph.successors(dependency)) {
                graph.removeEdge(dependency, dependent);
                if (graph.inDegree(dependent) == 0) {
                    queue.offer(dependent);
                }
            }
        }

        return sorted.build();
    }

    public static void main(String[] args) {
        Application<Configuration> application = new Application<>(new Conf())
            .component("logger", Logger.class)
            .component(
                "database",
                Database.class,
                ImmutableList.of("logger"))
            .component(
                "scheduler",
                Scheduler.class,
                ImmutableList.of("logger"))
            .component(
                "app",
                App.class,
                ImmutableList.of("database", "scheduler", "logger"))
            .start();
    }

    public static class App implements Component {

        public App(Conf config, Scheduler d, Database s) {
            java.lang.System.out.println("Application Created.");
        }

        @Override
        public void start() {
            java.lang.System.out.println("Application Started.");
        }

        @Override
        public void stop() {
            java.lang.System.out.println("Application Stopped.");
        }
    }

    public static class Database implements Component {

        public Database(Conf config) {
            java.lang.System.out.println("Database Created.");
        }

        @Override
        public void start() {
            java.lang.System.out.println("Database Started.");
        }

        @Override
        public void stop() {
            java.lang.System.out.println("Database Stopped.");
        }
    }

    public static class Scheduler implements Component {

        public Scheduler(Conf config) {
            java.lang.System.out.println("Scheduler Created.");
        }

        @Override
        public void start() {
            java.lang.System.out.println("Scheduler Started.");
        }

        @Override
        public void stop() {
            java.lang.System.out.println("Scheduler Stopped.");
        }
    }

    public static class Logger implements Component {

        public Logger(Conf config) {
            java.lang.System.out.println("Logger Created.");
        }

        @Override
        public void start() {
            java.lang.System.out.println("Logger Started.");
        }

        @Override
        public void stop() {
            java.lang.System.out.println("Logger Stopped.");
        }
    }

    public static class Conf implements Configuration {}
}
