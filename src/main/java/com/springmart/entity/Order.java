package com.springmart.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 注文エンティティ。
 * <p>
 * アクセサは {@link lombok.Data} による生成と手書きが混在しています。
 * <ul>
 *   <li><b>手書き</b>: {@code getId}, {@code getStatus}, {@code getTotalPrice},
 *       {@code setUser}, {@code setStatus}, {@code setTotalPrice}, {@code setOrderDetails}
 *       — {@link com.springmart.service.OrderService} などから参照。</li>
 *   <li><b>Lombok 生成</b>（{@code @Data}）: {@code getUser}, {@code getOrderDetails}, {@code getOrderedAt}
 *       など — 現状コードでは直接呼び出しは少ないが、JPA の双方向関連・永続化で利用。</li>
 * </ul>
 * Lombok を完全に外す場合は、上記を列挙してから手書きに置き換えること。
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Column(name = "status", nullable = false)
    private String status; // PENDING, COMPLETED

    @Column(name = "ordered_at", nullable = false, updatable = false)
    private LocalDateTime orderedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        orderedAt = LocalDateTime.now();
        if (status == null) {
            status = "COMPLETED";
        }
    }

    public Long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public Integer getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Integer totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
