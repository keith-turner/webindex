package io.fluo.webindex.data.fluo_cfm;

import java.util.Iterator;

import io.fluo.api.client.TransactionBase;
import io.fluo.api.observer.Observer.Context;
import io.fluo.recipes.export.ExportQueue;
import io.fluo.recipes.map.Update;
import io.fluo.recipes.map.UpdateObserver;

public class InlinkUpdateObserver extends UpdateObserver<String, Long> {

  private ExportQueue<String, long[]> inLinkExportQueue;

  @Override
  public void init(String mapId, Context observerContext) throws Exception {
    //TODO constant
    inLinkExportQueue = ExportQueue.getInstance("ileq", observerContext.getAppConfiguration());
  }

  @Override
  public void updatingValues(TransactionBase tx, Iterator<Update<String, Long>> updates) {
    while (updates.hasNext()) {
      Update<String, Long> update = updates.next();

      inLinkExportQueue.add(tx, update.getKey(), new long[]{update.getOldValue().or(0L), update.getNewValue().or(0L)});
    }
  }
}
