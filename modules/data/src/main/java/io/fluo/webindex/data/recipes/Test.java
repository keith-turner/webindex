package io.fluo.webindex.data.recipes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import io.fluo.api.data.Bytes;
import io.fluo.api.data.Column;
import io.fluo.api.data.RowColumn;
import io.fluo.recipes.serialization.SimpleSerializer;
import io.fluo.webindex.data.fluo_cfm.UriRefCountChange;

public class Test {
  public static class TW {
    Transmutable t;
  }

  static Kryo getKryo(){
    Kryo k = new Kryo();
    k.register(UriRefCountChange.class);
    k.register(RowColumn.class);
    k.register(Column.class);
    k.register(Bytes.of("").getClass());
    return k;
  }

  static class KryoSimplerSerializer implements SimpleSerializer {
    @Override
    public <T> byte[] serialize(T obj) {
      // TODO efficient object reuse (with thread safety)
      Kryo kryo = getKryo();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      Output output = new Output(baos);
      //kryo.writeObject(output, obj);
      kryo.writeClassAndObject(output, obj);
      output.close();
      return baos.toByteArray();
    }

    @Override
    public <T> T deserialize(byte[] serObj, Class<T> clazz) {
      Kryo kryo = getKryo();
      ByteArrayInputStream bais = new ByteArrayInputStream(serObj);
      Input input = new Input(bais);

      if (clazz.equals(Bytes.class)) {
        return (T)  kryo.readClassAndObject(input);
      }
      return kryo.readObject(input, clazz);
    }

  }

  public static void main(String[] args) {

    KryoSimplerSerializer kss = new KryoSimplerSerializer();

    byte[] sa = kss.serialize(new RowColumn("r1", new Column("cf","cq")));

    print(sa);
    //test1();
  }

  private static void test1() {
    KryoSimplerSerializer kss = new KryoSimplerSerializer();

    TW tw1 = new TW();

    tw1.t = new UriRefCountChange(9, 9);

    byte[] sa = kss.serialize(tw1);
    print(sa);


    kss = new KryoSimplerSerializer();

    TW tw2 = kss.deserialize(sa, TW.class);
  }

  private static void print(byte[] sa) {
    System.out.print(sa.length +" : ");

    for (byte b : sa) {
      if(b >=32 && b <= 126) {
        System.out.print((char)b);
      }else{
        System.out.print(".");
      }

    }

    System.out.println();

  }
}
