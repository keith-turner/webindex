package io.fluo.webindex.data.recipes;

import java.util.Collection;

import org.apache.accumulo.core.data.Mutation;

//TODO move to recipes
public interface Transmutable<K> {
  Collection<Mutation> toMutations(K key, long seq);
}
