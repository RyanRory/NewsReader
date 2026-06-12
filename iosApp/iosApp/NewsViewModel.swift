//
//  NewsViewModel.swift
//  iosApp
//
//  Created by Ryan ZHAO on 9/6/2026.
//

import SwiftUI
import SharedLibrary
import Combine

@MainActor
class NewsViewModel: ObservableObject {
    @Published var articles: [NewsArticle] = []
    @Published var starredArticles: [NewsArticle] = []
    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil

    // MARK: - Pagination

    @Published var isLoadingMore: Bool = false
    @Published var hasMore: Bool = true
    private var currentPage: Int32 = 1
    private let pageSize = 10

    // MARK: - Filters

    @Published var searchQuery: String = ""
    @Published var selectedCategory: String = "world"
    @Published var selectedLang: String = "en"

    // MARK: - Services

    private let fetchNewsService: FetchNewsService
    private let toggleStarService: ToggleStarService

    private var cancellables = Set<AnyCancellable>()

    init(
        fetchNewsService: FetchNewsService,
        toggleStarService: ToggleStarService
    ) {
        self.fetchNewsService = fetchNewsService
        self.toggleStarService = toggleStarService
        getStarredArticles()
        setupDebounce()
        Task {
            await fetchNews()
        }
    }

    // MARK: - Debounce

    private func setupDebounce() {
        Publishers.CombineLatest3($searchQuery, $selectedCategory, $selectedLang)
            .dropFirst()
            .debounce(for: .milliseconds(300), scheduler: DispatchQueue.main)
            .removeDuplicates { lhs, rhs in
                lhs.0 == rhs.0 && lhs.1 == rhs.1 && lhs.2 == rhs.2
            }
            .sink { [weak self] _, _, _ in
                Task { await self?.fetchNews() }
            }
            .store(in: &cancellables)
    }

    // MARK: - Fetch

    private func performFetch(page: Int32) async -> Result<[NewsArticle], Error> {
        do {
            let result = try await fetchNewsService.fetch(
                query: searchQuery,
                category: selectedCategory,
                lang: selectedLang,
                page: page
            )
            return .success(result)
        } catch {
            return .failure(error)
        }
    }

    private func fetchNews() async {
        // Do not send a request until the user types at last 3 characters.
        // 0 means not searching, we'll fetch for headlines.
        guard searchQuery.count >= 3 || searchQuery.count == 0 else {
            return
        }
        
        isLoading = true
        errorMessage = nil
        currentPage = 1
        hasMore = true

        switch await performFetch(page: 1) {
            case .success(let result):
                articles = result
                hasMore  = result.count >= pageSize
            case .failure(let error):
                errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func refresh() async {
        currentPage = 1
        hasMore = true

        switch await performFetch(page: 1) {
            case .success(let result):
                articles = result
                hasMore  = result.count >= pageSize
            case .failure(let error):
                errorMessage = error.localizedDescription
        }
    }

    func loadMore() {
        guard !isLoadingMore, hasMore else { return }

        isLoadingMore = true
        let nextPage = currentPage + 1

        Task {
            switch await performFetch(page: nextPage) {
                case .success(let result):
                    articles += result
                    hasMore = result.count >= pageSize
                    currentPage = nextPage
                case .failure(let error):
                    errorMessage = error.localizedDescription
            }
            isLoadingMore = false
        }
    }

    // MARK: - Starred

    func toggleStar(article: NewsArticle) {
        do {
            try toggleStarService.toggleStar(article: article)
            getStarredArticles()
            articles = articles.map { $0.id == article.id ? $0.toggleStarred() : $0 }
        } catch {
            errorMessage = error.localizedDescription
            print("Toggle star error: \(error)")
        }
    }

    func getStarredArticles() {
        do {
            starredArticles = try toggleStarService.getStarredArticles()
        } catch {
            errorMessage = error.localizedDescription
            print("Toggle star error: \(error)")
        }
    }
}

// MARK: - Convenience

extension NewsArticle {
    func toggleStarred() -> NewsArticle {
        return NewsArticle(
            id: self.id,
            title: self.title,
            description: self.description_,
            url: self.url,
            imageUrl: self.imageUrl,
            publishedAt: self.publishedAt,
            lang: self.lang,
            sourceName: self.sourceName,
            isStarred: !self.isStarred
        )
    }
}
