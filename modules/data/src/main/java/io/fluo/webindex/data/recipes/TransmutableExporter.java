/*
 * Copyright 2015 Fluo authors (see AUTHORS)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

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
