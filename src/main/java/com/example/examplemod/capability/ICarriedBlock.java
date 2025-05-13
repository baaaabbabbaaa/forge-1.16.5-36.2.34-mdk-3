package com.example.examplemod.capability;

public interface ICarriedBlock {
    CarriedBlockData getData();

    default boolean hasBlock() {
        return getData().hasBlock();
    }

    default void clear() {
        getData().clear();
    }
}
