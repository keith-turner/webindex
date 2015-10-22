package io.fluo.webindex.data.fluo_cfm;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;

import io.fluo.webindex.core.Constants;
import io.fluo.webindex.core.DataUtil;
import io.fluo.webindex.data.recipes.Transmutable;
import io.fluo.webindex.data.spark.IndexUtil;
import io.fluo.webindex.data.util.LinkUtil;
import org.apache.accumulo.core.data.Mutation;

public class UriCountExport implements Transmutable<String> {
  public long prevCount = 0;
  public long newCount = 0;

  public UriCountExport(){}

  public UriCountExport(long prevCount, long newCount){
    this.prevCount = prevCount;
    this.newCount = newCount;
  }

  @Override
  public Collection<Mutation> toMutations(String uri, long seq) {
    ArrayList<Mutation> mutations = new ArrayList<>(4);

    createTotalUpdates(mutations, uri, seq, prevCount, newCount);
    mutations.add(createDomainUpdate(uri, seq, prevCount, newCount));
    mutations.add(createPageUpdate(uri, seq, newCount));

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
    //TODO screwy case when it does not exists... prev is 0 and initial val could be 0
    if(prev != curr) {
      String cf = String.format("%s:%s", IndexUtil.revEncodeLong(prev), uri);
      m.putDelete(Constants.RANK.getBytes(), cf.getBytes(), seq);
    }
    String cf = String.format("%s:%s", IndexUtil.revEncodeLong(curr), uri);
    m.put(Constants.RANK.getBytes(), cf.getBytes(), seq, (""+curr).getBytes());
    return m;
  }

  private static Mutation createPageUpdate(String uri, long seq, long curr) {
    Mutation m = new Mutation("p:"+uri);
    //TODO constants for column
    m.put("page", "incount", seq, ""+curr);
    return m;
  }

  private static String createTotalRow(String uri, long curr) {
    return String.format("t:%s:%s", IndexUtil.revEncodeLong(curr), uri);
  }

  private static void createTotalUpdates(ArrayList<Mutation> mutations, String uri, long seq, long prev,
      long curr) {
    Mutation m;

    //TODO screwy case when it does not exists... prev is 0 and initial val could be 0
    if(prev != curr) {
      m = new Mutation(createTotalRow(uri, prev));
      m.putDelete("", "", seq);
      mutations.add(m);
    }

    m = new Mutation(createTotalRow(uri, curr));
    m.put("", "", seq,  "");
    mutations.add(m);
  }
}