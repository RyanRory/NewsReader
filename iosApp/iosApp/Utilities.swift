//
//  Utilities.swift
//  iosApp
//
//  Created by Ryan ZHAO on 10/6/2026.
//

import Foundation

extension String {
    func formattedPublishDate(lang: String = "en") -> String {
        let parser = ISO8601DateFormatter()
        parser.formatOptions = [.withInternetDateTime]
        
        guard let date = parser.date(from: self) else {
            return self
        }
        
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: localeIdentifier(for: lang))
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        
        return formatter.string(from: date)
    }
    
    private func localeIdentifier(for lang: String) -> String {
        switch lang {
            case "en": return "en_US"
            case "zh": return "zh_CN"
            case "fr": return "fr_FR"
            case "ja": return "ja_JP"
            case "pl": return "pl_PL"
            case "ta": return "ta_IN"
            case "vi": return "vi_VN"
            default:   return "en_US"
        }
    }
}
