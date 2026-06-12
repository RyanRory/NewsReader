//
//  StarredListView.swift
//  iosApp
//
//  Created by Ryan ZHAO on 10/6/2026.
//

import SwiftUI
import SharedLibrary

struct StarredListView: View {
    @EnvironmentObject var viewModel: NewsViewModel

    var body: some View {
        NavigationStack {
            Group {
                if viewModel.starredArticles.isEmpty {
                    ContentUnavailableView(
                        "No starred articles",
                        systemImage: "star",
                        description: Text("Star articles to save them here")
                    )
                    .accessibilityElement(children: .combine)
                } else {
                    List(viewModel.starredArticles, id: \.url) { article in
                        NewsItemRow(
                            article: article,
                            onOpenArticle: { openArticle(article) },
                            onStarTapped: { viewModel.toggleStar(article: article) },
                            isStarred: true
                        )
                        .onTapGesture { openArticle(article) }
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("Starred")
            .onAppear { viewModel.getStarredArticles() }
        }
    }

    private func openArticle(_ article: NewsArticle) {
        guard let url = URL(string: article.url) else { return }
        UIApplication.shared.open(url)
    }
}
