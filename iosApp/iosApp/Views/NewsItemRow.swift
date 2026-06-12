//
//  NewsItemRow.swift
//  iosApp
//
//  Created by Ryan ZHAO on 10/6/2026.
//

import SwiftUI
import SharedLibrary

struct NewsItemRow: View {
    let article: NewsArticle
    let onOpenArticle: () -> Void
    let onStarTapped: () -> Void
    let isStarred: Bool

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            AsyncImage(url: URL(string: article.imageUrl ?? "")) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fill)
            } placeholder: {
                Color.gray.opacity(0.2)
            }
            .frame(width: 90, height: 70)
            .clipShape(RoundedRectangle(cornerRadius: 8))
            .accessibilityHidden(true)

            VStack(alignment: .leading, spacing: 4) {
                Text(article.title)
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .lineLimit(2)

                Text(article.sourceName)
                    .font(.caption)
                    .foregroundStyle(.secondary)

                Text(article.publishedAt.formattedPublishDate(lang: article.lang))
                    .font(.caption2)
                    .foregroundStyle(.tertiary)
            }
            .accessibilityElement(children: .combine)

            Spacer()

            Button(action: onStarTapped) {
                Image(systemName: isStarred ? "star.fill" : "star")
                    .foregroundStyle(isStarred ? .yellow : .gray)
                    .imageScale(.large)
            }
            .buttonStyle(.plain)
            .accessibilityLabel(isStarred ? "Unstar article" : "Star article")
            .accessibilityAddTraits(isStarred ? [.isSelected] : [])
        }
        .padding(.vertical, 4)
        .contentShape(Rectangle())
        .accessibilityElement(children: .combine)
        .accessibilityAction(named: "Open article") { onOpenArticle() }
        .accessibilityHint("Tap to open article")
    }
}
