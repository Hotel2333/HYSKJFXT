package com.scau.hyskjf.controller;

import com.scau.hyskjf.pojo.Merchantaccount;
import com.scau.hyskjf.service.MerchantAccManageService;
import com.scau.hyskjf.util.json.ResponseCode;
import com.scau.hyskjf.util.json.ResponseJSON;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
* 商家账户管理：
* 使用者：商家管理员
* 功能：
* 1、商家添加子账号
* 2、子账号管理，查询账号信息，修改子账号的账号类型、密码、启用状态；
*    指定子账号的为会员充值权限（使用系统分配的根账号才能操作）
* */
@RestController
@RequestMapping("/merchantAccManage")
public class MerchantAccManageController {

    @Autowired
    MerchantAccManageService merchantAccManageService;
    /*
    * 添加子账号：
    * 输入：
    * Merchantaccount类：
    * 商家编号Integer、登陆名（手机号码）String、密码String、
    * 账号类型（11为管理员，2为前台账户，3为客户经理，14为部门经理,大于10的拥有为用户充值权限，管理员可为前台和客户经理添加充值权限，加权后数值为前台12和客户经理13）int、
    * 启用状态Boolean、结账账户Integer
    * 返回：
    * 成功码或失败码
    * */
    @RequestMapping(value = "/addAffiliateAccount", method = RequestMethod.POST)
    public ResponseJSON addAffiliateAccount( Merchantaccount merchantaccount){
        try{
            Md5Hash md5Hash =new Md5Hash(merchantaccount.getMacpasswd(),merchantaccount.getMacacc(),3);//MD5加密密码,登陆名作掩码，加密3次
            String MD5pwd = md5Hash.toString();
            merchantaccount.setMacpasswd(MD5pwd);
            merchantaccount.setMacenable(true);
            merchantAccManageService.addMerchantAccount(merchantaccount);//插入到商家账户表
            return new ResponseJSON(ResponseCode.SUCCESS);
        }catch(Exception e){
            return new ResponseJSON(ResponseCode.WARN);
        }
    }

    /*
     * (吕浩泰要求的接口)添加子账号：
     * 输入：
     * Merchantaccount类：
     * 商家编号Integer、登陆名（手机号码）String、密码String、
     * 账号类型（11为管理员，2为前台账户，3为客户经理，14为部门经理,大于10的拥有为用户充值权限，管理员可为前台和客户经理添加充值权限，加权后数值为前台12和客户经理13）int、
     * 启用状态Boolean、结账账户Integer
     *
     * 返回：
     * 商家ID对应的所有账号信息MerchantAccount类
     * */
    @RequestMapping(value = "/addAccountReturn", method = RequestMethod.POST)
    public ResponseJSON addAccountReturn(Merchantaccount merchantaccount){
        try{
            this.addAffiliateAccount(merchantaccount);
            List<Merchantaccount> accList = merchantAccManageService.queryMerchantAccountByMerID(merchantaccount.getMerid());
            return new ResponseJSON(ResponseCode.SUCCESS,accList);
        }catch(Exception e){
            return new ResponseJSON(ResponseCode.WARN);
        }
    }

    /*
    * 查询整个系统所有商家账户：
    * 输入：
    * 无
    * 返回：
    * 所有商家账户表的信息
    * */
    @RequestMapping(value = "/queryAll", method = RequestMethod.POST)
    public ResponseJSON queryAll(){
        try{
            List<Merchantaccount> accList =merchantAccManageService.queryAllMerchantAccount();
            return new ResponseJSON(ResponseCode.SUCCESS,accList);
        }catch(Exception e){
            return new ResponseJSON(ResponseCode.WARN);
        }
    }

    /*
     * 查询某一商家所有账户（根据商家编号id查询）：
     * 输入：
     * 商家编号
     * 返回：
     * 商家编号对应商家的所有商家账户表的信息
     * */
    @RequestMapping(value = "/queryByMerID", method = RequestMethod.POST)
    public ResponseJSON queryByMerID(Integer merID){
        try{
            List<Merchantaccount> accList =merchantAccManageService.queryMerchantAccountByMerID(merID);
            return new ResponseJSON(ResponseCode.SUCCESS,accList);
        }catch(Exception e){
            return new ResponseJSON(ResponseCode.WARN);
        }
    }

    /*
     * 根据登陆名查询账户（用户手机号）：
     * 输入：
     * 账户登陆名
     * 返回：
     * 账户登陆名对应账户信息
     * */
    @RequestMapping(value = "/queryByMacAcc", method = RequestMethod.POST)
    public ResponseJSON queryByMacAcc(String macAcc){
        try{
            Merchantaccount acc =merchantAccManageService.queryMerchantAccountByMacAcc(macAcc);
            return new ResponseJSON(ResponseCode.SUCCESS,acc);
        }catch(Exception e){
            return new ResponseJSON(ResponseCode.WARN);
        }
    }

    /*
     * 修改子账号密码(普通商家账号接口)
     * 输入：
     * 1、Merchantaccount类：必须有账户登录名macAcc+原密码macPasswd
     * 2、String 新密码
     * 返回：
     * 成功或失败码
     * */
    @RequestMapping(value = "/updatePwdByMacAcc", method = RequestMethod.POST)
    public ResponseJSON updatePwdByMacAcc(Merchantaccount merchantaccount,String newPwd){
        try{
            String uncheckedPwd = new Md5Hash(merchantaccount.getMacpasswd(),merchantaccount.getMacacc(),3).toString();//对用户输入账户的原密码进行MD5加密
            String newPwdMD5 = new Md5Hash(newPwd,merchantaccount.getMacacc(),3).toString();
            Merchantaccount acc =merchantAccManageService.queryMerchantAccountByMacAcc(merchantaccount.getMacacc());//从数据库获取登录名对应的账户实例
            String truePwd = acc.getMacpasswd();//获取正确的MD5密码
            if(truePwd.equals(uncheckedPwd)){//正确的MD5密码与输入的原密码MD5匹配，正确则执行更改密码
                acc.setMacpasswd(newPwdMD5);
                merchantAccManageService.updateMerchantAccount(acc);
                return new ResponseJSON(ResponseCode.SUCCESS);
            }
            else{
                return new ResponseJSON(ResponseCode.WARN,new String("输入的原密码不匹配"));
            }
        }catch(Exception e){
            return new ResponseJSON(ResponseCode.WARN);
        }
    }

    /*
     * 修改子账号密码(商家管理员账号接口)
     * 输入：
     * 1、Merchantaccount类：必须有账户登录名macAcc(原密码不需要)
     * 2、String 新密码
     * 返回：
     * 成功或失败码
     * */
    @RequestMapping(value = "/updateManagerPwdByMacAcc", method = RequestMethod.POST)
    public ResponseJSON updateManagerPwdByMacAcc(Merchantaccount merchantaccount,String newPwd){
        try{
            //String uncheckedPwd = new Md5Hash(merchantaccount.getMacpasswd(),merchantaccount.getMacacc(),3).toString();//对用户输入账户的原密码进行MD5加密
            Merchantaccount acc =merchantAccManageService.queryMerchantAccountByMacAcc(merchantaccount.getMacacc());//从数据库获取登录名对应的账户实例
            String newPwdMD5 = new Md5Hash(newPwd,merchantaccount.getMacacc(),3).toString();
            //String truePwd = acc.getMacpasswd();//获取正确的MD5密码
            //if(truePwd.equals(uncheckedPwd)){//正确的MD5密码与输入的原密码MD5匹配，正确则执行更改密码
            acc.setMacpasswd(newPwdMD5);
            merchantAccManageService.updateMerchantAccount(acc);
            return new ResponseJSON(ResponseCode.SUCCESS);
            //}
            //else{
            //    return new ResponseJSON(ResponseCode.WARN,new String("输入的原密码不匹配"));
            //}
        }catch(Exception e){
            return new ResponseJSON(ResponseCode.WARN);
        }
    }

    /*
    * 修改子账号的账号类型、密码、启用状态
    * 输入：
    * Merchantaccount类：必须有账户编号macID+修改的项
    * 返回：
    * 成功或失败码
    * */
    @RequestMapping(value = "/updateByMacID", method = RequestMethod.POST)
    public ResponseJSON updateByMacID(Merchantaccount merchantaccount){
        try{
            merchantaccount.setMacpasswd(null);//不能改密码
            merchantAccManageService.updateMerchantAccount(merchantaccount);
            return new ResponseJSON(ResponseCode.SUCCESS);
        }catch(Exception e){
            return new ResponseJSON(ResponseCode.WARN);
        }
    }

    /*
     * 删除子账号（实际操作只是把启用状态改为false）
     * 输入：
     * Merchantaccount类：必须有账户编号macID,其他项为null
     * 返回：
     * 成功或失败码
     * */
    @RequestMapping(value = "/deleteByMacID", method = RequestMethod.POST)
    public ResponseJSON deleteByMacID(Merchantaccount merchantaccount){
        try{
            merchantaccount.setMacenable(false);
            merchantAccManageService.updateMerchantAccount(merchantaccount);
            return new ResponseJSON(ResponseCode.SUCCESS);
        }catch(Exception e){
            return new ResponseJSON(ResponseCode.WARN);
        }
    }

    /*
     * 为前台或客户经理添加充值权限（实际操作只是把账户类型+10）（只有macAccType==11的管理员账户才能操作）
     * 输入：
     * Merchantaccount类：必须有账户编号macID,账户类型macAccType为2或3，其他项为null
     * 返回：
     * 成功或失败码
     * */
    @RequestMapping(value = "/addPermissionByMacID", method = RequestMethod.POST)
    public ResponseJSON addPermissionByMacID(Merchantaccount merchantaccount){
        try{
            if(merchantaccount.getMacacctype()!=null&&merchantaccount.getMacacctype()<10){
                merchantaccount.setMacacctype(merchantaccount.getMacacctype()+10);
                merchantAccManageService.updateMerchantAccount(merchantaccount);
                return new ResponseJSON(ResponseCode.SUCCESS);
            }
            else{
                return new ResponseJSON(ResponseCode.WARN);
            }
        }catch(Exception e){
            return new ResponseJSON(ResponseCode.WARN);
        }
    }

    /*
    * 短信验证更改密码
    * 未写完
    * */
    @RequestMapping(value = "/updatePwdByMassage", method = RequestMethod.POST)
    public ResponseJSON updateMerchantAccountPwdByMassage(){
        return new ResponseJSON(ResponseCode.WARN);

    }
}
