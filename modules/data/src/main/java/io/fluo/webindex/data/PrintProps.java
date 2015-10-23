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

import java.io.File;
import java.util.Iterator;

import io.fluo.api.config.FluoConfiguration;
import io.fluo.api.config.ObserverConfiguration;
import io.fluo.recipes.accumulo.export.AccumuloExporter;
import io.fluo.recipes.accumulo.export.TableInfo;
import io.fluo.recipes.export.ExportQueue;
import io.fluo.recipes.serialization.KryoSimplerSerializer;
import io.fluo.webindex.core.DataConfig;
import io.fluo.webindex.data.fluo.DomainMap;
import io.fluo.webindex.data.fluo.PageObserver;
import io.fluo.webindex.data.fluo.UriMap;
import io.fluo.webindex.data.recipes.Transmutable;
import io.fluo.webindex.data.recipes.TransmutableExporter;
import io.fluo.webindex.serialization.WebindexKryoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrintProps {

  private static final Logger log = LoggerFactory.getLogger(PrintProps.class);

  public static void configureApplication(FluoConfiguration appConfig, TableInfo exportTable,
      int numExportBuckets) {

    appConfig.addObserver(new ObserverConfiguration(PageObserver.class.getName()));

    KryoSimplerSerializer.setKryoFactory(appConfig, WebindexKryoFactory.class);

    UriMap.configure(appConfig, numExportBuckets);
    DomainMap.configure(appConfig, numExportBuckets);

    ExportQueue.configure(
        appConfig,
        new ExportQueue.Options("ileq", TransmutableExporter.class.getName(), String.class
            .getName(), Transmutable.class.getName(), 5));

    AccumuloExporter.setExportTableInfo(appConfig.getAppConfiguration(), "ileq", exportTable);
  }

  public static void main(String[] args) {

    if (args.length != 1) {
      log.error("Usage: Init <dataConfigPath>");
      System.exit(1);
    }
    DataConfig dataConfig = DataConfig.load(args[0]);
    FluoConfiguration fluoConfig = new FluoConfiguration(new File(dataConfig.getFluoPropsPath()));

    FluoConfiguration appConfig = new FluoConfiguration();

    configureApplication(appConfig,
        new TableInfo(fluoConfig.getAccumuloInstance(), fluoConfig.getAccumuloZookeepers(),
            fluoConfig.getAccumuloUser(), fluoConfig.getAccumuloPassword(),
            dataConfig.accumuloIndexTable), 13);

    Iterator<String> iter = appConfig.getKeys();

    while (iter.hasNext()) {
      String key = iter.next();
      System.out.println(key + " = " + appConfig.getProperty(key));
    }
  }
}
