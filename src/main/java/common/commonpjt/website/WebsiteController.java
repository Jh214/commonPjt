package common.commonpjt.website;

import common.commonpjt.website.dto.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class WebsiteController {
    @Autowired
    private WebsiteService websiteService;

    @GetMapping("/")
    public String index(){
        return "test";
    }

    @GetMapping("/search")
    public String search(@RequestParam("keyword") String keyword, @RequestParam(name = "userNo", required = false) Long userNo, Model model) {
        List<SearchResponse> websites = websiteService.searchWebsites(keyword, userNo);
        model.addAttribute("keyword", keyword);
        model.addAttribute("websites", websites);
        return "testresult";
    }
}
