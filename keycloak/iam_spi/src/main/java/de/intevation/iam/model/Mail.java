package de.intevation.iam.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "mail", schema = "keycloak")
public class Mail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "send_date", nullable = false)
    private Timestamp sendDate;

    @Column(name = "expiry_date")
    private Timestamp expiryDate;

    @Column(name = "sender", nullable = false)
    private String sender;

    @Column(name = "text", nullable = false)
    private String text;

    @Column(name ="subject", nullable = false)
    private String subject;

    @Column(name = "publish", nullable = false)
    private Boolean publish;

    @Column(name = "type", nullable = false)
    private Integer type;

    @Column(name = "receipient", nullable = false)
    private Integer receipient;

    @JsonIgnore
    @Column(name = "user_id")
    private String userId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Timestamp getSendDate() {
        return sendDate;
    }

    public void setSendDate(Timestamp sendDate) {
        this.sendDate = sendDate;
    }

    public Timestamp getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Timestamp expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Boolean getPublish() {
        return publish;
    }

    public void setPublish(Boolean publish) {
        this.publish = publish;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getReceipient() {
        return receipient;
    }

    public void setReceipient(Integer receipient) {
        this.receipient = receipient;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
