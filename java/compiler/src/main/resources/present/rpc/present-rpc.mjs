// Code generated by present.rpc.JavascriptGenerator. DO NOT EDIT.

// Used internally by Present RPC.
export var present_rpc = {
  post: function(service, path, arg) {
    var url = service.url + path;
    return new Promise(function (resolve, reject) {
      var xhr = new XMLHttpRequest();
      xhr.open("POST", url);
      xhr.onerror = function() {
        reject({
          request: xhr,
          code: xhr.status,
          message: xhr.statusText
        });
      }
      xhr.onload = function () {
        if (xhr.status >= 200 && xhr.status < 300) {
          resolve(xhr.response);
        } else {
          xhr.onerror();
        }
      };
      xhr.setRequestHeader("Content-Type", "application/json; charset=UTF-8");
      if (service.headers) {
        Object.keys(service.headers).forEach(function (key) {
          xhr.setRequestHeader(key, service.headers[key]);
        });
      }
      xhr.responseType = "json";
      xhr.send(JSON.stringify(arg));
    });
  }
};
