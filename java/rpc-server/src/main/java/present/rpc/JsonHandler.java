package present.rpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.squareup.wire.WireTypeAdapterFactory;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static present.rpc.Error.Type.CLIENT;
import static present.rpc.Error.Type.SERVER;

class JsonHandler implements RpcHandler {

  private static final boolean DEBUG = false;

  private static final Logger logger = LoggerFactory.getLogger(JsonHandler.class);

  private static Gson gson = new GsonBuilder()
      //.setPrettyPrinting()
      .registerTypeAdapterFactory(new WireTypeAdapterFactory())
      .create();

  @Override public void handle(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
      RpcService service, RpcMethod method) throws IOException {
    JsonResponse response = handle(httpRequest, service, method);
    try {
      if (response.error == null) {
        httpResponse.setStatus(200);
      } else {
        httpResponse.setStatus(response.error.type == CLIENT ? 400 : 500);
      }
      httpResponse.setContentType(RpcProtocol.JSON.contentType);
      try (OutputStreamWriter out = new OutputStreamWriter(
          httpResponse.getOutputStream(), StandardCharsets.UTF_8)) {
        gson.toJson(response, out);
        if (DEBUG) logger.info(gson.toJson(response));
      };
    } catch (IOException e) {
      logger.info("Error sending response to client.", e);
    }
  }

  private JsonResponse handle(HttpServletRequest servletRequest, final RpcService service,
      final RpcMethod method) throws IOException {
    final JsonRequest request;
    try {
      request = gson.fromJson(new InputStreamReader(servletRequest.getInputStream(),
          StandardCharsets.UTF_8), JsonRequest.class);
      if (DEBUG) logger.info(gson.toJson(request));
    } catch (IOException e) {
      logger.info("Invalid request.", e);
      return error(CLIENT, "Invalid request.");
    }

    final Object argument;
    try {
      argument = gson.fromJson(request.argument, method.argumentType());
    } catch (Exception e) {
      logger.info("Invalid argument.", e);
      return error(CLIENT, "Invalid argument.");
    }

    Object header = request.header == null ? null
        : gson.fromJson(request.header, service.headerType);
    try {
      Object result = service.invoke(header, method, argument);
      JsonElement jsonResult = gson.toJsonTree(result);
      return new JsonResponse(jsonResult);
    } catch (ClientException e) {
      logger.info("Client error.", e);
      return error(CLIENT, e.getMessage());
    } catch (Exception e) {
      logger.error("Server error.", e);
      return error(SERVER, RpcHandler.toString(e));
    }
  }

  private static JsonResponse error(Error.Type type, String message) {
    return new JsonResponse(new Error(type, message));
  }
}
