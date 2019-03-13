//
//  ProtobufTest.swift
//  present-rpc-example
//
//  Created by Patrick Niemeyer on 3/6/19.
//  Copyright © 2019 co.present. All rights reserved.
//

import Foundation

//
// If you want to talk to an RPC service using efficient protocol buffer serialization and your own
// networking code you are in the right place.
// This example uses the generated protocol buffer class EchoRequest to serialize the data for
// talking to the EchoService using a plain URLRequest.
// This example requires that the Apple Swift protocol buffer compiler is used to generate the message classes.
//
class ProtobufClientTest
{
    func run()
    {
        let echoRequest = Example_EchoMessage.with {
            $0.value = 42
        }

        var request = URLRequest(url: Config.echoServiceUrl)
        request.httpMethod = "POST"
        request.httpBody = try! echoRequest.serializedData()
        request.addValue("application/x-protobuf", forHTTPHeaderField: "Content-Type")
        
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
            guard let echoResponse = try? Example_EchoMessage(serializedData: content) else {
                print("Unable to parse response: \(String(describing: String(data: content, encoding: .utf8)))")
                return
            }
            
            print("Got Protobuf response: \(echoResponse.value)")
            assert(echoResponse.value == 42)
        }
        
        task.resume() // to start the url session
    }
}
