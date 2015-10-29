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

package io.fluo.webindex.data.fluo;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.fluo.api.client.TransactionBase;
import io.fluo.api.config.FluoConfiguration;
import io.fluo.api.observer.Observer.Context;
import io.fluo.recipes.export.ExportQueue;
import io.fluo.recipes.map.CollisionFreeMap;
import io.fluo.recipes.map.CollisionFreeMap.Options;
import io.fluo.recipes.map.Combiner;
import io.fluo.recipes.map.Update;
import io.fluo.recipes.map.UpdateObserver;
import io.fluo.webindex.core.DataUtil;
import io.fluo.webindex.data.recipes.Transmutable;
import io.fluo.webindex.data.util.LinkUtil;

/**
 * This class contains code related to a CollisionFreeMap that keeps track of the count of
 * information about URIs.
 */
public class UriMap {

  public static String URI_MAP_ID = "um";

  public static class UriInfo {

    public static final UriInfo EMPTY = new UriInfo(0, 0);

    // the numbers of documents that link to this URI
    public long linksTo;

    // the number of documents with this URI. Should be 0 or 1
    public int docs;

    public UriInfo() {}

    public UriInfo(long linksTo, int docs) {
      this.linksTo = linksTo;
      this.docs = docs;
    }

    public void add(UriInfo other) {
      Preconditions.checkArgument(this != EMPTY);
      this.linksTo += other.linksTo;
      this.docs += other.docs;
    }

    @Override
    public String toString() {
      return linksTo + " " + docs;
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof UriInfo) {
        UriInfo oui = (UriInfo) o;
        return linksTo == oui.linksTo && docs == oui.docs;
      }

      return false;
    }
  }

  /**
   * Combines updates made to the uri map
   */
  public static class UriCombiner implements Combiner<String, UriInfo, UriInfo> {
    @Override
    public Optional<UriInfo> combine(String key, Optional<UriInfo> currentValue,
        Iterator<UriInfo> updates) {

      UriInfo total = currentValue.or(new UriInfo(0, 0));

      while (updates.hasNext()) {
        total.add(updates.next());
      }

      if (total.equals(new UriInfo(0, 0))) {
        return Optional.absent();
      } else {
        return Optional.of(total);
      }
    }
  }

  /**
   * Observes uri map updates and adds those updates to an export queue.
   */
  public static class UriUpdateObserver extends UpdateObserver<String, UriInfo> {

    private ExportQueue<String, Transmutable<String>> exportQ;
    private CollisionFreeMap<String, Long, Long> domainMap;

    @Override
    public void init(String mapId, Context observerContext) throws Exception {
      // TODO constant
      exportQ = ExportQueue.getInstance("ileq", observerContext.getAppConfiguration());
      domainMap =
          CollisionFreeMap.getInstance(DomainMap.DOMAIN_MAP_ID,
              observerContext.getAppConfiguration());
    }

    @Override
    public void updatingValues(TransactionBase tx, Iterator<Update<String, UriInfo>> updates) {
      Map<String, Long> domainUpdates = new HashMap<>();

      while (updates.hasNext()) {
        Update<String, UriInfo> update = updates.next();

        UriInfo oldVal = update.getOldValue().or(UriInfo.EMPTY);
        UriInfo newVal = update.getNewValue().or(UriInfo.EMPTY);

        exportQ.add(tx, update.getKey(), new UriCountExport(oldVal, newVal));

        String pageDomain = getDomain(update.getKey());
        long domainDelta = (newVal.linksTo + newVal.docs) - (oldVal.linksTo + oldVal.docs);
        domainUpdates.merge(pageDomain, domainDelta, (o, n) -> o + n);
      }

      domainMap.update(tx, domainUpdates);
    }

    private String getDomain(String uri) {
      try {
        // TODO does this need to throw exception????????
        return LinkUtil.getReverseTopPrivate(DataUtil.toUrl(uri));
      } catch (ParseException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * A helper method for configuring the uri map before initializing Fluo.
   */
  public static void configure(FluoConfiguration config, int numBuckets) {
    CollisionFreeMap.configure(config, new Options(URI_MAP_ID, UriCombiner.class,
        UriUpdateObserver.class, String.class, UriInfo.class, UriInfo.class, numBuckets));
  }
}
