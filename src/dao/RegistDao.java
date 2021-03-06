/**
 * @program: szmhist
 * * @description: 现场挂号
 * * @author:cro
 * * @create: 2019-05-31 15:55
 **/

package dao;

import oracle.sql.TIMESTAMP;
import util.JdbcUtil;
import vo.*;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class RegistDao implements IRegistDao{

    Connection con = null;

    public void setConnection(Connection con) {
        this.con = con;
    }




    /**
     * 读取当前最大病历号
     *
     * @return 返回下一个可用的病历号
     */
    @Override
    public String selectMaxCaseNum() throws SQLException {
        String sql ="select max(CaseNumber)+1 from register";
        PreparedStatement pstm = con.prepareStatement(sql);
        ResultSet rs = pstm.executeQuery();
        String caseNum=null;
        while (rs.next()){
            caseNum=rs.getString(1);
        }
        JdbcUtil.release(null,pstm,null);
        return caseNum;
    }

    /**
     * 读取有效结算类别
     * @return id、编码、结算名称
     */
    @Override
    public List selectSettleCategories() throws SQLException {
        String sql ="select id,SettleCode,SettleName from settlecategory where DelMark=1 order by SequenceNo";
        PreparedStatement pstm = con.prepareStatement(sql);
        ResultSet rs = pstm.executeQuery();
        List<SettleCategory> settleCategories=new ArrayList<>();
        SettleCategory category=null;
        while (rs.next()){
            category=new SettleCategory();
            category.setId(rs.getInt(1));
            category.setSettleCode(rs.getString(2));
            category.setSettleName(rs.getString(3));
            settleCategories.add(category);
        }
        JdbcUtil.release(null,pstm,null);
        return settleCategories;
    }

    /**
     * 选择有效挂号级别
     *
     * @return list-registlevel
     */
    @Override
    public List selectRegistLevels() throws SQLException {
        String sql ="select id,RegistCode,RegistName from registlevel where DelMark=1 order by SequenceNo";
        PreparedStatement pstm = con.prepareStatement(sql);
        ResultSet rs = pstm.executeQuery();
        List<RegistLevel> list=new ArrayList<>();
        RegistLevel rl=null;
        while (rs.next()){
            rl=new RegistLevel();
            rl.setId(rs.getInt(1));
            rl.setRegistCode(rs.getString(2));
            rl.setRegistName(rs.getString(3));
            list.add(rl);
        }
        JdbcUtil.release(null,pstm,null);
        return list;
    }

    /**
     * 根据ID获取挂号费和初始号额
     *
     * @param id registLevel-id
     * @return 返回一个封装了挂号费和初始号额的registlevel对象
     */
    @Override
    public RegistLevel selectRegistLevelByID(int id) throws SQLException {
        String sql ="select RegistFee,RegistQuota from registlevel where id=?";
        PreparedStatement pstm = con.prepareStatement(sql);
        pstm.setInt(1,id);
        ResultSet rs = pstm.executeQuery();
        RegistLevel rl=null;
        while (rs.next()){
            rl=new RegistLevel();
            rl.setRegistFree(rs.getDouble(1));
            rl.setRegistquota(rs.getInt(2));
        }
        JdbcUtil.release(null,pstm,null);
        return rl;
    }

    /**
     * 读取有效临床科室
     *
     * @return list-department对象，id,registcode,registname
     */
    @Override
    public List selectDepartment() throws SQLException {
        String sql ="select id,deptcode,deptname from department where Depttype=1 and DelMark=1";
        PreparedStatement pstm = con.prepareStatement(sql);
        ResultSet rs = pstm.executeQuery();
        List<Department> list=new ArrayList<>();
        Department dpmt=null;
        while (rs.next()){
            dpmt=new Department();
            dpmt.setId(rs.getInt(1));
            dpmt.setDeptCode(rs.getString(2));
            dpmt.setDeptName(rs.getString(3));
            list.add(dpmt);
        }
        JdbcUtil.release(null,pstm,null);
        return list;
    }

    /**
     * 根据看诊日期,午别,排班科室,挂号级别读取当天出诊医生ID,姓名
     *
     * @return list-User对象-id,realname
     */
    @Override
    public List selectDoctorInfo(SchedDoctor sd) throws SQLException {
        String sql ="select user.id,user.realname from scheduling,user "
                +"where scheduling.UserId = user.DeptId "
                +"and scheduling.SchedDate = ? "
                +"and scheduling.Noon = ? "
                +"and scheduling.DeptID = ? "
                +"and user.RegistLeID = ? ";
        PreparedStatement pstm = con.prepareStatement(sql);
        Date date=new Date(sd.getSchedDate().getTime());
        pstm.setDate(1,date);
        pstm.setString(2,sd.getNoon());
        pstm.setInt(3,sd.getDeptID());
        pstm.setInt(4,sd.getRegistLeID());
        ResultSet rs = pstm.executeQuery();
        List<User> list=new ArrayList<>();
        User user=null;
        while (rs.next()){
            user=new User();
            user.setId(rs.getInt(1));
            user.setRealName(rs.getString(2));
            list.add(user);
        }
        JdbcUtil.release(null,pstm,null);
        return list;
    }

    /**
     * @param reg
     * @Description: 根据选中医生读取当日已用号额
     * @Param: [userId] 医生ID
     * @return: int 已用号额，当天共有多少人已预约
     * @Author: cro
     * @Date: 2019/6/1
     */
    @Override
    public int selectDoctorUsedId(Register reg) throws SQLException {
        String sql ="select count(id) from register where UserID=? and VisitDate=? and VisitState in (1,2,3)";
        PreparedStatement pstm = con.prepareStatement(sql);
        pstm.setInt(1,reg.getUserID());
        Date date=new Date(reg.getVisitDate().getTime());
        pstm.setDate(2,date);
        ResultSet rs = pstm.executeQuery();
        int allUsedId=0;
        while (rs.next()){
            allUsedId=rs.getInt(1);
        }
        JdbcUtil.release(null,pstm,null);
        return allUsedId;
    }

    /**
     * @param reg
     * @Description: 插入挂号记录,挂号时间为系统当前时间(需要设置)
     * @Param: [reg]
     * @return: java.lang.Boolean 是否插入成功
     * @Author: cro
     * @Date: 2019/6/1
     */
    @Override
    public Boolean insertRegist(Register reg) throws SQLException {
        String sql ="insert into register(CaseNumber,RealName,Gender,IDnumber,BirthDate,Age,AgeType," +
                "HomeAddress,VisitDate,Noon,DeptID,UserID,RegistLeID,SettleID,IsBook,RegistTime,RegisterID,VisitState) "
                +"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,1) ";
        PreparedStatement pstm = con.prepareStatement(sql);
        pstm.setString(1,reg.getCaseNumber());
        pstm.setString(2,reg.getRealName());
        pstm.setInt(3,reg.getGender());
        pstm.setString(4,reg.getIdNumber());
        Date date=new Date(reg.getBirthDate().getTime());
        pstm.setDate(5,date);
        pstm.setInt(6,reg.getAge());
        pstm.setString(7,reg.getAgeTpye());
        pstm.setString(8,reg.getHomeAddress());
        Date vistiDate=new Date(reg.getVisitDate().getTime());
        pstm.setDate(9,vistiDate);
        pstm.setString(10,reg.getNoon());
        pstm.setInt(11,reg.getDeptID());
        pstm.setInt(12,reg.getUserID());
        pstm.setInt(13,reg.getRegistLeID());
        pstm.setInt(14,reg.getSettLeID());
        pstm.setString(15,reg.getisBook());
        Timestamp registTime=new Timestamp(reg.getRegistTime().getTime());
        pstm.setTimestamp(16,registTime);
        pstm.setInt(17,reg.getRegisterID());
        pstm.executeUpdate();
        JdbcUtil.release(null,pstm,null);
        return true;
    }


    /**
     * 根据挂号ID 修改对应数据VisitState属性1-已挂号 2-医生接诊 3-看诊结束 4-已退号
     *
     * @param regID 挂号ID
     */
    @Override
    public void updateVisitState(int regID,int state) throws SQLException {
        String sql="UPDATE register set VisitState=? where ID=?";
        PreparedStatement pstmt=con.prepareStatement(sql);
        pstmt.setInt(1,state);
        pstmt.setInt(2,regID);
        pstmt.executeUpdate();
        JdbcUtil.release(null, pstmt, null);
    }

    @Override
    public Register getRegisterByCaseNumber(String casen) throws SQLException {
        String sql="select casenumber,realname,age,settleid,deptid,VisitDate,userid " +
                "from Register where casenumber=?";
        con=JdbcUtil.getConnection();
        PreparedStatement ps=con.prepareStatement(sql);
        ps.setString(1,casen);
        ResultSet rs=ps.executeQuery();
        Register reg=new Register();
        while(rs.next()){
            reg.setCaseNumber(rs.getString(1));
            reg.setRealName(rs.getString(2));
            reg.setAge(rs.getInt(3));
            reg.setSettLeID(rs.getInt(4));
            reg.setDeptID(rs.getInt(5));
            reg.setVisitDate(rs.getDate(6));
            reg.setUserID(rs.getInt(7));
        }
        JdbcUtil.release(con,null,null);
        return reg;
    }

    @Override
    public Invoice getInfByRegistid(int registid) throws SQLException {
        String sql="select id,invoicenum,money,state,creationtime,userid,registid,feetype,dailystate " +
                "from invoice where registid=?";
        con=JdbcUtil.getConnection();
        PreparedStatement ps=con.prepareStatement(sql);
        ps.setInt(1,registid);
        ResultSet rs=ps.executeQuery();
        Invoice inv=new Invoice();
        while(rs.next()){
            inv.setId(rs.getInt(1));
            inv.setInvoiceNum(rs.getString(2));
            inv.setMoney(rs.getDouble(3));
            inv.setState(rs.getInt(4));
            inv.setCreationTime(rs.getTimestamp(5));
            inv.setUserID(rs.getInt(6));
            inv.setRegistID(rs.getInt(7));
            inv.setFeeType(rs.getInt(8));
            inv.setDailyState(rs.getInt(9));
        }
        JdbcUtil.getConnection();
        return inv;
    }

    /**
     * 根据病历号和创建时间获取挂号id
     *
     * @param creatTime 创建时间
     * @param caseNum   病历号
     * @return 挂号id
     */
    @Override
    public int selectRegistIDByTime(String  creatTime, String caseNum) throws SQLException {
        String sql="SELECT id FROM register WHERE CaseNumber=? AND (RegistTime=?) AND VisitState IN (1,2,3)";
        PreparedStatement pstmt=con.prepareStatement(sql);
        DateFormat df=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Timestamp t=Timestamp.valueOf(creatTime);
        pstmt.setTimestamp(2,t);
        pstmt.setString(1,caseNum);
        ResultSet rs=pstmt.executeQuery();
        int n=0;
        while (rs.next()){
            n=rs.getInt(1);
        }
        JdbcUtil.release(null, pstmt, null);
        return n;
    }

    /**
     * 根据科室名获取ID
     *
     * @param deptname 科室名称
     */
    @Override
    public int getDeptIDbyName(String deptname) throws SQLException {
        String sql ="select id from department where DeptName=?";
        PreparedStatement pstmt=con.prepareStatement(sql);
        pstmt.setString(1,deptname);
        ResultSet rs=pstmt.executeQuery();
        int deptID=0;
        while(rs.next()){
            deptID=rs.getInt(1);
        }
        JdbcUtil.release(null,pstmt,null);
        return deptID;
    }

    /**
     * 根据id获取科室名
     *
     * @param id
     * @return
     */
    @Override
    public String getDeptNameByID(int id) throws SQLException {
        String sql ="select DeptName from department where ID=?";
        PreparedStatement pstmt=con.prepareStatement(sql);
        pstmt.setInt(1,id);
        ResultSet rs=pstmt.executeQuery();
        String n=null;
        while(rs.next()){
            n=rs.getString(1);
        }
        JdbcUtil.release(null,pstmt,null);
        return n;
    }

    @Override
    public List selectDeptNameByDate(java.util.Date date, String noon) throws SQLException {
        String sql ="SELECT d.DeptName,d.id FROM scheduling s,department d WHERE d.ID=s.DeptID AND SchedDate=? AND Noon=? GROUP BY d.DeptName, d.id";
        PreparedStatement pstmt=con.prepareStatement(sql);
        Date date1=new Date(date.getTime());
        pstmt.setDate(1,date1);
        pstmt.setString(2,noon);
        ResultSet rs=pstmt.executeQuery();
        List l=new ArrayList();
        Department d=null;
        while(rs.next()){
            d=new Department();
            d.setDeptName(rs.getString(1));
            d.setId(rs.getInt(2));
            l.add(d);
        }
        JdbcUtil.release(null,pstmt,null);
        return l;
    }
}
