//
//  JsonConvertible.swift
//  present-rpc-example
//
//  Created by Patrick Niemeyer on 3/6/19.
//  Copyright © 2019 co.present. All rights reserved.
//

import Foundation

public protocol JsonConvertible : Codable {
    init?(jsonString: String)
    init?(jsonData: Data)
    func toJson()->String?
    func toData()->Data?
}

public extension JsonConvertible {
    
    init?(jsonString: String) {
        guard let data = jsonString.data(using: .utf8) else { return nil }
        self.init(jsonData: data)
    }
    
    init?(jsonData: Data) {
        do {
            self = try JSONDecoder().decode(Self.self, from: jsonData)
        } catch {
            return nil
        }
    }
    
    func toJson()->String? {
        guard let data = toData() else { return nil }
        return String(data: data, encoding: .utf8)
    }
    
    func toData()->Data? {
        return try? JSONEncoder().encode(self)
    }
}
