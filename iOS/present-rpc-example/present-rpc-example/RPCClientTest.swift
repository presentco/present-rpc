//
//  RPCClientTest.swift
//  present-rpc-example
//
//  Created by Patrick Niemeyer on 3/13/19.
//  Copyright © 2019 co.present. All rights reserved.
//

import Foundation

//
// If you want to talk to an RPC service using efficient protocol buffer serialization and
// client service stubs that are generated for you, you are in the right place.
// This example uses a generated client service stub to invoke a method on the EchoService with a minimum
// of client side code.
// This example requires that the Apple Swift protocol buffer compiler and Present RPC compiler are used to
// generate the message classes and client side service stubs.
//
class RPCClientTest
{
    let echoService = Example_EchoService(serviceUrl: Config.echoServiceUrl)
    
    func run()
    {
        let echoMessage = Example_EchoMessage.with { $0.value = 42 }
        echoService.echo(arg: echoMessage) { result in
            switch result {
                case .Success(let response):
                    print("Got RPC response: \(response.value)")
                case .Failure(let error):
                    print("Error: \(error)")
            }
        }
    }
}
