package com.example.WeConnect_BE.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "\"CONVERSATION_TYPE\"")
public class ConversationType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String type;
}
