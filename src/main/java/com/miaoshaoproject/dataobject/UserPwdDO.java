package com.miaoshaoproject.dataobject;

public class UserPwdDO {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column user_pwd.id
     *
     * @mbggenerated Tue Apr 11 00:17:10 CST 2023
     */
    private Integer id;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column user_pwd.encrpt_pwd
     *
     * @mbggenerated Tue Apr 11 00:17:10 CST 2023
     */
    private String encrptPwd;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column user_pwd.user_id
     *
     * @mbggenerated Tue Apr 11 00:17:10 CST 2023
     */
    private Integer userId;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column user_pwd.id
     *
     * @return the value of user_pwd.id
     *
     * @mbggenerated Tue Apr 11 00:17:10 CST 2023
     */
    public Integer getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column user_pwd.id
     *
     * @param id the value for user_pwd.id
     *
     * @mbggenerated Tue Apr 11 00:17:10 CST 2023
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column user_pwd.encrpt_pwd
     *
     * @return the value of user_pwd.encrpt_pwd
     *
     * @mbggenerated Tue Apr 11 00:17:10 CST 2023
     */
    public String getEncrptPwd() {
        return encrptPwd;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column user_pwd.encrpt_pwd
     *
     * @param encrptPwd the value for user_pwd.encrpt_pwd
     *
     * @mbggenerated Tue Apr 11 00:17:10 CST 2023
     */
    public void setEncrptPwd(String encrptPwd) {
        this.encrptPwd = encrptPwd == null ? null : encrptPwd.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column user_pwd.user_id
     *
     * @return the value of user_pwd.user_id
     *
     * @mbggenerated Tue Apr 11 00:17:10 CST 2023
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column user_pwd.user_id
     *
     * @param userId the value for user_pwd.user_id
     *
     * @mbggenerated Tue Apr 11 00:17:10 CST 2023
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}