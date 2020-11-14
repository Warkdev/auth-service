package eu.getmangos.controllers;

import java.util.Date;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import org.slf4j.Logger;

import eu.getmangos.entities.Account;

@RequestScoped
public class AccountController {
    @Inject private Logger logger;

    @Inject private RealmCharactersController realmCharactersController;
    @Inject private WardenController wardenController;
    @Inject private AccountBannedController accountBannedController;

    @PersistenceContext(name = "AUTH_PU")
    private EntityManager em;

    @Transactional
    /**
     * Creates an account in the dabatase.
     * @param account The account to create.
     * @throws DAOException Send a DAOException if something happened during the data validation.
     */
    public void create(Account account) throws DAOException {
        logger.debug("create() entry.");
        if(account.getUsername().isBlank()) {
            logger.debug("create() exit.");
            throw new DAOException("Username is null or empty.");
        }
        if(search(account.getUsername()) != null) {
            logger.debug("create() exit.");
            throw new DAOException("Account already exist.");
        }
        account.setJoinDate(new Date(System.currentTimeMillis()));
        em.persist(account);
        logger.debug("create() exit.");
    }

    @Transactional
    /**
     * Updates an account in the dabatase.
     * @param account The account to edit.
     * @throws DAOException Send a DAOException if something happened during the data validation.
     */
    public void update(Account account) throws DAOException {
        logger.debug("update() entry.");
        if(account.getUsername().isBlank()) {
            logger.debug("update() exit.");
            throw new DAOException("Username is null or empty.");
        }
        Account existing = search(account.getUsername());
        if(existing == null) {
            logger.debug("update() exit.");
            throw new DAOException("Account doesn't exist.");
        }

        if(existing.getId() != account.getId()) {
            logger.debug("update() exit.");
            throw new DAOException("The provided account doesn't match the account to be updated.");
        }
        em.merge(account);
        logger.debug("update() exit.");
    }

    @Transactional
    /**
     * Delete an account in the database. This method will also delete all links with the realms for this account.
     * @param id The ID of the account to be deleted.
     * @throws DAOException Send a DAOException if something happened during the data validation.
     */
    public void delete(Integer id) throws DAOException {
        logger.debug("delete() entry.");

        Account account = find(id);
        if(account == null) {
            logger.debug("delete() exit.");
            throw new DAOException("The account doesn't exist");
        }

        wardenController.deleteLogsForAccount(id);
        realmCharactersController.deleteByAccount(id);
        accountBannedController.deleteForAccount(id);
        em.remove(account);

        logger.debug("delete() exit.");
    }

    /**
     * Retrieves an account by its ID.
     * @param id The ID of the account
     * @return The account if found, null otherwise.
     */
    public Account find(Integer id) {
        try {
            Account account = (Account) em.createNamedQuery("Account.findById").setParameter("id", id).getSingleResult();
            logger.debug("search() exit.");
            return account;
        } catch (NoResultException nre) {
            logger.debug("No account found with this id.");
            logger.debug("search() exit.");
            return null;
        }
    }

    /**
     * Search an account by its name
     * @param name The name of the account.
     * @return The matching account for the given name.
     */
    public Account search(String name) {
        logger.debug("search() entry.");
        try {
            Account account = (Account) em.createNamedQuery("Account.findByName").setParameter("name", name).getSingleResult();
            logger.debug("search() exit.");
            return account;
        } catch (NoResultException nre) {
            logger.debug("No account found with this name.");
            logger.debug("search() exit.");
            return null;
        }

    }

    /**
     * Retrieves all accounts from the database.
     * @return A list of Account.
     */
    @SuppressWarnings("unchecked")
    public List<Account> findAll() {
        return (List<Account>) em.createNamedQuery("Account.findAll").getResultList();
    }
}