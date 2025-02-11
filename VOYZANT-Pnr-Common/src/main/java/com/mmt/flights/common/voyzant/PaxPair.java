package com.mmt.flights.common.voyzant;

import java.util.Objects;

public class PaxPair<F, S> {
    F first;
    S second;

    public PaxPair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public F getFirst() {
        return this.first;
    }

    public S getSecond() {
        return this.second;
    }

    public void setSecond(S second) {
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaxPair<?, ?> pair = (PaxPair<?, ?>) o;
        return Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        return second.hashCode();
    }
}
