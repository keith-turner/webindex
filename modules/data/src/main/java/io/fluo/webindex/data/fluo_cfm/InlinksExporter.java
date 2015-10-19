package io.fluo.webindex.data.fluo_cfm;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;

import io.fluo.recipes.accumulo.export.AccumuloExporter;
import io.fluo.webindex.core.Constants;
import io.fluo.webindex.core.DataUtil;
import io.fluo.webindex.data.spark.IndexUtil;
import io.fluo.webindex.data.util.LinkUtil;
import org.apache.accumulo.core.data.Mutation;

public class InlinksExporter extends AccumuloExporter<String, long[]> {

  @Override
  protected Collection<Mutation> convert(String uri, long seq, long[] value) {

    ArrayList<Mutation> mutations = new ArrayList<>(4);

    long prev = value[0];
    long curr = value[1];

    createTotalUpdates(mutations, uri, seq, prev, curr);
    mutations.add(createDomainUpdate(uri, seq, prev, curr));
    mutations.add(createPageUpdate(uri, seq, curr));

    return mutations;
  }

  private static String getDomainRow(String pageUri) {
    String pageDomain;
    try {
      pageDomain = LinkUtil.getReverseTopPrivate(DataUtil.toUrl(pageUri));
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
    return "d:" + pageDomain;
  }

  private static Mutation createDomainUpdate(String uri, long seq, long prev, long curr) {
    Mutation m = new Mutation(getDomainRow(uri));
    String cf = String.format("%s:%s", IndexUtil.revEncodeLong(prev), uri);
    m.putDelete(Constants.RANK.getBytes(), cf.getBytes(), seq);
    cf = String.format("%s:%s", IndexUtil.revEncodeLong(curr), uri);
    m.put(Constants.RANK.getBytes(), cf.getBytes(), seq, (""+curr).getBytes());
    return m;
  }

  private static Mutation createPageUpdate(String uri, long seq, long curr) {
    Mutation m = new Mutation("p:"+uri);
    //TODO constants for column
    m.put("page", "incount", ""+curr);
    return m;
  }

  private static String createTotalRow(String uri, long curr) {
    return String.format("t:%s:%s", IndexUtil.revEncodeLong(curr), uri);
  }

  private static void createTotalUpdates(ArrayList<Mutation> mutations, String uri, long seq, long prev,
      long curr) {
    Mutation m = new Mutation(createTotalRow(uri, prev));
    m.putDelete("", "", seq);
    mutations.add(m);

    m = new Mutation(createTotalRow(uri, curr));
    m.put("", "", "");
    mutations.add(m);
  }
}