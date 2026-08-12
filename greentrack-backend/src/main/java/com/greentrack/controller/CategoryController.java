package com.greentrack.controller;
import com.greentrack.dto.response.ApiResponse;
import com.greentrack.entity.Category;
import com.greentrack.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/categories") @RequiredArgsConstructor
public class CategoryController {
    private final CategoryRepository categoryRepository;
    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(categoryRepository.findAll()));
    }
}