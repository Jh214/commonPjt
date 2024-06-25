package common.commonpjt.search;

import common.commonpjt.search.entity.Search;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.naming.directory.SearchResult;
import java.util.List;

@Controller
@AllArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/search")
    public String searchResults(@RequestParam("query") String query,
                                @RequestParam("engine") String engine,
                                Model model) {
        List<Search> results = searchService.search(query, engine);
        model.addAttribute("results", results);
        return "searchResults";
    }

}
