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

import io.fluo.api.data.Bytes;
import io.fluo.api.data.Column;
import io.fluo.api.data.RowColumn;
import scala.Tuple2;

public class Hex {
  public static void encNonAscii(StringBuilder sb, Bytes bytes) {
    for (int i = 0; i < bytes.length(); i++) {
      byte b = bytes.byteAt(i);
      if (b >= 32 && b <= 126) {
        sb.append((char) b);
      } else {
        sb.append(String.format("\\x%02x", b & 0xff));
      }
    }
  }

  public static String encNonAscii(Bytes bytes) {
    StringBuilder sb = new StringBuilder();
    encNonAscii(sb, bytes);
    return sb.toString();
  }

  public static void encNonAscii(StringBuilder sb, Column c, String sep) {
    encNonAscii(sb, c.getFamily());
    sb.append(sep);
    encNonAscii(sb, c.getQualifier());
  }

  public static void encNonAscii(StringBuilder sb, RowColumn rc, String sep) {
    encNonAscii(sb, rc.getRow());
    sb.append(sep);
    encNonAscii(sb, rc.getColumn(), sep);
  }

  public static String encNonAscii(Tuple2<RowColumn, Bytes> t, String sep) {
    StringBuilder sb = new StringBuilder();
    encNonAscii(sb, t._1(), sep);
    sb.append(sep);
    encNonAscii(sb, t._2());
    return sb.toString();
  }
}
