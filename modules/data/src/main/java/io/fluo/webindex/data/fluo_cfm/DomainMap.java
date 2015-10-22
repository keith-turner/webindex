package io.fluo.webindex.data.fluo_cfm;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.google.common.base.Optional;
import io.fluo.api.client.TransactionBase;
import io.fluo.api.config.FluoConfiguration;
import io.fluo.api.observer.Observer.Context;
import io.fluo.recipes.export.ExportQueue;
import io.fluo.recipes.map.CollisionFreeMap;
import io.fluo.recipes.map.CollisionFreeMap.Options;
import io.fluo.recipes.map.Combiner;
import io.fluo.recipes.map.Update;
import io.fluo.recipes.map.UpdateObserver;
import io.fluo.webindex.data.recipes.Transmutable;
import org.apache.accumulo.core.data.Mutation;

public class DomainMap {
  public static final String DOMAIN_MAP_ID = "dm";

  /**
   * Combines updates made to the uri map
   */
  public static class DomainCombiner implements Combiner<String, Long, Long> {
    @Override
    public Long combine(String key, Optional<Long> currentValue, Iterator<Long> updates) {
      long l = currentValue.or(0L);

      while (updates.hasNext()) {
        l += updates.next();
      }

      return l;
    }
  }

  /**
   * Observes uri map updates and adds those updates to an export queue.
   */
  public static class DomainUpdateObserver extends UpdateObserver<String, Long> {

    private ExportQueue<String, Transmutable<String>> exportQ;

    @Override
    public void init(String mapId, Context observerContext) throws Exception {
      //TODO constant
      exportQ = ExportQueue.getInstance("ileq", observerContext.getAppConfiguration());
    }

    @Override
    public void updatingValues(TransactionBase tx, Iterator<Update<String, Long>> updates) {
      while (updates.hasNext()) {
        Update<String, Long> update = updates.next();
        exportQ.add(tx, update.getKey(), new DomainExport(update.getNewValue().or(0L)));
      }
    }
  }

  public static class DomainExport implements Transmutable<String> {
    private long count;

    public DomainExport(){}

    public DomainExport(long c){
      this.count = c;
    }

    @Override
    public Collection<Mutation> toMutations(String domain, long seq) {
      Mutation m = new Mutation("d:"+domain);
      //TODO literals
      m.put("domain", "pagecount", count+"");
      return Collections.singleton(m);
    }
  }

  /**
   * A helper method for configuring the uri map before initializing Fluo.
   */
  public static void configure(FluoConfiguration config, int numBuckets) {
    CollisionFreeMap.configure(config, new Options(DOMAIN_MAP_ID, DomainCombiner.class, DomainUpdateObserver.class, String.class, Long.class, Long.class, numBuckets));
  }
}
