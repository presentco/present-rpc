//
//  ViewController.swift
//  present-rpc-example
//
//  Created by Patrick Niemeyer on 3/6/19.
//  Copyright © 2019 co.present. All rights reserved.
//

import UIKit

class ViewController: UIViewController
{

    override func viewDidLoad() {
        super.viewDidLoad()
        testJSON()
    }

    func testJSON()
    {
        class EchoRequest: JsonConvertible {
            public var value: Int
            public init(value: Int) {
                self.value = value
            }
        }
        class EchoResponse: JsonConvertible {
            public var value: Int
            public init(value: Int) {
                self.value = value
            }
        }
        
        let echoRequest = EchoRequest(value: 42)

        // Expecting the EchoService to be running on port 8080
        let url = URL(string: "http://localhost:8080/EchoService/echo")!
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.httpBody = echoRequest.toData() // {"value":42}
        request.addValue("application/json", forHTTPHeaderField: "Content-Type")
        
        // Add custom headers for context or authentication
        request.addValue("test-value", forHTTPHeaderField: "test-header")
        
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
            guard let echoResponse = EchoResponse(jsonData: content) else {
                print("Unable to parse response: \(String(describing: String(data: content, encoding: .utf8)))")
                return
            }
            
            print("Got response: \(echoResponse.value)")
            assert(echoResponse.value == 42)
        }
        
        task.resume() // to start the url session
    }
}

