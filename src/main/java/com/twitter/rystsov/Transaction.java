package com.twitter.rystsov;

import com.mongodb.DBObject;

import java.util.HashSet;
import java.util.Set;

/**
 * User: Denis Rystsov
 */
public class Transaction {
    private final Kv.KvEntity tx;
    private final Db db;
    private Set<Kv.KvEntity> log = new HashSet<Kv.KvEntity>();

    public Transaction(Db db, Kv.KvEntity tx) {
        this.tx = tx;
        this.db = db;
    }

    public void change(Kv.KvEntity entity, DBObject value) {
        entity.tx = tx.id;
        entity.updated = value;
        log.add(entity);
    }

    public void commit() {
        Set<Kv.KvEntity> altered = new HashSet<Kv.KvEntity>();
        for (Kv.KvEntity entity : log) {
            altered.add(
                db.update(entity)
            );
        }
        // if this operation pass, tx will be committed
        db.delete(tx);
        // tx is committed, this is just a clean up
        try {
            for (Kv.KvEntity entity : altered) {
                entity.value = entity.updated;
                entity.updated = null;
                entity.tx = null;
                db.update(entity);
            }
        } catch (Exception e) { }
    }
}
