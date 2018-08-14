package com.mar.store.mysql.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

/**
 * 用户信息
 *
 * @author zhoufang
 */
@Entity(name = "zeus_user")
public class ZeusUser {
    public enum UserStatus {
        WAIT_CHECK(0), CHECK_SUCCESS(1), Cancel(-1), CHECK_FAILED(-2);
        private int nCode;

        private UserStatus(int _nCode) {
            this.nCode = _nCode;
        }

        @Override
        public String toString() {
            return String.valueOf(this.nCode);
        }

        public int value() {
            return this.nCode;
        }
    }

    public static final ZeusUser ADMIN = new ZeusUser() {
        @Override
        public String getEmail() {
            return "fuwu-data@qunar.com";
        }


        @Override
        public String getName() {
            return "biadmin";
        }


        @Override
        public String getPhone() {
            return "";
        }


        @Override
        public String getUid() {
            return "lianxing.dong,yw.yan,peng.jin,jingqi.cao,weining.dong";
        }
    };
    public static ZeusUser USER = new ZeusUser(null, null, null, null);

    @Id
    @GeneratedValue
    private Long id;
    @Column
    private String name;
    @Column
    private String uid;
    @Column
    private String email;
    @Column
    private String password;
    @Column
    private String wangwang;
    @Column
    private String phone;
    @Column(name = "gmt_create")
    private Date gmtCreate;
    @Column(name = "gmt_modified")
    private Date gmtModified;
    @Column(name = "is_effective")
    private int isEffective;
    @Column(name = "user_type")
    private int userType;
    @Column
    private String description;

    public ZeusUser(String email, String name, String phone,
                    String uid) {
        this.email = email;
        this.name = name;
        this.uid = uid;
        this.phone = phone;
        // TODO Auto-generated constructor stub
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ZeusUser() {
        // TODO Auto-generated constructor stub
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWangwang() {
        return this.wangwang;
    }

    public void setWangwang(String wangwang) {
        this.wangwang = wangwang;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIsEffective() {
        return isEffective;
    }

    public void setIsEffective(int isEffective) {
        this.isEffective = isEffective;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ZeusUser [uid=" + uid + ", name=" + name + ", email=" + email + ", wangwang=" + wangwang + "]";
    }

}
