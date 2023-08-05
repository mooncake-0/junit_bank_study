package mooncake.example.bank.domain.account;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mooncake.example.bank.domain.user.User;
import mooncake.example.bank.handler.exception.CustomApiException;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name = "account_tb")
@Entity
@EntityListeners(AuditingEntityListener.class) // 날짜 관리
@Getter
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private Long number;

    @Column(nullable = false, length = 4)
    private Long password;

    @Column(nullable = false)
    private Long balance; // 잔액 (기본: 1000원)

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Account(Long id, Long number, Long password, Long balance, LocalDateTime createdAt, LocalDateTime updatedAt, User user) {
        this.id = id;
        this.number = number;
        this.password = password;
        this.balance = balance;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.user = user;
    }

    // DDD 로 인해, 여기서 체킹을 해보자
    public void checkOwner(Long checkingId) {
        if (this.getId() != checkingId) {
            throw new CustomApiException("계좌 소유자가 아닙니다");
        }
    }
}