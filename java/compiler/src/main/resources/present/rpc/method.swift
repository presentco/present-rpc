// {{Documentation}}
extension {{ServiceName}} {
    func {{MethodName}}( arg: {{RequestType}},
        completion: @escaping ({{ServiceName}}_Response<{{ResponseType}}>)->Void)
    {
        post(serviceMethod: "{{MethodName}}", arg: arg, completion: completion)
    }
}


