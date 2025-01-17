package com.post_show_blues.vine.dto;

import com.post_show_blues.vine.domain.category.Category;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Builder
@AllArgsConstructor
@Data
public class PageRequestDTO {

    private int page;

    private int size;

    private Category category;

    @Builder.Default
    private String keyword = "";

    public PageRequestDTO(){
        this.page = 1;
        this.size = 10;
    }


    public Pageable getPageable(Sort sort){
        return PageRequest.of(this.page-1, this.size, sort);
    }

}
