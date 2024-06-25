package common.commonpjt.search;

import common.commonpjt.search.entity.Search;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class SearchService {

    private final SearchRepository searchRepository;
    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String searchEngineId;
    private final String naverClientId;
    private final String naverClientSecret;
    private final String kakaoApiKey;
    private static final int RESULTS_PER_PAGE = 10;
    private static final int MAX_RESULTS = 100;

    public SearchService(SearchRepository searchRepository,
                         RestTemplate restTemplate,
                         @Value("${google.api.key}") String apiKey,
                         @Value("${google.search.engine.id}") String searchEngineId,
                         @Value("${naver.client.id}") String naverClientId,
                         @Value("${naver.client.secret}") String naverClientSecret,
                         @Value("${kakao.api.key}") String kakaoApiKey) {
        this.searchRepository = searchRepository;
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.searchEngineId = searchEngineId;
        this.naverClientId = naverClientId;  // @Value로 주입받은 값 할당
        this.naverClientSecret = naverClientSecret;  // @Value로 주입받은 값 할당
        this.kakaoApiKey = kakaoApiKey;
    }

    public List<Search> search(String query, String engine, int page) {
        List<Search> searchResults = new ArrayList<>();

        switch (engine.toLowerCase()) {
            case "google":
                searchResults = fetchGoogleResults(query, page);
                break;
            case "naver":
                searchResults = fetchNaverResults(query, page);
                break;
            case "kakao":
                searchResults = fetchKakaoResults(query, page);
                break;
            default:
                throw new IllegalArgumentException("Unknown search engine: " + engine);
        }

        saveSearchResults(searchResults);
        return searchResults;
    }

    private List<Search> fetchGoogleResults(String query, int page) {
        List<Search> searchResults = new ArrayList<>();
        int startIndex = (page - 1) * RESULTS_PER_PAGE + 1;

        String url = String.format("https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&q=%s&start=%d", apiKey, searchEngineId, query, startIndex);
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        List<Map<String, String>> items = (List<Map<String, String>>) response.get("items");

        if (items != null) {
            searchResults = items.stream()
                    .map(item -> new Search.Builder(item.get("title"), item.get("link"))
                            .snippet(item.get("snippet"))
                            .build())
                    .collect(Collectors.toList());
        }

        return searchResults;
    }

    private List<Search> fetchNaverResults(String query, int page) {
        int start = (page - 1) * RESULTS_PER_PAGE + 1;
        String url = String.format("https://openapi.naver.com/v1/search/webkr.json?query=%s&display=%d&start=%d", query, RESULTS_PER_PAGE, start);
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", naverClientId);
        headers.set("X-Naver-Client-Secret", naverClientSecret);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        List<Map<String, String>> items = (List<Map<String, String>>) response.getBody().get("items");

        if (items == null) {
            return new ArrayList<>();
        }

        return items.stream()
                .map(item -> new Search.Builder(item.get("title"), item.get("link"))
                        .snippet(item.get("description"))
                        .build())
                .collect(Collectors.toList());
    }

    private List<Search> fetchKakaoResults(String query, int page) {
        String url = String.format("https://dapi.kakao.com/v2/search/web?query=%s&size=%d&page=%d", query, RESULTS_PER_PAGE, page);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            List<Map<String, Object>> items = (List<Map<String, Object>>) response.getBody().get("documents");

            if (items == null) {
                return new ArrayList<>();
            }

            return items.stream()
                    .map(item -> new Search.Builder((String) item.get("title"), (String) item.get("url"))
                            .snippet((String) item.get("contents"))
                            .build())
                    .collect(Collectors.toList());
        } catch (RestClientException e) {
            // 로그를 남기고 빈 리스트 반환
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    public int getTotalPages(String query, String engine) {
        // 실제 검색 결과의 총 개수와 RESULTS_PER_PAGE 값을 이용하여 전체 페이지 수를 계산합니다.
        // 이는 임의의 값을 반환하도록 설정되어 있으므로, 실제 검색 API의 응답에 맞게 수정해야 합니다.
        int totalResults = 1000;  // 예시 값
        return (int) Math.ceil((double) totalResults / RESULTS_PER_PAGE);
    }

    private void saveSearchResults(List<Search> searchResults) {
        searchRepository.saveAll(searchResults);
    }
}
