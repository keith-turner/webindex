package io.fluo.webindex.data.fluo_cfm;

import java.util.Iterator;

import com.google.common.base.Optional;
import io.fluo.recipes.map.Combiner;

public class IniinkCombiner implements Combiner<String, Long, Long> {

  @Override
  public Long combine(String key, Optional<Long> currentValue, Iterator<Long> updates) {
    long total = currentValue.or(0L);

    while (updates.hasNext()) {
      total += updates.next();
    }

    return total;
  }

}
