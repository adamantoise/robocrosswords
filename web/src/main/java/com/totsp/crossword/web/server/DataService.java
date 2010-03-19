/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.totsp.crossword.web.server;

import com.google.appengine.api.datastore.Blob;
import com.totsp.crossword.puz.Box;
import com.totsp.crossword.web.server.model.PuzzleListing;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 *
 * @author kebernet
 */
public class DataService {

    private static final EntityManagerFactory emfInstance = Persistence.createEntityManagerFactory("transactions-optional");

    private EntityManager entityManager = emfInstance.createEntityManager();

    public DataService(){

    }


    public List<PuzzleListing> findAfterDateNoPuzzle(Date date){
        
        EntityTransaction t = entityManager.getTransaction();
        t.begin();
        Query q = entityManager.createNamedQuery("PuzzleListing.findAfterDate");
        q.setParameter("startDate", date);

        try{
            List<PuzzleListing> result = q.getResultList();
            t.commit();

            return result;
       
        } catch(NoResultException nre){
            nre.printStackTrace();
            t.rollback();
            return new ArrayList<PuzzleListing>();
        } 
    }

    public Object findById(Class clazz, Long id){
        EntityTransaction t = this.entityManager.getTransaction();
        t.begin();
        try{
            Object result = this.entityManager.getReference(clazz, id);
            t.commit();
            return result;
        } catch(NoResultException nre ){
            t.rollback();
            return null;
        }
    }


    public PuzzleListing store(PuzzleListing listing){
        EntityTransaction t = entityManager.getTransaction();
        t.begin();
        entityManager.persist(listing);
        t.commit();
        return listing;
    }

    public PuzzleListing findPuzzleListingBySourceAndDate(String source, Date date){
        EntityTransaction t = entityManager.getTransaction();
        t.begin();
        Query q = entityManager.createNamedQuery("PuzzleListing.findByDateAndSource");
        q.setParameter("source", source);
        q.setParameter("pubDate", date);

        try{
            PuzzleListing result = (PuzzleListing) q.getSingleResult();
            t.commit();

            return result;
       } catch(NoResultException nre){
            nre.printStackTrace();
            t.rollback();
            return null;
        } 
       
    }

    public void close(){
        this.entityManager.close();
    }

//    public List<PuzzleListing> findThisWeeksPuzzleListings(){
//
//    }



    byte[] serialize(Serializable s) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(s);
        oos.close();
        return baos.toByteArray();
    }

    Serializable deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return  (Serializable) ois.readObject();
    }

    PuzzleListing findListingById(Class<PuzzleListing> aClass, Long puzzleId) {
        PuzzleListing result = (PuzzleListing) this.findById(PuzzleListing.class, puzzleId);
        return result;
    }
}
