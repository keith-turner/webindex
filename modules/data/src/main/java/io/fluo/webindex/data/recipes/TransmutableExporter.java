package io.fluo.webindex.data.recipes;

import java.util.Collection;

import io.fluo.recipes.accumulo.export.AccumuloExporter;
import org.apache.accumulo.core.data.Mutation;

public class TransmutableExporter<K> extends AccumuloExporter<K, Transmutable<K>> {
  @Override
  protected Collection<Mutation> convert(K key, long seq, Transmutable<K> value) {
    return value.toMutations(key, seq);
  }
}