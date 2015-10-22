package io.fluo.webindex.data.fluo_cfm;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
import io.fluo.webindex.core.DataUtil;
import io.fluo.webindex.data.recipes.Transmutable;
import io.fluo.webindex.data.util.LinkUtil;

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
  public static class UriCombiner implements Combiner<String, UriInfo, UriInfo> {
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
   * Observes uri map updates and adds those updates to an export queue.
   */
  public static class UriUpdateObserver extends UpdateObserver<String, UriInfo> {

    private ExportQueue<String, Transmutable<String>> exportQ;
    private CollisionFreeMap<String, Long, Long> domainMap;

    @Override
    public void init(String mapId, Context observerContext) throws Exception {
      //TODO constant
      exportQ = ExportQueue.getInstance("ileq", observerContext.getAppConfiguration());
      domainMap = CollisionFreeMap.getInstance(DomainMap.DOMAIN_MAP_ID, observerContext.getAppConfiguration());
    }

    @Override
    public void updatingValues(TransactionBase tx, Iterator<Update<String, UriInfo>> updates) {
      Map<String, Long> domainUpdates = new HashMap<>();

      while (updates.hasNext()) {
        Update<String, UriInfo> update = updates.next();

        UriInfo oldVal = update.getOldValue().or(new UriInfo(0, 0));
        UriInfo newVal = update.getNewValue().or(new UriInfo(0, 0));

        System.out.println("adding export "+update.getKey()+" "+oldVal.linksTo+" "+newVal.linksTo);
        exportQ.add(tx, update.getKey(), new UriCountExport(oldVal.linksTo, newVal.linksTo));

        String pageDomain = getDomain(update.getKey());
        domainUpdates.merge(pageDomain, newVal.linksTo + newVal.docs, (o,n) -> o+n);
      }

      domainMap.update(tx, domainUpdates);
    }

    private String getDomain(String uri) {
      try {
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
    CollisionFreeMap.configure(config, new Options(URI_MAP_ID, UriCombiner.class, UriUpdateObserver.class, String.class, UriInfo.class, UriInfo.class, numBuckets));
  }
}
