package common.commonpjt.website;

import common.commonpjt.user.UserRepository;
import common.commonpjt.website.dto.SearchResponse;
import common.commonpjt.website.entity.SearchHistory;
import common.commonpjt.website.entity.Website;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WebsiteService {
    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    @Autowired
    private WebsiteRepository websiteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebScraper webScraper;

    public List<SearchResponse> searchWebsites(String keyword, Long userId) {
        // 웹 스크래핑 로직 구현
        List<Website> websites = webScraper.scrapeWebsites(keyword);

        // 검색 히스토리 저장
        SearchHistory searchHistory = new SearchHistory();
        searchHistory.setKeyword(keyword);
        searchHistory.setSearchDate(LocalDateTime.now());
        searchHistory.setUser(userRepository.findById(userId).orElse(null));
        searchHistory.getWebsites().addAll(websites);
        searchHistoryRepository.save(searchHistory);

        // 웹사이트 저장
        websiteRepository.saveAll(websites);

        return websites.stream().map(SearchResponseDTO::new).collect(Collectors.toList());
    }
}