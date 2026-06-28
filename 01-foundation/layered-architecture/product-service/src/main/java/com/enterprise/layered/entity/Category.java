package com.enterprise.layered.entity;

import com.enterprise.common.entity.AuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Product category — a supporting lookup entity. Inherits auditing/version from {@link AuditEntity}.
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
public class Category extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;
}
