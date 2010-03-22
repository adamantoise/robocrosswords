/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.totsp.crossword.web.server.model;

import com.google.appengine.api.datastore.Blob;
import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;


/**
 *
 * @author kebernet
 */
@Entity
@NamedQueries({@NamedQuery(name = "SavedPuzzle.findUserUriAndListingIds",
    query = "SELECT sp FROM com.totsp.crossword.web.server.model.SavedPuzzle sp " +
             "WHERE sp.userUri = :userUri and sp.listingId IN (:listingIds) ORDER BY sp.listingId"),
@NamedQuery(name = "SavedPuzzle.findUserUriAndListingId",
    query = "SELECT sp FROM com.totsp.crossword.web.server.model.SavedPuzzle sp " +
             "WHERE sp.userUri = :userUri and sp.listingId IN (:listingIds) ORDER BY sp.listingId")

})
public class SavedPuzzle implements Serializable {
    
    private Blob puzzleSerial;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long listingId;
    private String userUri;

    /**
     * Set the value of listingId
     *
     * @param newlistingId new value of listingId
     */
    public void setListingID(Long newlistingId) {
        this.listingId = newlistingId;
    }

    /**
     * Get the value of listingId
     *
     * @return the value of listingId
     */
    public Long getListingId() {
        return this.listingId;
    }

    /**
     * Set the value of puzzleSerial
     *
     * @param newpuzzleSerial new value of puzzleSerial
     */
    public void setPuzzleSerial(Blob newpuzzleSerial) {
        this.puzzleSerial = newpuzzleSerial;
    }

    /**
     * Get the value of puzzleSerial
     *
     * @return the value of puzzleSerial
     */
    public Blob getPuzzleSerial() {
        return this.puzzleSerial;
    }

    /**
     * Set the value of userURI
     *
     * @param newuserURI new value of userURI
     */
    public void setUserUri(String newuserURI) {
        this.userUri = newuserURI;
    }

    /**
     * Get the value of userURI
     *
     * @return the value of userURI
     */
    public String getUserUri() {
        return this.userUri;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
}
