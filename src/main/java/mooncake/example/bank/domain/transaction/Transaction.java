package mooncake.example.bank.domain.transaction;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mooncake.example.bank.domain.account.Account;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name = "transaction_tb")
@Entity
@EntityListeners(AuditingEntityListener.class) // 날짜 관리
@Getter
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "withdraw_account_id")
    private Account withdrawAccount; // 출금 Account

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deposit_account_id")
    private Account depositAccount; // 입금 Account

    @Column(nullable = false)
    private Long amount; // 이동한 정도

    // Account 객체는 최종 금액만 저장한 상태일 것이기 때문
    private Long withdrawAccountBalance; // 그 시점에서의 금액
    private Long depositAccountBalance; // 그 시점에서의 금액

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionEnum transactionType;


    // 계좌가 사라져도 로그는 남아야 한다
    private String sender;
    private String receiver;
    private String tel;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;


    @Builder
    public Transaction(Long id, Account withdrawAccount, Account depositAccount, Long amount, Long withdrawAccountBalance, Long depositAccountBalance, TransactionEnum transactionType, String sender, String receiver, String tel, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.withdrawAccount = withdrawAccount;
        this.depositAccount = depositAccount;
        this.amount = amount;
        this.withdrawAccountBalance = withdrawAccountBalance;
        this.depositAccountBalance = depositAccountBalance;
        this.transactionType = transactionType;
        this.sender = sender;
        this.receiver = receiver;
        this.tel = tel;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
