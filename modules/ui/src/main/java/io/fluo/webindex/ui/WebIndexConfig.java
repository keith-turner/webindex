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

package io.fluo.webindex.ui;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.fluo.webindex.core.DataConfig;
import org.hibernate.validator.constraints.NotEmpty;

public class WebIndexConfig extends Configuration {

  @NotEmpty
  private String dataConfigPath;

  @JsonProperty
  public String getDataConfigPath() {
    return dataConfigPath;
  }

  @JsonProperty
  public void getDataConfigPath(String dataConfigPath) {
    this.dataConfigPath = dataConfigPath;
  }

  public DataConfig getDataConfig() {
    System.out.println(dataConfigPath);
    return DataConfig.load(dataConfigPath);
  }
}
