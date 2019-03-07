//
//  JSONConvertible.swift
//  present-rpc-example
//
//  Created by Patrick Niemeyer on 3/6/19.
//  Copyright © 2019 co.present. All rights reserved.
//

import Foundation

public protocol JSONConvertible : Codable {
    init?(jsonString: String)
    init?(jsonData: Data)
    func toJSON()->String?
    func toData()->Data?
}

public extension JSONConvertible {
    
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
    
    func toJSON()->String? {
        guard let data = toData() else { return nil }
        return String(data: data, encoding: .utf8)
    }
    
    func toData()->Data? {
        return try? JSONEncoder().encode(self)
    }
}
