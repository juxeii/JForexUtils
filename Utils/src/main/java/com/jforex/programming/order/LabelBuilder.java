package com.jforex.programming.order;

@FunctionalInterface
public interface LabelBuilder {

    abstract String createUnique(String body);
}
