//
//  JSONTest.swift
//  present-rpc-example
//
//  Created by Patrick Niemeyer on 3/6/19.
//  Copyright © 2019 co.present. All rights reserved.
//

import Foundation

//
// If you want to talk to an RPC service using JSON and your own networking code you are in the right place.
// This example sends and receives JSON in an exchange with the EchoService using a plain URLRequest.
// No protocol buffer libs or schema are required to talk to the service in this fashion.
//
class JSONClientTest
{
    // This helper class serializes itself to and initialize itself from JSON, however you can construct the
    // JSON string yourself if desired.
    class EchoMessage: JSONConvertible {
        public var value: Int
        public init(value: Int) {
            self.value = value
        }
    }

    func run()
    {
        let echoRequest = EchoMessage(value: 42)
        
        var request = URLRequest(url: Config.echoServiceUrl)
        request.httpMethod = "POST"
        request.httpBody = echoRequest.toData() // {"value":42}
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        
        let task = URLSession.shared.dataTask(with: request)
        { (data, response, error) in
            guard error == nil else {
                print("error: \(String(describing: error))");
                return
            }
            guard let content = data else {
                print("Failed to get response.")
                return
            }
            guard let echoResponse = EchoMessage(jsonData: content) else {
                print("Unable to parse response: \(String(describing: String(data: content, encoding: .utf8)))")
                return
            }
            
            print("Got JSON response: \(echoResponse.value)")
            assert(echoResponse.value == 42)
        }
        
        task.resume() // to start the url session
    }
}

