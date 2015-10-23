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

import java.util.SortedSet;

import org.apache.hadoop.io.Text;
import org.junit.Assert;
import org.junit.Test;

public class IndexEnvTest {

  @Test
  public void testGetSplits() throws Exception {
    SortedSet<Text> splits = IndexEnv.getDefaultSplits();
    Assert.assertEquals(97, splits.size());
    Assert.assertEquals(new Text("d:bz.vividracing"), splits.first());
    Assert.assertEquals(new Text("t:fefeff:do.dig/buk.edu.ng"), splits.last());
  }
}
