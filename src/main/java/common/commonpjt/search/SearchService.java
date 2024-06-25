package common.commonpjt.search;

import common.commonpjt.search.entity.Search;
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
    private static final int RESULTS_PER_PAGE = 10;
    private static final int MAX_RESULTS = 100;

    public SearchService(SearchRepository searchRepository,
                         RestTemplate restTemplate,
                         @Value("${google.api.key}") String apiKey,
                         @Value("${google.search.engine.id}") String searchEngineId,
                         @Value("${naver.client.id}") String naverClientId,
                         @Value("${naver.client.secret}") String naverClientSecret) {
        this.searchRepository = searchRepository;
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.searchEngineId = searchEngineId;
        this.naverClientId = naverClientId;  // @Value로 주입받은 값 할당
        this.naverClientSecret = naverClientSecret;  // @Value로 주입받은 값 할당
    }

    public List<Search> search(String query, String engine) {
        List<Search> searchResults = new ArrayList<>();

        switch (engine.toLowerCase()) {
            case "google":
                searchResults = fetchGoogleResults(query);
                break;
            case "naver":
                searchResults = fetchNaverResults(query);
                break;
            default:
                throw new IllegalArgumentException("Unknown search engine: " + engine);
        }

        saveSearchResults(searchResults);
        return searchResults;
    }

    private List<Search> fetchGoogleResults(String query) {
        List<Search> searchResults = new ArrayList<>();
        int startIndex = 1;

        while (startIndex <= MAX_RESULTS) {
            String url = String.format("https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&q=%s&start=%d", apiKey, searchEngineId, query, startIndex);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            List<Map<String, String>> items = (List<Map<String, String>>) response.get("items");

            if (items == null) {
                break;
            }

            List<Search> results = items.stream()
                    .map(item -> new Search.Builder(item.get("title"), item.get("link"))
                            .snippet(item.get("snippet"))
                            .build())
                    .collect(Collectors.toList());

            searchResults.addAll(results);
            startIndex += RESULTS_PER_PAGE;
        }

        return searchResults;
    }

    private List<Search> fetchNaverResults(String query) {
        String url = String.format("https://openapi.naver.com/v1/search/webkr.json?query=%s&display=%d&start=1", query, RESULTS_PER_PAGE);
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", naverClientId);
        headers.set("X-Naver-Client-Secret", naverClientSecret);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class,  // 여기에 응답 타입을 지정해야 함
                query,  // 여기에 URI 변수를 전달해야 함
                RESULTS_PER_PAGE);  // 여기에 URI 변수를 전달해야 함

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


    private void saveSearchResults(List<Search> searchResults) {
        searchRepository.saveAll(searchResults);
    }
}
