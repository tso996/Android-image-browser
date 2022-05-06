package uk.ac.tees.aad.b1143506.Model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class ToDoModel {
    private int id, status;
    private String task,location;
    private byte[] bitmapImageByte;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public byte[] getImage() {
        return bitmapImageByte;
    }

    public void setImage(byte[] bitmapImageArray) {
        this.bitmapImageByte = bitmapImageArray;
    }
}
