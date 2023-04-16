package com.capri;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.RollbackException;
import javax.transaction.Transactional;

import com.capri.core.utils.Strings;

@Transactional
public final class EntityService<TEntity extends Serializable> {
  private final Class<TEntity> entity;
  private static String PERSISTENCE_UNIT_NAME = null;
  private static EntityManagerFactory ENTITY_MANAGER_FACTORY = null;
  private static boolean IS_ENTITY_MANAGER_FACTORY_CREATED = false;

  public EntityService(Class<TEntity> entity) {
    this.entity = entity;
  }

  public static void setPersistenceUnit(String unitName) throws Exception {
    if (IS_ENTITY_MANAGER_FACTORY_CREATED) {
      throw new Exception("Persistence unit \"" + unitName + "\" has already been set.");
    }

    if (!Strings.isNullOrWhiteSpace(unitName)) {
      IS_ENTITY_MANAGER_FACTORY_CREATED = true;
      PERSISTENCE_UNIT_NAME = unitName;
      ENTITY_MANAGER_FACTORY = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
    }
  }

  public <TKey extends Object> boolean delete(TKey key) {
    TEntity entity = get(key);
    EntityManager manager = null;
    EntityTransaction transaction = null;

    try {
      manager = getEntityManager();
      transaction = manager.getTransaction();
      transaction.begin();
      if (manager.contains(entity)) {
        manager.remove(entity);
      } else {
        manager.remove(manager.merge(entity));
      }
      transaction.commit();
      return true;
    } catch (IllegalStateException exception) {
      transaction.rollback();
      return false;
    } catch (RollbackException exception) {
      transaction.rollback();
      return false;
    } catch (Exception exception) {
      transaction.rollback();
      return false;
    } finally {
      manager.close();
    }
  }

  public TEntity create(TEntity entity) {
    EntityManager manager = null;
    EntityTransaction transaction = null;

    try {
      manager = getEntityManager();
      transaction = manager.getTransaction();
      transaction.begin();
      if (manager.contains(entity))
        return entity; // if it exists no need to create it
      manager.persist(entity);
      transaction.commit();
      return entity;
    } catch (IllegalStateException exception) {
      transaction.rollback();
      return null;
    } catch (RollbackException exception) {
      transaction.rollback();
      return null;
    } catch (Exception exception) {
      transaction.rollback();
      return null;
    } finally {
      manager.close();
    }
  }

  public List<TEntity> getAll() {
    EntityManager manager = null;
    EntityTransaction transaction = null;
    List<TEntity> resultList = null;
    List<TEntity> emptyList = new ArrayList<TEntity>();
    final String query = "SELECT e FROM " + this.entity.getSimpleName() + " e";

    try {
      manager = getEntityManager();
      transaction = manager.getTransaction();
      transaction.begin();
      resultList = manager.createQuery(query, this.entity).getResultList();
      transaction.commit();
      return resultList != null ? resultList : emptyList;
    } catch (IllegalStateException exception) {
      transaction.rollback();
      return emptyList;
    } catch (RollbackException exception) {
      transaction.rollback();
      return emptyList;
    } catch (Exception exception) {
      transaction.rollback();
      return emptyList;
    } finally {
      manager.close();
    }
  }

  public <TKey extends Object> TEntity get(TKey key) {
    EntityManager manager = null;
    EntityTransaction transaction = null;
    TEntity result = null;

    try {
      manager = getEntityManager();
      transaction = manager.getTransaction();
      transaction.begin();
      result = manager.find(this.entity, key);
      transaction.commit();
      return result;
    } catch (IllegalStateException exception) {
      transaction.rollback();
      return null;
    } catch (RollbackException exception) {
      transaction.rollback();
      return null;
    } catch (Exception exception) {
      transaction.rollback();
      return null;
    } finally {
      manager.close();
    }
  }

  public TEntity update(TEntity entity) {
    EntityManager manager = null;
    EntityTransaction transaction = null;

    try {
      manager = getEntityManager();
      transaction = manager.getTransaction();
      transaction.begin();
      manager.merge(entity);
      transaction.commit();
      return entity;
    } catch (IllegalStateException exception) {
      transaction.rollback();
      return null;
    } catch (RollbackException exception) {
      transaction.rollback();
      return null;
    } catch (Exception exception) {
      transaction.rollback();
      return null;
    } finally {
      manager.close();
    }
  }

  private static EntityManager getEntityManager() throws IllegalStateException, NullPointerException {
    if (ENTITY_MANAGER_FACTORY == null) {
      throw new NullPointerException("Persistence unit name is not defined.");
    }

    return ENTITY_MANAGER_FACTORY.createEntityManager();
  }
}
