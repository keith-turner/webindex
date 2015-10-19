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

package io.fluo.webindex.data.fluo_cfm;

import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import io.fluo.api.client.TransactionBase;
import io.fluo.api.data.Bytes;
import io.fluo.api.data.Column;
import io.fluo.api.observer.AbstractObserver;
import io.fluo.api.types.TypedTransactionBase;
import io.fluo.recipes.export.ExportQueue;
import io.fluo.recipes.map.CollisionFreeMap;
import io.fluo.recipes.transaction.RecordingTransactionBase;
import io.fluo.recipes.transaction.TxLog;
import io.fluo.webindex.core.DataUtil;
import io.fluo.webindex.core.models.Page;
import io.fluo.webindex.data.util.FluoConstants;
import io.fluo.webindex.data.util.LinkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PageObserver extends AbstractObserver {

  private static final Logger log = LoggerFactory.getLogger(PageObserver.class);
  private static final Gson gson = new Gson();
  private ExportQueue<Bytes, TxLog> exportQueue;

  private CollisionFreeMap<String, Long, Long> inLinkMap;

  @Override
  public void init(Context context) throws Exception {
    //exportQueue = ExportQueue.getInstance(IndexExporter.QUEUE_ID, context.getAppConfiguration());

    //TODO constant
    inLinkMap = CollisionFreeMap.getInstance("ilm", context.getAppConfiguration());
  }

  @Override
  public void process(TransactionBase tx, Bytes row, Column col) throws Exception {

    RecordingTransactionBase rtx = RecordingTransactionBase.wrap(tx, IndexExporter.getFilter());
    TypedTransactionBase ttx = FluoConstants.TYPEL.wrap(rtx);
    String nextJson = ttx.get().row(row).col(FluoConstants.PAGE_NEW_COL).toString("");
    if (nextJson.isEmpty()) {
      log.error("An empty page was set at row {} col {}", row.toString(), col.toString());
      return;
    }

    String curJson = ttx.get().row(row).col(FluoConstants.PAGE_CUR_COL).toString("");
    Set<Page.Link> curLinks = Collections.emptySet();
    if (!curJson.isEmpty()) {
      Page curPage = gson.fromJson(curJson, Page.class);
      curLinks = curPage.getOutboundLinks();
    }

    if (curJson.isEmpty() && !nextJson.equals("delete")) {
      Long incount = ttx.get().row(row).col(FluoConstants.PAGE_INCOUNT_COL).toLong();
      if (incount == null) {
        ttx.mutate().row(row).col(FluoConstants.PAGE_INCOUNT_COL).set(new Long(0));
      }
      updateDomainPageCount(ttx, row, 1);
    }

    Page nextPage;
    if (nextJson.equals("delete")) {
      ttx.mutate().row(row).col(FluoConstants.PAGE_CUR_COL).delete();
      ttx.get().row(row).col(FluoConstants.PAGE_INCOUNT_COL).toLong(); // get for indexing
      ttx.mutate().row(row).col(FluoConstants.PAGE_INCOUNT_COL).delete();
      updateDomainPageCount(ttx, row, -1);
      nextPage = Page.EMPTY;
    } else {
      ttx.mutate().row(row).col(FluoConstants.PAGE_CUR_COL).set(nextJson);
      nextPage = gson.fromJson(nextJson, Page.class);
    }

    Set<Page.Link> nextLinks = nextPage.getOutboundLinks();
    String pageUri = row.toString().substring(2);

    Map<String, Long> updates = new HashMap<>();

    Sets.SetView<Page.Link> addLinks = Sets.difference(nextLinks, curLinks);
    for (Page.Link link : addLinks) {
      updates.put(link.getUri(), 1L);
    }

    Sets.SetView<Page.Link> delLinks = Sets.difference(curLinks, nextLinks);
    for (Page.Link link : delLinks) {
      updates.put(link.getUri(), -1L);
    }

    inLinkMap.update(tx, updates);

    // clean up
    ttx.mutate().row(row).col(FluoConstants.PAGE_NEW_COL).delete();

    TxLog txLog = rtx.getTxLog();
    if (!txLog.isEmpty()) {
      //exportQueue.add(tx, row, txLog);
    }
  }

  @Override
  public ObservedColumn getObservedColumn() {
    return new ObservedColumn(FluoConstants.PAGE_NEW_COL, NotificationType.STRONG);
  }

  public static void updateDomainPageCount(TypedTransactionBase ttx, Bytes pageRow, long change) {
    Bytes domainRow = getDomainRow(pageRow);
    if (!domainRow.equals(Bytes.EMPTY)) {
      Long prevCount = ttx.get().row(domainRow).col(FluoConstants.PAGECOUNT_COL).toLong(0);
      Long curCount = prevCount + change;
      if (curCount == 0) {
        ttx.mutate().row(domainRow).col(FluoConstants.PAGECOUNT_COL).delete();
        log.debug("Deleted pagecount for {}", domainRow);
      } else {
        ttx.mutate().row(domainRow).col(FluoConstants.PAGECOUNT_COL).set(curCount);
        log.debug("Updated pagecount for {} from {} to {}", domainRow, prevCount, curCount);
      }
    }
  }

  public static Bytes getDomainRow(Bytes pageRow) {
    String pageUri = pageRow.toString().substring(2);
    try {
      String pageDomain = LinkUtil.getReverseTopPrivate(DataUtil.toUrl(pageUri));
      return Bytes.of("d:" + pageDomain);
    } catch (ParseException e) {
      return Bytes.EMPTY;
    }
  }
}
