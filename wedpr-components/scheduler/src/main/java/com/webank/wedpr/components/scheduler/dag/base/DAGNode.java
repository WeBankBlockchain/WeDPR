package com.webank.wedpr.components.scheduler.dag.base;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class DAGNode<T> {

    private List<DAGNode<T>> parents;
    private List<DAGNode<T>> children;
    private T object;

    protected DAGNode(T object) {
        this.object = object;
        parents = new LinkedList<>();
        children = new LinkedList<>();
    }

    /**
     * Performs Depth-first search on children and executes lambda on every node
     *
     * @param consumer lambda to be executed on nodes
     */
    public void visit(Consumer<DAGNode<T>> consumer) {
        Set<DAGNode<T>> visited = new HashSet<>();
        consumer.accept(this);
        for (DAGNode<T> node : children) {
            consumer.accept(node);
            node.getChildren().forEach(n -> n.visit(consumer, visited));
        }
    }

    void visit(Consumer<DAGNode<T>> consumer, Set<DAGNode<T>> visited) {
        consumer.accept(this);
        visited.add(this);
        for (DAGNode<T> node : children) {
            if (visited.contains(node)) {
                continue;
            }
            node.visit(consumer, visited);
        }
    }

    public T getObject() {
        return object;
    }

    List<DAGNode<T>> getParents() {
        return parents;
    }

    List<DAGNode<T>> getChildren() {
        return children;
    }

    public void addParents(DAGNode<T>... par) {
        for (DAGNode<T> n : par) {
            addParent(n);
        }
    }

    public void addParent(DAGNode<T> parent) {
        if (parent == this) {
            throw new CycleFoundException(this.toString() + "->" + this.toString());
        }
        parents.add(parent);
        if (parent.getChildren().contains(this)) {
            return;
        }
        parent.addChild(this);
    }

    public void addChild(DAGNode<T> child) {
        if (child == this) {
            throw new CycleFoundException(this.toString() + "->" + this.toString());
        }
        children.add(child);
        if (child.getParents().contains(this)) {
            return;
        }
        child.addParent(this);
    }

    @Override
    public String toString() {
        return "Node{"
                + "object="
                + object.toString()
                +
                //                ", parents=" + parents.size() +
                //                ", children=" + children.size() +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DAGNode) {
            return object.equals(((DAGNode<T>) obj).getObject());
        } else {
            return false;
        }
    }
}
