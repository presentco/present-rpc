package present.rpc;

import com.google.gson.JsonElement;

public class JsonResponse {

  /*
    Corresponding protocol buffer:

    // Result of an RPC invocation.
    message Response {
      oneof result {
        // Present when an error occurs.
        Error error = 1;

        // Encoded result from RPC method
        bytes result = 2;
      }
    }

    // RPC Error
    message Error {
      enum Type {
        // There was a problem with the request itself.
        CLIENT = 1;

        // An error occurred while processing the request.
        SERVER = 2;
      }

      required Type type = 1;

      // Descriptive message or a key to a descriptive message (when localized)
      required string message = 2;
    }
  */

  public final JsonElement result;
  public final Error error;

  public JsonResponse(Error error) {
    this.result = null;
    this.error = error;
  }

  public JsonResponse(JsonElement result) {
    this.result = result;
    this.error = null;
  }

  private JsonResponse() {
    this.result = null;
    this.error = null;
  }
}
