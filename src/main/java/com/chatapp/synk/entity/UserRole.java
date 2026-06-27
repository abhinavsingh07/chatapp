package com.chatapp.synk.entity;

import com.chatapp.synk.enums.RoleName;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "user_roles", schema = "chatapp")
public class UserRole {
    public static final String ALIAS_USER_ROLE = "USRL";
    @Id
    @GenericGenerator(name = "snowflake_gen", strategy = "com.chatapp.synk.config.snowflakeConfig.SnowflakeIdentifierGenerator")
    @GeneratedValue(generator = "snowflake_gen")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "identifier_id", length = 50)
    private String identifierId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleName name;

    public UserRole(String identifierId, RoleName name) {
        this.identifierId = identifierId;
        this.name = name;
    }

    public UserRole() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdentifierId() {
        return identifierId;
    }

    public void setIdentifierId(String identifierId) {
        this.identifierId = identifierId;
    }

    public RoleName getName() {
        return name;
    }

    public void setname(RoleName name) {
        this.name = name;
    }
}