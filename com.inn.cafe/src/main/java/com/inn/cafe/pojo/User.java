package com.inn.cafe.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;

@NamedQuery(name="User.findByEmailId", query="select u from User u where u.email = :email")
@NamedQuery(name="User.getAllUsers", query="select new com.inn.cafe.wrapper.UserWrapper(u.id, u.name, u.email, u.contactNumber, u.status) from User u where u.role = 'user'")
@NamedQuery(name="User.getAllAdmins", query="select u.email from User u where u.role = 'admin'")
@NamedQuery(name="User.updateStatus", query="update User u set u.status=:status where u.id=:id")
@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name="user")
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {
    private static final long serialVersionUID = 1l;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private Integer id;

    @Column(name="name")
    private String name;

    @Column(name="contactNumber")
    private String contactNumber;

    @Column(name="email")
    private String email;

    @Column(name="password")
    private String password;

    @Column(name = "status")
    private String status;

    @Column(name="role")
    private String role;

}
