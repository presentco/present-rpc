package present.rpc;

import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import okio.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static present.rpc.Error.Type.CLIENT;
import static present.rpc.Error.Type.SERVER;

class ProtoHandler implements RpcHandler {

  private static final Logger logger = LoggerFactory.getLogger(ProtoHandler.class);

  private static final String TEXT_PLAIN = "text/plain";

  @Override public void handle(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
      RpcService service, RpcMethod method) throws IOException {
    Response response = handle(httpRequest, service, method);
    try {
      if (response.error == null) {
        httpResponse.setStatus(200);
      } else {
        httpResponse.setStatus(response.error.type == CLIENT ? 400 : 500);

        // Used by App Engine Task Queue.
        if (TEXT_PLAIN.equals(httpRequest.getHeader("Accept"))) {
          httpResponse.setContentType("text/plain");
          httpResponse.getWriter().println(response.error.message);
          return;
        }
      }
      httpResponse.setContentType(RpcProtocol.PROTO.contentType);
      response.encode(httpResponse.getOutputStream());
    } catch (IOException e) {
      logger.error("Error sending response to client.", e);
    }
  }

  private Response handle(HttpServletRequest servletRequest, final RpcService service,
      final RpcMethod method) throws IOException {
    final Request request;
    try {
      request = Request.ADAPTER.decode(servletRequest.getInputStream());
    } catch (IOException e) {
      logger.error("Invalid request.", e);
      return error(CLIENT, "Invalid request.");
    }

    final Message argument;
    try {
      argument = method.argumentFromBytes(request.argument);
    } catch (Exception e) {
      logger.error("Invalid argument.", e);
      return error(CLIENT, "Invalid argument.");
    }

    ProtoAdapter headerAdapter = ProtoAdapter.get(service.headerType);
    Object header = request.header == null ? null : headerAdapter.decode(request.header);
    try {
      Object result = service.invoke(header, method, argument);
      ByteString encodedResult = method.resultToBytes(result);
      return new Response(null, encodedResult);
    } catch (ClientException e) {
      logger.info("Client error.", e);
      return error(CLIENT, e.getMessage());
    } catch (Exception e) {
      logger.error("Server error.", e);
      return error(SERVER, RpcHandler.toString(e));
    }
  }

  private static Response error(Error.Type type, String message) {
    return new Response(new Error(type, message), null);
  }
}
