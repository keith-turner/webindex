package webindex.data.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;
import java.util.stream.StreamSupport;

import org.archive.io.ArchiveRecord;
import org.archive.io.warc.WARCReader;
import org.archive.io.warc.WARCReaderFactory;

import webindex.core.models.Link;
import webindex.core.models.Page;

public class StatsGen {

  private static Stream<String> p2us(Page page) {
    Builder<String> builder = Stream.builder();
    if (page.getUri() != null) {
      builder.accept(page.getUrl().length() + " " + page.getUri().length());
    }

    Set<Link> links = page.getOutboundLinks();
    for (Link link : links) {
      builder.accept(link.getUrl().length() + " " + link.getUri().length());
    }

    return builder.build();
  }

  public static void main(String[] args) throws Exception {
    File input = new File(args[0]);
    File output = new File(args[1]);

    try (WARCReader ar = WARCReaderFactory.get(input); BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)))) {

      Iterator<ArchiveRecord> iter = ar.iterator();
      Iterable<ArchiveRecord> iterable = () -> iter;
      Stream<ArchiveRecord> targetStream = StreamSupport.stream(iterable.spliterator(), true);

      targetStream.map(ArchiveUtil::buildPageIgnoreErrors).flatMap(page -> p2us(page)).forEach(line -> {
        try {
          out.write(line);
          out.newLine();
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });
    }
  }
}
