package common.commonpjt.search;

import common.commonpjt.search.entity.Search;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final RestTemplate restTemplate;

    @Value("${google.api.key}")
    private String googleApiKey;
    @Value("${google.search.engine.id}")
    private String googleSearchEngineId;
    @Value("${naver.client.id}")
    private String naverClientId;
    @Value("${naver.client.secret}")
    private String naverClientSecret;
    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    private static final int RESULTS_PER_PAGE = 10;

    public List<Search> search(String query, String engine, int page) {
        switch (engine.toLowerCase()) {
            case "google":
                return fetchGoogleResults(query, page);
            case "naver":
                return fetchNaverResults(query, page);
            case "kakao":
                return fetchKakaoResults(query, page);
            default:
                throw new IllegalArgumentException("Unknown search engine: " + engine);
        }
    }

    public int getTotalPages(String query, String engine) {
        int totalResults = fetchTotalResults(query, engine);
        return (int) Math.ceil((double) totalResults / RESULTS_PER_PAGE);
    }

    private int fetchTotalResults(String query, String engine) {
        switch (engine.toLowerCase()) {
            case "google":
                return fetchGoogleTotalResults(query);
            case "naver":
                return fetchNaverTotalResults(query);
            case "kakao":
                return fetchKakaoTotalResults(query);
            default:
                throw new IllegalArgumentException("Unknown search engine: " + engine);
        }
    }

    private List<Search> fetchGoogleResults(String query, int page) {
        int startIndex = (page - 1) * RESULTS_PER_PAGE + 1;
        String url = String.format("https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&q=%s&start=%d",
                googleApiKey, googleSearchEngineId, query, startIndex);

        try {
            ResponseEntity<Map> responseEntity = restTemplate.getForEntity(url, Map.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> response = responseEntity.getBody();
                if (response != null && response.containsKey("items")) {
                    List<Map<String, String>> items = (List<Map<String, String>>) response.get("items");
                    if (items != null) {
                        return items.stream()
                                .map(item -> new Search(item.get("title"), item.get("link"), item.get("snippet")))
                                .collect(Collectors.toList());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // 예외 처리 필요
        }

        return new ArrayList<>();
    }

    private int fetchGoogleTotalResults(String query) {
        String url = String.format("https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&q=%s&num=1",
                googleApiKey, googleSearchEngineId, query);

        try {
            ResponseEntity<Map> responseEntity = restTemplate.getForEntity(url, Map.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> response = responseEntity.getBody();
                if (response != null && response.containsKey("searchInformation") && response.get("searchInformation") instanceof Map) {
                    Map<String, Object> searchInfo = (Map<String, Object>) response.get("searchInformation");
                    Object totalResultsObj = searchInfo.get("totalResults");
                    if (totalResultsObj != null) {
                        return Integer.parseInt(totalResultsObj.toString());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // 예외 처리 필요
        }

        return 0;
    }

    private List<Search> fetchNaverResults(String query, int page) {
        int start = (page - 1) * RESULTS_PER_PAGE + 1;
        String url = String.format("https://openapi.naver.com/v1/search/webkr.json?query=%s&display=%d&start=%d",
                query, RESULTS_PER_PAGE, start);
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", naverClientId);
        headers.set("X-Naver-Client-Secret", naverClientSecret);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> response = responseEntity.getBody();
                if (response != null && response.containsKey("items")) {
                    List<Map<String, String>> items = (List<Map<String, String>>) response.get("items");
                    if (items != null) {
                        return items.stream()
                                .map(item -> new Search(item.get("title"), item.get("link"), item.get("description")))
                                .collect(Collectors.toList());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // 예외 처리 필요
        }

        return new ArrayList<>();
    }

    private int fetchNaverTotalResults(String query) {
        String url = String.format("https://openapi.naver.com/v1/search/webkr.json?query=%s&display=1&start=1",
                query);
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", naverClientId);
        headers.set("X-Naver-Client-Secret", naverClientSecret);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> response = responseEntity.getBody();
                if (response != null && response.containsKey("total")) {
                    return (int) response.get("total");
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // 예외 처리 필요
        }

        return 0;
    }

    private List<Search> fetchKakaoResults(String query, int page) {
        if (page < 1) {
            throw new IllegalArgumentException("Page number cannot be less than 1");
        }

        int start = Math.max((page - 1) * RESULTS_PER_PAGE, 1);
        String url = String.format("https://dapi.kakao.com/v2/search/web?query=%s&size=%d&page=%d",
                query, RESULTS_PER_PAGE, start);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                List<Map<String, String>> items = (List<Map<String, String>>) response.getBody().get("documents");

                if (items != null) {
                    return items.stream()
                            .map(item -> new Search(item.get("title"), item.get("url"), item.get("contents")))
                            .collect(Collectors.toList());
                }
            } else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
                String responseBody = response.getBody().toString();
                throw new RuntimeException("Kakao API 호출 중 오류 발생: " + responseBody);
            }
        } catch (RestClientException e) {
            e.printStackTrace(); // RestTemplate에서 발생하는 예외 처리
            throw new RuntimeException("Kakao API 호출 중 오류 발생", e);
        }

        return new ArrayList<>();
    }

    private int fetchKakaoTotalResults(String query) {
        String url = String.format("https://dapi.kakao.com/v2/search/web?query=%s&size=1&page=1", query);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> meta = (Map<String, Object>) response.getBody().get("meta");
                if (meta != null) {
                    Object totalCountObj = meta.get("total_count");
                    if (totalCountObj != null) {
                        return Integer.parseInt(totalCountObj.toString());
                    }
                }
            } else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
                String responseBody = response.getBody().toString();
                throw new RuntimeException("Kakao API 호출 중 오류 발생: " + responseBody);
            }
        } catch (RestClientException e) {
            e.printStackTrace(); // RestTemplate에서 발생하는 예외 처리
            throw new RuntimeException("Kakao API 호출 중 오류 발생", e);
        }

        return 0;
    }

}
