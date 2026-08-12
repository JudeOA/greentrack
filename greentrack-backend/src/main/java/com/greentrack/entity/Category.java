package com.greentrack.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "categories")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Integer id;
    @Column(nullable = false, unique = true, length = 100) private String name;
    @Column(length = 50) private String icon;
    @Column(columnDefinition = "TEXT") private String description;
}