package mooncake.example.bank.domain.transaction;

import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;

import javax.persistence.EntityManager;
import java.util.List;

interface TransactionDao {

    List<Transaction> findTransactionList(@Param("accountId") Long accountId, @Param("transactionType") TransactionEnum tranasctionType
            , @Param("page") Integer page);
}


// 규칙 / 기존 Repository 이름 앞 + Impl 이 붙어야 한다
// 동적 쿼리를 만들기 위해 직접 짠다 (구분값을 가지고 다른 쿼리 수행)
@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionDao {

    private final EntityManager em;

    @Override
    public List<Transaction> findTransactionList(Long accountId, TransactionEnum transactionType, Integer page) {
        // 요구사항 : 특정 계좌의 Transaction 내역을 반환해달라
        // 우선, accountId 의 출금 내역을 모두 반환해달라
        // select * from Transaction t where t.withdraw_account_id = account_id
        // 입금 내역을 모두 반환해달라
        // select * from Transaction t where t.deposit_account_id = account_id
        // 모든 이체 내역을 반환해달라
        // select * from Transaction t where t.deposit_account_id = account_id or t.withdraw_account_id = account_id;
        // join 할 필요가 없는디..?
        // query : select * from transaction
        String jpqlQuery = "";
        jpqlQuery += "select t from Transaction t ";
        if (transactionType.equals(TransactionEnum.WITHDRAW)) {
            jpqlQuery += "where t.withdrawAccount.id = :accountId";
        } else if (transactionType.equals(TransactionEnum.DEPOSIT)) {
            jpqlQuery += "where t.depositAccount.id = :accountId";
        } else { // ALL 혹은 TRANSFER 내역 // TRANSFER 만 조회해달라고 하는건 없다고 가정 // 모든 입출금 내역 조회 ㅇㅇ
            jpqlQuery += "where t.withdrawAccount.id = :accountId " +
                    "or t.depositAccount.id = :accountId";
        }

        /*
         ACCOUNT 를 FETCH JOIN 해도 된다
         근데 대부분 AccountPs 가 이미 있는 상태에서 해당 쿼리를 탄다
         Transfer Type 일 때만 반대쪽 계좌가 연동이 되면 좋으니 fetch join 을 해도 된다. 근데 나머진 Service 메서드에 이미 AccountPs 를 해놓은 상태라 굳이 싶긴 하다
         - 물론 나중 요구사항 떄 편하기 위해 그냥 해놓아도 됨

         */
        return em.createQuery(jpqlQuery, Transaction.class)
                .setParameter("accountId", accountId)
                .setFirstResult(page * 5) // 0, 5, 10 ..
                .setMaxResults(5)
                .getResultList(); // 5권씩 나온다

    }
}
