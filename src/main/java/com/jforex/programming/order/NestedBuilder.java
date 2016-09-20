package com.jforex.programming.order;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class NestedBuilder<T, V> {
    /**
     * To get the parent builder
     *
     * @return T the instance of the parent builder
     */
    public T done() {
        final Class<?> parentClass = parent.getClass();
        try {
            final V build = this.build();
            final String methodname = "with" + build.getClass().getSimpleName();
            final Method method = parentClass.getDeclaredMethod(methodname, build.getClass());
            method.invoke(parent, build);
        } catch (NoSuchMethodException
                | IllegalAccessException
                | InvocationTargetException e) {
            e.printStackTrace();
        }
        return parent;
    }

    public abstract V build();

    protected T parent;

    /**
     * @param parent
     * @return
     */
    @SuppressWarnings("unchecked")
    public <P extends NestedBuilder<T, V>> P withParentBuilder(final T parent) {
        this.parent = parent;
        return (P) this;
    }
}
