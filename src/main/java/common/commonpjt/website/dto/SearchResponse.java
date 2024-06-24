package common.commonpjt.website.dto;

import common.commonpjt.website.entity.Website;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private String url;
    private String title;
    private String description;

    public SearchResponse(Website website) {
        this.url = website.getUrl();
        this.title = website.getTitle();
        this.description = website.getDescription();
    }
}
