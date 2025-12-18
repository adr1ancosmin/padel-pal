package com.padelpal.courtservice.model;

import jakarta.persistence.*;

@Entity
@Table(name = "courts")
public class Court {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clubName;
    private String courtName;
    private boolean indoor;

    public Court() {
    }

    public Court(String clubName, String courtName, boolean indoor) {
        this.clubName = clubName;
        this.courtName = courtName;
        this.indoor = indoor;
    }

    public Long getId() {
        return id;
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public String getCourtName() {
        return courtName;
    }

    public void setCourtName(String courtName) {
        this.courtName = courtName;
    }

    public boolean isIndoor() {
        return indoor;
    }

    public void setIndoor(boolean indoor) {
        this.indoor = indoor;
    }
}
