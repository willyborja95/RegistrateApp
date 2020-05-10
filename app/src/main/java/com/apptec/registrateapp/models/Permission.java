package com.apptec.registrateapp.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Calendar;

@Entity
public class Permission implements Serializable {

    @PrimaryKey
    private int id;

    private String comment;

    @ColumnInfo(name = "permissionType")
    private PermissionType permissionType;

    @ColumnInfo(name = "permissionStatus")
    private PermissionStatus permissionStatus;

    @ColumnInfo(name = "startDate")
    private Calendar startDate;

    @ColumnInfo(name = "endDate")
    private Calendar endDate;


    public Permission(int id, String comment, PermissionType permissionType, PermissionStatus permissionStatus, Calendar startDate, Calendar endDate) {
        this.id = id;
        this.comment = comment;
        this.permissionType = permissionType;
        this.permissionStatus = permissionStatus;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Permission() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public PermissionType getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(PermissionType permissionType) {
        this.permissionType = permissionType;
    }

    public PermissionStatus getPermissionStatus() {
        return permissionStatus;
    }

    public void setPermissionStatus(PermissionStatus permissionStatus) {
        this.permissionStatus = permissionStatus;
    }

    public Calendar getStartDate() {
        return startDate;
    }

    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    public Calendar getEndDate() {
        return endDate;
    }

    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }
}

