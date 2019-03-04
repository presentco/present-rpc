package present.rpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.WireTypeAdapterFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * Encoding used for RPC arguments and results.
 */
public enum RpcEncoding {

  JSON("application/json") {
    private final Gson gson = new GsonBuilder()
        //.setPrettyPrinting()
        .registerTypeAdapterFactory(new WireTypeAdapterFactory())
        .create();

    @Override Object decode(Class<?> type, InputStream in) {
      return gson.fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), type);
    }

    @Override void encode(Object o, OutputStream out) throws IOException {
      try (OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) {
        gson.toJson(o, writer);
      };
    }

    @Override byte[] encode(Object o) throws IOException {
      return gson.toJson(o).getBytes(StandardCharsets.UTF_8);
    }
  },

  PROTO("application/x-protobuf") {
    @Override Object decode(Class<?> type, InputStream in) throws IOException {
      return ProtoAdapter.get(type).decode(in);
    }

    @SuppressWarnings("unchecked")
    @Override void encode(Object o, OutputStream out) throws IOException {
      ProtoAdapter protoAdapter = ProtoAdapter.get(o.getClass());
      protoAdapter.encode(out, o);
    }

    @SuppressWarnings("unchecked")
    @Override byte[] encode(Object o) throws IOException {
      ProtoAdapter protoAdapter = ProtoAdapter.get(o.getClass());
      return protoAdapter.encode(o);
    }
  };

  public final String contentType;

  RpcEncoding(String contentType) {
    this.contentType = contentType;
  }

  abstract Object decode(Class<?> type, InputStream in) throws IOException;

  abstract void encode(Object o, OutputStream out) throws IOException;

  abstract byte[] encode(Object o) throws IOException;
}
