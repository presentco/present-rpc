//
//  ProtobufTest.swift
//  present-rpc-example
//
//  Created by Patrick Niemeyer on 3/6/19.
//  Copyright © 2019 co.present. All rights reserved.
//

import Foundation

class ProtobufTest
{
    func run()
    {
        let echoRequest = Example_EchoMessage.with {
            $0.value = 42
        }

        // For running this test in the simulator:
        // Run the EchoService on port 8080 of the local host.
        // For testing on a real device you replace localhost with your local IP address.
        // To allow testing without https we override NSAppTransportSecurity in info.plist.
        let url = URL(string: "http://localhost:8080/EchoService/echo")!
        
        var request = URLRequest(url: url)
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
