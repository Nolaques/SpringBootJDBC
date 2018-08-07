package com.company.dao;


import com.company.exception.BankTransactionException;
import com.company.mapper.BankAccountMapper;
import com.company.model.BankAccountInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;

@Repository
@Transactional
public class BankAccountDAO extends JdbcDaoSupport {

    @Autowired
    public BankAccountDAO(@Qualifier("dataSource") DataSource dataSource) {
        this.setDataSource(dataSource);

    }


    public List<BankAccountInfo> getBankAccounts() {
        //select ba.Id, ba.Full_name, ba.Balance From Bank_Account ba
        String sql = BankAccountMapper.BASE_SQL;

        Object[] params = new Object[]{};
        BankAccountMapper mapper = new BankAccountMapper();
        List<BankAccountInfo> list = this.getJdbcTemplate().query(sql, params, mapper);

        return list;
    }


    public BankAccountInfo findBankAccount(Long id) {
        //select ba.Id, ba.Full_Name, ba/Balance from Bank_Account ba
        //where ba.Id = ?
        String sql = BankAccountMapper.BASE_SQL + " where ba.Id = ? ";

        Object[] params = new Object[]{id};
        BankAccountMapper mapper = new BankAccountMapper();
        try {
            BankAccountInfo bankAccount = this.getJdbcTemplate().queryForObject(sql, params, mapper);
            return bankAccount;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    //mandatory: transaction must be created before
    @Transactional(propagation = Propagation.MANDATORY)
    public void addAmount(Long id, double amount) throws BankTransactionException {
        BankAccountInfo accountInfo = this.findBankAccount(id);
        if (accountInfo == null) {
            throw new BankTransactionException("Account not found " + id);
        }
        double newBalance = accountInfo.getBalance() + amount;
        if (accountInfo.getBalance() + amount < 0) {
            throw new BankTransactionException(
                    "The money in the account '" + id + "' is not enough (" + accountInfo.getBalance() + ")");
        }
        accountInfo.setBalance(newBalance);
        //update to DB
        String sqlUpdate = "Update Bank_Account set Balance = ? where Id = ?";
        this.getJdbcTemplate().update(sqlUpdate, accountInfo.getBalance(), accountInfo.getId());
    }

    //do not catch BankTransactionException in this method
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = BankTransactionException.class)
    public void sendMoney(Long fromAccountId, Long toAccountId, double amount) throws BankTransactionException {
        try {


            addAmount(toAccountId, amount);
            addAmount(fromAccountId, -amount);
        } catch (BankTransactionException e) {
            //=>call transaction rollback
            throw e;
        }
    }
}
