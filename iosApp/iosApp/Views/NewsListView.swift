//
//  NewsListView.swift
//  iosApp
//
//  Created by Ryan ZHAO on 10/6/2026.
//

import SwiftUI
import SharedLibrary

struct NewsListView: View {
    @EnvironmentObject var viewModel: NewsViewModel

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                FilterView()
                    .padding(.horizontal)
                    .padding(.bottom, 8)

                if viewModel.isLoading {
                    Spacer()
                    ProgressView().accessibilityLabel("Loading articles")
                    Spacer()
                } else if let error = viewModel.errorMessage {
                    Spacer()
                    ContentUnavailableView(
                        "Something went wrong",
                        systemImage: "exclamationmark.triangle",
                        description: Text(error)
                    )
                    .accessibilityElement(children: .combine)
                    Spacer()
                } else if viewModel.articles.isEmpty {
                    Spacer()
                    ContentUnavailableView(
                        "No articles found",
                        systemImage: "newspaper",
                        description: Text("Try a different search or category")
                    )
                    .accessibilityElement(children: .combine)
                    Spacer()
                } else {
                    List(viewModel.articles, id: \.id) { article in
                        NewsItemRow(
                            article: article,
                            onOpenArticle: { openArticle(article) },
                            onStarTapped: { viewModel.toggleStar(article: article) },
                            isStarred: article.isStarred
                        )
                        .onTapGesture {
                            openArticle(article)
                        }
                        .onAppear {
                            if article.id == viewModel.articles.last?.id {
                                viewModel.loadMore()
                            }
                        }
                    }
                    .listStyle(.plain)
                    .refreshable {
                        await viewModel.refresh()
                    }
                    
                    if viewModel.isLoadingMore {
                        HStack {
                            Spacer()
                            ProgressView().accessibilityLabel("Loading more articles")
                            Spacer()
                        }
                        .padding()
                    }
                }
            }
            .navigationTitle("Headlines")
            .searchable(
                text:   $viewModel.searchQuery,
                prompt: "Search news..."
            )
            .accessibilityLabel("Search news articles")
        }
    }

    private func openArticle(_ article: NewsArticle) {
        guard let url = URL(string: article.url) else { return }
        UIApplication.shared.open(url)
    }
}
