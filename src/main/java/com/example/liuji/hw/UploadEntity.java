package com.example.liuji.hw;

import java.util.List;

public class UploadEntity
{
    private String doc_id;
    private String sent_id;
    private List<Mentity> entities;



    public void setDoc_id(String doc_id)
    {
        this.doc_id = doc_id;
    }
    public void setSent_id(String sent_id)
    {
        this.sent_id = sent_id;
    }
    public void setEntities(List<Mentity> entities)
    {
        this.entities =entities;
    }
}
class Mentity
{
    String EntityName;
    String Start;
    String End;
    String NerTag;
}
