package io.fluo.webindex.data.fluo_cfm;

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

/**
 * This class contains code related to a CollisionFreeMap that keeps track of the count of information about URIs.
 */
public class UriMap {

  public static String URI_MAP_ID = "um";

  public static class UriInfo {
    //the numbers of documents that link to this URI
    long linksTo;

    //the number of documents with this URI.  Should be 0 or 1
    int docs;

    public UriInfo(){}

    public UriInfo(long linksTo, int docs) {
      this.linksTo = linksTo;
      this.docs = docs;
    }

    public void add(UriInfo other) {
      this.linksTo += other.linksTo;
      this.docs += other.docs;
    }

  }

  /**
   * Combines updates made to the uri map
   */
  public static class ImCombiner implements Combiner<String, UriInfo, UriInfo> {
    @Override
    public UriInfo combine(String key, Optional<UriInfo> currentValue, Iterator<UriInfo> updates) {

      UriInfo total = currentValue.or(new UriInfo(0, 0));

      while (updates.hasNext()) {
        total.add(updates.next());
      }

      //TODO when both 0, return null
      return total;
    }
  }

  /**
   * Observes in link map updates and adds those updates to an export queue.
   */
  public static class UpdateExporter extends UpdateObserver<String, UriInfo> {

    private ExportQueue<String, UriRefCountChange> inLinkExportQueue;

    @Override
    public void init(String mapId, Context observerContext) throws Exception {
      //TODO constant
      inLinkExportQueue = ExportQueue.getInstance("ileq", observerContext.getAppConfiguration());
    }

    @Override
    public void updatingValues(TransactionBase tx, Iterator<Update<String, UriInfo>> updates) {
      while (updates.hasNext()) {
        Update<String, UriInfo> update = updates.next();

        UriInfo oldVal = update.getOldValue().or(new UriInfo(0, 0));
        UriInfo newVal = update.getNewValue().or(new UriInfo(0, 0));

        inLinkExportQueue.add(tx, update.getKey(), new UriRefCountChange(oldVal.linksTo, newVal.linksTo));
      }
    }
  }

  /**
   * A helper method for configuring the in link map before initializing Fluo.
   */
  public static void configure(FluoConfiguration config, int numBuckets) {
    CollisionFreeMap.configure(config, new Options(URI_MAP_ID, ImCombiner.class, UpdateExporter.class, String.class, UriInfo.class, UriInfo.class, numBuckets));
  }
}
