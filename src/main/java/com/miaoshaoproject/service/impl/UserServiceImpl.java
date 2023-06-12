package com.miaoshaoproject.service.impl;

import com.miaoshaoproject.dao.UserDOMapper;
import com.miaoshaoproject.dao.UserPwdDOMapper;
import com.miaoshaoproject.dataobject.UserDO;
import com.miaoshaoproject.dataobject.UserPwdDO;
import com.miaoshaoproject.error.BusinessException;
import com.miaoshaoproject.error.EmBusinessError;
import com.miaoshaoproject.service.UserService;
import com.miaoshaoproject.service.model.UserModel;
import com.miaoshaoproject.validator.ValidationResult;
import com.miaoshaoproject.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired(required = false)
    UserDOMapper userDOMapper;

    @Autowired(required = false)
    UserPwdDOMapper userPwdDOMapper;

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if (userModel == null){
            throw  new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
//        if (StringUtils.isEmpty(userModel.getName())||userModel.getAge() == null ||
//        userModel.getGender() == null || StringUtils.isEmpty(userModel.getTelphone())){
//            throw  new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
//        }

        ValidationResult res = validator.validate(userModel);
        if (res.isHasError()){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,res.getErrMsg());
        }
        //把Model转成DaoObject
        UserDO uerDo = convertFromModel(userModel);
        try{
            //由于数据库的telphone已经是唯一索引，因此如果有重复，就会执行错误抛异常
            userDOMapper.insertSelective(uerDo);
        }catch (DuplicateKeyException ex){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"Duplicate telphone");
        }
        userModel.setId(uerDo.getId());
        UserPwdDO userPwdDO = convertPwdFromModel(userModel);
        userPwdDOMapper.insertSelective(userPwdDO);
        return;
    }

    @Override
    public UserModel validateLogin(String telphone, String encrptPassword) throws BusinessException {
        //通过手机号得到用户信息
        UserDO userDO = userDOMapper.selectByTelphone(telphone);
        if (userDO == null){
            throw  new BusinessException(EmBusinessError.LOGIN_FAILED);
        }
        UserPwdDO userPwdDO = userPwdDOMapper.selectByUserId(userDO.getId());
        UserModel userModel = convertFromDataObj(userDO, userPwdDO);
        //检验密码是否正确
        if (!StringUtils.equals(userModel.getEncrptPwd(),encrptPassword)){
            throw  new BusinessException(EmBusinessError.LOGIN_FAILED);
        }
        return userModel;
    }

    private UserDO convertFromModel(UserModel userModel) {
        if (userModel == null){
            return null;
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel,userDO);
        return userDO;
    }

    private UserPwdDO convertPwdFromModel(UserModel userModel){
        if (userModel == null){
            return null;
        }
        UserPwdDO userPwdDO = new UserPwdDO();
        userPwdDO.setEncrptPwd(userModel.getEncrptPwd());
        userPwdDO.setUserId(userModel.getId());
        return userPwdDO;
    }

    @Override
    public UserModel getUserById(Integer id) {
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if (userDO == null){
            return null;
        }
        UserPwdDO userPwdDO = userPwdDOMapper.selectByUserId(id);
        return convertFromDataObj(userDO,userPwdDO);
    }

    //从缓存中取用户信息
    @Override
    public UserModel getUserByIdInCache(Integer id) {
       UserModel userModel = (UserModel)redisTemplate.opsForValue().get("user_validate_"+id);
       if (userModel == null){
           userModel = this.getUserById(id);
           redisTemplate.opsForValue().set("user_validate_"+id,userModel);
           redisTemplate.expire("user_validate_"+id,10, TimeUnit.MINUTES);
       }
       return userModel;
    }

    private UserModel convertFromDataObj(UserDO userDO, UserPwdDO userPwdDO){
        if (userDO == null){
            return null;
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO,userModel);
        if (userPwdDO != null){
            userModel.setEncrptPwd(userPwdDO.getEncrptPwd());
        }
        return userModel;
    }
}
