package org.averyocean65.swp;

public class Result<T> {
    public Result(boolean success, T value) {
        this.success = success;
        this.value = value;
    }

    public boolean success;
    public T value;
}
