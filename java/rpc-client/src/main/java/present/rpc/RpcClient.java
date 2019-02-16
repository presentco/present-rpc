package present.rpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.WireTypeAdapterFactory;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Creates clients for services.
 *
 * @author Bob Lee (bob@present.co)
 */
public class RpcClient {

  private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

  /**
   * Creates a client proxy that can invoke a remote service at the given URL.
   */
  public static <T> T create(String url, Class<?> headerType, Class<T> serviceType,
      RpcFilter filter) {
    return create(RpcProtocol.PROTO, url, headerType, serviceType, filter);
  }

  /**
   * Creates a client proxy that can invoke a remote service at the given URL.
   */
  public static <T> T create(RpcProtocol protocol, String url, Class<?> headerType,
      Class<T> serviceType, RpcFilter filter) {
    Handler handler;
    switch (protocol) {
      case PROTO:
        handler = new ProtoHandler(url, headerType, serviceType, filter);
        break;
      case JSON:
        handler = new JsonHandler(url, headerType, serviceType, filter);
        break;
      default:
        throw new AssertionError();
    }
    return serviceType.cast(Proxy.newProxyInstance(serviceType.getClassLoader(),
        new Class<?>[] {serviceType}, handler));
  }

  private static abstract class Handler implements InvocationHandler {

    private final String url;
    private final Class<?> headerType;
    private final String serviceName;
    private final RpcFilter filter;
    private final Map<String, RpcMethod> methods;
    private final MediaType mediaType = MediaType.parse(contentType());

    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
        .addNetworkInterceptor(new GzipVerificationInterceptor())
        .readTimeout(30, TimeUnit.SECONDS)
        .build();

    private Handler(String url, Class<?> headerType, Class<?> serviceType, RpcFilter filter) {
      this.url = url;
      this.headerType = headerType;
      this.filter = filter == null ? RpcFilter.NOOP : filter;
      methods = RpcMethod.mapFor(serviceType);
      this.serviceName = serviceType.getSimpleName();
    }

    @Override public Object invoke(Object proxy, Method javaMethod, Object[] args)
        throws Throwable {
      final String methodName = javaMethod.getName();
      final RpcMethod method = methods.get(methodName);
      final Object argument = args[0];
      final String url = this.url + "/" + serviceName + "/" + methodName;

      final RpcInvocation invocation = new RpcInvocation(headerType, method, null, args[0], null) {
        @Override public Object proceed() throws Exception {
          String traceId = UUID.randomUUID().toString().replace("-", "");
          RequestBody requestBody = RequestBody.create(mediaType, encodeRequest(getHeader(), argument));
          okhttp3.Request request = new okhttp3.Request.Builder()
              .url(url)
              .addHeader("User-Agent", "Present RpcClient (gzip)")
              .addHeader("X-Cloud-Trace-Context", traceId + "/0;o=1")
              .method("POST", requestBody)
              .build();
          okhttp3.Response response = httpClient.newCall(request).execute();
          ResponseBody responseBody = response.body();
          if (responseBody == null) throw new ServerException("Missing response body.");
          return decodeResult(responseBody.bytes(), method.resultType());
        }
      };

      return this.filter.filter(invocation);
    }

    abstract String contentType();

    abstract byte[] encodeRequest(Object header, Object argument);

    abstract Object decodeResult(byte[] body, Class<?> resultType) throws IOException;

    void handle(Error error) {
      if (error != null) {
        switch (error.type) {
          case CLIENT: throw new ClientException(error.message);
          case SERVER: throw new ServerException(error.message);
        }
        throw new AssertionError();
      }
    }
  }

  private static class ProtoHandler extends Handler {
    private ProtoHandler(String url, Class<?> headerType, Class<?> serviceType, RpcFilter filter) {
      super(url, headerType, serviceType, filter);
    }

    @Override String contentType() {
      return RpcProtocol.PROTO.contentType;
    }

    byte[] encodeRequest(Object header, Object argument) {
      ByteString argumentBytes = ByteString.of(((Message) argument).encode());
      ByteString headerBytes = ByteString.of(((Message) header).encode());
      Request request = new Request(headerBytes, argumentBytes);
      return request.encode();
    }

    Object decodeResult(byte[] responseBody, Class<?> resultType) throws IOException {
      Response response = Response.ADAPTER.decode(responseBody);
      handle(response.error);
      return ProtoAdapter.get(resultType).decode(response.resultBytes);
    }
  }

  private static class JsonHandler extends Handler {

    private static Gson gson = new GsonBuilder().registerTypeAdapterFactory(
        new WireTypeAdapterFactory()).create();

    private JsonHandler(String url, Class<?> headerType, Class<?> serviceType, RpcFilter filter) {
      super(url, headerType, serviceType, filter);
    }

    @Override String contentType() {
      return RpcProtocol.JSON.contentType;
    }

    byte[] encodeRequest(Object header, Object argument) {
      Object request = new JsonRequest(gson.toJsonTree(header), gson.toJsonTree(argument));
      return gson.toJson(request).getBytes(UTF_8);
    }

    Object decodeResult(byte[] responseBody, Class<?> resultType) throws IOException {
      JsonResponse response = gson.fromJson(new String(responseBody, UTF_8), JsonResponse.class);
      handle(response.error);
      return gson.fromJson(response.result, resultType);
    }
  }

  private static class GzipVerificationInterceptor implements Interceptor {
    @Override public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
      okhttp3.Response response = chain.proceed(chain.request());
      if (!"gzip".equals(response.header("Content-Encoding"))) {
        // logger.warn("Response wasn't gzipped.");
      }
      return response;
    }
  }
}
