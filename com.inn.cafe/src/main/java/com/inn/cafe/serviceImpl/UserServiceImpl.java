package com.inn.cafe.serviceImpl;

import com.google.common.base.Strings;
import com.inn.cafe.constants.CafeConstants;
import com.inn.cafe.dao.UserDao;
import com.inn.cafe.jwt.CustomerUersDetailsService;
import com.inn.cafe.jwt.JwtFilter;
import com.inn.cafe.jwt.JwtUtil;
import com.inn.cafe.pojo.User;
import com.inn.cafe.service.UserService;
import com.inn.cafe.utils.CafeUtils;
import com.inn.cafe.utils.EmailUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;

import com.inn.cafe.wrapper.UserWrapper;


@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserDao userDao;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    CustomerUersDetailsService customerUersDetailsService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    EmailUtils emailUtils;

    @Override
    public ResponseEntity<String> signup(Map<String, String> requestMap) {
        log.info("Inside signup {}", requestMap);
        try{
            if(validateSignUpMap(requestMap)){
                User user = userDao.findByEmailId(requestMap.get("email"));
                if(Objects.isNull(user)){
                    userDao.save(getUserFromMap(requestMap));
                    return CafeUtils.getResponseEntity("Successflly registered.", HttpStatus.OK);
                }else{
                    return CafeUtils.getResponseEntity("Email Already Exists.", HttpStatus.BAD_REQUEST);
                }
            }else{
                return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean validateSignUpMap(Map<String, String> requestMap){
        if(requestMap.containsKey("name") && requestMap.containsKey("contactNumber") && requestMap.containsKey("email") && requestMap.containsKey("password")){
            return true;
        }
        return false;
    }

    private User getUserFromMap(Map<String, String> requestMap){
        User user = new User();
        user.setName(requestMap.get("name"));
        user.setContactNumber(requestMap.get("contactNumber"));
        user.setEmail(requestMap.get("email"));
        user.setPassword(requestMap.get("password"));
        user.setStatus("false");
        user.setRole("user");
        return user;
    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        log.info("Inside login {}", requestMap);
        try{
            Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(requestMap.get("email"), requestMap.get("password")));
            if(auth.isAuthenticated()){
                if(customerUersDetailsService.getUserDetail().getStatus().equalsIgnoreCase("true")){
                    return new ResponseEntity<String >("{\"token\":\""+jwtUtil.generateToken(customerUersDetailsService.getUserDetail().getEmail(), customerUersDetailsService.getUserDetail().getRole())+"\"}", HttpStatus.OK);
                }else{
                    return new ResponseEntity<String>("{\"message\":\""+"Wait for admin approuval."+"\"}", HttpStatus.BAD_REQUEST);
                }
            }
        }catch (Exception ex){
            log.error("{}", ex);
        }

        return new ResponseEntity<String>("{\"message\":\""+"Bad Credentials."+"\"}", HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUsers() {
        try{
            if(jwtFilter.isAdmin()){
                return new ResponseEntity<>(userDao.getAllUsers(), HttpStatus.OK);
            }else{
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try{
            if(jwtFilter.isAdmin()){
                Optional<User> optional = userDao.findById(Integer.parseInt(requestMap.get("id")));
                if(!optional.isEmpty()){
                    userDao.updateStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
                    sendMailToAllAdmins(requestMap.get("status"), optional.get().getEmail(), userDao.getAllAdmins());
                    return CafeUtils.getResponseEntity("User Status updated successfully.", HttpStatus.OK);
                }else{
                    CafeUtils.getResponseEntity("User id doesn't exists.", HttpStatus.OK);
                }
            }else{
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void sendMailToAllAdmins(String status, String user, List<String> allAdmins) {
        allAdmins.remove(jwtFilter.getCurrentUser());
        if(status != null && status.equalsIgnoreCase("true")){
            emailUtils.senSimpleMessage(jwtFilter.getCurrentUser(), "Account Approuved", "USER: "+user+"\nis approuved by\nADMIN: "+jwtFilter.getCurrentUser(), allAdmins);
        }else{
            emailUtils.senSimpleMessage(jwtFilter.getCurrentUser(), "Account Disabled", "USER: "+user+"\nis disabled by\nADMIN: "+jwtFilter.getCurrentUser(), allAdmins);
        }
    }

    @Override
    public ResponseEntity<String> checkToken() {
        return CafeUtils.getResponseEntity("true", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
        try{
            User userObj = userDao.findByEmail(jwtFilter.getCurrentUser());
            if(!userObj.equals(null)){
                if(userObj.getPassword().equals(requestMap.get("oldPassword"))){
                    userObj.setPassword(requestMap.get("newPassword"));
                    userDao.save(userObj);
                    return CafeUtils.getResponseEntity("Password Updated Successfully", HttpStatus.OK);
                }
                return CafeUtils.getResponseEntity("Incorrect old password", HttpStatus.BAD_REQUEST);
            }
            return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
        try{
            User user = userDao.findByEmail(requestMap.get("email"));
            if(!Objects.isNull(user) && !Strings.isNullOrEmpty(user.getEmail()))
                emailUtils.forgotMail(user.getEmail(), "Credentials by Cafe Management System", user.getPassword());
            return CafeUtils.getResponseEntity("Check your mail for credentials.", HttpStatus.OK);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
