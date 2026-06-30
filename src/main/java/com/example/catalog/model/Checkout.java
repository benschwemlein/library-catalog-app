package com.example.catalog.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "checkout")
@Setter
@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Checkout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_id")
    private Long itemId; // This column is for the ID reference

    @ManyToOne
    @JoinColumn(name = "item_id", referencedColumnName = "id", insertable = false, updatable = false)
    private CatalogItem item; // This property is used for the relationship

    private Boolean checkedOut;
    private LocalDateTime checkoutDateTime;
    private LocalDateTime checkinDateTime;

    @Column(name = "checkedout_by")
    private Long checkedoutById;

    @ManyToOne
    @JoinColumn(name = "checkedout_by", referencedColumnName = "id", insertable = false, updatable = false)
    private User checkedoutBy; // This property is used for the relationship

}
