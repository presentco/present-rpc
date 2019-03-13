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
        
        // Run the tests
        JSONClientTest().run()
        ProtobufClientTest().run()
        RPCClientTest().run()
    }
}

