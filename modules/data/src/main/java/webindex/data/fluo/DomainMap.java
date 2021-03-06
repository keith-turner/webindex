/*
 * Copyright 2015 Webindex authors (see AUTHORS)
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

package webindex.data.fluo;

import java.util.Iterator;
import java.util.Optional;

import org.apache.fluo.api.client.TransactionBase;
import org.apache.fluo.api.config.FluoConfiguration;
import org.apache.fluo.api.metrics.Meter;
import org.apache.fluo.api.observer.Observer.Context;
import org.apache.fluo.recipes.core.export.ExportQueue;
import org.apache.fluo.recipes.core.map.CollisionFreeMap;
import org.apache.fluo.recipes.core.map.CollisionFreeMap.Options;
import org.apache.fluo.recipes.core.map.Combiner;
import org.apache.fluo.recipes.core.map.Update;
import org.apache.fluo.recipes.core.map.UpdateObserver;
import webindex.core.models.export.DomainUpdate;
import webindex.core.models.export.IndexUpdate;
import webindex.data.FluoApp;

public class DomainMap {

  public static final String DOMAIN_MAP_ID = "dm";

  /**
   * Combines updates made to the domain map
   */
  public static class DomainCombiner implements Combiner<String, Long> {
    @Override
    public Optional<Long> combine(String key, Iterator<Long> updates) {
      long l = 0;

      while (updates.hasNext()) {
        l += updates.next();
      }

      if (l == 0) {
        // returning absent will delete the map entry
        return Optional.empty();
      } else {
        return Optional.of(l);
      }
    }
  }

  /**
   * Observes domain map updates and adds those updates to an export queue.
   */
  public static class DomainUpdateObserver extends UpdateObserver<String, Long> {

    private ExportQueue<String, IndexUpdate> exportQ;
    private Meter domainsNew;
    private Meter domainsChanged;

    @Override
    public void init(String mapId, Context observerContext) throws Exception {
      exportQ =
          ExportQueue.getInstance(FluoApp.EXPORT_QUEUE_ID, observerContext.getAppConfiguration());
      domainsNew = observerContext.getMetricsReporter().meter("webindex_domains_new");
      domainsChanged = observerContext.getMetricsReporter().meter("webindex_domains_changed");
    }

    @Override
    public void updatingValues(TransactionBase tx, Iterator<Update<String, Long>> updates) {
      while (updates.hasNext()) {
        Update<String, Long> update = updates.next();
        String domain = update.getKey();
        Long oldVal = update.getOldValue().orElse(0L);
        Long newVal = update.getNewValue().orElse(0L);
        if (oldVal == 0L && newVal > 0L) {
          domainsNew.mark();
        }
        exportQ.add(tx, domain, new DomainUpdate(domain, oldVal, newVal));
        domainsChanged.mark();
      }
    }
  }

  /**
   * A helper method for configuring the domain map before initializing Fluo.
   */
  public static void configure(FluoConfiguration config, int numBuckets, int numTablets) {
    CollisionFreeMap.configure(config, new Options(DOMAIN_MAP_ID, DomainCombiner.class,
        DomainUpdateObserver.class, String.class, Long.class, numBuckets)
        .setBucketsPerTablet(numBuckets / numTablets));
  }
}
