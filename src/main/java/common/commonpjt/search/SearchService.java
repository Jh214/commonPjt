package common.commonpjt.search;

import common.commonpjt.search.entity.Search;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private final SearchRepository searchRepository;
    private final String api_key = "AIzaSyBiY4-SF28ECJ6e2iA2QqTb0LwtsGO9ArM";
    private final String cx = "164bb0c48976e4f24";
    private final RestTemplate restTemplate;

    public SearchService(SearchRepository searchRepository) {
        this.searchRepository = searchRepository;
        this.restTemplate = new RestTemplate();
    }

    public List<Search> search(String query) {
        String url = String.format("https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&q=%s", api_key, cx, query);
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        List<Map<String, String>> items = (List<Map<String, String>>) response.get("items");

        List<Search> searchResults = items.stream()
                .map(item -> new Search.Builder(item.get("title"), item.get("link"))
                        .snippet(item.get("snippet"))
                        .build())
                .collect(Collectors.toList());

        searchRepository.saveAll(searchResults);
        return searchResults;
    }
}