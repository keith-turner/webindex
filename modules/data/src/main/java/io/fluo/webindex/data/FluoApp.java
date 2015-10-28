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

package io.fluo.webindex.data;

import io.fluo.api.config.FluoConfiguration;
import io.fluo.api.config.ObserverConfiguration;
import io.fluo.recipes.accumulo.export.AccumuloExporter;
import io.fluo.recipes.accumulo.export.TableInfo;
import io.fluo.recipes.export.ExportQueue;
import io.fluo.recipes.serialization.KryoSimplerSerializer;
import io.fluo.webindex.data.fluo.DomainMap;
import io.fluo.webindex.data.fluo.PageObserver;
import io.fluo.webindex.data.fluo.UriMap;
import io.fluo.webindex.data.recipes.Transmutable;
import io.fluo.webindex.data.recipes.TransmutableExporter;
import io.fluo.webindex.serialization.WebindexKryoFactory;

public class FluoApp {

  public static int NUM_BUCKETS = 119;

  public static void configureApplication(FluoConfiguration appConfig, TableInfo exportTable,
      int numMapBuckets) {

    appConfig.addObserver(new ObserverConfiguration(PageObserver.class.getName()));

    KryoSimplerSerializer.setKryoFactory(appConfig, WebindexKryoFactory.class);

    UriMap.configure(appConfig, numMapBuckets);
    DomainMap.configure(appConfig, numMapBuckets);

    ExportQueue.configure(
        appConfig,
        new ExportQueue.Options("ileq", TransmutableExporter.class.getName(), String.class
            .getName(), Transmutable.class.getName(), numMapBuckets));

    AccumuloExporter.setExportTableInfo(appConfig.getAppConfiguration(), "ileq", exportTable);
  }

}
