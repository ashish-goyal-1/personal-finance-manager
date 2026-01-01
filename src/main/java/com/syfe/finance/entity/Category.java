package com.syfe.finance.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "category", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "user_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * Returns true if this is a system default category (user is null).
     * Returns false if this is a user-created custom category.
     */
    public boolean isDefault() {
        return user == null;
    }

    /**
     * Returns true if this is a custom category created by a user.
     */
    public boolean isCustom() {
        return user != null;
    }
}
