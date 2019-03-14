package present.rpc;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  public static <T> T create(String url, Class<T> serviceType, RpcInterceptor interceptor) {
    return create(RpcEncoding.PROTO, url, serviceType, interceptor);
  }

  /**
   * Creates a client proxy that can invoke a remote service at the given URL.
   */
  public static <T> T create(RpcEncoding encoding, String url, Class<T> serviceType,
      RpcInterceptor interceptor) {
    return serviceType.cast(Proxy.newProxyInstance(serviceType.getClassLoader(),
        new Class<?>[] {serviceType}, new Handler(encoding, url, serviceType, interceptor)));
  }

  private static class Handler implements InvocationHandler {

    private final RpcEncoding encoding;
    private final String url;
    private final String serviceName;
    private final RpcInterceptor interceptor;
    private final Map<String, RpcMethod> methods;
    private final MediaType mediaType;

    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
        .addNetworkInterceptor(new GzipVerificationInterceptor())
        .readTimeout(30, TimeUnit.SECONDS)
        .build();

    private Handler(RpcEncoding encoding, String url, Class<?> serviceType,
        RpcInterceptor interceptor) {
      this.encoding = encoding;
      this.url = url;
      this.interceptor = interceptor == null ? RpcInterceptor.NOOP : interceptor;
      methods = RpcMethod.mapFor(serviceType);
      this.serviceName = serviceType.getSimpleName();
      this.mediaType = MediaType.parse(encoding.contentType);
    }

    @Override public Object invoke(final Object proxy, final Method javaMethod, Object[] args)
        throws Throwable {
      String methodName = javaMethod.getName();
      final RpcMethod method = methods.get(methodName);
      final Object argument = args[0];
      final String url = this.url + "/" + serviceName + "/" + methodName;
      final ClientHeaders headers = new ClientHeaders();
      final RpcInvocation invocation = new RpcInvocation(headers, method, args[0], null) {
        @Override public Object proceed() throws Exception {
          String traceId = UUID.randomUUID().toString().replace("-", "");
          RequestBody requestBody = RequestBody.create(mediaType, encoding.encode(argument));
          Request.Builder builder = new Request.Builder().url(url)
              .method("POST", requestBody)
              // Putting gzip in the agent prompts Google to gzip responses.
              .addHeader("User-Agent", "Present RpcClient (gzip)")
              .addHeader("X-Cloud-Trace-Context", traceId + "/0;o=1");
          for (Map.Entry<String, String> entry : headers.headers.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
          }
          Request request = builder.build();
          okhttp3.Response response = httpClient.newCall(request).execute();
          int code = response.code();
          if (code >= 400 && code < 500) {
            throw new ClientException(response.message());
          }
          if (code >= 500) throw new ServerException(response.message());
          // assert code == 200
          ResponseBody responseBody = response.body();
          if (responseBody == null) throw new RuntimeException("Missing response body.");
          return encoding.decode(method.resultType(), responseBody.byteStream());
        }
      };

      return this.interceptor.intercept(invocation);
    }
  }

  private static class GzipVerificationInterceptor implements Interceptor {
    @Override public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
      okhttp3.Response response = chain.proceed(chain.request());
      if (!"gzip".equals(response.header("Content-Encoding"))) {
        logger.info("Response wasn't gzipped.");
      }
      return response;
    }
  }

  private static class ClientHeaders implements RpcHeaders {

    // TODO: Make this case-insensitive?
    private final Map<String, String> headers = new HashMap<>();

    @Override public void put(String name, String value) {
      headers.put(name, value);
    }

    @Override public String get(String name) {
      return headers.get(name);
    }
  }
}
