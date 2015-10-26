package io.fluo.webindex.serialization;

import java.util.ArrayList;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.pool.KryoFactory;
import io.fluo.webindex.core.models.Page.Link;
import io.fluo.webindex.data.fluo.DomainExport;
import io.fluo.webindex.data.fluo.PageExport;
import io.fluo.webindex.data.fluo.UriCountExport;
import io.fluo.webindex.data.fluo.UriMap.UriInfo;

public class WebindexKryoFactory implements KryoFactory{

  @Override
  public Kryo create() {
    Kryo kryo = new Kryo();

    kryo.register(UriInfo.class);
    kryo.register(DomainExport.class);
    kryo.register(UriCountExport.class);
    kryo.register(PageExport.class);
    kryo.register(ArrayList.class);
    kryo.register(Link.class);

    kryo.setRegistrationRequired(true);

    return kryo;
  }

}
