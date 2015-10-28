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

package io.fluo.webindex.data.spark;

import java.io.Serializable;

import io.fluo.recipes.serialization.SimpleSerializer;
import io.fluo.webindex.data.fluo.UriMap.UriInfo;
import org.apache.commons.configuration.Configuration;

class TestSerializer implements SimpleSerializer, Serializable {
  private static final long serialVersionUID = 1L;

  @Override
  public void init(Configuration appConfig) {
    // TODO Auto-generated method stub

  }

  @Override
  public <T> byte[] serialize(T obj) {
    switch (obj.getClass().getSimpleName()) {
      case "Long":
      case "String":
        return obj.toString().getBytes();
      case "UriInfo":
        UriInfo ui = (UriInfo) obj;
        return (ui.linksTo + "," + ui.docs).getBytes();
      default:
        throw new IllegalArgumentException();
    }
  }

  @Override
  public <T> T deserialize(byte[] serObj, Class<T> clazz) {
    switch (clazz.getSimpleName()) {
      case "Long":
        return clazz.cast(Long.valueOf(new String(serObj)));
      case "String":
        return clazz.cast(new String(serObj));
      case "UriInfo":
        String[] sa = new String(serObj).split(",");
        return clazz.cast(new UriInfo(Long.parseLong(sa[0]), Integer.parseInt(sa[1])));
      default:
        throw new IllegalArgumentException();
    }
  }
}
