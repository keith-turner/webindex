package io.fluo.webindex.data.fluo;

import java.util.Collection;
import java.util.Collections;

import io.fluo.webindex.core.Constants;
import io.fluo.webindex.data.recipes.Transmutable;
import org.apache.accumulo.core.data.Mutation;

public class DomainExport implements Transmutable<String> {
  private long count;

  public DomainExport() {}

  public DomainExport(long c) {
    this.count = c;
  }

  @Override
  public Collection<Mutation> toMutations(String domain, long seq) {
    Mutation m = new Mutation("d:" + domain);
    if (count == 0) {
      m.putDelete(Constants.DOMAIN, Constants.PAGECOUNT, seq);
    } else {
      m.put(Constants.DOMAIN, Constants.PAGECOUNT, seq, count + "");
    }
    return Collections.singleton(m);
  }
}