package mooncake.example.bank.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import mooncake.example.bank.domain.transaction.Transaction;
import mooncake.example.bank.domain.transaction.TransactionEnum;

public class TransactionRespDto {

    @Getter
    @Setter
    public static class TransactionDto {
        private Long id;
        private TransactionEnum transactionType;
        private String sender;
        private String receiver;

        private Long amount;
        private String tel;
        private String createdAt;

        @JsonIgnore // 응답에 보낼 값은 아니다. 다만, 내가 Test 를 위해 사용하겠다는 뜻 (그 용도로 보통 사용)
        private Long depositAccountBalance;

        public TransactionDto(Transaction transaction) {
            this.id = transaction.getId();
            this.transactionType = transaction.getTransactionType();
            this.sender = transaction.getSender();
            this.receiver = transaction.getReceiver();
            this.amount = transaction.getAmount();
            this.depositAccountBalance = transaction.getDepositAccountBalance();
            this.tel = transaction.getTel();
            this.createdAt = transaction.getCreatedAt() + "";
        }

    }
}


