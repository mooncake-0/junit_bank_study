package mooncake.example.bank.domain.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByNumber(Long number);

    // named query : select * from account a where a.user_id = {id}
    List<Account> findByUser_id(Long id);

}
