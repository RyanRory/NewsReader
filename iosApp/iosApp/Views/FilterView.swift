//
//  FilterView.swift
//  iosApp
//
//  Created by Ryan ZHAO on 10/6/2026.
//

import SwiftUI

struct FilterView: View {
    @EnvironmentObject var viewModel: NewsViewModel

    let categories = ["general", "world", "nation", "business", "technology", "entertainment", "sports", "science", "health"]
    let languages  = ["en", "zh", "fr", "ja", "pl", "ta", "vi"]

    var body: some View {
        HStack(spacing: 16) {
            Text("Filtered by")
                .font(.subheadline)
                .fontWeight(.regular)
                .padding(.leading, 8)
            
            // Category picker
            Picker("Category", selection: $viewModel.selectedCategory) {
                ForEach(categories, id: \.self) { category in
                    Text(category.capitalized).tag(category)
                }
            }
            .fixedSize()
            .pickerStyle(.menu)
            .padding(.vertical, 4)
            .background(Color(.systemGray6))
            .clipShape(RoundedRectangle(cornerRadius: 8))
            .accessibilityLabel("Category filter")
            .accessibilityValue(viewModel.selectedCategory.capitalized)
            
            // Language picker
            Picker("Language", selection: $viewModel.selectedLang) {
                ForEach(languages, id: \.self) { lang in
                    Text(lang.uppercased()).tag(lang)
                }
            }
            .fixedSize()
            .pickerStyle(.menu)
            .padding(.vertical, 4)
            .background(Color(.systemGray6))
            .clipShape(RoundedRectangle(cornerRadius: 8))
            .accessibilityLabel("Language filter")
            .accessibilityValue(
                Locale(identifier: "en")
                    .localizedString(forLanguageCode: viewModel.selectedLang)?
                    .capitalized ??
                viewModel.selectedLang.uppercased()
            )
            
            Spacer()
        }
        .accessibilityElement(children: .contain)
    }
}
