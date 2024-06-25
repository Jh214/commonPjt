package common.commonpjt.search;

import common.commonpjt.search.entity.Search;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@AllArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/search")
    public String searchResults(@RequestParam("query") String query,
                                @RequestParam("engine") String engine,
                                @RequestParam(value = "page", defaultValue = "1") int page,
                                Model model) {
        List<Search> results = searchService.search(query, engine, page);
        model.addAttribute("results", results);
        model.addAttribute("query", query);
        model.addAttribute("engine", engine);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", searchService.getTotalPages(query, engine));
        return "searchResults_" + engine;  // 검색 엔진별로 다른 템플릿 사용
    }


}
