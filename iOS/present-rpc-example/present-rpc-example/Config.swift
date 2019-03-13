//
//  Config.swift
//  present-rpc-example
//
//  Created by Patrick Niemeyer on 3/13/19.
//  Copyright © 2019 co.present. All rights reserved.
//

import Foundation

struct Config
{
    // Run this test in the simulator with the EchoService running on port 8080 of the local host.
    // For testing on a physical device replace "localhost" with the IP address of the local host.
    // Note: To allow testing without https we have overridden NSAppTransportSecurity in info.plist,
    // but production applications must use https URLs.
    static let echoServiceUrl = URL(string: "http://localhost:8080/EchoService")!
}
