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

import java.util.Collection;

import org.apache.accumulo.core.data.Mutation;
import org.apache.fluo.api.metrics.Meter;
import org.apache.fluo.api.metrics.MetricsReporter;
import org.apache.fluo.recipes.accumulo.export.AccumuloExporter;
import org.apache.fluo.recipes.core.export.Exporter;
import org.apache.fluo.recipes.core.export.SequencedExport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webindex.core.IndexClient;
import webindex.core.models.export.DomainUpdate;
import webindex.core.models.export.IndexUpdate;
import webindex.core.models.export.PageUpdate;
import webindex.core.models.export.UriUpdate;

public class IndexExporter extends AccumuloExporter<String, IndexUpdate> {

  private static final Logger log = LoggerFactory.getLogger(IndexExporter.class);

  private Meter pagesExported;
  private Meter linksExported;
  private Meter domainsExported;

  @Override
  public void init(Exporter.Context context) throws Exception {
    super.init(context);
    MetricsReporter reporter = context.getObserverContext().getMetricsReporter();
    pagesExported = reporter.meter("webindex_pages_exported");
    linksExported = reporter.meter("webindex_links_exported");
    domainsExported = reporter.meter("webindex_domains_exported");
  }

  @Override
  protected Collection<Mutation> translate(SequencedExport<String, IndexUpdate> export) {
    if (export.getValue() instanceof DomainUpdate) {
      domainsExported.mark();
      return IndexClient.genDomainMutations((DomainUpdate) export.getValue(), export.getSequence());
    } else if (export.getValue() instanceof PageUpdate) {
      pagesExported.mark();
      return IndexClient.genPageMutations((PageUpdate) export.getValue(), export.getSequence());
    } else if (export.getValue() instanceof UriUpdate) {
      linksExported.mark();
      return IndexClient.genUriMutations((UriUpdate) export.getValue(), export.getSequence());
    }

    String msg =
        "An object with an IndexUpdate class (" + export.getValue().getClass().toString()
            + ") was placed on the export queue";
    log.error(msg);
    throw new IllegalStateException(msg);
  }
}
