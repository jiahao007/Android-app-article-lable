package com.example.liuji.hw;

import java.util.List;

public class Relation
{
    private String doc_id;
    private String sent_id;
    private String title;
    private String sent_ctx;
    private List<ErE> triples;

    public String getDoc_id()
    {
        return doc_id;
    }
    public void setDoc_id(String doc_id)
    {
        this.doc_id = doc_id;
    }
    public String getSent_id()
    {
        return sent_id;
    }
    public void setSent_id(String sent_id)
    {
        this.sent_id = sent_id;
    }
    public String getTitle()
    {
        return title;
    }
    public void setTitle(String title)
    {
        this.title = title;
    }
    public String getSent_ctx()
    {
        return sent_ctx;
    }
    public void setSent_ctx(String sent_ctx)
    {
        this.sent_ctx = sent_ctx;
    }
    public List getTriples()
    {
        return triples;
    }
    public void setTriples(List triples)
    {
        this.triples = triples;
    }
}

class ErE
{
    public String id;
    public String left_e_start;
    public String left_e_end;
    public String right_e_start;
    public String right_e_end;
    public String relation_start;
    public String relation_end;
    public String left_entity;
    public String right_entity;
    public String relation_id;
    public String status = "1";
}