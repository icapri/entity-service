package com.capri;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;
import javax.transaction.Transactional;

/**
 * Defines a generic manager for the basis CRUD operations on
 * all the entities.
 */
@Transactional
public class EntityService<TEntity extends Serializable> {
  /**
   * Contains the type of the entity.
   */
  private final Class<TEntity> entity;

  /**
   * Contains the entity manager factory for the given persistence unit.
   */
  private static final EntityManagerFactory managerFactory = Persistence
      .createEntityManagerFactory("pubdta");

  /**
   * Initializes a new instance of the {@code EntityService} class.
   *
   * @param entity Contains the entity to manage.
   */
  public EntityService(Class<TEntity> entity) {
    this.entity = entity;
  }

  /**
   * Deletes the entity with the given id.
   *
   * @param id Contains the id of the entity to be deleted.
   * @return whether the entity with the given id was deleted.
   */
  public <TKey extends Object> boolean delete(TKey id) {
    TEntity entity = getById(id);
    EntityTransaction transaction = null;
    EntityManager manager = null;

    try {
      manager = managerFactory.createEntityManager();
      transaction = manager.getTransaction();
      transaction.begin();
      if (manager.contains(entity)) {
        manager.remove(entity);
      } else {
        manager.remove(manager.merge(entity));
      }
      transaction.commit();
      return true;
    } catch (IllegalStateException | RollbackException exception) {
      rollbackTransaction(transaction);
      return false;
    } finally {
      closeManager(manager);
    }
  }

  /**
   * Persists the given entity to the database and returns a `true` value
   * in case the transaction is committed successfully and `false` otherwise.
   *
   * @param entity Contains the entity to be persisted in the database.
   * @return whether the given entity has been persisted.
   */
  public TEntity create(TEntity entity) {
    EntityTransaction transaction = null;
    EntityManager manager = null;
    try {
      manager = managerFactory.createEntityManager();
      transaction = manager.getTransaction();
      transaction.begin();
      if (manager.contains(entity))
        return entity; // if it exists no need to create it
      manager.persist(entity);
      transaction.commit();
      return entity;
    } catch (IllegalStateException | RollbackException exception) {
      rollbackTransaction(transaction);
      return null;
    } finally {
      closeManager(manager);
    }
  }

  /**
   * Gets the entire list of entities of the given type from the database.
   * 
   * @return all the entities of the given type.
   */
  public List<TEntity> getAll() {
    EntityTransaction transaction = null;
    List<TEntity> resultList = null;
    List<TEntity> emptyList = new ArrayList<TEntity>();
    final String query = "SELECT e FROM " + this.entity.getSimpleName() + " e";
    EntityManager manager = null;
    try {
      manager = managerFactory.createEntityManager();
      transaction = manager.getTransaction();
      transaction.begin();
      resultList = manager.createQuery(query, this.entity).getResultList();
      transaction.commit();
      return resultList != null ? resultList : emptyList;
    } catch (IllegalStateException | RollbackException exception) {
      rollbackTransaction(transaction);
      return emptyList;
    } finally {
      closeManager(manager);
    }
  }

  /**
   * Gets a given entity by id.
   *
   * @param id Contains the id of the entity to be gotten.
   * @return the entity with the given id.
   */
  public <TKey extends Object> TEntity getById(TKey id) {
    EntityTransaction transaction = null;
    TEntity result = null;
    EntityManager manager = null;
    try {
      manager = managerFactory.createEntityManager();
      transaction = manager.getTransaction();
      transaction.begin();
      result = manager.find(this.entity, id);
      transaction.commit();
      return result;
    } catch (IllegalStateException | RollbackException exception) {
      rollbackTransaction(transaction);
      return null;
    } finally {
      closeManager(manager);
    }
  }

  /**
   * Updates the given entity in the database.
   * 
   * @param entity Contains the entity to be updated.
   * @return whether the entity was edited successfully in the database.
   */
  public TEntity update(TEntity entity) {
    EntityTransaction transaction = null;
    EntityManager manager = null;
    try {
      manager = managerFactory.createEntityManager();
      transaction = manager.getTransaction();
      transaction.begin();
      manager.merge(entity);
      transaction.commit();
      return entity;
    } catch (IllegalStateException | RollbackException exception) {
      rollbackTransaction(transaction);
      return null;
    } finally {
      closeManager(manager);
    }
  }

  /**
   * Closes the entity manager.
   */
  private static final void closeManager(EntityManager manager) {
    if (manager.isOpen()) {
      try {
        manager.close();
      } catch (IllegalStateException e) {
        // do nothing
      }
    }
  }

  /**
   * Rolls back the transactiojn in case it is active.
   */
  private static final void rollbackTransaction(EntityTransaction transaction) {
    if (transaction.isActive()) {
      try {
        transaction.rollback();
      } catch (PersistenceException | IllegalStateException e) {
        // do nothing
      }
    }
  }
}
